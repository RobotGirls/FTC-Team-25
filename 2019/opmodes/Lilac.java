package opmodes;

/**
 * FTC Team 25: Created by Bella Heinrichs on 10/23/2018.
 */

public class Lilac
{
    // Autonomous constants. (values not accurate)
    public static final int TICKS_PER_INCH = 106;
    public static final int TICKS_PER_DEGREE = 22;
    public final static double STRAIGHT_SPEED = 0.4;
    public final static double SIDEWAYS_DETACH_SPEED = 0.5;
    public final static double TURN_SPEED = 0.2;
    public final static int TURN_Multiplier = -1;

    private static double SERVO_DOMAIN = 256.0;

    // Latch Constants (MAY NEED LIMIT SWITCH)
    public static double LATCH_SPEED = 0.5;

    // Minerals Constants (IS IT NEEDED FOR CAMERA)
    public final static double MINERALS_SPEED = 1;
    public static double CAMERA_ROTATE = 150  / SERVO_DOMAIN; //?

    // Marker Constants (DEPENDING ON HOW DEPLOYED)
    public static double MARKER_OPEN  = 30 / SERVO_DOMAIN;
    public static double MARKER_CLOSED  = 356  / SERVO_DOMAIN;

}
