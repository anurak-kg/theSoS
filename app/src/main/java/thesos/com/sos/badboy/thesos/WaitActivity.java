package thesos.com.sos.badboy.thesos;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class WaitActivity extends ActionBarActivity {
    private Accident accident;
    private Uri imagesUri;
    private ParseUser currentUser;
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
       Log.d("theSos",accident.getAccidentType());
        setContentView(R.layout.activity_wait);
    }

    private void sendAccident() {

    }

    private void fireRescuer() {

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

}
