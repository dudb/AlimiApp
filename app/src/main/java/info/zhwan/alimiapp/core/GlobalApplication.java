package info.zhwan.alimiapp.core;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.kakao.auth.KakaoSDK;
import com.kakao.push.PushService;
import com.kakao.usermgmt.response.model.UserProfile;

public class GlobalApplication extends Application {
    private static volatile GlobalApplication instance = null;
    private ImageLoader imageLoader;
    private KakaoSDKAdapter kakaoSDKAdapter;

    //
    private UserProfile userProfile;

    public static GlobalApplication getGlobalApplicationContext() {
        if(instance == null)
            throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        kakaoSDKAdapter = new KakaoSDKAdapter();
        KakaoSDK.init(kakaoSDKAdapter);
        PushService.init();

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {
            final LruCache<String, Bitmap> imageCache = new LruCache<String, Bitmap>(30);

            @Override
            public void putBitmap(String key, Bitmap value) {
                imageCache.put(key, value);
            }

            @Override
            public Bitmap getBitmap(String key) {
                return imageCache.get(key);
            }
        };

        imageLoader = new ImageLoader(requestQueue, imageCache);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public KakaoSDKAdapter getKakaoSDKAdapter() {
        return kakaoSDKAdapter;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }
}
