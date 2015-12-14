package thesos.com.sos.badboy.thesos;

import android.app.Fragment;
import android.content.Intent;
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

public class RescuerActivity extends AppCompatActivity{

    private static final String TAG = "TheSos";
    private static final double MAX_NEAR_KILOMATE = 10;
    private static final int LIMIT_RESCURER = 5;
    private static ParseGeoPoint currentLocation;
    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescuer);
        currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && currentUser.isAuthenticated()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, UserView.newInstance(currentUser.getObjectId()))
                    .commit();
        }else{
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

    private void bindFragment() {

    }

    private void getNearRescuer() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.whereWithinKilometers("location", currentLocation, MAX_NEAR_KILOMATE);
        query.setLimit(LIMIT_RESCURER);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                for (ParseObject object : list) {
                    ParseGeoPoint location = object.getParseGeoPoint("location");
                    Log.d(TAG, object.getString("name") + "อยู่ห่างจากผู้ใช้ " + location.distanceInKilometersTo(currentLocation) + " กม.");
                }
            }
        });


    }

    private void goToAccidentListActivity(){
        Intent i = new Intent(this, AccidentListActivity.class);
        startActivity(i);
        finish();
    }

    private void pushAccidentNotic() {
        JSONObject data = new JSONObject();
        try {
            data.put("title", "ทดสอบหน่อยไอสาด");
            data.put("text", "อุบัติเหตุทางเรือ @Chalong ระยะห่าง 1.2 กม.");
            data.put("accident_id", "gXMeLvFipa");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ParsePush push = new ParsePush();
        push.setChannel("");
        push.setData(data);
        push.sendInBackground();
        Toast.makeText(getApplicationContext(), "Send", Toast.LENGTH_LONG).show();
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

        return currentUser.getObjectId();
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
