package thesos.com.sos.badboy.thesos;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class RouteActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        Button reportBtn = (Button) findViewById(R.id.redirectReportBtn);
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReportActivity();
            }
        });
        Button rescuerBtn = (Button) findViewById(R.id.redirectRescuerBtn);
        rescuerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRescuerData();
            }
        });


    }
    private void showReportActivity() {
        Intent i = new Intent(this, ReportActivity.class);
        startActivity(i);
        finish();
    }
    private void showRescuerData() {
        Intent i = new Intent(this, RescuerActivity.class);
        startActivity(i);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route, menu);
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
