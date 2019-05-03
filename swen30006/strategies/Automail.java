package strategies;

import automail.IMailDelivery;
import automail.Robot;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * An Automail system manages a mail pool and a set of robots.
 * It located at the mail room, which is on the ground floor, of a building.
 * It sorts mail items received based on their arrival time and priority, then
 * assigns robots to deliver the items as a team or individually.<br/><br/>
 */
public class Automail {

	private static Logger log = Logger.getLogger(Automail.class.getName());
	private static volatile Automail INSTANCE = null;

	private static Robot[] robots;
    private static IMailPool mailPool;

	/** Use Bill Pugh to implement Singleton */
	public static Automail getInstance(IMailPool mailPool, IMailDelivery delivery, Robot[] robots) {
		if (INSTANCE == null) {
			synchronized (Automail.class) {
				INSTANCE = new Automail(mailPool, delivery, robots);
			}
		}
		return INSTANCE;
	}

	public static Automail getInstance() {
		if (INSTANCE == null) {
			log.warning("Could not get the instance before initialisation");
		}
		return INSTANCE;
	}

    private Automail(IMailPool mailPool, IMailDelivery delivery, Robot[] robots) {
    	/** Initialize the MailPool */
    	this.mailPool = mailPool;
    	
    	/** Initialize robots */
    	this.robots = robots;
		Arrays.setAll(robots, i -> new Robot(delivery, mailPool));
	}

	public static Robot[] getRobots() {
		return robots;
	}

	/**
	 * This method gets the robot from the robot list by id
	 * @param i id of the robot to get
	 * @return Robot
	 */
	public static Robot getRobot(int i) {
		if (i < robots.length) {
			return robots[i];
		}
		return null;
	}

	/**
	 * This method gets the mail pool of the Automail system
	 * @return a mail pool with IMailPool interface implemented
	 */
	public static IMailPool getMailPool() {
		return mailPool;
	}
    
}
