package Swing;
import BackendLogic.Logic;
import org.json.simple.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


// this class is the frontend
public class GUI extends JFrame {
    private JSONObject weatherData;

    // CONSTRUCTOR
    public GUI(){
        // setup the gui and add a title
        super("Weather App");

        // configure the gui to end the program's process once it has been closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // set the size of the gui (in pixels)
        setSize(450, 650);

        // load the gui at the center of the screen
        setLocationRelativeTo(null);

        // make our layout manager null to manually position our components within the gui
        setLayout(null);

        // prevent any resize of the gui
        setResizable(false);

        addGUIComponents();
    }

    /**
     * To debug the components, and see how big they are, use these 2 lines
     * componentName.setBackground(Color.RED);
     * componentName.setOpaque(true);
     */
    private void addGUIComponents() {
        // warning text component
        JLabel warningText = new JLabel("Couldn't find location");
        warningText.setForeground(Color.RED);
        warningText.setBounds(46, 60, 450, 38);
        warningText.setFont(new Font("Dialog", Font.PLAIN, 30));
        warningText.setVisible(false);
        this.add(warningText);

        // search text field component
        JTextField searchTextField = new JTextField();

        // set the location and size of the searchTextField component
        searchTextField.setBounds(15, 15, 351, 45);

        // change the font style and size
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        // add focus listener to hide warning massage
        searchTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // Action when it gains the focus
                warningText.setVisible(false);
            }

            @Override
            public void focusLost(FocusEvent e){
                // action after it lose the focus
                // useless but necessary...
            }
        });
        this.add(searchTextField);

        // weather image
        JLabel weatherConditionImage = new JLabel(loadImage("src/Assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        this.add(weatherConditionImage);

        // temperature text
        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        // center the text
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(temperatureText);

        // weather condition description
        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(weatherConditionDesc);

        // humidity image
        JLabel humidityImage = new JLabel(loadImage("src/Assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        this.add(humidityImage);

        // humidity text
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        this.add(humidityText);

        // wind speed image
        JLabel windSpeedImage = new JLabel(loadImage("src/Assets/windspeed.png"));
        windSpeedImage.setBounds(220, 500, 74, 66);
        this.add(windSpeedImage);

        // wind speed text
        JLabel windSpeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>");
        windSpeedText.setBounds(310, 500, 85, 55);
        windSpeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        this.add(windSpeedText);

        // search button component
        JButton searchButton = new JButton(loadImage("src/Assets/search.png"));

        // change the cursor to a hand cursor when hovering over this button
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get location from user
                String userInput = searchTextField.getText();

                // validate input - remove withespaces to ensure non-empty text
                if(userInput.replaceAll("\\s", "").length() <= 0){
                    System.out.println("Input is too short after removing whitespaces");
                    return;
                }

                // retrieve weather data
                // weatherData properties: temperature, weatherCondition, humidity, windspeed
                weatherData = Logic.getWeatherData(userInput.toLowerCase().trim());

                if(weatherData == null) {
                    System.out.println("Couldn't retrieve data from provided location");
                    warningText.setVisible(true);
                    return;
                }

                // UPDATE GUI

                // update weather image
                String weatherCondition = (String) weatherData.get("weatherCondition");

                // depending on the condition, we will update the weather image that corresponds with the condition
                switch (weatherCondition.toLowerCase()){
                    case "clear" -> weatherConditionImage.setIcon(loadImage("src/Assets/clear.png"));
                    case "cloudy" -> weatherConditionImage.setIcon(loadImage("src/Assets/cloudy.png"));
                    case "rain" -> weatherConditionImage.setIcon(loadImage("src/Assets/rain.png"));
                    case "snow" -> weatherConditionImage.setIcon(loadImage("src/Assets/snow.png"));
                    default -> System.out.println("No matching weather condition while trying to update weatherConditionImage");
                }

                // update temperature text
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");

                // update weather condition text
                weatherConditionDesc.setText(weatherCondition);

                // update humidity text
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

                // update windspeed text
                double windspeed = (double) weatherData.get("windspeed");
                windSpeedText.setText("<html><b>Windspeed</b> " + windspeed + "km/h</html>");

            }
        });

        this.add(searchButton);
    }

    // method used to create images in the gui components
    private ImageIcon loadImage(String resourcePath) {
        try {
            // read the image file from the path given
            BufferedImage image = ImageIO.read(new File(resourcePath));

            // return an image icon so that the component can render it
            return new ImageIcon(image);
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("Could not find resource");
            return null;
        }
    }

}
