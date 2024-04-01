package gibbie.dino.readers.retrofitsetup;


import android.util.Log;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator
{
    // region Constants
    private static final int DISK_CACHE_SIZE = 10 * 1024 * 1024; // 10MB

    private static OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();

    // endregion

    private static Retrofit.Builder retrofitBuilder
            = new Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create());

    private static OkHttpClient defaultOkHttpClient = new OkHttpClient.Builder()
            .connectTimeout(40, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .cache(getCache())
            .build();

    // No need to instantiate this class.
    private ServiceGenerator()
    {
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl)
    {

        return createService(serviceClass, baseUrl, null);
    }

    public static <S> S createService(Class<S> serviceClass)
    {
        return createService(serviceClass, WebUrl.BASEAPIURL, null);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, Interceptor networkInterceptor) {
        OkHttpClient.Builder okHttpClientBuilder = defaultOkHttpClient.newBuilder();

        if (networkInterceptor != null) {
            okHttpClientBuilder.addNetworkInterceptor(networkInterceptor);
        }

        OkHttpClient modifiedOkHttpClient = okHttpClientBuilder
//                .addInterceptor(getHttpLoggingInterceptor())
                .build();

        retrofitBuilder.client(modifiedOkHttpClient);
        retrofitBuilder.baseUrl(baseUrl);

        Retrofit retrofit = retrofitBuilder.build();
        return retrofit.create(serviceClass);
    }

    private static okhttp3.Cache getCache() {

        okhttp3.Cache cache = null;
        // Install an HTTP cache in the application cache directory.
        try {
            File cacheDir = new File(DinoReadersApplication.getCacheDirectory(), "http");
            cache = new okhttp3.Cache(cacheDir, DISK_CACHE_SIZE);
        } catch (Exception e) {
            Log.e(e.toString(), "Unable to install disk cache.");
        }
        return cache;
    }

    private static HttpLoggingInterceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        return httpLoggingInterceptor;
    }

}


