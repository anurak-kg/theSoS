/**
 * Created by Anurak on 16/10/58.
 */
package thesos.com.sos.badboy.thesos;
import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GeocodeAddress {
    OkHttpClient client = new OkHttpClient();
    JSONObject jsonObject ;

    public GeocodeAddress(double latitude, double longitude) {
        String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&sensor=false&language=th";
        Log.d(AccidentListActivity.TAG,url);
        try {
            String data = run(url);
            jsonObject = new JSONObject(data);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


    }
    public  String getFomatLineNumber() throws JSONException {
        JSONArray jsonArray = jsonObject.getJSONArray("results");
        return  jsonArray.getJSONObject(0).getString("formatted_address");

    }

    public JSONObject getJsonObject() throws JSONException {

            return jsonObject.getJSONObject("results");

    }

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}

