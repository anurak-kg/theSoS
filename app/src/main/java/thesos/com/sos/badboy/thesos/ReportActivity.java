package thesos.com.sos.badboy.thesos;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.parse.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class ReportActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public Location locationGPS;
    String telephone;
    private TextView usernameTxtView;
    private TextView emailTxtView;
    private GoogleMap map;
    private String fbId;
    private ParseUser currentUser;
    private double longitude;
    private double latitude;
    private Button alertBtn;
    private Accident accident;
    private ProfilePictureView profilePicture;
    public static final int REQUEST_CAMERA = 2;

    ImageView imageView;
    Uri uri;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng myLocation;
    private Marker marker;
    private MarkerOptions markerOptions;
    private String[] accidentTypeArray = {"รถชน", "คนจมน้ำ", "สัตว์เข้าบ้าน", "ทำคลอด", "ช่วยคนฆ่าคนตาย", "ไฟไหม้", "น้ำท่วม", "อื่นๆ"};
    private String type;
    private ArrayList<String> arrayAccidnet;
    private Button buttonIntent;
    private int CAMERA_PHOTO_WIDTH = 600;
    private int CAMERA_PHOTO_HEIGHT = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setTitle("แจ้งเหตุร้ายเหตุด่วน ");

        bindLayout();
        arrayAccidnet = new ArrayList<String>(Arrays.asList(accidentTypeArray));

        //  logoutBtn.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //          onLogoutButtonClicked();
        //      }
        //  });

        //Fetch Facebook user info if it is logged
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && currentUser.isAuthenticated()) {
            makeMeRequest();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.report_fragment_container, UserView.newInstance(currentUser.getObjectId()))
                    .commit();
        }

        bindMapWidget();
        //init Provider
        initialLocation();

    }

    private void initialLocation() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void bindLayout() {
        buttonIntent = (Button) findViewById(R.id.takePictureBtn);
        buttonIntent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp =
                        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "IMG_" + timeStamp + ".jpg";
                File f = new File(Environment.getExternalStorageDirectory()
                        , "DCIM/Camera/" + imageFileName);
                uri = Uri.fromFile(f);

                intent.putExtra("outputX", CAMERA_PHOTO_WIDTH);
                intent.putExtra("outputY", CAMERA_PHOTO_HEIGHT);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);


                startActivityForResult(Intent.createChooser(intent
                        , "Take a picture with"), REQUEST_CAMERA);
            }
        });

        final Spinner accidentType = (Spinner) findViewById(R.id.AccidentDropDown);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, accidentTypeArray);
        accidentType.setAdapter(adapter);
        accidentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = accidentTypeArray[position];
                Log.d(TheSosApplication.TAG, "Type = " + type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        alertBtn = (Button) findViewById(R.id.alertBtn);
        alertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myLocation.latitude != 10 && myLocation.longitude != 10) {
                    redirectToWaiting();
                } else {
                    Toast.makeText(ReportActivity.this, "ไม่พบตำแหน่งของผู้ใช้ โปรดเช็คการตั้งค่า GPS", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void redirectToWaiting() {
        Intent i = new Intent(this, WaitActivity.class);
        i.putExtra("accident", getAccidentData());
        if (uri != null) {
            i.putExtra("uri", uri.getPath());
        }
        startActivity(i);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("theSoS", requestCode + " " + resultCode);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            getContentResolver().notifyChange(uri, null);
            ContentResolver cr = getContentResolver();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, uri);
                // imageView.setImageBitmap(bitmap);
                buttonIntent.setText("ถ่ายภาพใหม่");
                Toast.makeText(getApplicationContext()
                        , uri.getPath(), Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Accident getAccidentData() {

        accident = new Accident();
        accident.setAccidentType(type);
        accident.setLocation(myLocation.latitude, myLocation.longitude);
        accident.setAccidentStatus("waiting");
        accident.setAccidentDescription(type);
        if (uri != null) {
            accident.setUri(uri.getPath());
        }
        return accident;
    }


    private void bindMapWidget() {
        try {
         /*   MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
*/

            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMyLocationEnabled(true);
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d(TheSosApplication.TAG, "Long" + longitude + "  Lat :" + latitude);
            myLocation = new LatLng(latitude, longitude);
            markerOptions = new MarkerOptions().position(myLocation).title("My Location");
            marker = map.addMarker(markerOptions);

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));

        } catch (NullPointerException e) {
            Toast.makeText(ReportActivity.this, "เกิดข้อผิดผลาดในการหาตำแหน่งปัจจุบัน", Toast.LENGTH_SHORT).show();
            Log.d(TheSosApplication.TAG, "Null Error Location");
            myLocation = new LatLng(10, 10);
            markerOptions = new MarkerOptions().position(myLocation).title("My Location");
            marker = map.addMarker(markerOptions);

            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void makeMeRequest() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse response) {
                        if (user != null) {

                            JSONObject userProfile = new JSONObject();
                            try {


                                userProfile.put("facebookId", user.getLong("id"));
                                userProfile.put("name", user.getString("name"));
                                if (user.getString("gender") != null) {
                                    userProfile.put("gender", (String) user.getString("gender"));
                                }
                                if (user.getString("email") != null) {
                                    userProfile.put("email", (String) user.getString("email"));
                                }

                                ParseUser currentUser = ParseUser.getCurrentUser();
                                currentUser.put("name", user.getString("name"));
                                currentUser.put("profile", userProfile);

                                if (locationGPS != null) {
                                    currentUser.put("location", new ParseGeoPoint(locationGPS.getLatitude(), locationGPS.getLongitude()));
                                }
                                currentUser.saveInBackground();

                            } catch (JSONException e) {
                                Log.d("My", "Error parsing returned user data. " + e);
                            }

                        } else if (response.getError() != null) {
                            switch (response.getError().getCategory()) {
                                case LOGIN_RECOVERABLE:
                                    Log.d("theSOS",
                                            "Authentication error: " + response.getError());
                                    break;

                                case TRANSIENT:
                                    Log.d("theSOS",
                                            "Transient error. Try again. " + response.getError());
                                    break;

                                case OTHER:
                                    Log.d("theSOS",
                                            "Some other error: " + response.getError());
                                    break;
                            }
                        }
                    }
                }
        );
        request.executeAsync();
    }


    public void logout(MenuItem item) {
        ParseUser.logOut();
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report, menu);
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

    public void setProfileId(String profileId) {

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
        mLocationRequest.setInterval(1000); // Update location every second

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
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        animateMarker(marker, myLocation, false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));

    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }
}
