import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.io.*;
import java.util.ArrayList;
import java.net.*;
import java.util.Iterator;

/**
 * A class designed to create a GUI for a state and then display it for a user, showing different colors based on the
 * alerts present in the different counties, if no alerts are present the image will display as all white
 *
 * @author Collin James
 * @version 11/28/2024
 */
public class StateGUI extends JComponent implements Runnable {
    private String stateAbbreviation;
    private File alertOutput;
    private Graphics2D graphics2d;
    private Image image;
    private File stateFile;
    private static HashMap<String, County> counties;
    private ArrayList<AlertButton> alertButtons;

    public StateGUI(String stateAbbreviation, String alertOutput, String stateFile) {
        this.stateAbbreviation = stateAbbreviation;
        this.alertOutput = new File(alertOutput);
        //comment out the line below to try out test files
        getActiveAlerts();

        this.stateFile = new File(stateFile);
        counties = new HashMap<>();
        alertButtons = new ArrayList<>();

        createCounties(4);
        try {
            setCountyColors();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                for (String key : counties.keySet()) {
                    drawCounty(counties.get(key), counties.get(key).getColor());
                }
            }
        });
    }

    /**
     * A function used to get the different alerts present in the given state.
     * Calls the API and prints all outputs to the alertOutput File
     */
    public void getActiveAlerts() {
        String weatherServiceURL = "https://api.weather.gov/alerts/active/area/" + stateAbbreviation;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            URL alertAPI = new URL(weatherServiceURL);
            in = new BufferedReader(new InputStreamReader(alertAPI.openConnection().getInputStream()));
            out = new PrintWriter(new FileWriter(alertOutput));
            String line = in.readLine();
            while (line != null) {
                out.println(line);
                line = in.readLine();
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A getter method for the alert buttons that are created by the setCountyColors Method
     *
     * @return alertButtons arrayList
     */
    public ArrayList<AlertButton> getAlertButtons() {
        return alertButtons;
    }

    /**
     * A function that fills out a hashmap of Counties based on a provided text file.
     * NOTE: The text file provided is in the format of "state".txt, and requires a user to enter all counties and
     * their associated grid points, which takes a really long time. Because of this, there is currently only one file.
     * However, the implementation exists to add additional files as the need arises.
     *
     * @param reduceSizeBy- a parameter that scales down the state image to be easily viewed on a screen
     */
    public void createCounties(int reduceSizeBy) {
        try (BufferedReader bfr = new BufferedReader(new FileReader(stateFile))) {
            String line = bfr.readLine();
            while (line != null) {
                String[] information = line.split(":");
                String countyName = information[0];
                String countyIdentifier = information[1];
                String[] xCoordinates = information[2].split(",");
                String[] yCoordinates = information[3].split(",");
                int[] xCoords = new int[xCoordinates.length];
                int[] yCoords = new int[xCoordinates.length];
                for (int i = 0; i < xCoordinates.length; i++) {
                    xCoords[i] = Integer.parseInt(xCoordinates[i]) / reduceSizeBy;
                    yCoords[i] = Integer.parseInt(yCoordinates[i]) / reduceSizeBy;
                }
                County newCounty = new County(countyIdentifier, countyName, xCoords, yCoords);
                counties.put(countyIdentifier, newCounty);
                line = bfr.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A function that takes the hashmap of counties, uses the output file and maps the JSON tree, and then matches
     * the counties with any associated special weather alerts. It also creates 1 button per alert, allowing a user to
     * access the specific information affecting a region.
     *
     * @throws IOException
     */
    public void setCountyColors() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode reader = mapper.readTree(alertOutput).get("features");
        Iterator<JsonNode> alerts = reader.elements();
        ArrayList<String> alertCounties = new ArrayList<>();
        while (alerts.hasNext()) {
            JsonNode alert = alerts.next();
            alert = alert.get("properties");
            String alertType = alert.get("event").asText();
            Color alertColor = null;
            Iterator<JsonNode> identifierInfo = alert.get("geocode").get("UGC").elements();
            String alertRawInfo = "";
            while (identifierInfo.hasNext()) {
                String check = identifierInfo.next().asText();
                if (check.contains(stateAbbreviation)) {
                    alertRawInfo = alertRawInfo + check + ",";
                }
            }
            //trim data so that it is just the identifier (some counties are broken into smaller subsections
            //which can make information harder to parse)
            String[] convertedInformation = alertRawInfo.split(",");
            for (int i = 0; i < convertedInformation.length; i++) {
                if (convertedInformation[i].charAt(3) != '0') {
                    convertedInformation[i] = "INZ0" + convertedInformation[i].substring(4);
                }
            }
            ArrayList<County> affectedCounties = new ArrayList<>();
            for (int i = 0; i < convertedInformation.length; i++) {
                if (!alertCounties.contains(convertedInformation[i])) {
                    alertCounties.add(convertedInformation[i]);
                    if (counties.keySet().contains(convertedInformation[i])) {
                        County alertCounty = counties.get(convertedInformation[i]);
                        affectedCounties.add(alertCounty);
                        alertCounty.setCountyColor(alertType);
                        if (alertColor == null) {
                            alertColor = alertCounty.getColor();
                        }
                    }
                }
            }
            String hazardInfo = alert.get("description").asText();
            AlertButton ab = new AlertButton(alertType, affectedCounties, alertColor, hazardInfo);
            mouseEvents(ab);
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(null, ab.getAlert(), "Hazard information",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });

            alertButtons.add(ab);
            //System.out.println(ab.getCounties());
            alertCounties.clear();
            alertColor = null;
        }
    }

    /**
     * A function designed to give each button the ability to darken the counties that are affected by the
     * hazard specified, as well as lighten them when a user leaves the button, allowing the user to easily scan through
     * the affected regions.
     *
     * @param ab- an alert button that has the new events made for it.
     */
    public void mouseEvents(AlertButton ab) {
        ab.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                for (County c : ab.getCounties()) {
                    County county = counties.get(c.getCountyIdentifier());
                    county.setSelected(true);
                    drawCounty(county, county.getColor());
                    repaint();

                }
            }

            public void mouseExited(MouseEvent e) {
                for (County c : ab.getCounties()) {

                    County county = counties.get(c.getCountyIdentifier());
                    county.setSelected(false);
                    drawCounty(county, county.getColor());
                    repaint();


                }
            }
        });
    }


    public static void main(String[] args) {

        StateGUI test = new StateGUI("IN", "activeAlerts.json", "Indiana.txt");

        SwingUtilities.invokeLater(test);
//        StateGUI test = new StateGUI(null, "IN", "StateAlert.json");
//        StateGUI.invokeLater();
    }

    /**
     * A function that draws a county out based on the x and y coordinate information that is provided to it
     *
     * @param county-    the county being drawn
     * @param fillColor- the color the given county should appear
     */
    public void drawCounty(County county, Color fillColor) {
        graphics2d.setColor(fillColor);
        graphics2d.setStroke(new BasicStroke(1));
        int polygonLength = county.getYCoordinates().length;
        graphics2d.fillPolygon(county.getXCoordinates(), county.getYCoordinates(), polygonLength);
        graphics2d.setStroke(new BasicStroke(2f));
        graphics2d.setColor(Color.BLACK);
        graphics2d.drawPolygon(county.getXCoordinates(), county.getYCoordinates(), polygonLength);
    }

    /**
     * A run method to allow the GUI to run on an EDT instead of on main.
     */
    public void run() {

        JFrame frame = new JFrame("Indiana");
        Container content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        StateGUI check = new StateGUI("IN", "activeAlerts.json", "Indiana.txt");
        check.getActiveAlerts();
        content.add(check, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 0));
        System.out.println(check.getAlertButtons().size());
        for (AlertButton ab : check.getAlertButtons()) {
            buttonPanel.add(ab);
        }
        content.add(buttonPanel, BorderLayout.SOUTH);

        JPanel zipSearchPanel = ZipCodeGUI.createSearchPanel();
        content.add(zipSearchPanel, BorderLayout.NORTH);


        frame.setSize(400, 675);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

    }

    /**
     * A paint component override
     *
     * @param g the <code>Graphics</code> object to protect
     */
    protected void paintComponent(Graphics g) {
        if (counties.isEmpty()) {
            createCounties(4);
            try {

                setCountyColors();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (image == null) {
            image = createImage(getSize().width, getSize().height);

            graphics2d = (Graphics2D) image.getGraphics();

            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphics2d.setPaint(Color.white);
            graphics2d.fillRect(0, 0, getSize().width, getSize().height);
            graphics2d.setPaint(Color.black);
            repaint();
        }
        g.drawImage(image, 0, 0, null);
        repaint();
    }
}
