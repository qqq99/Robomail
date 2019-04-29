package strategies;

import automail.MailItem;
import automail.Robot;
import exceptions.ItemTooHeavyException;

import java.util.LinkedList;
import java.util.List;

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
	 * @throws ItemTooHeavyException 
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
	 * This method returns the max. weight this mail pool can handle
	 * @return Maximum weight the mail pool can handle
	 */
	int getSysMaxWeight();
	
	/**
	 * 
	 * @return
	 */
	int getNumOfMailItemRejected();

}
