import java.util.HashMap;
import java.awt.Color;

/**
 * An abstract class designed to store a hashmap used by all counties.
 * IMPORTANT NOTE: Currently this is an incomplete list, since there are ~130 potential hazards that can appear
 * on the NWS, because of this, I selected 18 of the alerts that were commonly displayed, and added associated colors
 * with them.
 */
public abstract class SevereCountyWeather {
    public static HashMap<String, Color> colorInformation = new HashMap<String, Color>();

    public void setColorInformation() {
        colorInformation.put("No Alert", new Color(255, 255, 255));
        colorInformation.put("Law Enforcement Warning", new Color(128, 128, 128));
        colorInformation.put("Blizzard Warning", new Color(255, 0, 0));
        colorInformation.put("Heavy Freezing Spray Warning", new Color(0, 255, 255));
        colorInformation.put("Winter Storm Warning", new Color(0, 204, 204));
        colorInformation.put("High Wind Warning", new Color(255, 128, 0));
        colorInformation.put("Storm Warning", new Color(149, 0, 179));
        colorInformation.put("Avalanche Warning", new Color(0, 115, 230));
        colorInformation.put("Flood Warning", new Color(0, 230, 38));
        colorInformation.put("High Surf Warning", new Color(0, 102, 34));
        colorInformation.put("Gale Warning", new Color(255, 204, 230));
        colorInformation.put("Winter Weather Advisory", new Color(147, 231, 251));
        colorInformation.put("Red Flag Warning", new Color(255, 77, 106));
        colorInformation.put("Winter Storm Watch", new Color(0, 204, 204, 128));
        colorInformation.put("Gale Watch", new Color(255, 204, 230, 128));
        colorInformation.put("Flood Watch", new Color(0, 230, 38, 128));
        colorInformation.put("Extreme Fire Danger", new Color(153, 0, 0));
        colorInformation.put("Special Weather Statement", new Color(253, 217, 181));
        colorInformation.put("Freeze Watch", new Color(176, 224, 230));


    }
}



