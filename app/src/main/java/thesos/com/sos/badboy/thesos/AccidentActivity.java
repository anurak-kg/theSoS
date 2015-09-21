package thesos.com.sos.badboy.thesos;

import android.app.ProgressDialog;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class AccidentActivity extends ActionBarActivity {

    private String accidentId;
    private static final String TAG = "TheSos";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident);
        accidentId = getIntent().getExtras().getString("accident_id");
        if(accidentId != null){
            getAccidentData();
        }
    }

    private void getAccidentData() {
        progressDialog = ProgressDialog.show(AccidentActivity.this, "", "Loading...");
        new Thread() {
            public void run() {
                try {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("accident");
                    query.whereNotEqualTo("objectId", accidentId);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                Toast.makeText(getApplicationContext(), parseObject.getObjectId(), Toast.LENGTH_LONG).show();


                            } else {
                                Toast.makeText(getApplicationContext(), "เกิดข้อผิดผลาด : ไม่สามารถหาข้อมูลการเกิดอุบัติเหตุได้", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Error: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });


                } catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
                // dismiss the progress dialog
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
