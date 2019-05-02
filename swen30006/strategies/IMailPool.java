package strategies;

import automail.MailItem;
import automail.Robot;
import exceptions.ItemTooHeavyException;

import java.util.LinkedList;

/**
 * addToPool is called when there are mail items newly arrived at the building to add to the MailPool or
 * if a robot returns with some undelivered items - these are added back to the MailPool.
 * The data structure and algorithms used in the MailPool is your choice.
 * 
 */
public interface IMailPool {
	
	/**
     * Adds an item to the mail pool
     * @param mailItem the mail item being added.
     */
    void addToPool(MailItem mailItem);
    
    /**
     * This method loads up any waiting robots with mail items, if any.
     */
	void step() throws ItemTooHeavyException;

	/**
     * @param robot refers to a robot which has arrived back ready for more mailItems to deliver
     */	
	void registerWaiting(Robot robot);

	/**
	 * This method returns the number of robots of the mail pool
	 * @return The number of robots at the mail pool
	 */
	int getNumOfRobots();

	/**
	 * This method gets a list of robots from the mail pool
	 * @return A LinkedList of robots at the mail pool
	 */
	LinkedList getRobots();

	/**
	 * This method returns the number of mailItems rejected due to weight over the mail pool's limit
	 * @return The number of rejected mailItems
	 */
	int getNumOfMailItemRejected();

	/**
	 * This method returns the mail pool's maximum accepted weight of mailItems could be delivered
	 * by the number of robots it has and the maximum team capacity
	 * @return The maximum weight robots of the mail pool can deliver
	 */
	int getSysMaxWeight();

	/**
	 * This method returns the number of robots delivering a mail item.
	 * @param mailItem The id of the mail item delivering by one or more robots
	 * @return The number of robots delivering the mail item
	 */
	int getRobotsDelivering(MailItem mailItem);

	/**
	 * This method removes a robot from a list of mail items delivering.
	 * @param mailItem The mail item being delivered
	 */
	void removeRobotFromDelivery(MailItem mailItem);

}
