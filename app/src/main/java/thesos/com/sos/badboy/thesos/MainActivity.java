package thesos.com.sos.badboy.thesos;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class MainActivity extends ActionBarActivity {

    ProgressDialog progressDialog;
    private ImageButton loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getHashKey();
        bindWidget();

        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
            Intent i = new Intent(this, RescuerActivity.class);
            startActivity(i);
        }
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //telephoneAlert();
                onLoginButtonClicked();
            }
        });
    }

    private void getHashKey() {
        try {
            PackageInfo info =     getPackageManager().getPackageInfo("thesos.com.sos.badboy.thesos",     PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sign= Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("MY KEY HASH:", sign);
                Toast.makeText(getApplicationContext(), sign, Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
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
                } else if (user.isNew()) {
                    Log.d("TheSos", "User signed up and logged in through Facebook!");
                    showRouteActivity();
                   // showExtraData();
                } else {
                    Log.d("TheSos", "User logged in through Faceb+ook!");
                    showRouteActivity();

                    //showUserDetailsActivity();
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
