package automail;

/**
 * A set of values for mail pool and robot to set conditions
 * based on the team size of the robots required to deliver a mail item
 */
public enum TeamSize {
    ONE(1),
    TWO(2), // Not used in this version
    THREE(3); // Not used in this version

    private int teamSize;

    TeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public int getValue() {
        return this.teamSize;
    }

}
