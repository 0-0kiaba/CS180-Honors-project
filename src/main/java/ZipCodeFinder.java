import java.io.*;
import java.net.*;

import com.fasterxml.jackson.databind.*;

/**
 * A Class designed to find an inputted zip code, and return the associated longitude and latitude of the given county
 * Uses the zipcodestack api, which is free but has a limit of 10,000 calls a month, which should not be an issue, but
 * is worth mentioning.
 * @author Collin James
 * @version 11/28/2024
 */
public class ZipCodeFinder {
    public final static String zipCodeAPI = "https://api.zipcodestack.com/v1/";
    //API KEY GOES HERE v
    public final static String zipCodeKey = "";
    //API KEY GOES HERE ^
    String charset = "UTF-8";
    private File outputFile;

    public ZipCodeFinder(String outputFile) {
        this.outputFile = new File(outputFile);
    }

    /**
     * A function that calls the zipStackApi and prints the associated information into an output file provided
     * in the class's constructor.
     * @param zipCode a String of a zip code
     * @return true if the function printed to the output file successfully and false otherwise
     */
    public boolean searchForInfo(String zipCode) {
        boolean zipPrinted = false;
        PrintWriter pw = null;
        BufferedReader bfr = null;
        try {
            String searchFormat = String.format("search?codes=%s&country=us&apikey=%s", zipCode, zipCodeKey);
            String urlFormat = zipCodeAPI + searchFormat;
            URL url = new URL(urlFormat);

            bfr = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
            pw = new PrintWriter(new FileWriter(outputFile));
            String line = bfr.readLine();
            while (line != null) {
                pw.println(line);
                line = bfr.readLine();
            }
            pw.flush();
            zipPrinted = true;


        } catch (MalformedURLException e) {
            System.out.println("Malformed URL issue");
        } catch (IOException e) {
            System.out.println("I/O error");
        } finally {
            try {
                pw.close();
                bfr.close();
            } catch (Exception e) {
                System.out.println("issue closing streams");
            }
        }
        return zipPrinted;
    }

    /**
     * A function to check whether a given zip code entry is valid or not.
     * @return true if the input was valid, and false otherwise
     */
    public boolean checkValidInput() {

        try (BufferedReader bfr = new BufferedReader(new FileReader(outputFile))) {
            ObjectMapper om = new ObjectMapper();
            JsonNode validCheck = om.readTree(outputFile).get("results");
            return validCheck.elements().hasNext();

        } catch (IOException e) {
            System.out.println("I/O error");
        }
        return false;
    }

    /**
     * Returns the longitude and latitude of a zip code assuming a valid API entry exists in the output file
     * @return a double array consisting of [latitude, longitude]
     * @throws IOException
     */
    public double[] getCoordinatesFromZip() throws IOException {
        ObjectMapper om = new ObjectMapper();
        JsonNode locationFinder = om.readTree(outputFile).get("results").elements().next();
        locationFinder = locationFinder.elements().next();
        double[] coordinates = {locationFinder.get("latitude").asDouble(), locationFinder.get("longitude").asDouble()};
        return coordinates;
    }


}
