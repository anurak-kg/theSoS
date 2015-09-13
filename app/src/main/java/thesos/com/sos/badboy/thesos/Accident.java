package thesos.com.sos.badboy.thesos;

import android.location.Location;

import com.parse.ParseGeoPoint;

/**
 * Created by iMan on 9/12/2557.
 */
public class Accident {
    private String accidentId;
    private String accidentType;
    private ParseGeoPoint location;
    private String rescuerId;
    private String victimId;
    private String accidentDescription;
    private String accidentStatus;


    public String getAccidentId() {
        return accidentId;
    }

    public void setAccidentId(String accidentId) {
        this.accidentId = accidentId;
    }

    public String getAccidentType() {
        return accidentType;
    }

    public void setAccidentType(String accidentType) {
        this.accidentType = accidentType;
    }

    public ParseGeoPoint getLocation() {
        return location;
    }

    public void setLocation(ParseGeoPoint location) {
        this.location = location;
    }

    public String getRescuerId() {
        return rescuerId;
    }

    public void setRescuerId(String rescuerId) {
        this.rescuerId = rescuerId;
    }

    public String getVictimId() {
        return victimId;
    }

    public void setVictimId(String victimId) {
        this.victimId = victimId;
    }

    public String getAccidentDescription() {
        return accidentDescription;
    }

    public void setAccidentDescription(String accidentDescription) {
        this.accidentDescription = accidentDescription;
    }

    public String getAccidentStatus() {
        return accidentStatus;
    }

    public void setAccidentStatus(String accidentStatus) {
        this.accidentStatus = accidentStatus;
    }
}
