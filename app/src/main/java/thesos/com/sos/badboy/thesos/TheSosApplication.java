package thesos.com.sos.badboy.thesos;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

/**
 * Created by Anurak on 19/09/58.
 */
public class TheSosApplication extends  MultiDexApplication{

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialization code here
        // Set your Parse app id and client key in strings.xml
        Parse.initialize(this, getString(R.string.applicationId), getString(R.string.clientKey));
        ParseInstallation.getCurrentInstallation().saveInBackground();

        //Facebook initialize
        ParseFacebookUtils.initialize(this);

        //Subscribe to notifications - default channel ""
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("TheSos", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("TheSos", "failed to subscribe for push", e);
                }
            }
        });

    }
}
