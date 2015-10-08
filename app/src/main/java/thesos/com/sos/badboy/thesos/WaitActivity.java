package thesos.com.sos.badboy.thesos;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.logging.LogRecord;

public class WaitActivity extends AppCompatActivity {
    private static final String TAG = "theSos";
    private Accident accident;
    private Uri imagesUri;
    private ParseUser currentUser;
    Thread t;
    private TextView status;
    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String currentStatus = bundle.getString("status");
            setStatusText(currentStatus);
        }
    };
    private String objectId;
    private AccidentReport acidentReport;
    private Button waitingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fetch Facebook user info if it is logged
        currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && currentUser.isAuthenticated()) {
            makeMeRequest();
        }
        accident = (Accident) getIntent().getSerializableExtra("accident");
        if (getIntent().getExtras().getString("uri") != null) {
            imagesUri = Uri.parse(getIntent().getExtras().getString("uri"));
            Log.d("theSos", imagesUri.getPath());
        }
        Log.d("theSos", accident.getAccidentType());
        setContentView(R.layout.activity_wait);
        bindLayout();
    }

    private void bindLayout() {
        status = (TextView) findViewById(R.id.loadingTxtTop);

        waitingBtn = (Button) findViewById(R.id.startThread);
        waitingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

    }

    private void start() {
        try {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        //เริ่มติดต่อ
                        updateCurrentStatus("เตีมความพร้อมข้อมูล");
                        updateCurrentStatus("กำลังส่งข้อมูลขึ้น Server");
                        Log.d("theSos", "Prepare Send Accident to Parse");

                        acidentReport = new AccidentReport();
                        //acidentReport.setImagesUri(imagesUri);
                        acidentReport.setCurrentUser(currentUser);
                        acidentReport.setTextUI(status);
                        acidentReport.report();

                        //updateCurrentStatus("รอการติดต่อกลับ");

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            t = new Thread(run);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateCurrentStatus(String txt) {
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("status", txt);
        message.setData(bundle);
        handler.sendMessageDelayed(message, 0);
    }




    private void fireRescuer() {

    }


    private void uploadFileToParse() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wait, menu);
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

    private void makeMeRequest() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse response) {
                        if (user != null) {

                            JSONObject userProfile = new JSONObject();
                            try {

                                // profilePicture.setProfileId(user.getString("id"));

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
                                currentUser.put("type", "User");
                                currentUser.put("profile", userProfile);

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
    private void setStatusText(String status) {
        this.status.setText(status);
    }

}
