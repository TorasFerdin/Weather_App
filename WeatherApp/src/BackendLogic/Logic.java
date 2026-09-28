package BackendLogic;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


/**
 * Retrieve weather data from API - this backend logic will fetch the latest weather
 * data from the external API and return it. The GUI will display this data to the user.
 */
public class Logic {

    // fetch weather data for given location
    public static JSONObject getWeatherData(String locationName){
        // get location coordinates using the geolocation API
        JSONArray locationData = getLocationData(locationName);

        if(locationData == null) return null;

        // extract latitude and longitude data
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // build API request URL with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,weather_code,wind_speed_10m,relative_humidity_2m&timezone=Europe%2FBerlin";

        try{
            // call API and get response
            HttpURLConnection connection = fetchApiResponse(urlString);

            // check for response status
            // 200 - means that the connection was a success
            if(connection.getResponseCode() != 200){
                System.out.println("☹️ Error: could not connect to API ☹️");
                return null;
            }
            else {
                // store resulting json data
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNext()){
                    // read and store into the string uilder
                    resultJson.append(scanner.nextLine());
                }

                // close scanner
                scanner.close();

                // close url connection
                connection.disconnect();

                // parse through our data
                JSONParser parser = new JSONParser();
                JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // retrieve hourly data
                JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

                // we want to get the current hour's data
                // so we need to get the index of our current hour
                JSONArray time = (JSONArray) hourly.get("time");
                int index = findIndexOfCurrentTime(time);

                // get temperature
                JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
                double temperature = (double) temperatureData.get(index);

                // get weather code
                JSONArray weatherCodes = (JSONArray) hourly.get("weather_code");
                String weatherCondition = convertWeatherCode((long) weatherCodes.get(index));

                // get humidity
                JSONArray humidityData = (JSONArray) hourly.get("relative_humidity_2m");
                long humidity = (long) humidityData.get(index);

                // get windspeed
                JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
                double windspeed = (double) windspeedData.get(index);

                // build the weather json data object that we are going to access in our frontend (GUI)
                JSONObject weatherDataReady = new JSONObject();
                weatherDataReady.put("temperature", temperature);
                weatherDataReady.put("weatherCondition", weatherCondition);
                weatherDataReady.put("humidity", humidity);
                weatherDataReady.put("windspeed", windspeed);

                return weatherDataReady;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // retrieves geographic coordinates for given location name
    private static JSONArray getLocationData(String locationName) {
        // replace any whitespaces in location name to '+' to adhere to API's request format (e.g: new+york)
        locationName = locationName.replaceAll(" ", "+");

        // build API url with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                            locationName + "&count=10&language=en&format=json"    ;

        try {
            // call API and get a response
            HttpURLConnection connection = fetchApiResponse(urlString);

            // check response status
            // 200 means successful connection
            if(connection.getResponseCode() != 200){
                System.out.println("☹️ Error: could not connect to API ☹️");
                return null;
            }
            else {
                // store the api results
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());

                // read and store the resulting json data into our string builder
                while (scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }

                // close scanner
                scanner.close();

                // close url connection
                connection.disconnect();

                // parse the JSON string into a JSON obj
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // get the list of location data the API generated from the location name
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // couldn't find location
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try{
            // attempt to create connection
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // set request method to GET
            connection.setRequestMethod("GET");

            // connect to the API
            connection.connect();
            return connection;
        }
        catch (IOException e){
            e.printStackTrace();
        }

        // could not make connection
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        // iterate through the time list and see which one matches our current time
        for (int i = 0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);

            if(time.equalsIgnoreCase(currentTime)){
                // return the index
                return i;
            }
        }

        System.out.println("Matching currentTime in the timeList JSONArray not found");
        return -1;
    }

    private static String getCurrentTime(){
        // get current date and time
        LocalDateTime currentDateTime = LocalDateTime.now(); // 2025-09-26T12:41:38.285616

        // format the date to be like --> 2023-09-02T00:00 <-- this is how is read in the API
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // format and print the current date and time
        String formattedCurrentDateTime = currentDateTime.format(timeFormatter); // 2025-09-26T12:00

        return formattedCurrentDateTime;
    }

    // convert weather code to something more readable (i.e. 0 = clear sky)
    /*   FULL LIST:
       0	Clear sky
       1, 2, 3	Mainly clear, partly cloudy, and overcast
       45, 48	Fog and depositing rime fog
       51, 53, 55	Drizzle: Light, moderate, and dense intensity
       56, 57	Freezing Drizzle: Light and dense intensity
       61, 63, 65	Rain: Slight, moderate and heavy intensity
       66, 67	Freezing Rain: Light and heavy intensity
       71, 73, 75	Snow fall: Slight, moderate, and heavy intensity
       77	Snow grains
       80, 81, 82	Rain showers: Slight, moderate, and violent
       85, 86	Snow showers slight and heavy
       95 *	Thunderstorm: Slight or moderate
       96, 99 *	Thunderstorm with slight and heavy hail
    */
    private static String convertWeatherCode(long weatherCode){
        String weatherCondition = "";

        if(weatherCode == 0L) weatherCondition = "Clear";
        else if(weatherCode > 0 && weatherCode <= 3L)  weatherCondition = "Cloudy";
        else if((weatherCode >= 51L && weatherCode <= 67L)      ||
                (weatherCode >= 80L && weatherCode <= 99L)      ){
            weatherCondition = "Rain";
        }
        else if(weatherCode >= 71L && weatherCode <= 77L) weatherCondition = "Snow";

        return weatherCondition;
    }
}
