package strategies;

import java.util.*;

import automail.*;
import automail.Robot.RobotState;
import exceptions.ItemTooHeavyException;

/**
 * A MailPool subsystem receives and delivers mail items in a building
 * with the use of robots.<br/><br/>
 *
 * A mail item can be carried by one to three robots.
 * If a mail item requires more than one robot to carry, it can only be
 * carried by a maximum of three robots with their hands.<br/><br/>
 *
 * Any mail items exceeding the INDIVIDUAL_MAX_WEIGHT of a {@link automail.Robot}
 * cannot be put into the robot's tube.
 */
public class MailPool implements IMailPool {

	private class Item {
		int priority;
		int destination;
		MailItem mailItem;
		// Use stable sort to keep arrival time relative positions
		
		public Item(MailItem mailItem) {
			this.priority = (mailItem instanceof PriorityMailItem) ? ((PriorityMailItem) mailItem).getPriorityLevel() : 1;
			this.destination = mailItem.getDestFloor();
			this.mailItem = mailItem;
		}
	}
	
	public class ItemComparator implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			int order = 0;
			if (i1.priority < i2.priority) {
				order = 1;
			} else if (i1.priority > i2.priority) {
				order = -1;
			} else if (i1.destination < i2.destination) {
				order = 1;
			} else if (i1.destination > i2.destination) {
				order = -1;
			}
			return order;
		}
	}
	
	private int MAX_WEIGHT;
	
	private List<MailItem> mailRejectedList = new ArrayList<>();
	private HashMap<MailItem, Integer> robotsDeliveringMap = new HashMap<>();

	private LinkedList<Item> pool;
	private LinkedList<Robot> robots;
	private int numOfRobots;

	public MailPool(LinkedList<Item> pool, LinkedList<Robot> robots, int nrobots){
		// Start empty
		this.pool = pool;
		this.robots = robots;
		this.numOfRobots = nrobots;

		setMaxWeight(nrobots);
	}

	/**
	 * This method adds a mailItem into the pool and sort in by priority in descending order
	 */
	public void addToPool(MailItem mailItem){
		if (mailItem.getWeight() > getSysMaxWeight()) {
			System.out.printf("T: %3d > Item too heavy. Rejected addToPool [%s]%n",
					Clock.Time(), mailItem.toString());
    		mailRejectedList.add(mailItem);
			return;
    	}
		Item item = new Item(mailItem);
		pool.add(item);
		pool.sort(new ItemComparator());
	}
	
	/**
	 * This method loops through a list of robots and 
	 * loads mailItems to them based on priorities and arrival time
	 */
	@Override
	public void step() throws ItemTooHeavyException {
		try{
			ListIterator<Robot> i = robots.listIterator();
			ListIterator<Item> j = pool.listIterator();
			
			while (i.hasNext() && j.hasNext()) {
				MailItem mailItem = j.next().mailItem;
				int numOfRobotsNeeded = getNumOfRobotsNeeded(mailItem);
				int numOfRobotsAvailable = getNumOfRobotsAvailable();
				
				// Stop if not enough robots can be used for team delivery. Return to wait for robots to come back
				if (numOfRobotsNeeded > numOfRobotsAvailable) { 
					return;
				}
				
				// Continue as there are enough robots for delivering this mailItem
				switch(numOfRobotsNeeded) {
					case 1:
						loadRobot(i, j, mailItem); // load hand first, then tube
						break;
					default:
						// Load more than one robot
						loadRobots(i, j, mailItem);
						break;
				}

			}
		} catch (Exception e) { 
            throw e; 
        } 
	}
	
	/**
	 * This method loads mailItems to a robot
	 * @param i a listIterator of robots
	 * @param j a listIterator of mailItems to be assigned and delivered
	 * @param mailItem the first mailItem to be added to this robot
	 * @throws ItemTooHeavyException
	 */
	private void loadRobot(ListIterator<Robot> i, ListIterator<Item> j, MailItem mailItem)
			throws ItemTooHeavyException {
		// System.out.printf("P: %3d%n", pool.size());
		MailItem item = mailItem;
		Robot robot = i.next();
		assert(robot.isEmpty());
		try {
			// Add to hand
			robot.addToHand(item); // hand first as we want higher priority delivered first
			robotsDeliveringMap.put(item, 1);
			j.remove();
			
			// Add to tube
			if (j.hasNext()) {
				item = j.next().mailItem;
				if (getNumOfRobotsNeeded(item) == TeamSize.ONE.getValue()) {
					robot.addToTube(item);
					robotsDeliveringMap.put(item, TeamSize.ONE.getValue());
					j.remove();
				} else {
					j.previous(); // Move back the pointer 
				}
			}
			robot.dispatch(); // send the robot off if it has any items to deliver
			i.remove();       // remove from mailPool queue
		} catch (Exception e) { 
            throw e; 
        }
	}

	/**
	 * This method loads mailItems to a team of robots
	 * @param i a listIterator of robots
	 * @param j a listIterator of mailItems to be assigned and delivered
	 * @param mailItem the first mailItem to be added to this robot
	 * @throws ItemTooHeavyException
	 */
	private void loadRobots(ListIterator<Robot> i, ListIterator<Item> j, MailItem mailItem) 
			throws ItemTooHeavyException {
		// Check item weight and decide the number of robots requiredMailItem item = mailItem;
		int numOfRobotsNeeded = getNumOfRobotsNeeded(mailItem);
		List<Robot> robotsToDispatch = new ArrayList<>();
		try {
			for (int count = 0; count < numOfRobotsNeeded; count++) {
				Robot robot = i.next();
				robotsToDispatch.add(robot);

				assert(robot.isEmpty());
				robot.addToHand(mailItem); // hand first as we want higher priority delivered first

				robot.turnTeamModeOn();
				i.remove(); // remove robot from robot queue
			}
			robotsToDispatch.forEach(robot -> robot.dispatch()); // send the robots off as a team
			robotsDeliveringMap.put(mailItem, numOfRobotsNeeded);
			j.remove(); // remove mailItem from mailPool queue
		} catch (Exception e) { 
            throw e; 
        } 
	}

	@Override
	public void registerWaiting(Robot robot) { // assumes won't be there already
		robots.add(robot);
	}

	/**
	 * This method sets the maximum weight the mail pool
	 * can accept based on the number of robots available.
	 * @param numOfRobots the number of robots can be used by the Automail system
	 */
	private void setMaxWeight(int numOfRobots){
		switch(numOfRobots) {
			case 0:
				MAX_WEIGHT = 0;
				break;
			case 1:
				MAX_WEIGHT = Robot.INDIVIDUAL_MAX_WEIGHT;
				break;
			case 2:
				MAX_WEIGHT = Robot.PAIR_MAX_WEIGHT;
				break;
			case 3:
			default: // 3 or more robots
				MAX_WEIGHT = Robot.TRIPLE_MAX_WEIGHT;
				break;
		}
	}

	/**
	 * This method returns the total number of robots available.
	 * @return the number of robots at the mail pool
	 */
	public int getNumOfRobots(){
		return numOfRobots;
	}

	/**
	 * This method gets the number of robots needed to deliver a mailItem
	 * @param mailItem a mailItem to be delivered
	 * @return the number of robots needed to deliver this mail item,
	 * ranging from 1 for one robot, to 3 for three robots.<br/><br/>
	 * Return -1 if more than 3 robots is needed.
	 */
	private int getNumOfRobotsNeeded(MailItem mailItem) {
		int mailItemWeight = mailItem.getWeight();
		if (mailItemWeight <= Robot.INDIVIDUAL_MAX_WEIGHT) {
			return 1;
		}
		if (mailItemWeight <= Robot.PAIR_MAX_WEIGHT) {
			return 2;
		}
		if (mailItemWeight <= Robot.TRIPLE_MAX_WEIGHT) {
			return 3;
		}
		return -1;
	}

	/**
	 * This method gets the number of robots waiting at the mail room
	 * and ready to load and deliver mail items.
	 * @return the number of robots waiting for delivery assignments
	 */
	private int getNumOfRobotsAvailable() {
		int count = 0;
		for (Robot robot: robots) {
			if (robot.isEmpty() && robot.currentState == RobotState.WAITING) {
				count++;
			}
		}
		return count;
	}

	/**
	 * This method gets the number of mail items rejected by the mail pool.
	 * @return the number of mail items rejected for delivery
	 */
	public int getNumOfMailItemRejected() {
		return mailRejectedList.size();
	}

	/**
	 * This method gets the maximum weight of a mail item the mail pool accepts.
	 * @return the maximum weight of a mail item accepted for delivery
	 */
	public int getSysMaxWeight() {
		return MAX_WEIGHT;
	}

	/**
	 * This method gets the number of robots delivering a mail item.
	 * @param mailItem The id of the mail item delivering by one or more robots
	 * @return the number of robots being used for delivering the mail item.
	 */
	public int getRobotsDelivering(MailItem mailItem){
		return robotsDeliveringMap.get(mailItem);
	}

	/**
	 * This method reduces the number of robots delivering a mail item.
	 * It is called when a robot delivers as a team and it reaches the destination floor.
	 * It is used to make sure all robots arrive at the destination floor
	 * before updating the delivery status of a mail item.
	 * @param mailItem the mail item being delivered
	 */
	public void removeRobotFromDelivery(MailItem mailItem){
		int currentTeamSize = robotsDeliveringMap.get(mailItem);
		robotsDeliveringMap.put(mailItem, --currentTeamSize);
	}

	@Override
	public String toString() {
		return "MailPool{" +
				"pool=" + pool +
				", robots=" + robots +
				", numOfRobots=" + numOfRobots +
				'}';
	}
}
