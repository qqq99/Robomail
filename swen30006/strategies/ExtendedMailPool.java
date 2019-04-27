package strategies;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import automail.Robot.RobotState;
import exceptions.ItemTooHeavyException;

public class ExtendedMailPool implements IMailPool {
	
	private final int MAX_TEAM_SIZE = 3; // For clarity. Not in used

	private class Item {
		int priority;
		int destination;
		MailItem mailItem;
		// Use stable sort to keep arrival time relative positions
		
		public Item(MailItem mailItem) {
			priority = (mailItem instanceof PriorityMailItem) ? ((PriorityMailItem) mailItem).getPriorityLevel() : 1;
			destination = mailItem.getDestFloor();
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
	
	private LinkedList<Item> pool;
	private LinkedList<Robot> robots;

	public ExtendedMailPool(int nrobots){
		// Start empty
		pool = new LinkedList<Item>();
		robots = new LinkedList<Robot>();
	}

	/**
	 * This method adds a mailItem into the pool and sort in by priority in descending order
	 */
	public void addToPool(MailItem mailItem) {
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
				// Load mailItem by (1) number of robots required
				MailItem mailItem = j.next().mailItem;
				int numOfRobotsNeeded = getNumOfRobotsNeeded(mailItem);
				int numOfRobotsAvailable = getNumOfRobotsAvailable();
				
				// Check edge case: mailItem weight exceeds max weight of the team delivery capacity
				if (numOfRobotsNeeded == -1) {
					System.out.printf("Weight of this mailItem exceeds the system's capacity: [%s]%n", mailItem.toString());
					return;
				}
				
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
	 * @param i A listIterator of robots
	 * @param j A listIterator of mailItems to be assigned and delivered
	 * @param mailItem The first mailItem to be added to this robot
	 * @throws ItemTooHeavyException
	 */
	private void loadRobot(ListIterator<Robot> i, ListIterator<Item> j, MailItem mailItem) throws ItemTooHeavyException {
		// System.out.printf("P: %3d%n", pool.size());
		MailItem item = mailItem;
		Robot robot = i.next();
		assert(robot.isEmpty());
		try {
			// Add to hand
			robot.addToHand(item); // hand first as we want higher priority delivered first
			j.remove();
			
			// Add to tube
			if (j.hasNext()) {
				item = j.next().mailItem;
				if (getNumOfRobotsNeeded(item) == 1) {
					robot.addToTube(item);
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
				i.remove(); // remove robot from robot queue
			}
			robotsToDispatch.forEach(robot -> robot.dispatch()); // send the robots off as a team
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
	 * This method gets the number of robots needed to deliver a mailItem
	 * @param mailItem MailItem to be delivered
	 * @return 1 to 3 for the number of robots needed. -1 if more than 3 robots is needed.
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
	 * This method calculates the number of robots with empty hands and a state of WAITING
	 * @return The number of robots can be used for team delivery
	 */
	private int getNumOfRobotsAvailable() {
		int count = 0;
		for (Robot robot: robots) {
			if (robot.isEmpty() && robot.current_state == RobotState.WAITING) {
				count++;
			}
		}
		return count; 
	}

}
