package thesos.com.sos.badboy.thesos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andreabaccega.widget.FormEditText;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;


public class TelephoneActivity extends AppCompatActivity {
    EditText phoneNoEditText;
    String phoneNumber;
    private android.widget.TextView textView2;
    private FormEditText addnametextedit;
    private FormEditText addcardidtextedit;
    private FormEditText addtelephonetextedit;
    private Button nextTelephoneBtn;
    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telephone);

        this.bindWidget();
        this.initParseUser();
        nextTelephoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickNext(v);

            }
        });
    }

    void initParseUser() {
        currentUser = ParseUser.getCurrentUser();
        if (!currentUser.isAuthenticated()) {
            Intent view = new Intent(this, MainActivity.class);
            startActivity(view);
        } else {
            Log.d(TheSosApplication.TAG, "currentUser = " + currentUser.getString("card_id"));
        }
    }

    private void bindWidget() {
        this.nextTelephoneBtn = (Button) findViewById(R.id.nextTelephoneBtn);
        this.addtelephonetextedit = (FormEditText) findViewById(R.id.add_telephone_text_edit);
        this.addcardidtextedit = (FormEditText) findViewById(R.id.add_card_id_text_edit);
        this.addnametextedit = (FormEditText) findViewById(R.id.add_name_text_edit);
        this.textView2 = (TextView) findViewById(R.id.textView2);
    }

    private void goToProfile() {
        Intent i = new Intent(this, ReportActivity.class);

        i.putExtra("Phone", phoneNumber);
        startActivity(i);
    }

    public void onClickNext(View v) {
        FormEditText[] allFields = {addcardidtextedit, addnametextedit, addtelephonetextedit};


        boolean allValid = true;
        for (FormEditText field : allFields) {
            allValid = field.testValidity() && allValid;
        }
        if(addcardidtextedit.getText().length() != 13){
            allValid = false;
            Toast.makeText(TelephoneActivity.this, "รูปแบบรหัสประชาชนไม่ถุกต้อง", Toast.LENGTH_SHORT).show();
        }
        if (addnametextedit.getText().length() == 0){
            allValid = false;
            Toast.makeText(TelephoneActivity.this, "กรุณาใส่ชื่อครับ", Toast.LENGTH_SHORT).show();

        }
        if (addtelephonetextedit.getText().length() != 10){
            allValid = false;
            Toast.makeText(TelephoneActivity.this, "เบอร์โทรไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
        }

        if (allValid) {
            Log.d(TheSosApplication.TAG, "pass");
            updateProfile();

        } else {
            Log.d(TheSosApplication.TAG, "failed" + currentUser.toString());
        }
    }

    private void updateProfile() {
        currentUser.put("telephone", addtelephonetextedit.getText().toString());
        currentUser.put("card_id", addcardidtextedit.getText().toString());
        currentUser.put("name", addnametextedit.getText().toString());
        currentUser.put("type", "User");
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d(TheSosApplication.TAG, "ParseUser Saved !!");
                Intent intent = new Intent(TelephoneActivity.this, ReportActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_telephone, menu);
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
