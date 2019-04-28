package strategies;

import automail.IMailDelivery;
import automail.Robot;

public class Automail {
	      
	private static int SYS_MAX_WEIGHT;
    
	private Robot[] robots;
    private IMailPool mailPool;
    
    public Automail(IMailPool mailPool, IMailDelivery delivery, int numRobots) {
    	// Swap between simple provided strategies and your strategies here
    	    	
    	/** Initialize the MailPool */
    	this.mailPool = mailPool;
    	
    	/** Initialize robots */
    	robots = new Robot[numRobots];
    	for (int i = 0; i < numRobots; i++) robots[i] = new Robot(delivery, mailPool);
    }

	public Robot[] getRobots() {
		return robots;
	}
	
	public Robot getRobot(int i) {
		if (i < robots.length) {
			return robots[i];
		}
		return null;
	}

	public IMailPool getMailPool() {
		return mailPool;
	}
    
}
