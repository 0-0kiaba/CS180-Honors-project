import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * A JButton subclass designed to store different counties, used in StateGUI
 *
 * @author Collin James
 * @version 11/28/2024
 */
public class AlertButton extends JButton {
    private ArrayList<County> counties;
    private Color alertColor;
    private String hazardInfo;

    public AlertButton(String text, ArrayList<County> counties, Color alertColor, String hazardInfo) {
        super(text);
        this.counties = counties;
        this.alertColor = alertColor;
        this.hazardInfo = hazardInfo;
    }

    /**
     * A method to return the counties stored in this class
     * @return counties instance variable
     */
    public ArrayList<County> getCounties() {
        return counties;
    }

    /**
     * A method to return the various county names stored in the class, and display them in a way that is human readable
     * @return the county names
     */
    public String getCountyNames() {
        String retValue = "Affected Counties: ";
        int count = 19;
        for (County c : counties) {
            retValue += c.getCountyName() + ", ";
            count+= c.getCountyName().length();
            if (count > 50) {
                retValue += "\n                 ";
                count = 17;
            }
        }
        return retValue.substring(0, retValue.length() - 2);
    }

    /**
     * A method to return hazard information stored in the variable
     * @return hazardInfo
     */
    public String getHazardInformation() {
        return "Hazard Information: \n" + hazardInfo;

    }

    /**
     * A format method to return both the counties affected by a hazard and what the hazard it
     * @return the alert information.
     */
    public String getAlert() {
        String retValue = getCountyNames() + "\n" + getHazardInformation();
        return retValue;
    }


}
