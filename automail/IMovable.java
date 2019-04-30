package automail;

/**
 * A Movable is called whenever an object has to move within a unit of time
 */
public interface IMovable {
    /**
     * Move an object. For example,<br/>
     * - a mail pool loads an item to a robot
     * - a robot moves to the next floor<br/>
     */
    void step();
}
