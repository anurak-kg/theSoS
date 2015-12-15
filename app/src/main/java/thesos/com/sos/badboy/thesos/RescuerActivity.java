package thesos.com.sos.badboy.thesos;

import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RescuerActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "TheSos";
    private static final double MAX_NEAR_KILOMATE = 10;
    private static final int LIMIT_RESCURER = 5;
    private static final long TIME_INTERVAL_LOCATION = 10000;
    private static ParseGeoPoint currentLocation;
    private ParseUser currentUser;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescuer);
        initialLocation();

        currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && currentUser.isAuthenticated()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, UserView.newInstance(currentUser.getObjectId()))
                    .commit();
        } else {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }

        Button actListViewBtn = (Button) findViewById(R.id.act_list_btn);
        actListViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RescuerActivity.this, AccidentListActivity.class);
                startActivity(i);
            }
        });
        Switch subscribeToggle = (Switch) findViewById(R.id.rescurer_sub);
        subscribeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (!ifSubscribed()) {
                        //สมัครรับข้อมูล
                        subscribed();
                    }
                } else {
                    if (!ifSubscribed()) {
                        //ยกเลิกการรับข้อมูล
                        unSubscribed();
                    }
                }

            }
        });
        currentLocation = new ParseGeoPoint(7.848657250419213, 98.32979081334793);


    }

    private void goToAccidentListActivity() {
        Intent i = new Intent(this, AccidentListActivity.class);
        startActivity(i);
        finish();
    }


    private void subscribed() {
        String channel = getChannel();
        ParsePush.subscribeInBackground(channel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(getApplicationContext(), "Subscribed ", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getChannel() {

        return "A" + currentUser.getObjectId();
    }

    private void unSubscribed() {
        String channel = getChannel();
        ParsePush.unsubscribeInBackground(channel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(getApplicationContext(), "unSubscribe", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initialLocation() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(TIME_INTERVAL_LOCATION); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TheSosApplication.TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TheSosApplication.TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TheSosApplication.TAG, "Location received: " + location.toString());
        currentUser.put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d(TheSosApplication.TAG, "Saved new Location!");

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rescuer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean ifSubscribed() {
        //check if device is already subscribed to Giants channel
        if (ParseInstallation.getCurrentInstallation().getList("channels").contains(getChannel())) {
            return true;
        } else {
            return false;
        }
    }


}
