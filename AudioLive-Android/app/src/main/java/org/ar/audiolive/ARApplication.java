package org.ar.audiolive;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.kongzue.dialog.util.DialogSettings;
import com.yanzhenjie.kalle.BodyRequest;
import com.yanzhenjie.kalle.BuildConfig;
import com.yanzhenjie.kalle.Headers;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.KalleConfig;
import com.yanzhenjie.kalle.OkHttpConnectFactory;
import com.yanzhenjie.kalle.Request;
import com.yanzhenjie.kalle.RequestMethod;
import com.yanzhenjie.kalle.Response;
import com.yanzhenjie.kalle.Url;
import com.yanzhenjie.kalle.UrlRequest;
import com.yanzhenjie.kalle.connect.Interceptor;
import com.yanzhenjie.kalle.connect.RealTimeNetwork;
import com.yanzhenjie.kalle.connect.http.Chain;
import com.yanzhenjie.kalle.connect.http.LoggerInterceptor;
import com.yanzhenjie.kalle.cookie.DBCookieStore;
import com.yanzhenjie.kalle.util.IOUtils;

import org.ar.audiolive.manager.ARServerManager;
import org.ar.audiolive.util.Constants;
import org.ar.audiolive.util.SpUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.yanzhenjie.kalle.Headers.KEY_COOKIE;

public class ARApplication extends Application {

    private static ARApplication appInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance =this;
        SpUtil.init(this);
        DialogSettings.style = DialogSettings.STYLE.STYLE_IOS;
        kalle();
    }

    public static ARApplication the(){
        return appInstance;
    }

    public Context getContent(){
        return getApplicationContext();
    }

    private void kalle() {
        Kalle.setConfig(KalleConfig.newBuilder()
                //.addHeader("Connection","close")
                .connectFactory(OkHttpConnectFactory.newBuilder().build())
                .cookieStore(DBCookieStore.newBuilder(this).build())
                .connectionTimeout(100, TimeUnit.SECONDS)
                .network(new RealTimeNetwork(this))
                .addInterceptor(new LoggerInterceptor("AudioLive", BuildConfig.DEBUG))
                .addInterceptor(new LoginInterceptor())
                .build());
    }

    private class LoginInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request =chain.request();
            Response originResponse =chain.proceed(request);
            if (originResponse.code() ==401){  //登录信息过期
                Log.i("重新登录", "intercept: ");
                ARServerManager.getInstance().signIn(SpUtil.getString(Constants.UID)); //重新登录
            }
            return originResponse;
        }
    }

    public class RedirectInterceptor implements Interceptor {

        public RedirectInterceptor() {
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            if (response.isRedirect()) {
                Url oldUrl = request.url();
                Url url = oldUrl.location(response.headers().getLocation());
                Headers headers = request.headers();
                headers.remove(KEY_COOKIE);

                RequestMethod method = request.method();
                Request newRequest;
                if (method.allowBody()) {
                    newRequest = BodyRequest.newBuilder(url.builder(), request.method())
                            .setHeaders(headers)
                            .setParams(request.copyParams())
                            .body(request.body())
                            .build();
                } else {
                    newRequest = UrlRequest.newBuilder(url.builder(), request.method())
                            .setHeaders(headers)
                            .build();
                }
                IOUtils.closeQuietly(response);
                return chain.proceed(newRequest);
            }
            return response;
        }
    }

    public class RetryInterceptor implements Interceptor {

        private int mCount;

        public RetryInterceptor(int count) {
            this.mCount = count;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            try {
                return chain.proceed(chain.request());
            } catch (IOException e) {
                if (mCount > 0) {
                    mCount--;
                    return intercept(chain);
                }
                throw e;
            }
        }
    }
}
