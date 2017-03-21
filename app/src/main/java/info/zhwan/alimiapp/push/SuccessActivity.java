package info.zhwan.alimiapp.push;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.network.ErrorResult;
import com.kakao.push.PushActivity;
import com.kakao.push.PushService;
import com.kakao.push.response.model.PushTokenInfo;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.helper.Utility;
import com.kakao.util.helper.log.Logger;

import java.util.Arrays;
import java.util.List;

import info.zhwan.alimiapp.MainActivity;
import info.zhwan.alimiapp.R;
import info.zhwan.alimiapp.core.GlobalApplication;
import info.zhwan.alimiapp.core.widget.DialogBuilder;
import info.zhwan.alimiapp.core.widget.KakaoToast;
import info.zhwan.alimiapp.core.widget.ProfileLayout;

/**
 * @author Jihwan Hwang
 */
public class SuccessActivity extends PushActivity {

    private UserProfile userProfile;
    private ProfileLayout profileLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        initializeProfileView();
    }

    private void initializeProfileView() {
        profileLayout = (ProfileLayout) findViewById(R.id.com_kakao_user_profile);
        profileLayout.setMeResponseCallback(new MeResponseCallback() {
            @Override
            public void onNotSignedUp() {
//                redirectSignupActivity();
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Logger.e(message);
                KakaoToast.makeToast(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onSuccess(UserProfile result) {
//                KakaoToast.makeToast(getApplicationContext(), "succeeded to get user profile", Toast.LENGTH_SHORT).show();
                if (result != null) {
                    SuccessActivity.this.userProfile = result;
                    userProfile.saveUserToCache();
                    showProfile();
                    saveUserId();
                }
            }

            private void saveUserId() {
                SQLiteDatabase sqLiteDatabase = openOrCreateDatabase("alimiapp.db", MODE_PRIVATE, null);
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS ALIMIAPP_TAB");
                sqLiteDatabase.execSQL("CREATE TABLE ALIMIAPP_TAB (id text)");
                sqLiteDatabase.execSQL("INSERT INTO ALIMIAPP_TAB (id) VALUES ('" + userProfile.getId() + "')");
            }
        });
        profileLayout.requestMe();
    }

    private void showProfile() {
        if (profileLayout != null) {
            profileLayout.setUserProfile(userProfile);
        }
    }

    @Override
    protected void redirectLoginActivity() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected String getDeviceUUID() {
        return GlobalApplication.getGlobalApplicationContext().getKakaoSDKAdapter().getPushConfig().getDeviceUUID();
    }

    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.registger_button:
                registerToken();
                break;
            case R.id.unregistger_button:
                unregisterToken();
                break;
            case R.id.view_button:
                viewToken();
                break;
            case R.id.logout_button:
                logout();
                break;
        }
    }

    private void registerToken() {
        PushService.registerPushToken(new KakaoPushResponseCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                KakaoToast.makeToast(getApplicationContext(), "succeeded to register push token", Toast.LENGTH_SHORT).show();
            }
        }, FirebaseInstanceId.getInstance().getToken(), KakaoSDK.getAdapter().getPushConfig().getDeviceUUID(), Utility.getAppVersion(this));
    }

    private void unregisterToken() {
        PushService.deregisterPushToken(new KakaoPushResponseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                KakaoToast.makeToast(getApplicationContext(), "succeeded to deregister push token", Toast.LENGTH_SHORT).show();
            }
        }, KakaoSDK.getAdapter().getPushConfig().getDeviceUUID());
    }

    private void viewToken() {
        PushService.getPushTokens(new KakaoPushResponseCallback<List<PushTokenInfo>>() {
            @Override
            public void onSuccess(List<PushTokenInfo> result) {
                String message = "succeeded to get push tokens." +
                        "\ncount=" + result.size() +
                        "\ntokens=" + Arrays.toString(result.toArray(new PushTokenInfo[result.size()]));
                new DialogBuilder(SuccessActivity.this)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
    }

    private void logout() {
        unregisterToken();
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                redirectLoginActivity();
            }
        });
    }

    abstract class KakaoPushResponseCallback<T> extends ApiResponseCallback<T> {
        @Override
        public void onFailure(ErrorResult errorResult) {
            KakaoToast.makeToast(getApplicationContext(), "failure : " + errorResult, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSessionClosed(ErrorResult errorResult) {
            System.out.println(errorResult);
            redirectLoginActivity();
        }

        @Override
        public void onNotSignedUp() {
//            redirectSignupActivity();
        }
    }
}
