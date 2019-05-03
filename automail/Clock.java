package automail;

public class Clock {
	
	/** Represents the current time **/
    private static int Time = 0;
    
    /** The threshold for the latest time for mail to arrive **/
    public static int LAST_DELIVERY_TIME;

    /**
     * @return The current time as an int value
     */
    public static int Time() {
    	return Time;
    }
    
    /**
     * This method increases the current time int value by 1
     */
    public static void Tick() {
    	Time++;
    }
}
