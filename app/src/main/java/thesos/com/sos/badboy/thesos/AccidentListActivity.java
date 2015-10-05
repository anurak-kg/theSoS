package thesos.com.sos.badboy.thesos;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class AccidentListActivity extends AppCompatActivity {

    ListView listview;
    List<ParseObject> ob;
    ProgressDialog mProgressDialog;
    private ArrayList<Accident> accidentlist;
    private AccidentList adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_list);
        setAdapter();
        new RemoteDataTask().execute();

    }

    private void setAdapter() {
        //AccidentList adapter = new AccidentList()

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accident_list, menu);
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


    // RemoteDataTask AsyncTask
    private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //สร้าง Dialog การโหลดข้อมูล
            mProgressDialog = new ProgressDialog(AccidentListActivity.this);
            mProgressDialog.setTitle("กำลังโหลดข้อมูลอุบบัติเหตุ");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            accidentlist = new ArrayList<Accident>();
            try {

                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("accident");
                ob = query.find();
                for (ParseObject acc : ob) {

                    //โหลดภาพ
                    ParseFile image = (ParseFile) acc.get("photo");

                    Accident accident = new Accident();
                    accident.setAccidentId(acc.getObjectId());
                    accident.setAccidentType(acc.getString("accidentType"));
                    accident.setUri(image.getUrl());

                    accidentlist.add(accident);
                }
            } catch (ParseException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Locate the listview in listview_main.xml
            listview = (ListView) findViewById(R.id.listView);
            // Pass the results into ListViewAdapter.java
            adapter = new AccidentList(AccidentListActivity.this, accidentlist);
            // Binds the Adapter to the ListView
            listview.setAdapter(adapter);
            // Close the progressdialog
            mProgressDialog.dismiss();
        }
    }
}
