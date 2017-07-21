package com.example.liuhaifeng.httpdemo;

import android.app.Application;
import android.net.Uri;


import com.squareup.picasso.Downloader;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import javax.net.ssl.X509TrustManager;


import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;


/**
 * Created by liuhaifeng on 2017/4/25.
 */

public class Myapp extends Application {
    public  static OkHttpClient okHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        HttpsUtils.SSLParams sslParams = null;
        try {
            InputStream in = getResources().getAssets().open("server.cer");
            InputStream[] inputStreams = new InputStream[1];
            inputStreams[0] = in;
            sslParams = HttpsUtils.getSslSocketFactory(inputStreams, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .sslSocketFactory(sslParams.sSLSocketFactory)
                //，忽略hostname 的验证。（仅仅用于测试阶段，不建议用于发布后的产品中。）
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return session.getPeerHost().equals(hostname);
                    }
                })
                .build();
        Picasso.setSingletonInstance(new Picasso.Builder(this).
                downloader(new ImageDownLoader(okHttpClient))
                .build());
    }





    public class ImageDownLoader implements Downloader {
        OkHttpClient client = null;

        public ImageDownLoader(OkHttpClient client) {
            this.client = client;
        }

        @Override
        public Response load(Uri uri, int networkPolicy) throws IOException {

            CacheControl cacheControl = null;
            if (networkPolicy != 0) {
                if (NetworkPolicy.isOfflineOnly(networkPolicy)) {
                    cacheControl = CacheControl.FORCE_CACHE;
                } else {
                    CacheControl.Builder builder = new CacheControl.Builder();
                    if (!NetworkPolicy.shouldReadFromDiskCache(networkPolicy)) {
                        builder.noCache();
                    }
                    if (!NetworkPolicy.shouldWriteToDiskCache(networkPolicy)) {
                        builder.noStore();
                    }
                    cacheControl = builder.build();
                }
            }

            Request.Builder builder = new Request.Builder().url(uri.toString());
            if (cacheControl != null) {
                builder.cacheControl(cacheControl);
            }

            okhttp3.Response response = client.newCall(builder.build()).execute();
            int responseCode = response.code();
            if (responseCode >= 300) {
                response.body().close();
                throw new ResponseException(responseCode + " " + response.message(), networkPolicy,
                        responseCode);
            }

            boolean fromCache = response.cacheResponse() != null;

            ResponseBody responseBody = response.body();
            return new Response(responseBody.byteStream(), fromCache, responseBody.contentLength());

        }

        @Override
        public void shutdown() {

            Cache cache = client.cache();
            if (cache != null) {
                try {
                    cache.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}
