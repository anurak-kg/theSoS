package thesos.com.sos.badboy.thesos;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.parse.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ReportActivity extends ActionBarActivity {

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
    private BestLocationListener mBestLocationListener;
    private BestLocationProvider mBestLocationProvider;
    private ProfilePictureView profilePicture;
    public static final int REQUEST_CAMERA = 2;

    ImageView imageView;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setTitle("แจ้งเหตุร้ายเหตุด่วน ");

        bindLayout();


        Button testBtn = (Button) findViewById(R.id.testBtn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToTestActivity();
            }
        });
        //Button logoutBtn = (Button) findViewById(R.id.logout);
        Bundle i = getIntent().getExtras();
        if (i != null) {
            telephone = i.getString("Phone");
        }

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
        }

        bindMapWidget();
    }

    private void bindLayout() {
        Button buttonIntent = (Button) findViewById(R.id.takePictureBtn);
        buttonIntent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp =
                        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "IMG_" + timeStamp + ".jpg";
                File f = new File(Environment.getExternalStorageDirectory()
                        , "DCIM/Camera/" + imageFileName);
                uri = Uri.fromFile(f);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(Intent.createChooser(intent
                        , "Take a picture with"), REQUEST_CAMERA);
            }
        });

        Spinner accidentType = (Spinner) findViewById(R.id.AccidentDropDown);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.accident_type, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accidentType.setAdapter(adapter);


        usernameTxtView = (TextView) findViewById(R.id.username);
        emailTxtView = (TextView) findViewById(R.id.email);
        profilePicture = (ProfilePictureView) findViewById(R.id.profilePicture);
        alertBtn = (Button) findViewById(R.id.alertBtn);
        alertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sendAlert();
                redirectToWaiting();
                Log.d("Alert", "Clikkkkkkkkkkk");
            }
        });
    }

    private void goToTestActivity() {
        Intent i = new Intent(this, TestActivity.class);
        startActivity(i);
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
                Toast.makeText(getApplicationContext()
                        , uri.getPath(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Accident getAccidentData() {

        accident = new Accident();
        accident.setAccidentType("อุบัติเหตุทางรถ");
        accident.setLocation(locationGPS.getLatitude(), locationGPS.getLongitude());
        accident.setAccidentStatus("waiting");
        accident.setAccidentDescription("Bla Bla Bla");
        //accident.setUri(uri.toString());
        return accident;

        /*ParseObject pr = new ParseObject("accident");
        pr.put("accidentType", "อุบัติเหตุทางรถยนต์");
        pr.put("location", new ParseGeoPoint(locationGPS.getLatitude(), locationGPS.getLongitude()));
        pr.put("accidentDescription", "Bla Bla Bla");
        pr.put("rescuerId", ParseObject.createWithoutData("_User", rescuerId));
        pr.put("victimId", currentUser);
        pr.put("status", "waiting");
        pr.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "Send Success", Toast.LENGTH_SHORT).show();
                    Log.d("Alert", "Send Success" + latitude + " l :" + longitude);
                } else {
                    Log.d("Alert", "Send Error" + e.getLocalizedMessage());
                    makeText("Error Code 0x40002");
                }

            }
        });*/


    }

    private void sendAlert() {
        final ParseGeoPoint userGeolocation = new ParseGeoPoint(locationGPS.getLatitude(), locationGPS.getLongitude());
        ParseQuery query = new ParseQuery("_User");
        query.whereNear("location", userGeolocation);
        query.whereNotEqualTo("username", currentUser.getUsername());
        /*query.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    Toast.makeText(
                            getApplicationContext(),
                            parseObject.getString("username") + "  ห่างกัน: " + userGeolocation.distanceInKilometersTo(parseObject.getParseGeoPoint("location")) + "กม.",
                            Toast.LENGTH_LONG).show();
                    sendAlertInfo(parseObject.getObjectId());
                } else {
                    Log.d("Send", "Error Code 0x40003");
                    e.printStackTrace();
                }
            }
        });*/

    }

    private void bindMapWidget() {
        try {
            /*map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMyLocationEnabled(true);
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d("mapWidget", "Long" + longitude + "  Lat :" + latitude);
            LatLng myLocation = new LatLng(latitude, longitude);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));*/
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

                                profilePicture.setProfileId(user.getString("id"));

                                userProfile.put("facebookId", user.getLong("id"));
                                userProfile.put("name", user.getString("name"));
                                if (user.getString("gender") != null) {
                                    userProfile.put("gender", (String) user.getString("gender"));
                                }
                                if (user.getString("email") != null) {
                                    userProfile.put("email", (String) user.getString("email"));
                                }

                                if (telephone != null) {
                                    userProfile.put("telephone", telephone);
                                }


                           /*     Criteria criteria = new Criteria();
                                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                                String provider = locationManager.getBestProvider(criteria, true);
                                Location location = locationManager.getLastKnownLocation(provider);
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();


                                Geocoder gcd = new Geocoder(ReportActivity.this, Locale.getDefault());

                                List<Address> addresses = null;
                                try {
                                    addresses = gcd.getFromLocation(latitude, longitude, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (addresses.size() > 0) {
                                    System.out.println(addresses.get(0).getLocality());
                                    if (addresses.get(0).getLocality() != null) {
                                        userProfile.put("city", addresses.get(0).getLocality());
                                    }

                                }
*/
                                ParseUser currentUser = ParseUser.getCurrentUser();
                                currentUser.put("name", user.getString("name"));
                                currentUser.put("type", "User");
                                currentUser.put("profile", userProfile);

                                if (locationGPS != null) {
                                    currentUser.put("location", new ParseGeoPoint(locationGPS.getLatitude(), locationGPS.getLongitude()));
                                }
                                currentUser.saveInBackground();

                                // Show the user info
                                updateViewsWithProfileInfo();
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

    private void updateViewsWithProfileInfo() {
        currentUser = ParseUser.getCurrentUser();
        if (currentUser.has("profile")) {
            JSONObject userProfile = currentUser.getJSONObject("profile");
            try {

                if (userProfile.has("facebookId")) {
                    setProfileId(userProfile.getString("facebookId"));
                } else {
                    // Show the default, blank user profile picture
                    // userProfilePictureView.setProfileId(null);
                }

                if (userProfile.has("name")) {
                    usernameTxtView.setText(userProfile.getString("name"));
                } else {
                    usernameTxtView.setText("");
                }

                if (userProfile.has("gender")) {
                    // userGenderView.setText(userProfile.getString("gender"));
                } else {
                    // userGenderView.setText("");
                }

                if (userProfile.has("email")) {
                    emailTxtView.setText(userProfile.getString("email"));
                } else {
                    emailTxtView.setText("");
                }

            } catch (JSONException e) {
                Log.d("My", "Error parsing saved user data.");
            }
        }
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

    public void initLocation() {
        if (mBestLocationListener == null) {
            mBestLocationListener = new BestLocationListener() {


                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }

                public void onLocationUpdateTimeoutExceeded(BestLocationProvider.LocationType type) {
                }

                public void onLocationUpdate(Location location, BestLocationProvider.LocationType type, boolean isFresh) {
                    locationGPS = location;
                }
            };

            if (mBestLocationProvider == null) {
                mBestLocationProvider = new BestLocationProvider(this, true, false, 10000, 1000, 2, 0);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initLocation();
        mBestLocationProvider.startLocationUpdatesWithListener(mBestLocationListener);
    }


    public void makeText(String text) {
        Toast toast = new Toast(getApplicationContext());
        toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
    }
}
