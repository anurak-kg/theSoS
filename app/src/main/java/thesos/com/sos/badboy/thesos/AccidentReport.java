package thesos.com.sos.badboy.thesos;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AccidentReport {

    private static final long TIME_PROCESS_SLEEP = 1000;
    private static final long MAX_TIME_WAIT = 20;  //ระยะเวลาที่รอการติดต่อจากเจ้าหน้าที่
    private static final double MAX_NEAR_KILOMETER = 10;
    private static final int LIMIT_RESCUER = 5;
    private static final ParseGeoPoint currentUserLocation = new ParseGeoPoint(7.8481069, 98.329275);
    private Context context;

    private Uri imagesUri;
    private String TAG = "theSos";
    private ParseObject currentUser;
    private TextView status;
    private String objectId = "3r5fNg3CTs"; //objectId ของ อุบัติเหตุที่เกิดตอนนี้
    private List<ParseObject> list;
    public Spanned currentStatus;
    private boolean running;
    private ProgressBar loadingProgressBar;
    private boolean progressRunning;

    public AccidentReport(Context c) {
        this.context = c;
    }

    public void report() {
        try {
            setErrorStatus("มีข้อผิดผลาด");
            Thread.sleep(1000);
            this.setLoadingProgressBar(true);
            // ส่งข้อมูลการเกิดอุบัติเหตุขึ้นสู่ Server PARSE
            // sendAccidentToServer();

            // ค้นห้ากู้ภับที่ใกล้ที่สุด
            this.findNearRescuer();
            // แสดงรายชื่อกู้ภัยที่พบ
            this.showAllLocation();

            // สร้าง Temporary Table เพื่อใช้ติดต่อระหว่าง ผู้ใช้และกู้ภัย
             this.generateTempData();

            // ส่งการแจ้งเตือนสู่กู้ภัย
            this.sendAccidentToRescuer();

            //สิ้นสุด
            if (this.progressRunning) {
                this.setLoadingProgressBar(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendNotification(String tempId, String title, String description, ParseGeoPoint rescuerLocation) throws ParseException {
        JSONObject data = new JSONObject();
        try {
            data.put("title", title);
            data.put("text", description);
            data.put("accident_id", getObjectId());
            data.put("temp_id", tempId);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ParsePush push = new ParsePush();
        push.setChannel("");
        push.setData(data);
        push.send();
    }

    private String updateRescuerStatus(String objectId) {
        this.running = true;
        this.setCurrentStatus("กำลังติดต่อกู้ภัย");
        long start = new Date().getTime();
        long currentTime;

        try {
            //ดาวน์โหลดข้อมูล จาก Parse
            ParseQuery tempData = ParseQuery.getQuery("tempdata");
            tempData.whereEqualTo("objectId", objectId);
            ParseObject object = tempData.getFirst();
            object.getRelation("d");

            ParseUser rescuer = object.getParseUser("rescuer").fetchIfNeeded();
            //ส่ง Notification ไปยังกู้ภัย
            this.sendNotification(this.objectId,object.getObjectId(),"อุบัติเหตุทางเรือ",currentUserLocation);


            // Update สถานะเป็นกำลังส่ง

            // ตรวจสอบสถานะการติดต่อ
            do {
                // คำนวญเวลา เพื่อกำหนดเวลาในการติดต่อ
                currentTime = new Date().getTime();
                long diff = currentTime - start;
                long diffSeconds = diff / 1000 % 60;

                //โหลดข้อมูลจาก Parse Server
                ParseQuery tempQuery = ParseQuery.getQuery("tempdata");
                tempQuery.whereEqualTo("objectId", objectId);
                ParseObject tempObject = tempQuery.getFirst();

                //ดึงสถานะจาก Server
                String rescuerStatus = tempObject.getString("status");

                //อัพเดทสถานะ และเวลา 
                this.setCurrentStatus(Html.fromHtml("กำลังติดต่อ <b>" + rescuer.getString("name") + "</b>  เหลือเวลาอีก " + (MAX_TIME_WAIT - diffSeconds) + " วินาที"));

                Log.d(TAG, tempObject.getObjectId() + " ||  สถานะ -> " + rescuerStatus);

                //เมื่อกู้ภัยตอบรับ
                if (rescuerStatus.equals("accept")) {
                    return "found";
                }
                //เมื่อกู้ภัยปฏิเสธ
                else if (rescuerStatus.equals("reject")) {
                    this.setCurrentStatus("เจ้าหน้าที่ปฏิเสธ! ระบบกำลังติดต่อเจ้าหน้าที่ท่านอื่น");
                    return "not_found";
                }

                //เมื่อเวลาเกินจากที่ต้องใว้ให้ค้นหาคนถัดไป
                if (diffSeconds > MAX_TIME_WAIT) {
                    this.setCurrentStatus("หมดเวลา");
                    return "not_found";
                }

                //หยุดเพื่อหน่วงการประมวณผล
                Thread.sleep(TIME_PROCESS_SLEEP);

            } while (running);

        } catch (ParseException | InterruptedException e) {
            this.setCurrentStatus("เกิดข้อผิดผลาด !!! [0x100001]");
            e.printStackTrace();
        }
        return "not_found";


    }

    private void setTempStatus(String objectId, String status) {
        ParseQuery<ParseObject> tempdata = ParseQuery.getQuery("tempdata");
        tempdata.whereEqualTo("objectId", this.getObjectId());
        try {
            ParseObject temp = tempdata.getFirst();
            temp.put("status", status);
            temp.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void sendAccidentToRescuer() {
        synchronized (this) {

            try {
                ParseQuery<ParseObject> accident = ParseQuery.getQuery("accident");
                accident.whereEqualTo("objectId", this.getObjectId());
                ParseQuery<ParseObject> query = ParseQuery.getQuery("tempdata");
                query.whereMatchesQuery("accident", accident);
                //ค้นหาเฉพาะที่มีสถานะ pending
                query.whereEqualTo("status", "pending");

                List<ParseObject> listTemp;
                listTemp = query.find();

                //ถ้าไม่พบกู้ภัยที่มีสถานะ waiting
                int count = query.count();
                Log.d(TAG, "Count : " + count);
                if (count == 0) {
                    this.setCurrentStatus("ไม่พบกู้ภัย" + query.count());
                    throw new Exception();
                }

                //ส่งการแจ้งเตือนไปยังกู้ภัย
                for (ParseObject temp : listTemp) {
                    Log.d(TAG, "ID = " + temp.getObjectId() + "  |  Status " + temp.getString("status"));

                    //เตียมข้อมูลการส่ง
                    String currentReportStatus = this.updateRescuerStatus(temp.getObjectId());
                    if (currentReportStatus.equals("found")) {
                        this.setCurrentStatus("กรุณารอสักครู่ เจ้าหน้ากำลังเดินไปที่เกิดเหตุ...");
                        break;
                    }
                    this.setCurrentStatus("กำลังค้นหาคนต่อไป");
                    Thread.sleep(100);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }

    public void findNearRescuer() throws Exception {
        try {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
            query.whereWithinKilometers("location", currentUserLocation, MAX_NEAR_KILOMETER);
            query.whereNotEqualTo("objectId", currentUser.getObjectId());
            // เฉพาะเจ้าหน้าที่
            // query.whereEqualTo("type","Rescuer");

            //จำกัดเจ้าหน้าที่ไว้กี่คน
            query.setLimit(LIMIT_RESCUER);
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

    private void saveTempData(ParseObject rescuer) {

        ParseObject object = new ParseObject("tempdata");
        object.put("status", "pending");
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
            this.saveTempData(object);
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

    public Spanned getCurrentStatus() {
        return currentStatus;
    }

    private void setCurrentStatus(String currentStatus) {
        this.currentStatus = Html.fromHtml(currentStatus);
        this.updateStatus();
    }

    private void setErrorStatus(String currentStatus) {
        this.currentStatus = Html.fromHtml("<font color=\"red\"><b>" + currentStatus + "</b></font>");
        this.updateStatus();
    }

    private void setCurrentStatus(Spanned currentStatus) {
        this.currentStatus = currentStatus;
        this.updateStatus();
    }


    private void setCurrentColor(int color) {
        // status.setTextColor(color);
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

    public void setLoadingHandle(ProgressBar loading) {
        this.loadingProgressBar = loading;
    }

    public void setLoadingProgressBar(boolean running) {
        this.progressRunning = running;
        if (running) {
            loadingProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                }
            });
        } else {
            loadingProgressBar.post(new Runnable() {
                @Override
                public void run() {
                    loadingProgressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}
