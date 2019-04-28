package strategies;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.Robot;
import exceptions.ItemTooHeavyException;

public class MailPool implements IMailPool {

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

    private class SpecialItem extends Item {
        private int robotNeed;

        public SpecialItem(int robotNeed, MailItem mailItem) {
            super(mailItem);
            this.robotNeed = robotNeed;
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
    private LinkedList<SpecialItem> specialItemsPool;
    private LinkedList<Robot> robots;

    public MailPool(int nrobots) {
        // Start empty
        pool = new LinkedList<Item>();
        specialItemsPool = new LinkedList<>();
        robots = new LinkedList<Robot>();
    }

    public void addToPool(MailItem mailItem) {
        if (mailItem.getWeight() <= Robot.INDIVIDUAL_MAX_WEIGHT) {
            Item item = new Item(mailItem);
            pool.add(item);
        } else {
            int robotsNeed = 2;
            if (mailItem.getWeight() > Robot.PAIR_MAX_WEIGHT) {
                robotsNeed = 3;
            }

            specialItemsPool.add(new SpecialItem(robotsNeed, mailItem));
        }

        ItemComparator comparator = new ItemComparator();
        pool.sort(comparator);
        specialItemsPool.sort(comparator);
    }

    @Override
    public void step() throws ItemTooHeavyException {
        try {
            if (specialItemsPool.size() > 0) {
                if (stepWithHeavierItems()) {
                    stepWithNormalItems();
                }
            } else {
                stepWithNormalItems();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean stepWithHeavierItems() throws ItemTooHeavyException {
        SpecialItem toBeDeliver = specialItemsPool.get(0);

        int need = toBeDeliver.robotNeed;
        if (robots.size() >= need) {
            specialItemsPool.remove(0);
            for (int i = 0; i < need; i++) {
                robots.get(i).addToHand(toBeDeliver.mailItem);
            }

            ListIterator<Item> iterator = pool.listIterator();
            int index = 0;
            while (iterator.hasNext() && index < need) {
                Item next = iterator.next();
                robots.get(index).addToTube(next.mailItem);
                iterator.remove();
                ++index;
            }

            for (int i = 0; i < need; i++) {
                robots.get(0).dispatch();
                robots.remove(0);
            }
            return true;
        } else {
            return false;
        }
    }

    private void stepWithNormalItems() throws ItemTooHeavyException {
        ListIterator<Robot> i = robots.listIterator();
        while (i.hasNext()) loadRobot(i);
    }

    private void loadRobot(ListIterator<Robot> i) throws ItemTooHeavyException {
        Robot robot = i.next();
        assert (robot.isEmpty());
        // System.out.printf("P: %3d%n", pool.size());
        ListIterator<Item> j = pool.listIterator();
        if (pool.size() > 0) {
            try {
                robot.addToHand(j.next().mailItem); // hand first as we want higher priority delivered first
                j.remove();
                if (pool.size() > 0) {
                    robot.addToTube(j.next().mailItem);
                    j.remove();
                }
                robot.dispatch(); // send the robot off if it has any items to deliver
                i.remove();       // remove from mailPool queue
            } catch (Exception e) {
                throw e;
            }
        }
    }

    @Override
    public void registerWaiting(Robot robot) { // assumes won't be there already
        robots.add(robot);
    }

}
