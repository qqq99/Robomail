package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.IMailPool;
import java.util.Map;
import java.util.TreeMap;

/**
 * The robot delivers mail!
 */
public class Robot implements IMovable{

    public static final int INDIVIDUAL_MAX_WEIGHT = 2000;
    public static final int PAIR_MAX_WEIGHT = 2600;
    public static final int TRIPLE_MAX_WEIGHT = 3000;

    IMailDelivery delivery;
    protected final String id;
    /** Possible states the robot can be in */
    public enum RobotState { DELIVERING, WAITING, RETURNING }
    public RobotState currentState;
    private int currentFloor;
    private int destinationFloor;
    private IMailPool mailPool;
    private boolean receivedDispatch;
    
    private MailItem deliveryItem;
    private MailItem tube;

    /** For switching between individual and team mode */
    private boolean teamMode; // true for working as a team. false for working as an individual
    private int waitCounter; // Count the number of time to wait before moving a step
    
    private int deliveryCounter;
    

    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
    public Robot(IMailDelivery delivery, IMailPool mailPool){
    	id = "R" + hashCode();
        // currentState = RobotState.WAITING;
    	this.currentState = RobotState.RETURNING;
        this.currentFloor = Building.MAILROOM_LOCATION;
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
        this.teamMode = false;
        this.waitCounter = 0;
    }
    
    /**
     * This method set a robot's receivedDispatch boolean value to true
     */
    public void dispatch() {
    	receivedDispatch = true;
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    @Override
    public void step() {
        try {
            switch(currentState) {
                /** This state is triggered when the robot is returning to the mailroom after a delivery */
                case RETURNING:
                    /** If its current position is at the mailroom, then the robot should change state */
                    if(currentFloor == Building.MAILROOM_LOCATION){
                        if (tube != null) {
                            mailPool.addToPool(tube);
                            System.out.printf("T: %3d > old addToPool [%s]%n", Clock.Time(), tube.toString());
                            tube = null;
                        }
                        /** Tell the sorter the robot is ready */
                        mailPool.registerWaiting(this);
                        changeState(RobotState.WAITING);
                    } else {
                        /** If the robot is not at the mailroom floor yet, then move towards it! */
                        moveTowards(Building.MAILROOM_LOCATION);
                        break;
                    }
                case WAITING:
                    /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                    if(!isEmpty() && receivedDispatch){
                        receivedDispatch = false;
                        deliveryCounter = 0; // reset delivery counter
                        setRoute();
                        changeState(RobotState.DELIVERING);
                    }
                    break;
                case DELIVERING:
                    if(currentFloor == destinationFloor){ // If already here drop off either way

                        if (mailPool.getRobotsDelivering(deliveryItem) == TeamSize.ONE.getValue()){
                            /**
                             * Last robot to deliver this item(as an individual or as a team),
                             * report this to the simulator!
                             */
                            delivery.deliver(deliveryItem);
                            turnTeamModeOff(); // Turn off team mode and reset waitCounter to zero
                        } else {
                            mailPool.removeRobotFromDelivery(deliveryItem);
                        }
                        deliveryItem = null;
                        deliveryCounter++;
                        if(deliveryCounter > 2){  // Implies a simulation bug
                            throw new ExcessiveDeliveryException();
                        }
                        /** Check if want to return, i.e. if there is no item in the tube*/
                        if(tube == null){
                            changeState(RobotState.RETURNING);
                        }
                        else{
                            /** If there is another item, set the robot's route to the location to deliver the item */
                            deliveryItem = tube;
                            tube = null;
                            setRoute();
                            changeState(RobotState.DELIVERING);
                        }


                    } else {
                        /** The robot is not at the destination yet, move towards it! */
                        if (!isTeamModeOn()){
                            /** Working as an individual */
                            moveTowards(destinationFloor);
                        } else {
                            /** Working as a team. */
                            if (finishWaiting()){
                                // Finish waiting and move a step
                                moveTowards(destinationFloor);
                                resetWaitCounter(); // reset for the next move
                            } else {
                                // Keep waiting
                                countWait();
                            }
                        }

                    }
                    break;
            }
        }
        catch (ExcessiveDeliveryException e){
            e.printStackTrace();
        }
    }

    /**
     * Sets the route for the robot
     */
    private void setRoute() {
        /** Set the destination floor */
        destinationFloor = deliveryItem.getDestFloor();
    }

    /**
     * Generic function that moves the robot towards the destination
     * @param destination the floor towards which the robot is moving
     */
    private void moveTowards(int destination) {
        if(currentFloor < destination){
            currentFloor++;
        } else {
            currentFloor--;
        }
    }
    
    private String getIdTube() {
    	return String.format("%s(%1d)", id, (tube == null ? 0 : 1));
    }
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    private void changeState(RobotState nextState){
    	assert(!(deliveryItem == null && tube != null));
    	if (currentState != nextState) {
    		// Example: R(1) means tube is also filled before delivery
            System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), currentState, nextState);
    	}
    	currentState = nextState;
    	if(nextState == RobotState.DELIVERING){
            System.out.printf("T: %3d > %7s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
    	}
    }

