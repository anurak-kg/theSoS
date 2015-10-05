package thesos.com.sos.badboy.thesos;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Anurak on 05/10/58.
 */
public class AccidentReport {

    private Uri imagesUri;
    private String TAG = "theSos";

    private Object currentUser;
    private String objectId;


    String currentStatus;

    public void report() {
        sendAccidentToServer();
    }

    public void findNearRecuser() {

    }

    private void sendAccidentToServer() {
        try {
            Log.d("theSos", "Start Send Accdent to Parse");

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

    public void setImagesUri(Uri imagesUri) {
        this.imagesUri = imagesUri;
    }

    public Object getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Object currentUser) {
        this.currentUser = currentUser;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }


}
