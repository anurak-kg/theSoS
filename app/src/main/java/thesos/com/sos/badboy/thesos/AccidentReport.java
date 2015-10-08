package thesos.com.sos.badboy.thesos;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class AccidentReport {

    private Uri imagesUri;
    private String TAG = "theSos";
    private ParseObject currentUser;
    private TextView status;

    private String objectId = "3r5fNg3CTs";
    private List<ParseObject> list;
    private static final ParseGeoPoint currentUserLocation = new ParseGeoPoint(7.8481069, 98.329275);
    private static final double MAX_NEAR_KILOMATE = 10;
    private static final int LIMIT_RESCURER = 5;

    String currentStatus;


    public void report() {
        try {
            //sendAccidentToServer();
            this.findNearRescuer();
            this.showAllLocation();
            //this.generateTempData();
            sendAccidentToRescuer();
        } catch (Exception e) {
            e.printStackTrace();
            // this.setCurrentStatus("Error !!!");
            // this.updateStatus();
        }

    }
    public void sendAccidentToRescuer(){
        synchronized (this){
            do {

                try {
                    ParseQuery<ParseObject> accident = ParseQuery.getQuery("accident");
                    accident.whereEqualTo("objectId", this.getObjectId());


                    ParseQuery<ParseObject> query = ParseQuery.getQuery("tempdata");
                    query.whereMatchesQuery("accident", accident);
                    List<ParseObject> listTemp = null;
                    listTemp = query.find();

                    for (ParseObject temp:listTemp) {
                        Log.d(TAG,"ID = " + temp.getObjectId() + "Status " + temp.getString("status"));

                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }while (false);
        }

    }

    public void findNearRescuer() throws Exception {
        try {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
            query.whereWithinKilometers("location", currentUserLocation, MAX_NEAR_KILOMATE);
            query.whereNotEqualTo("objectId", currentUser.getObjectId());
            query.setLimit(LIMIT_RESCURER);
            list = query.find();
            Log.d(TAG, "พบกู้ภัยทั้งสิ้น" + list.size() + " คน.");

            if (list.size() == 0) {
                this.setCurrentStatus("ไม่พบกู้ภัย");
                throw new Exception();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void saveTempData(ParseObject rescuer, Integer round) {

        ParseObject object = new ParseObject("tempdata");
        if (round == 0) {
            object.put("status", "waiting");
        } else {
            object.put("status", "pending");
        }
        ParseObject res = ParseObject.createWithoutData("_User", rescuer.getObjectId());
        object.put("rescuer", res);
        object.put("location", rescuer.getParseGeoPoint("location"));
        object.put("accident", ParseObject.createWithoutData("accident", getObjectId()));

        try {
            object.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void generateTempData() {
        int index = 0;
        for (ParseObject object : list) {
            this.saveTempData(object, index++);
        }

    }

    private void showAllLocation() {

        for (ParseObject object : list) {
            ParseGeoPoint location = object.getParseGeoPoint("location");
            Log.d(TAG, object.getString("name") + "อยู่ห่างจากผู้ใช้ " + location.distanceInKilometersTo(currentUserLocation) + " กม.");
        }
    }

    private void sendAccidentToServer() {
        try {
            Log.d("theSos", "Start Send Accident to Parse");

            ParseObject pr = new ParseObject("accident");
            pr.put("accidentType", "อุบัติเหตุทางรถยนต์");
            //pr.put("location", new ParseGeoPoint(accident.getLatitude(), accident.getLongitude()));
            pr.put("accidentDescription", "Bla Bla Bla");
            if (imagesUri != null) {
                File file = new File(imagesUri.getPath());
                if (file.exists()) {
                    byte[] videoBytes = convertImageToByte(imagesUri);
                    String fileName = UUID.randomUUID().toString() + ".jpg";
                    Log.d("theSos", "ParseFile Cloud : " + fileName);
                    ParseFile photo = new ParseFile(fileName, videoBytes);
                    photo.save();
                    pr.put("photo", photo);
                }

            }
            pr.put("victimId", currentUser);
            pr.put("status", "waiting");
            pr.saveInBackground();
            pr.save();
            objectId = pr.getObjectId();
            Log.d(TAG, "The object id is: " + pr.getObjectId());
            Log.d(TAG, "Send Object to Parse Server");
        } catch (IOException e) {
            Log.d("theSos", "Error Save (0x00102)");
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private byte[] convertImageToByte(Uri uri) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(new File(uri.getPath()));
        byte[] buf = new byte[1024];
        int n;
        while (-1 != (n = fis.read(buf))) {
            baos.write(buf, 0, n);
        }
        byte[] videoBytes = baos.toByteArray();
        Log.d("theSos", "convert video to byte");
        return videoBytes;
    }

    public void updateStatus() {
        status.post(new Runnable() {
            @Override
            public void run() {
                status.setText(currentStatus);
            }
        });
    }

    public void setImagesUri(Uri imagesUri) {
        this.imagesUri = imagesUri;
    }

    public Object getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(ParseUser currentUser) {
        this.currentUser = currentUser;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setTextUI(TextView textUI) {
        this.status = textUI;
    }
}
