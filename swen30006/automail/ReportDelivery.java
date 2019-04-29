package automail;

import exceptions.MailAlreadyDeliveredException;
import strategies.Automail;

import java.util.ArrayList;

public class ReportDelivery implements IMailDelivery {

    private static double TOTAL_SCORE;
    private static ArrayList<MailItem> MAIL_DELIVERED;

    static {
        MAIL_DELIVERED = new ArrayList<>();
        TOTAL_SCORE = 0;
    }

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

    private static double calculateDeliveryScore(MailItem deliveryItem) {
        // Penalty for longer delivery times
        final double penalty = 1.2;
        double priority_weight = 0;
        // Take (delivery time - arrivalTime)**penalty * (1+sqrt(priority_weight))
        if(deliveryItem instanceof PriorityMailItem){
            priority_weight = ((PriorityMailItem) deliveryItem).getPriorityLevel();
        }
        return Math.pow(Clock.Time() - deliveryItem.getArrivalTime(),penalty)*(1+Math.sqrt(priority_weight));
    }

    public static void printResults(Automail automail, MailGenerator mailGenerator){
        System.out.println("T: "+Clock.Time()+" | Simulation complete!");

        // For debugging when robot is more than one and max. weight exceeds team capacity of the system
        if (automail.getMailPool().getNumOfMailItemRejected() > 0){
            System.out.printf("Created: %6d; Delivered: %4d; Rejected: %5d%n",
                    mailGenerator.getMailCreated(),
                    MAIL_DELIVERED.size(),
                    automail.getMailPool().getNumOfMailItemRejected());
        }

        System.out.println("Final Delivery time: "+Clock.Time());
        System.out.printf("Final Score: %.2f%n", TOTAL_SCORE);
    }


}
