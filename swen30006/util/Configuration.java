package util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class Configuration {

    private static Logger log = Logger.getLogger(Configuration.class.getName());
    private static final String CONFIGURATION_FILE = "automail.properties";
    private static Properties automailProperties;

    // Use Bill Pugh to implement Singleton
    private Configuration(){}

    // use static initializer to read the configuration file when the class is loaded
    static {
        automailProperties = new Properties();
        // Default properties
        // automailProperties.setProperty("Robots", "Big,Careful,Standard,Weak");
        automailProperties.setProperty("Robots", "Standard");
        automailProperties.setProperty("MailPool", "strategies.SimpleMailPool");
        automailProperties.setProperty("Floors", "10");
        automailProperties.setProperty("Fragile", "false");
        automailProperties.setProperty("Mail_to_Create", "80");
        automailProperties.setProperty("Last_Delivery_Time", "100");

        // Read properties
        FileReader inStream = null;
        try {
            inStream = new FileReader(CONFIGURATION_FILE);
            automailProperties.load(inStream);
        }
        catch (IOException e) {
            log.warning("Could not read file " + CONFIGURATION_FILE);
        }
        finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            }
            catch (IOException e){
                log.warning("Could not read file " + CONFIGURATION_FILE);
            }
        }

    }

    public static String getProperty(String key){
        return automailProperties.getProperty(key);
    }

}
