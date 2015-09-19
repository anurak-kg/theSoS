package thesos.com.sos.badboy.thesos;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

public class RescuerActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescuer);
        Switch subscribeToggle = (Switch)findViewById(R.id.rescurer_sub);
        subscribeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    if(!ifSubscribed()){
                        //สมัครรับข้อมูล
                        subscribed();
                    }
                }else{
                    if(!ifSubscribed()){
                        //ยกเลิกการรับข้อมูล
                        unSubscribed();
                    }
                }

            }
        });
    }
    private  void  subscribed(){
        String channel = getChannel();
        ParsePush.subscribeInBackground(channel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(getApplicationContext(), "Subscribed ", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getChannel() {
        return "Test";
    }

    private void unSubscribed(){
        String channel = getChannel();
        ParsePush.unsubscribeInBackground(channel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(getApplicationContext(),"unSubscribe",Toast.LENGTH_LONG).show();
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
    public boolean ifSubscribed(){
        //check if device is already subscribed to Giants channel
        if(ParseInstallation.getCurrentInstallation().getList("channels").contains(getChannel())) {
            return true;
        }
        else{
            return false;
        }
    }
}
