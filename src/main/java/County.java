import java.awt.*;

/**
 * A class designed to store information of a State's county, and give a GUI the ability to draw them out,
 * fill them with alert colors, and darken and lighten them depending on if an associated alert button has been
 * selected
 *
 * @author Collin James
 * @version 11/28/2024
 */
public class County extends SevereCountyWeather {
    private int[] xCoordinates;
    private int[] yCoordinates;
    private String countyIdentifier;
    private String countyName;
    private Color color;
    private boolean selected;


    public County(String countyIdentifier, String countyName, int[] xCoordinates, int[] yCoordinates) {
        if (colorInformation.isEmpty()) {
            setColorInformation();
        }
        this.countyIdentifier = countyIdentifier;
        this.countyName = countyName;
        this.xCoordinates = xCoordinates;
        this.yCoordinates = yCoordinates;
        this.color = Color.WHITE;
        selected = false;
    }

    /**
     * sets the selected instance variable
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * A getter method for the county's X coordinate information
     *
     * @return County's x coordinates
     */
    public int[] getXCoordinates() {
        return xCoordinates;
    }

    /**
     * A getter method for the county's Y coordinate information
     *
     * @return County's y coordinates
     */
    public int[] getYCoordinates() {
        return yCoordinates;
    }

    /**
     * A getter method for the county's zone identifier, a parameter that the API uses to highlight affected counties
     *
     * @return County's zone identifier
     */
    public String getCountyIdentifier() {
        return countyIdentifier;
    }

    /**
     * A function to return the county's color, and a darker version if it is currently being selected.
     *
     * @return The county's color.
     */
    public Color getColor() {
        if (selected) {
            return color.darker();
        } else {
            return color;
        }
    }

    /**
     * @return the county's name
     */
    public String getCountyName() {
        return countyName;
    }

    /**
     * A function used to set a county's color based on a string value using the county hashmap.
     * if the key is not present, sets the color to gray.
     * @param colorAlert
     */
    public void setCountyColor(String colorAlert) {
        if(colorInformation.containsKey(colorAlert)) {
            this.color = colorInformation.get(colorAlert);
        } else {
            this.color = Color.GRAY;
        }
    }


}
