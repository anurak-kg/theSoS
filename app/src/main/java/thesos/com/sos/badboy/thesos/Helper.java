package thesos.com.sos.badboy.thesos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anurak on 17/10/58.
 */

public class Helper {
    public static String getFacebookProfileUrl(String facebookId){
        return "https://graph.facebook.com/"+facebookId+"/picture?type=large";
    }

}

