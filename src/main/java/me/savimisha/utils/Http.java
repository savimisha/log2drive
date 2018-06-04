package me.savimisha.utils;

import okhttp3.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

public class Http {
    private static final long DEFAULT_CONNECT_TIMEOUT = 10000;
    private static final long DEFAULT_READ_TIMEOUT = 10000;
    private static final long DEFAULT_WRITE_TIMEOUT = 10000;

    private static OkHttpClient client = null;

    private static void initialize() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .cookieJar(new JavaNetCookieJar(cookieManager));
        client = builder.build();
    }

    public static OkHttpClient client(){
        if (client == null){
            initialize();
        }
        return client;
    }

    public static void cancelAll() {
        client.dispatcher().cancelAll();
    }

}
