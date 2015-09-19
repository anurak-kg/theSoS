/**
 * Created by Anurak on 19/09/58.
 */

package thesos.com.sos.badboy.thesos;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import org.json.JSONException;
import org.json.JSONObject;

public class AccidentPushBroadcastReceive extends ParsePushBroadcastReceiver {
    public static final String PARSE_DATA_KEY = "com.parse.Data";
    private static final String TAG = "TheSos";

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // deactivate standard notification
        return null;
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Log.d(TAG,"Push Open");
        Intent i = new Intent(context, AccidentActivity.class);
        i.putExtras(intent.getExtras());
        context.startActivity(i);

    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.d(TAG,"Push Receive");
        JSONObject data = getDataFromIntent(intent);
        // Do something with the data. To create a notification do:

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

       Notification notification =
                new NotificationCompat.Builder(context) // this is context
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("มีการแจ้งอุบัติเหตุ !!!!")
                        .setContentText("สวัสดีครับ ยินดีต้อนรับเข้าสู่บทความ Android Notification :)")
                        .setAutoCancel(true)
                        .build();

        notificationManager.notify("TheSos", 0, notification);

        Intent i = new Intent(context, AccidentActivity.class);
        i.putExtras(intent.getExtras());
        context.startActivity(i);
    }

    private JSONObject getDataFromIntent(Intent intent) {
        JSONObject data = null;
        try {
            data = new JSONObject(intent.getExtras().getString(PARSE_DATA_KEY));
        } catch (JSONException e) {
            // Json was not readable...
        }
        return data;
    }
}
