package automail;

import exceptions.ItemTooHeavyException;
import strategies.Automail;
import strategies.MailPool;
import strategies.IMailPool;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

/**
 * This class simulates the behaviour of AutoMail
 */
public class Simulation {	
	
    /** Constant for the mail generator */
    private static int MAIL_TO_CREATE;
    private static int MAIL_MAX_WEIGHT;

    public static void main(String[] args) throws IOException {
    	Properties automailProperties = new Properties();
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
			inStream = new FileReader("automail.properties");
			automailProperties.load(inStream);
		} finally {
			 if (inStream != null) {
	                inStream.close();
	            }
		}

		//Seed
		String seedProp = automailProperties.getProperty("Seed");
		// Floors
		Building.FLOORS = Integer.parseInt(automailProperties.getProperty("Floors"));
        System.out.printf("Floors: %5d%n", Building.FLOORS);
        // Fragile
        boolean fragile = Boolean.parseBoolean(automailProperties.getProperty("Fragile"));
        System.out.printf("Fragile: %5b%n", fragile);
		// Mail_to_Create
		MAIL_TO_CREATE = Integer.parseInt(automailProperties.getProperty("Mail_to_Create"));
        System.out.printf("Mail_to_Create: %5d%n", MAIL_TO_CREATE);
        // Mail_to_Create
     	MAIL_MAX_WEIGHT = Integer.parseInt(automailProperties.getProperty("Mail_Max_Weight"));
        System.out.printf("Mail_Max_Weight: %5d%n", MAIL_MAX_WEIGHT);
		// Last_Delivery_Time
		Clock.LAST_DELIVERY_TIME = Integer.parseInt(automailProperties.getProperty("Last_Delivery_Time"));
        System.out.printf("Last_Delivery_Time: %5d%n", Clock.LAST_DELIVERY_TIME);
		// Robots
		int robots = Integer.parseInt(automailProperties.getProperty("Robots"));
		System.out.print("Robots: "); System.out.println(robots);
		assert(robots > 0);
		// MailPool
		IMailPool mailPool = new MailPool(new LinkedList<>(), new LinkedList<>(), robots);

		// End properties

        /** Used to see whether a seed is initialized or not */
        HashMap<Boolean, Integer> seedMap = new HashMap<>();
        
        /** Read the first argument and save it as a seed if it exists */
        if (args.length == 0 ) { // No arg
        	if (seedProp == null) { // and no property
        		seedMap.put(false, 0); // so randomise
        	} else { // Use property seed
        		seedMap.put(true, Integer.parseInt(seedProp));
        	}
        } else { // Use arg seed - overrides property
        	seedMap.put(true, Integer.parseInt(args[0]));
        }
        Integer seed = seedMap.get(true);
        System.out.printf("Seed: %s%n", seed == null ? "null" : seed.toString());
        ReportDelivery reportDelivery;
        Automail automail = new Automail(mailPool, ReportDelivery.getInstance(), new Robot[robots]);
        MailGenerator mailGenerator = new MailGenerator(
        		MAIL_TO_CREATE, MAIL_MAX_WEIGHT, automail.getMailPool(), seedMap);
        
        /** Initiate all the mail */
        mailGenerator.generateAllMail();
        // PriorityMailItem priority;  // Not used in this version
        while(mailGenerator.getMailCreated() !=
				ReportDelivery.getNumOfMailDelivered() + automail.getMailPool().getNumOfMailItemRejected()) {
        	// Add items to the pool
        	mailGenerator.step();
            try {
				// Load items to the robots
            	automail.getMailPool().step();
            	// Move robots
				for (int k=0; k<automail.getMailPool().getNumOfRobots(); k++) automail.getRobot(k).step();
			} catch (ItemTooHeavyException e) {
				e.printStackTrace();
				System.out.println("Simulation unable to complete.");
				System.exit(0);
			}
            Clock.Tick();
        }
        ReportDelivery.printResults(automail, mailGenerator);
    }
    

}
