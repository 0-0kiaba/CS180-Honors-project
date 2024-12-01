import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

import com.fasterxml.jackson.databind.*;

/**
 * A class designed to display a GUI that gets a user inputted zip code and returns a message that tells a user
 * if there is severe weather in the entered area.
 *
 * @author Collin James
 * @version 11/28/2024
 */
public class ZipCodeGUI {
    public ZipCodeGUI() {
        JFrame jf = new JFrame("ZipCode finder");
        jf.setSize(640, 480);
        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jf.setVisible(true);

        Panel inputPanel = new Panel();
        GridLayout gridLayout = new GridLayout(3, 0);
        inputPanel.setLayout(gridLayout);
        jf.add(inputPanel);
        JLabel inputLabel = new JLabel();
        inputPanel.add(inputLabel);
        JTextField zipEntry = new JTextField();
        inputPanel.add(zipEntry);

        inputPanel.add(new JButton("Search"));
        jf.pack();
    }

    /**
     * A funtion that creates a JPanel of the GUI and adds functionality to the button, allowing the user to search
     * for severe weather alerts by entering a zip code.
     * @return JPanel that has a label, an area to enter a zip code, and a search button
     */
    public static JPanel createSearchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 0));
        JLabel inputLabel = new JLabel("Find out if there are any weather alerts in your county!");
        panel.add(inputLabel);
        JTextField zipEntry = new JTextField();
        panel.add(zipEntry);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int zipCode = Integer.parseInt(zipEntry.getText());
                    ZipCodeFinder zipFinder = new ZipCodeFinder("zipcode.json");
                    if (zipFinder.searchForInfo(zipEntry.getText()) && zipFinder.checkValidInput()) {
                        double[] zipCodeLocation = zipFinder.getCoordinatesFromZip();
                        File zipHazardInformation = new File("zipHazardInformation.json");
                        String countyInfo = pointReport(zipCodeLocation, zipHazardInformation);
                        System.out.println(countyInfo);
                        if (countyInfo != null) {
                            File zipOutputAlerts = new File("zipOutputAlerts.json");
                            displayZipResults(countyInfo, zipOutputAlerts);
                        }
                    }

                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid zip code!");
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(null, "Something went wrong when looking up " +
                            "your zipCode!");
                }
            }
        });
        panel.add(searchButton);
        return panel;
    }

    /**
     * A function which takes latitude and longitude of a county and finds the corresponding zoneID that is present
     * in the NWS API.
     * @param coordinates the latitude and longitude of a county
     * @param outputPath the file which information is printed to
     * @return null if nothing is found, and the zone ID of the county if the county is found
     * @throws IOException
     */
    private static String pointReport(double[] coordinates, File outputPath) throws IOException {
        String returnVal = null;
        URL pointLookUp = new URL(String.format("https://api.weather.gov/points/%.4f,%.4f",
                coordinates[0], coordinates[1]));
        BufferedReader bfr = null;
        PrintWriter pw = null;
        try {
            bfr = new BufferedReader(new InputStreamReader(pointLookUp.openConnection().getInputStream()));
            pw = new PrintWriter(new FileWriter(outputPath));
            String line = bfr.readLine();
            while (line != null) {
                pw.println(line);
                line = bfr.readLine();
            }
            pw.flush();
            ObjectMapper om = new ObjectMapper();
            JsonNode information = om.readTree(outputPath);
            String zoneID = information.get("properties").get("forecastZone").asText();
            zoneID = zoneID.substring(zoneID.lastIndexOf('/') + 1);
            returnVal = zoneID;
        } finally {
            bfr.close();
            pw.close();
        }


        return returnVal;
    }

    /**
     * A function that returns any information of hazards in an area based on a NWS zone identifier in the form of a
     * Message Dialog. Also displays an all clear message if no alerts are present of a given zone.
     * @param countyInfo the zone identifier of a county
     * @param outputPath the file that any hazard information will be printed to
     * @throws IOException
     */
    private static void displayZipResults(String countyInfo, File outputPath) throws IOException {
        URL countyAlerts = new URL("https://api.weather.gov/alerts/active/zone/" + countyInfo);
        BufferedReader bfr = null;
        PrintWriter pw = null;
        try {
            bfr = new BufferedReader(new InputStreamReader(countyAlerts.openConnection().getInputStream()));
            pw = new PrintWriter(new FileWriter(outputPath));
            String line = bfr.readLine();
            while (line != null) {
                pw.println(line);
                line = bfr.readLine();
            }
            pw.flush();
            ObjectMapper om = new ObjectMapper();
            JsonNode information = om.readTree(outputPath);
            information = information.get("features");
            if (information.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no weather alerts in your county!");
            } else {
                information = information.elements().next().get("properties");
                String alertType = information.get("event").asText();
                String alertInformation = information.get("description").asText();
                JOptionPane.showMessageDialog(null, "Weather Alert Found: " + alertType +
                        "\nInformation: \n" + alertInformation);
            }
        } finally {
            bfr.close();
            pw.close();
        }
    }

}
