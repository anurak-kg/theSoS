package thesos.com.sos.badboy.thesos;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.directions.route.Route;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    private ImageButton loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //getHashKey();
        bindWidget();

        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            checkUserType(currentUser.getString("type"));
            Log.d(TheSosApplication.TAG,"User Type =" + currentUser.getString("type"));
           // showExtraData();

        }
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //telephoneAlert();
                onLoginButtonClicked();
            }
        });

    }

    private void checkUserType(String type) {
        Log.d(TheSosApplication.TAG,"User Type = " +type);


        if (type == null) {
            showExtraData();
        } else if (type.equals("User")) {
            Log.d(TheSosApplication.TAG,"User Type = " +type);

            Intent i = new Intent(this, ReportActivity.class);
            startActivity(i);
            finish();
        } else if (type.equals("Rescuer")) {
            Log.d(TheSosApplication.TAG,"User Type = " +type);

            Intent i = new Intent(this, RouteActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void bindWidget() {
        loginBtn = (ImageButton) findViewById(R.id.loginBtn);
    }

    private void onLoginButtonClicked() {
        MainActivity.this.progressDialog =
                ProgressDialog.show(MainActivity.this, "", "Logging in...", true);

        List<String> permissions = Arrays.asList("public_profile", "email");
        // NOTE: for extended permissions, like "user_about_me", your app must be reviewed by the Facebook team
        // (https://developers.facebook.com/docs/facebook-login/permissions/)

        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                MainActivity.this.progressDialog.dismiss();
                if (user == null) {
                    Log.d("TheSos", "Uh oh. The user cancelled the Facebook login.");
                    Toast.makeText(MainActivity.this, "เกิดข้อผิดผลาดในการขอสิทธิ Facebook", Toast.LENGTH_SHORT).show();
                } else if (user.isNew()) {
                    Log.d("TheSos", "User signed up and logged in through Facebook!");
                    showExtraData();

                    //showRouteActivity();
                    // showExtraData();
                } else {
                        Log.d("TheSos", "User logged in through Facebook!");
                    checkUserType(user.getString("type"));

                }
            }
        });
    }

    private void showExtraData() {
        Intent i = new Intent(this, TelephoneActivity.class);
        startActivity(i);
        finish();
    }

    private void showRouteActivity() {
        Intent i = new Intent(this, RouteActivity.class);
        startActivity(i);
        finish();
    }

    private void showUserDetailsActivity() {
        startActivity(new Intent(this, ReportActivity.class));
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
