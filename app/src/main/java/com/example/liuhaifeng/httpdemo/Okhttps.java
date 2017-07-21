package com.example.liuhaifeng.httpdemo;

import android.util.Log;

import com.github.lazylibrary.util.Base64;
import com.github.lazylibrary.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;

/**
 * Created by liuhaifeng on 2017/4/25.
 */

public class Okhttps {

    /**
     * 单向认证
     */
    public static SSLContext Okhttps_d(InputStream inputStream) {
        SSLContext sslContext = null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("x.509");
            Certificate cert = certificateFactory.generateCertificate(inputStream);
            Log.d("cert key", ((X509Certificate) cert).getPublicKey().toString());


            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
            String info = null;
            // 获得证书版本
            info = String.valueOf(((X509Certificate) cert).getVersion());
            System.out.println("证书版本:" + info);
            // 获得证书序列号
            info =((X509Certificate) cert).getSerialNumber().toString(16);
            System.out.println("证书序列号:" + info);
            // 获得证书有效期
            Date beforedate = ((X509Certificate) cert).getNotBefore();
            info = dateformat.format(beforedate);
            System.out.println("证书生效日期:" + info);
            Date afterdate = ((X509Certificate) cert).getNotAfter();
            info = dateformat.format(afterdate);
            Date newtime=new Date(System.currentTimeMillis());

            String new_time=dateformat.format(newtime);
            int i= Integer.parseInt(new_time.substring(0,3));
            System.out.println("当前日期:" + i);

            System.out.println("证书失效日期:" + info);
            // 获得证书主体信息
            info =((X509Certificate) cert).getSubjectDN().getName();
            System.out.println("证书拥有者:" + info);
            // 获得证书颁发者信息
            info = ((X509Certificate) cert).getIssuerDN().getName();
            System.out.println("证书颁发者:" + info);
            // 获得证书签名算法名称
            info = ((X509Certificate) cert).getSigAlgName();
            System.out.println("证书签名算法:" + info);

            //生成包含服务器证书的keyStore
            String keyStoreType = KeyStore.getDefaultType();
            Log.d("keystore type", keyStoreType);
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);

            keyStore.load(null, null);
            keyStore.setCertificateEntry("cert", cert);

            //用含有服务器证书的keystore生成一个TrustManager
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            Log.d("tmfAlgorithm", tmfAlgorithm);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
            trustManagerFactory.init(keyStore);

            //生成一个使用我们的TrustManager的SSLContext
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return sslContext;
    }

    /**
     * 双向认证
     */
    public static SSLContext Okhttps_s(InputStream ksIn, InputStream tsIn, String password) {
        KeyStore keyStore = null;
        SSLContext sslContext = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            // 客户端信任的服务器端证书
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            //读取证书
            //加载证书
            keyStore.load(ksIn, password.toCharArray());
//            IOUtils.close(ksIn);
            //初始化SSLContext
            sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keyStore, password.toCharArray());
            trustStore.load(null);
            Certificate certificate =
                    CertificateFactory.getInstance("X.509").generateCertificate(tsIn);
            //设置自己的证书
            trustStore.setCertificateEntry("server1", certificate);
            //通过信任管理器获取一个默认的算法
            String algorithm = TrustManagerFactory.getDefaultAlgorithm();
            //算法工厂创建
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
            trustManagerFactory.init(trustStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),  new SecureRandom());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return sslContext;
    }

    /**
     * http
     */
    public static OkHttpClient Okhttp() {
        return new OkHttpClient.Builder().connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS).build();
    }

}
