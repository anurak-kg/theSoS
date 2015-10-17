package thesos.com.sos.badboy.thesos;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anurak on 18/10/58.
 */

public class MapRoute {
    public Context mContext;
    private GoogleMap map;
    private LatLng accidentLatLng;
    private LatLng rescuerLatLng;

    public MapRoute(Context context, GoogleMap map) {
        this.mContext = context;
        this.map = map;
    }

    public void start() {
        Log.d(TheSosApplication.TAG, "Route Procress Start");
        String url = makeURL(rescuerLatLng.latitude, rescuerLatLng.longitude, accidentLatLng.latitude, accidentLatLng.longitude);
        new routeTaskAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }


    public void setMarkMap() {

        // ปักหมุดตำแหน่งกุ้ภัย
        MarkerOptions options = new MarkerOptions();
        options.position(getRescuerLatLng());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        map.addMarker(options);
        options.getPosition();

        // ปักหมุดที่ตำแหน่งอุบัติเหตุ
        options = new MarkerOptions();
        options.position(getAccidentLatLng());
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        map.addMarker(options);
    }
    public void setMapZoomFit() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(getRescuerLatLng());
        builder.include(getAccidentLatLng());
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
        map.moveCamera(cu);
        map.animateCamera(cu);
    }
    private List<LatLng> decodePoly(String encoded) {
        Log.d(TheSosApplication.TAG, "Start Decode PolyLine");

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

    private String makeURL(double sourcelat, double sourcelog, double destlat, double destlog) {
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

    public LatLng getRescuerLatLng() {
        return rescuerLatLng;
    }

    public void setRescuerLatLng(LatLng rescuerLatLng) {
        this.rescuerLatLng = rescuerLatLng;
    }

    public LatLng getAccidentLatLng() {
        return accidentLatLng;
    }

    public void setAccidentLatLng(LatLng accidentLatLng) {
        this.accidentLatLng = accidentLatLng;
    }

    private class routeTaskAsync extends AsyncTask<String, Void, String> {
        String url;
        private ProgressDialog progressDialog;
        // private android.content.Context mContext;
        OkHttpClient client = new OkHttpClient();


        @Override
        protected String doInBackground(String... params) {
            Log.d(TheSosApplication.TAG, "Start fetch route ");
            Log.d(TheSosApplication.TAG, "route google url is :  " + params[0]);

            try {
                Request request = new Request.Builder()
                        .url(params[0])
                        .build();
                Response response = client.newCall(request).execute();
                String data =  response.body().string();
                return data;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            Log.d(TheSosApplication.TAG, "Pre fetch route ");

            /*progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();*/
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TheSosApplication.TAG, "Post fetch route ");

            //progressDialog.hide();
            if (result != null) {
                drawPath(result);
            }
        }
    }

}
