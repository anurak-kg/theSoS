package thesos.com.sos.badboy.thesos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AccidentListActivity extends AppCompatActivity {

    ListView listview;
    List<ParseObject> ob;
    ProgressDialog mProgressDialog;
    private ArrayList<Accident> accidentlist;
    private AccidentList adapter;
    public static final String TAG = "TheSos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_list);
        bindListView();
        new RemoteDataTask().execute();

    }

    private void bindListView() {
        listview = (ListView) findViewById(R.id.listView);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), accidentlist.get(position).getAccidentId(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(AccidentListActivity.this,AccidentActivity.class);
                intent.putExtra("objectId",accidentlist.get(position).getAccidentId());
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accident_list, menu);
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


    // RemoteDataTask AsyncTask
    private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //สร้าง Dialog การโหลดข้อมูล
            mProgressDialog = new ProgressDialog(AccidentListActivity.this);
            mProgressDialog.setTitle("กำลังโหลดข้อมูลอุบัติเหตุุ");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            accidentlist = new ArrayList<Accident>();
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH,Calendar.getInstance().get(Calendar.MONTH));
                Date date = calendar.getTime();


                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("accident");
                query.whereGreaterThanOrEqualTo("createdAt", date);
                query.setLimit(100);
                query.orderByDescending("createdAt");
                query.include("victimId");
                ob = query.find();
                Log.d(TheSosApplication.TAG,"Accident summary = " +ob.size());
                for (ParseObject acc : ob) {

                    //ดาวน์โหลดข้อมูล
                    ParseFile image = (ParseFile) acc.get("photo");
                    ParseUser victim = acc.getParseUser("victimId");


                    Accident accident = new Accident();
                    accident.setAccidentId(acc.getObjectId());
                    accident.setAccidentType(acc.getString("accidentType"));
                    accident.setAccidentStatus(acc.getString("status"));
                    Log.d(TheSosApplication.TAG,victim.toString());
                    Log.d(TheSosApplication.TAG,victim.getString("name"));

                    //ค้นหา Location name
                    try {
                        ParseGeoPoint geoPoint = acc.getParseGeoPoint("location");
                        if (geoPoint != null) {
                            GeocodeAddress geocodeAddress = new GeocodeAddress(geoPoint.getLatitude(), geoPoint.getLongitude());
                            if (geocodeAddress.getFomatLineNumber() != null) {
                                Log.d(TAG, "geoPoint  =   " + geocodeAddress.getFomatLineNumber());
                                accident.setAddress(geocodeAddress.getFomatLineNumber());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        accident.setAddress("เกิดปัญหาในการดึงชื่อ");

                    } catch (Exception e){
                        accident.setAddress("เกิดปัญหาในการดึงชื่อ");
                    }


                    if (image != null) {
                        accident.setUri(image.getUrl());
                    }

                    //โหลดข้อมูลผู้แจ้ง

                    accidentlist.add(accident);
                }
            } catch (ParseException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Locate the listview in listview_main.xml
            // Pass the results into ListViewAdapter.java
            adapter = new AccidentList(AccidentListActivity.this, accidentlist);
            // Binds the Adapter to the ListView
            listview.setAdapter(adapter);
            // Close the progressdialog
            mProgressDialog.dismiss();
        }
    }
}
