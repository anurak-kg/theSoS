package thesos.com.sos.badboy.thesos;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thesos.com.sos.badboy.thesos.BestLocationProvider.LocationType;


public class TestActivity extends ActionBarActivity {
    private static final LatLng AMSTERDAM = new LatLng(52.37518, 4.895439);
    private static final LatLng PARIS = new LatLng(48.856132, 2.352448);
    private static final LatLng FRANKFURT = new LatLng(50.111772, 8.682632);
    public Location locationGPS;
    public RelativeLayout accidentMainLayout;
    Boolean listenAlert = false;
    BestLocationProvider mBestLocationProvider;
    BestLocationListener mBestLocationListener;
    User user;
    Alert alert;
    private Thread t;
    private TextView mTvLog;
    private TextView listenTextView;
    private ToggleButton tgBtn;
    private Button testBTN;
    private accidentListening thread;
    private TextView accidentVicTxt;
    private GoogleMap map;
    private ArrayList<LatLng> markerPoints;
    private Button rescueAcceptBtn;
    private Location victimLocation;
    private LatLng start;
    private LatLng end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        user = new User();
        bindWidget();
        // mTvLog = (TextView) findViewById(R.id.mTvLog);
        thread = new accidentListening();
        thread.start();
        alert = new Alert(getApplicationContext());

    }

    private void bindWidget() {

        //SetLayout
        accidentMainLayout = (RelativeLayout) this.findViewById(R.id.accidentMainLayout);
        //Set Widget
        tgBtn = (ToggleButton) findViewById(R.id.toggleButton);
        testBTN = (Button) findViewById(R.id.testBtn);
        accidentVicTxt = (TextView) findViewById(R.id.accidentVicTxt);
        bindMap();

        rescueAcceptBtn = (Button) findViewById(R.id.rescueAcceptBtn);
        testBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToVictim();

            }
        });

    }
    private void bindMap() {
        map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.accidentMap)).getMap();
       // map.setMyLocationEnabled(true);
    }

    private void routeToDestination() {
        // start = new LatLng(18.015365, -77.499382);
        // end = new LatLng(18.012590, -77.500659);
        start = new LatLng(locationGPS.getLatitude(), locationGPS.getLongitude());
        end = new LatLng(victimLocation.getLatitude(), victimLocation.getLongitude());

        String url = makeURL(start.latitude, start.longitude, end.latitude, end.longitude);
        new connectAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    private void navigateToVictim() {
        routeToDestination();
        setMarkMap();
        mapZoomFit();
    }

    private void setMarkMap() {
        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        map.addMarker(options);
        options.getPosition();
        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        map.addMarker(options);
    }

    private void mapZoomFit() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(start);
        builder.include(end);
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
        map.moveCamera(cu);
        map.animateCamera(cu);
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat, double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        return urlString.toString();
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public void drawPath(String result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            for (int z = 0; z < list.size() - 1; z++) {
                LatLng src = list.get(z);
                LatLng dest = list.get(z + 1);
                Polyline line = map.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                        .width(2)
                        .color(Color.BLUE).geodesic(true));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void updateUiAccident(Accident accident, ParseObject victimObj) {
        //Bind Widget
        TextView type = (TextView) findViewById(R.id.accidentTypeTxt);
        Location location = new Location("");
        //location.setLatitude(accident.getLocation().getLatitude());
       // location.setLongitude(accident.getLocation().getLongitude());
        setVictimLocation(location);
        accidentVicTxt.setText(victimObj.getString("name"));
        //update name
        //updateUiAccidentVictim(accident.getVictimId());
        type.setText(accident.getAccidentType());
        String rescueUsername;


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

                public void onLocationUpdateTimeoutExceeded(LocationType type) {
                }

                public void onLocationUpdate(Location location, LocationType type, boolean isFresh) {
                    locationGPS = location;
                    //user.updateLocation(location);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
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

    public Location getVictimLocation() {
        return victimLocation;
    }

    public void setVictimLocation(Location victimLocation) {
        this.victimLocation = victimLocation;
    }


    class accidentListening extends Thread {
        boolean listenRun = true;

        public void setListenRun(boolean status) {
            this.listenRun = status;
        }

        public void run() {
            Log.d("Loop", "Start");
            try {
                synchronized (this) {
                    while (listenRun) {
                        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("accident");
                        query.whereEqualTo("rescuerId", ParseObject.createWithoutData("_User", "GvFOd4AFsb"));
                        query.include("victimId");

                        /*query.getFirstInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e == null) {
                                    Accident accident = new Accident();
                                    accident.setVictimId(parseObject.getObjectId());
                                    accident.setAccidentType(parseObject.getString("accidentType"));
                                    accident.setLocation(parseObject.getParseGeoPoint("location"));
                                    // accident.setVictimId(parseObject.getString("victimId"));
                                    ParseObject victim = parseObject.getParseObject("victimId");
                                    updateUiAccident(accident, victim);
                                    alert.playAlarmSound();
                                    accidentMainLayout.setVisibility(View.VISIBLE);
                                    listenRun = false;
                                    run();
                                    Log.d("Loop", "Found");
                                } else {
                                    Log.d("Loop", "Not Found");
                                }
                            }
                        });*/
                        Thread.sleep(5000);
                    }
                }
                Thread.sleep(5000);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


    private class connectAsyncTask extends AsyncTask<String, Void, String> {
        String url;
        private ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            Log.d("data", "doInBackground");
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(params[0]);              //Calling json parsing class(no 9) and passing url variable
            return json;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            Log.d("data", "onPreExecute");

            progressDialog = new ProgressDialog(TestActivity.this);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("data", "onPostExecute");

            progressDialog.hide();
            if (result != null) {
                drawPath(result);
            }
        }
    }

}