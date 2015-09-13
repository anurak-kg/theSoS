package thesos.com.sos.badboy.thesos;

import android.location.Location;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by iMan on 9/12/2557.
 */
public class User {
    ParseUser currentUser;

    public void User() {
        currentUser = ParseUser.getCurrentUser();
    }

    public boolean updateLocation(Location location) {
        if (location != null) {
            currentUser.put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        } else {
            return false;
        }
    }
}
