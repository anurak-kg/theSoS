package thesos.com.sos.badboy.thesos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.senab.photoview.PhotoViewAttacher;

public class AccidentActivity extends AppCompatActivity {

    private String objectId;
    private static final String TAG = "TheSos";
    private ProgressDialog progressDialog;
    private TextView txtAccidentType;
    private ImageView accidentPhoto;
    private String mode = "normal";
    private android.support.v7.widget.CardView accidentlayouttop;
    private TextView typeOfAccident;
    private boolean alert = false;
    private TextView typeOfAccidentTxt;
    private android.support.v7.widget.CardView accidentlayoutmiddle;
    private android.support.v7.widget.CardView accidentviewmap;
    private android.support.v7.widget.CardView accidentviewphoto;
    private android.widget.Button AccidentAcceptBtn;
    private android.widget.Button AccidentCancelBtn;
    private android.support.v7.widget.CardView accidentviewaccept;
    private ParseUser victim;
    private ParseGeoPoint accidentLocation;
    private GoogleMap map;
    private android.widget.RelativeLayout accidentuserfragment;
    private android.widget.RelativeLayout accidentviewscrollview;
    private String tempId;
    private TextView dateOfAccident;
    private TextView dateAccidentTextView;
    private PhotoViewAttacher mAttacher;
    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident);
        this.dateAccidentTextView = (TextView) findViewById(R.id.dateAccidentTextView);
        this.dateOfAccident = (TextView) findViewById(R.id.dateOfAccident);
        this.accidentviewscrollview = (RelativeLayout) findViewById(R.id.accident_view_scrollview);
        this.accidentuserfragment = (RelativeLayout) findViewById(R.id.accident_user_fragment);
        this.accidentviewaccept = (CardView) findViewById(R.id.accident_view_accept);
        this.AccidentCancelBtn = (Button) findViewById(R.id.AccidentCancelBtn);
        this.AccidentAcceptBtn = (Button) findViewById(R.id.AccidentAcceptBtn);
        this.accidentviewphoto = (CardView) findViewById(R.id.accident_view_photo);
        this.accidentPhoto = (ImageView) findViewById(R.id.accidentPhoto);
        this.accidentviewmap = (CardView) findViewById(R.id.accident_view_map);
        this.accidentlayoutmiddle = (CardView) findViewById(R.id.accident_layout_middle);
        this.typeOfAccidentTxt = (TextView) findViewById(R.id.typeOfAccidentTxt);
        this.typeOfAccident = (TextView) findViewById(R.id.typeOfAccident);
        this.accidentlayouttop = (CardView) findViewById(R.id.accident_layout_top);

        currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
        }

        objectId = getIntent().getExtras().getString("objectId");
        mode = getIntent().getExtras().getString("mode");
        tempId = getIntent().getExtras().getString("tempId");


        bindLayout();
        if (objectId != null) {
            Log.d(TheSosApplication.TAG, "Accident Id = " + objectId);
            Log.d(TheSosApplication.TAG, "Temp Id = " + tempId);
            getAccidentData();
        }
        if (mode != null && mode.equals("ALERT")) {
            alert = true;
            setAlertVisible(View.VISIBLE);
        } else {
            setAlertVisible(View.GONE);

        }

    }

    private void updateAccidentStatus(final String status) {
        ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("tempdata");
        parseQuery.whereEqualTo("objectId", tempId);
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                object.put("status", status);
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Toast.makeText(AccidentActivity.this, "ส่งข้อมูลไปยังที่เกิดเหตุแล้ว", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setAlertVisible(int view) {
        accidentviewaccept.setVisibility(view);

    }

    private void bindLayout() {
        txtAccidentType = (TextView) findViewById(R.id.typeOfAccidentTxt);
        AccidentAcceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAccidentStatus("ACCEPT");
                setAlertVisible(View.GONE);

            }
        });
        AccidentCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAccidentStatus("REJECT");
                setAlertVisible(View.GONE);

            }
        });
        accidentPhoto = (ImageView) findViewById(R.id.accidentPhoto);
        accidentPhoto.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.loading));

        //Bind Map
        map = ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.accident_view_map_fragment)).getMap();

        //ตั้งให้ Map Fragment ไม่สามารถ เลื่อนได้
        ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.accident_view_map_fragment)).setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                accidentviewscrollview.requestDisallowInterceptTouchEvent(true);
            }
        });

    }

    private void getAccidentData() {
        progressDialog = ProgressDialog.show(AccidentActivity.this, "", "Loading...");
        new Thread() {
            public void run() {
                try {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("accident");
                    query.whereEqualTo("objectId", objectId);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {


                                Log.d(TheSosApplication.TAG, "Download Accident Data!!");
                                Log.d(TheSosApplication.TAG, "|| Accident ID = " +parseObject.getObjectId());
                                victim = parseObject.getParseUser("victimId");
                                accidentLocation = parseObject.getParseGeoPoint("location");
                                txtAccidentType.setText(parseObject.getString("accidentType"));
                                String type = parseObject.getString("accidentType");

                                //โหลดรูปภาพ
                                ParseFile imageFile = parseObject.getParseFile("photo");
                                if (imageFile != null) {
                                    Log.d(TAG, "PhotoUrl " + imageFile.getUrl());
                                    Glide.with(AccidentActivity.this)
                                            .load(imageFile.getUrl())
                                            .centerCrop()
                                            .error(R.drawable.no_photo_grey)
                                            .into(accidentPhoto);
                                    mAttacher = new PhotoViewAttacher(accidentPhoto);
                                }else{
                                    accidentPhoto.setImageResource(R.drawable.no_photo_grey);
                                }

                                //ข้อมูลเวลา
                                Date date = parseObject.getCreatedAt();
                                if (date != null) {
                                    String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                                    dateAccidentTextView.setText(format);
                                }
                                //ข้อมูลประเภท
                                Log.d(TheSosApplication.TAG, "|| Type = "+ type);

                                if (type != null) {
                                    typeOfAccidentTxt.setText(parseObject.getString("accidentType"));
                                }


                                setVictimFragments();
                                moveAndMarkMapCamera();



                            } else {
                                Toast.makeText(getApplicationContext(), "เกิดข้อผิดผลาด : ไม่สามารถหาข้อมูลการเกิดอุบัติเหตุได้", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Error: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "เกิดข้อผิดผลาด 0x00001 : ไม่สามารถหาข้อมูลการเกิดอุบัติเหตุได้", Toast.LENGTH_LONG).show();
                    Log.e("tag", e.getMessage());
                }
                progressDialog.dismiss();
            }
        }.start();


    }

    private void moveAndMarkMapCamera() {
        double longitude = accidentLocation.getLongitude();
        double latitude = accidentLocation.getLatitude();
        Log.d(TheSosApplication.TAG, "Accident Location = Long : " + longitude + "  Lat : " + latitude);
        if (accidentLocation != null) {
            LatLng latLng = new LatLng(latitude, longitude);
            if (alert) {
                //เริ่มกระบวนการในการหาเส้นทาง
                ParseGeoPoint parseGeoPoint = currentUser.getParseGeoPoint("location");
                if (parseGeoPoint != null) {
                    MapRoute route = new MapRoute(AccidentActivity.this, map);
                    route.setAccidentLatLng(latLng);
                    route.setRescuerLatLng(new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude()));
                    route.start();
                    route.setMarkMap();
                    route.setMapZoomFit();
                } else {
                    Toast.makeText(AccidentActivity.this, "ไม่สามารถนำทางได้เนื่องจากไม่พบตำแหน่งกู้ภัย", Toast.LENGTH_LONG).show();
                }

            } else {
                //Mark ตำแหน่งบนแผนที่
                map.addMarker(new MarkerOptions()
                        .title("จุดเกิดเหตุอุบัติเหตุ !")
                        .position(latLng));
            }

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));


        }
    }


    private void setVictimFragments() {
        if (victim != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.accident_user_fragment, UserView.newInstance(victim.getObjectId()))
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accident, menu);
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
}
