package com.example.liuhaifeng.httpdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import static com.example.liuhaifeng.httpdemo.Myapp.okHttpClient;
import static com.example.liuhaifeng.httpdemo.Okhttps.Okhttp;
import static com.example.liuhaifeng.httpdemo.Okhttps.Okhttps_s;


public class MainActivity extends Activity {
    private static final String OKHTTPS_ONE_WAY_SERVER = "server3.cer";
    private static final String OKHTTPS_ONE_WAY_URL = "https://mykomatsu.komatsu.com.cn/MobileTest/upload.html";
    private static final String OKHTTPS_TWO_WAY_SERVER = "server(2).cer";
    private static final String OKHTTPS_TWO_WAY_CLIENT = "client(1).p12";
    private static final String OKHTTPS_TWO_WAY_CLIENT_PASSWORD = "123456";
    private static final String OKHTTPS_TWO_WAY_URL = " https://192.168.18.83/testweb/test?action=3";
    private static final String OKHTTP_URL = " http://192.168.18.83:8080/testweb/test?action=3";
    private static final String OKHTTPS_BAIDU_URL = "https://www.baidu.com/?tn=58025142_oem_dg";
    private static final String OKHTTPS_BAIDU_SERVER = "bbai.cer";
    private static final String TAG = "HttpDemo";
    SSLContext sslContext;


    @InjectView(R.id.btn_http)
    Button btnHttp;
    @InjectView(R.id.btn_https_d)
    Button btnHttpsD;
    @InjectView(R.id.btn_https_s)
    Button btnHttpsS;
    @InjectView(R.id.tv_success)
    TextView tvSuccess;

    @InjectView(R.id.btn_tobaidu)
    Button btnTobaidu;
    @InjectView(R.id.img)
    ImageView img;

    private Handler myhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                tvSuccess.setText(msg.obj.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
//        Picasso.with(MainActivity.this).load("https://mykomatsu.komatsu.com.cn/Storage/Plat/APP/AppSlidAd/201705021356103554870.jpg").into(img);
        Glide.with(MainActivity.this).load("https://mykomatsu.komatsu.com.cn/Storage/Plat/APP/AppSlidAd/201705021356103554870.jpg").skipMemoryCache(true).into(img);

    }


    @OnClick({R.id.btn_http, R.id.btn_https_d, R.id.btn_https_s, R.id.btn_tobaidu})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_http:
                tvSuccess.setText("");
                okHttpClient = Okhttp();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Request request = new Request.Builder().get().url("www.baidu.com").build();
                        try {
                            Response response = okHttpClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                myhandler.obtainMessage(1, response.body().string()).sendToTarget();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.btn_https_d:
                tvSuccess.setText("");
                HttpsUtils.SSLParams sslParams = null;
                try {
                    InputStream in = getResources().getAssets().open("mykomatsu server.cer");
                    InputStream[] inputStreams = new InputStream[1];
                    inputStreams[0] = in;
                    sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
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
                                return true;
                            }
                        })
                        .build();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Request request = new Request.Builder().get().url("https://mykomatsu.komatsu.com.cn/Mobiletest/api/Index/main?fun=accountLogin&vst=1&ver=1.0&token=&data={\"lo\":114.62265435270568,\"model\":\"CHM-CL00\",\"username\":\"ji_apptest\",\"devicebrand\":\"Honor\",\"la\":38.04641040765567,\"sys_ver\":\"4.4.4\",\"password\":\"49ba59abbe56e057\",\"loginType\":1,\"network\":\"wifi\",\"deviceid\":\"ffffffff-d7c7-0931-f78a-e0aa27d6a967\",\"version\":\"2.0\"}").build();
                        try {
                            Response response = okHttpClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                Log.d("*********", response.toString());
                                myhandler.obtainMessage(1, response.body().string()).sendToTarget();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                break;
            case R.id.btn_https_s:
                tvSuccess.setText("");
                //
                try {
                    sslContext = Okhttps_s(getResources().getAssets().open(OKHTTPS_TWO_WAY_CLIENT), getAssets().open(OKHTTPS_TWO_WAY_SERVER), OKHTTPS_TWO_WAY_CLIENT_PASSWORD);
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                            .readTimeout(10000L, TimeUnit.MILLISECONDS)
                            .sslSocketFactory(sslContext.getSocketFactory())
                            //，忽略hostname 的验证。（仅仅用于测试阶段，不建议用于发布后的产品中。）
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    Boolean b = false;
                                    if (session.getPeerHost().equals(hostname)) {
                                        b = true;
                                    }
                                    return b;
                                }
                            }).build();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Request request = new Request.Builder().get().url(OKHTTPS_TWO_WAY_URL).build();
                        try {
                            Response response = okHttpClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                myhandler.obtainMessage(1, response.body().string()).sendToTarget();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.btn_tobaidu:

//                HttpsUtils.SSLParams sslParams=null;
//                try {
//                    sslParams = HttpsUtils.getSslSocketFactory(getResources().getAssets().open(OKHTTPS_BAIDU_SERVER), null, null);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }



                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                        .readTimeout(10000L, TimeUnit.MILLISECONDS)
//                        .sslSocketFactory(sslParams.sSLSocketFactory)

//                        .hostnameVerifier(new HostnameVerifier() {
//                            @Override
//                            public boolean verify(String hostname, SSLSession session) {
//                                Boolean b=false;
//
//                                if(session.getPeerHost().equals("www.baidu.com")){
//                                    b=true;
//                                }
//                                return b;
//                            }
//                        })
                        .build();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Request request = new Request.Builder().get().url(OKHTTPS_BAIDU_URL).build();
                        try {
                            Response response = okHttpClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                myhandler.obtainMessage(1, response.body().string()).sendToTarget();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
        }
    }


//    @OnClick(R.id.btn_tobaidu)
//    public void onViewClicked() {
//    }
}
