package util;

import java.util.*;
import java.util.logging.Logger;

import automail.Building;
import automail.Clock;
import automail.MailItem;
import automail.PriorityMailItem;
import strategies.IMailPool;

/**
 * This class is used to generate mail items and load them into the Automail system.
 */
public class MailGenerator{

    private static Logger log = Logger.getLogger(MailGenerator.class.getName());
    private static MailGenerator INSTANCE = null;
    private static int MAIL_TO_CREATE;
    private static int MAIL_MAX_WEIGHT;
    private static int mailCreated;

    private static Random random;
    /** This seed is used to make the behaviour deterministic */
    
    private static boolean complete;
    private static IMailPool mailPool;
    private static Map<Integer,ArrayList<MailItem>> allMail;

    /** Use Bill Pugh to implement Singleton */
    public static MailGenerator getInstance(
            int mailToCreate, int mailMaxWeight, IMailPool mailPool, HashMap<Boolean,Integer> seed) {
        if (INSTANCE == null) {
            synchronized (MailGenerator.class) {
                INSTANCE = new MailGenerator(mailToCreate, mailMaxWeight, mailPool, seed);
            }
        }
        return INSTANCE;
    }

    public static MailGenerator getInstance() {
        if (INSTANCE == null) {
            log.warning("Could not get the instance before initialisation");
        }
        return INSTANCE;
    }

    /**
     * Constructor for mail generation
     * @param mailToCreate roughly how many mail items to create
     * @param mailPool where mail items go on arrival
     * @param seed random seed for generating mail
     */
    private MailGenerator(int mailToCreate, int mailMaxWeight, IMailPool mailPool, HashMap<Boolean,Integer> seed){
        if(seed.containsKey(true)){
        	this.random = new Random((long) seed.get(true));
        }
        else{
        	this.random = new Random();	
        }
        // Vary arriving mail by +/-20%
        MAIL_TO_CREATE = mailToCreate*4/5 + random.nextInt(mailToCreate*2/5);
        MAIL_MAX_WEIGHT = mailMaxWeight;
        // System.out.println("Num Mail Items: "+MAIL_TO_CREATE);
        mailCreated = 0;
        complete = false;
        allMail = new HashMap<Integer,ArrayList<MailItem>>();
        this.mailPool = mailPool;
    }

    /**
     * @return a new mail item that needs to be delivered
     */
    private static MailItem generateMail(){
    	MailItem newMailItem;
        int dest_floor = generateDestinationFloor();
        int priority_level = generatePriorityLevel();
        int arrival_time = generateArrivalTime();
        int weight = generateWeight();
        // Check if arrival time has a priority mail
        if(	(random.nextInt(6) > 0) ||  // Skew towards non priority mail
        	(allMail.containsKey(arrival_time) &&
        	allMail.get(arrival_time).stream().anyMatch(e -> PriorityMailItem.class.isInstance(e))))
        {
        	newMailItem = new MailItem(dest_floor,arrival_time,weight);      	
        } else {
        	newMailItem = new PriorityMailItem(dest_floor,arrival_time,weight,priority_level);
        }
        return newMailItem;
    }

    /**
     * @return a destination floor between the ranges of GROUND_FLOOR to FLOOR
     */
    private static int generateDestinationFloor(){
        return Building.LOWEST_FLOOR + random.nextInt(Building.FLOORS);
    }

    /**
     * @return a random priority level selected from 1 - 100
     */
    private static int generatePriorityLevel(){
        return 10*(1 + random.nextInt(10));
    }

    /**
     * @return a random weight
     */
    private static int generateWeight(){
    	final double mean = 200.0; // grams for normal item
    	final double stddev = 1000.0; // grams
    	double base = random.nextGaussian();
    	if (base < 0) base = -base;
    	int weight = (int) (mean + base * stddev);
        return weight > MAIL_MAX_WEIGHT ? MAIL_MAX_WEIGHT : weight;
    }
    
    /**
     * @return a random arrival time before the last delivery time
     */
    private static int generateArrivalTime(){
        return 1 + random.nextInt(Clock.LAST_DELIVERY_TIME);
    }

    /**
     * This class initializes all mail and sets their corresponding values
     */
    public static void generateAllMail(){
        while(!complete){
            MailItem newMail = generateMail();
            int timeToDeliver = newMail.getArrivalTime();
            /** Check if key exists for this time **/
            if(allMail.containsKey(timeToDeliver)){
                /** Add to existing array */
                allMail.get(timeToDeliver).add(newMail);
            }
            else{
                /** If the key doesn't exist then set a new key along with the array of MailItems to add during
                 * that time step.
                 */
                ArrayList<MailItem> newMailList = new ArrayList<MailItem>();
                newMailList.add(newMail);
                allMail.put(timeToDeliver,newMailList);
            }
            /** Mark the mail as created */
            mailCreated++;

            /** Once we have satisfied the amount of mail to create, we're done!*/
            if(mailCreated == MAIL_TO_CREATE){
                complete = true;
            }
        }
    }
    
    /**
     * While there are steps left, create a new mail item to deliver
     * @return Priority
     */
    public static PriorityMailItem step(){
    	PriorityMailItem priority = null;
    	// Check if there are any mail to create
        if(allMail.containsKey(Clock.Time())){
            for(MailItem mailItem : allMail.get(Clock.Time())){
        		if (mailItem instanceof PriorityMailItem) priority = ((PriorityMailItem) mailItem);
	            System.out.printf("T: %3d > new addToPool [%s]%n", Clock.Time(), mailItem.toString());
	            mailPool.addToPool(mailItem);
            }
        }
        return priority;
    }

    /**
     * This method gets the number of mail items created
     * @return the number of mail items created
     */
    public static int getMailCreated() {
        return mailCreated;
    }

}
