package info.zhwan.alimiapp.push;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kakao.auth.Session;
import com.kakao.usermgmt.response.model.UserProfile;

import java.nio.charset.Charset;
import java.util.Iterator;

import info.zhwan.alimiapp.R;
import info.zhwan.alimiapp.core.GlobalApplication;

public class GcmIntentService extends IntentService {
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle data = intent.getExtras();
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
//        for (Iterator<String> iterator = extras.keySet().iterator(); iterator.hasNext();) {
//            String key = iterator.next();
//            System.out.println(key + "\t" + extras.getString(key));
//        }
        if (!data.isEmpty()) {
            sendNotification( data );
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    private void sendNotification(Bundle data) {
        String userId = getUserId();
        String title = data.getString("title");
        String bookId = data.getString("bookid");
        String message = new String(data.getString("message").getBytes(), Charset.defaultCharset());
        Log.d("GcmIntentService", "ID is " + userId);
        Log.d("GcmIntentService", "title is " + title);
        Log.d("GcmIntentService", "bookId is " + bookId);
        Log.d("GcmIntentService", "message is " + message);

//        Intent intent = new Intent(this, SuccessActivity.class);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ip/bookId/userId"));

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.push_noti_icon)
//                .setTicker("간단한 알림 창")
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true);

        NotificationManager notiManager = NotificationManager.class.cast(getSystemService(Context.NOTIFICATION_SERVICE));
        notiManager.notify((int)System.currentTimeMillis(), builder.build());
    }

    private String getUserId() {
        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase("alimiapp.db", MODE_PRIVATE, null);
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT id from ALIMIAPP_TAB", null);
        cursor.moveToNext();
        return cursor.getString(0);
    }
}
