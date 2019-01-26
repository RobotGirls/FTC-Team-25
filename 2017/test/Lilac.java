package test;

/**
 * FTC Team 25: Created by Bella Heinrichs on 10/23/2018.
 */

public class Lilac
{
    // Autonomous constants. (values not accurate)
    public static final int TICKS_PER_INCH = 60;
    public static final int TICKS_PER_DEGREE = 19;
    public final static double STRAIGHT_SPEED = 1;
    public final static double TURN_SPEED = 1;
    public final static int turnMultiplier = -1;

    private static double SERVO_DOMAIN = 256.0;

    // Latch Constants (MAY NEED LIMIT SWITCH)
    public static double LATCH_POWER = 0.1;

    // Minerals Constants (IS IT NEEDED FOR CAMERA)
    public final static double MINERALS_SPEED = 1;
    public static double CAMERA_ROTATE = 150  / SERVO_DOMAIN; //?

    // Marker Constants (DEPENDING ON HOW DEPLOYED)
    public static double MARKER_OPEN  = 10  / SERVO_DOMAIN;
    public static double MARKER_CLOSED  = 10  / SERVO_DOMAIN;

}
