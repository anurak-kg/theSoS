/**
 * Created by Anurak on 19/09/58.
 */
package thesos.com.sos.badboy.thesos;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        try {
            Log.d(TAG, "Push Receive");
            JSONObject data = getDataFromIntent(intent);
            Intent showFullQuoteIntent = new Intent(context, AccidentActivity.class);

            showFullQuoteIntent.putExtra("objectId", data.getString("accident_id"));
            showFullQuoteIntent.putExtra("mode", "ALERT");


            int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, uniqueInt, showFullQuoteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification = null;

            notification = new NotificationCompat.Builder(context) // this is context
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(data.getString("title"))
                    .setContentText(data.getString("text"))
                    .setSound(Uri.parse("android.resource://thesos.com.sos.badboy.thesos/" + R.raw.alarmsound))
                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify("TheSos", 0, notification);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private JSONObject getDataFromIntent(Intent intent) {
        JSONObject data = null;
        try {
            data = new JSONObject(intent.getExtras().getString(PARSE_DATA_KEY));
        } catch (JSONException e) {
            Log.d(TAG, "Error Parse JSON Object");
        }
        return data;
    }

}
