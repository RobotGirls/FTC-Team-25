package opmodes;

/*
 * FTC Team 25: cmacfarl, February 24, 2016
 */

public class NeverlandAutonomousConstants {
    public static final double SPEED_STRAIGHT = 0.45;
    public static final double SPEED_TURN = 0.152;
    public static final double SPEED_TARGET_LINE = 0.75;

    public static final int RED_TURN_MULTIPLIER = -1;
    public static final int BLUE_TURN_MULTIPLIER = 1;

    public static final int COMPENSATION_DELAY = 900;
    public static final int DELAY_BEFORE_START = 0000;

    // Distance and turn amounts.
    public static final int DISTANCE_FROM_BEACON = 21;
    public static final int DISTANCE_FROM_MOUNTAIN = 35;

    // Sensors.
    public static final int MOVING_AVG_SET_SIZE = 4;
    public static final float COLOR_THRESHOLD = 287;
}
