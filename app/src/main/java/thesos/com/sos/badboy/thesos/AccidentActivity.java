package thesos.com.sos.badboy.thesos;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class AccidentActivity extends AppCompatActivity {

    private String accidentId;
    private static final String TAG = "TheSos";
    private ProgressDialog progressDialog;
    private TextView txtAccidentType;
    private ImageView accidentPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident);
        accidentId = getIntent().getExtras().getString("accident_id");
        bindLayout();
        if (accidentId != null) {
            getAccidentData();
        }

    }

    private void bindLayout() {
        txtAccidentType = (TextView) findViewById(R.id.typeOfAccidentTxt);
        accidentPhoto = (ImageView) findViewById(R.id.accidentPhoto);
        // Uri uriLoadingPhoto = Uri.parse("android.resource://thesos.com.sos.badboy.thesos/" + R.raw.loadingtxt);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading);
        accidentPhoto.setImageBitmap(bitmap);


    }

    private void getAccidentData() {
        progressDialog = ProgressDialog.show(AccidentActivity.this, "", "Loading...");
        new Thread() {
            public void run() {
                try {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("accident");
                    query.whereNotEqualTo("objectId", accidentId);
                    // query.getFirst();
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(final ParseObject parseObject, ParseException e) {
                            if (e == null) {

                                txtAccidentType.setText(parseObject.getString("accidentType"));
                                //โหลดรูปภาพ
                                ParseFile imageFile = parseObject.getParseFile("photo");
                                imageFile.getDataInBackground(new GetDataCallback() {
                                    public void done(byte[] data, ParseException e) {
                                        if (e == null) {
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                            accidentPhoto.setImageBitmap(bitmap);
                                        } else {
                                            Log.d(TAG, "Error: ไม่พบภาพถ่าย");
                                        }
                                    }
                                });

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
