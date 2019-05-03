package util;

import automail.*;
import exceptions.MailAlreadyDeliveredException;
import strategies.Automail;

import java.util.ArrayList;

/**
 * A ReportDelivery is responsible for tracking the delivery status
 * of mail items accepted by the system. It also generate a delivery summary report
 * after all delivery items are delivered by the robots, excluding
 * mail items over team capacity, which won't be assigned to any robots by the
 * MailPool to deliver.<br/><br/>
 * For example, if the Automail system only has 2 robots, the team capacity is 2600 grams.
 * Any mail items have a weight over 2600 grams are rejected in the report
 * for calculating the delivery time and score.
 */
public class ReportDelivery implements IMailDelivery {

    private static double TOTAL_SCORE = 0;
    private static ArrayList<MailItem> MAIL_DELIVERED = new ArrayList<>();

    /** Use Bill Pugh to implement Singleton */
    private ReportDelivery(){}

    private static class SingletonHelper{
        private static final ReportDelivery INSTANCE = new ReportDelivery();
    }

    /**
     * This method is called to initialise a Singleton object of the ReportDelivery class
     * @return A ReportDelivery
     */
    public static ReportDelivery getInstance(){
        return SingletonHelper.INSTANCE;
    }

    /**
     * This method gets the number of mail items delivered
     * @return The total number of mail items delivered
     */
    public static int getNumOfMailDelivered(){
        return MAIL_DELIVERED.size();
    }

    /** Confirm the delivery and calculate the total score */
    public void deliver(MailItem deliveryItem){
        if(!MAIL_DELIVERED.contains(deliveryItem)){
            MAIL_DELIVERED.add(deliveryItem);
            System.out.printf("T: %3d > Delivered(%4d) [%s]%n",
                    Clock.Time(), MAIL_DELIVERED.size(), deliveryItem.toString());
            // Calculate delivery score
            TOTAL_SCORE += calculateDeliveryScore(deliveryItem);
        }
        else{
            try {
                throw new MailAlreadyDeliveredException();
            } catch (MailAlreadyDeliveredException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method calculate the total score of delivering all mail items
     * @param deliveryItem A mail item delivered by the robot(s)
     * @return The score of delivering a mail item
     */
    private double calculateDeliveryScore(MailItem deliveryItem) {
        // Penalty for longer delivery times
        final double penalty = 1.2;
        double priority_weight = 0;
        // Take (delivery time - arrivalTime)**penalty * (1+sqrt(priority_weight))
        if(deliveryItem instanceof PriorityMailItem){
            priority_weight = ((PriorityMailItem) deliveryItem).getPriorityLevel();
        }
        return Math.pow(Clock.Time() - deliveryItem.getArrivalTime(),penalty)*(1+Math.sqrt(priority_weight));
    }

    /**
     * This method prints the results of the Simulation to the screen.<br/><br/>
     * It contains:<br/>
     * - A simulation complete statement<br/>
     * - A statement showing the number of delivered, created, and rejected mail items
     * (visible only when there is any mail item rejected due to reasons like
     * mail item's weight exceed the team capacity of the Automail system)<br/>
     * - Total time to process mail items received + deliver mail items accepted<br/>
     * - Total score of the simulation
     * @param automail The Automail system
     * @param mailGenerator The mail generator
     */
    public static void printResults(){
        System.out.println("T: "+Clock.Time()+" | Simulation complete!");

        // For debugging when robot is more than one and max. weight exceeds team capacity of the system
        if (Automail.getMailPool().getNumOfMailItemRejected() > 0){
            System.out.printf("Created: %6d; Delivered: %4d; Rejected: %5d%n",
                    MailGenerator.getMailCreated(),
                    MAIL_DELIVERED.size(),
                    Automail.getMailPool().getNumOfMailItemRejected());
        }

        System.out.println("Final Delivery time: "+Clock.Time());
        System.out.printf("Final Score: %.2f%n", TOTAL_SCORE);
    }


}
