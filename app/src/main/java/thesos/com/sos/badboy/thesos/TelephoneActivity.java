package thesos.com.sos.badboy.thesos;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class TelephoneActivity extends ActionBarActivity {
    EditText phoneNoEditText;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telephone);
        bindWidget();
        Button nextBtn = (Button) findViewById(R.id.nextTelephoneBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNoEditText.getText().length() < 10) {
                    Toast.makeText(getApplicationContext(),
                            "รูปแบบเบอร์โทศัพท์ไม่ถูกต้อง", Toast.LENGTH_LONG).show();
                } else {
                    phoneNumber = phoneNoEditText.getText().toString();
                    goToProfile();
                }

            }
        });
    }

    private void bindWidget() {
        phoneNoEditText = (EditText) findViewById(R.id.phoneNumber);
    }

    private void goToProfile() {
        Intent i = new Intent(this, ReportActivity.class);
        i.putExtra("Phone", phoneNumber);
        startActivity(i);
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
