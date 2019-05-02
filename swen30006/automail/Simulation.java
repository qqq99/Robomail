package automail;

import exceptions.ItemTooHeavyException;
import strategies.Automail;
import strategies.MailPool;
import strategies.IMailPool;
import util.Configuration;
import util.MailGenerator;
import util.ReportDelivery;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class simulates the behaviour of AutoMail
 */
public class Simulation {	

    public static void main(String[] args){
    	
    	/** Constant for the mail generator */
        int MAIL_TO_CREATE;
        int MAIL_MAX_WEIGHT;

    	/** Configuration class is loaded */
		//Seed
		String seedProp = Configuration.getProperty("Seed");
		// Floors
		Building.FLOORS = Integer.parseInt(Configuration.getProperty("Floors"));
        System.out.printf("Floors: %5d%n", Building.FLOORS);
        // Fragile
        boolean fragile = Boolean.parseBoolean(Configuration.getProperty("Fragile"));
        System.out.printf("Fragile: %5b%n", fragile);
		// Mail_to_Create
		MAIL_TO_CREATE = Integer.parseInt(Configuration.getProperty("Mail_to_Create"));
        System.out.printf("Mail_to_Create: %5d%n", MAIL_TO_CREATE);
        // Mail_to_Create
     	MAIL_MAX_WEIGHT = Integer.parseInt(Configuration.getProperty("Mail_Max_Weight"));
        System.out.printf("Mail_Max_Weight: %5d%n", MAIL_MAX_WEIGHT);
		// Last_Delivery_Time
		Clock.LAST_DELIVERY_TIME = Integer.parseInt(Configuration.getProperty("Last_Delivery_Time"));
        System.out.printf("Last_Delivery_Time: %5d%n", Clock.LAST_DELIVERY_TIME);
		// Robots
		int robots = Integer.parseInt(Configuration.getProperty("Robots"));
		System.out.print("Robots: "); System.out.println(robots);
		assert(robots > 0);
		// MailPool
		IMailPool mailPool = new MailPool(new LinkedList<>(), new LinkedList<>(), robots);

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

        Automail automail = new Automail(mailPool, ReportDelivery.getInstance(), new Robot[robots]);

        /** Initiate all the mail */
        MailGenerator mailGenerator = new MailGenerator(
        		MAIL_TO_CREATE, MAIL_MAX_WEIGHT, automail.getMailPool(), seedMap);
        mailGenerator.generateAllMail();
        // PriorityMailItem priority;  // Not used in this version
        while(mailGenerator.getMailCreated() !=
				ReportDelivery.getNumOfMailDelivered() + automail.getMailPool().getNumOfMailItemRejected()) {

        	/** Add mail items to the pool */
        	mailGenerator.step();
            try {
            	/** Load mail items to the robots */
            	automail.getMailPool().step();

            	/** Move the robots */
				for (int k=0; k<automail.getMailPool().getNumOfRobots(); k++) automail.getRobot(k).step();

			} catch (ItemTooHeavyException e) {
				e.printStackTrace();
				System.out.println("Simulation unable to complete.");
				System.exit(0);
			}
            Clock.Tick();
        }

        /** Generate the delivery report */
        ReportDelivery.printResults(automail, mailGenerator);
    }
    

}
