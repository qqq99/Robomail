package strategies;

import automail.IMailDelivery;
import automail.Robot;

import java.util.Arrays;

/**
 * An Automail is a system that manages a mail pool and a set of robots.
 * It located at the mail room, which is on the ground floor, of a building.
 * It sorts mail items received based on their arrival time and priority, then
 * assigns robots to deliver the items as a team or individually.<br/><br/>
 */
public class Automail {
    
	private Robot[] robots;
    private IMailPool mailPool;
    
    public Automail(IMailPool mailPool, IMailDelivery delivery, Robot[] robots) {

    	/** Initialize the MailPool */
    	this.mailPool = mailPool;
    	
    	/** Initialize robots */
    	this.robots = robots;
		Arrays.setAll(robots, i -> new Robot(delivery, mailPool));
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

	/**
	 * This method gets the mail pool of the automail
	 * @return A mail pool with IMailPool interface implemented
	 */
	public IMailPool getMailPool() {
		return mailPool;
	}
    
}