	public MailItem getTube() {
		return tube;
	}
    
	static private int count = 0;
	static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

	@Override
	public int hashCode() {
		Integer hash0 = super.hashCode();
		Integer hash = hashMap.get(hash0);
		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
		return hash;
	}

	public boolean isEmpty() {
		return (deliveryItem == null && tube == null);
	}

    /**
     * This method adds a mail item to the hands of a robot
     * if the hands are empty
     * @param mailItem A mail item to be added to the hands
     * @throws ItemTooHeavyException
     */
	public void addToHand(MailItem mailItem) throws ItemTooHeavyException {
		assert(deliveryItem == null);
		deliveryItem = mailItem;
		// Adjust the max. weight a robot's hand can carry
        // based on the team capacity and number of robots in the mail pool
		if (deliveryItem.weight > mailPool.getSysMaxWeight()) throw new ItemTooHeavyException();
	}

    /**
     * This method adds a mail item to the tube of a robot
     * if the tube is empty
     * @param mailItem A mail item to be added to the tube
     * @throws ItemTooHeavyException
     */
	public void addToTube(MailItem mailItem) throws ItemTooHeavyException {
		assert(tube == null);
		tube = mailItem;
		if (tube.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

    /**
     * This method gets the team mode of a robot
     * @return true if the robot is in a team. false is the robot is working individually
     */
	public boolean getTeamMode(){
	    return teamMode;
    }

    /**
     * This method turns on the team mode of a robot
     */
    public void turnTeamModeOn(){
	    teamMode = true;
    }

    /**
     * This method turns off the team mode of a robot after
     * a robot finishes delivering as a team with its hand
     */
    private void turnTeamModeOff(){
	    teamMode = false;
	    resetWaitCounter();
    }

    /**
     * This method checks if the team mode is on
     * @return <b>true</b> if it is on, which means the robot is delivering as a team.<br/>
     * <b>false</b> if the robot is delivering as an individual.
     */
    private boolean isTeamModeOn(){
	    return teamMode == true;
    }

    /**
     * This method returns the wait counter value
     * @return The amount of time a robot has been waiting in a team before the next move
     */
    public int getWaitCounter(){ // Not used in this version. Good practice to keep a getter
	    return waitCounter;
    }

    /**
     * This method checks whether a robot, working in a team, finishes waiting
     * and is ready for the next move
     */
    private boolean finishWaiting(){
        return waitCounter == 2;
    }

    /**
     * This method adds the wait counter by one unit of time.
     */
    private void countWait(){
        waitCounter++;
    }

    /**
     * This method resets the wait counter to 0
     * when a robot switches from team mode to individual mode
     */
    private void resetWaitCounter(){
	    waitCounter = 0;
    }

}
