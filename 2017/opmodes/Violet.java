package opmodes;

/**
 * FTC Team 25: Created by Elizabeth Wu on 10/28/17.
 */

public class Violet
{

    // Autonomous constants.
    public static final int TICKS_PER_INCH = 36;
    public static final int TICKS_PER_DEGREE = 10;
    public final static double STRAIGHT_SPEED = 0.7;
    public final static double TURN_SPEED = 0.5;
    public final static int turnMultiplier = -1;
    public final static int COLOR_PORT = 1;
    public final static int COLOR_THRESHOLD = 278;
    public final static int RED_THRESHOLD = 1300;
    public final static int BLUE_THRESHOLD = 1700;

    private static double SERVO_DOMAIN = 256.0;

    public static double S1_OPEN = 10 / SERVO_DOMAIN;
    public static double S2_OPEN = 255 / SERVO_DOMAIN;
    public static double S3_OPEN = 224 / SERVO_DOMAIN;
    public static double S4_OPEN = 10 / SERVO_DOMAIN;
    public static double S1_CLOSED = 150 / SERVO_DOMAIN;
    public static double S2_CLOSED = 120 / SERVO_DOMAIN;
    public static double S3_CLOSED = 85 / SERVO_DOMAIN;
    public static double S4_CLOSED = 140 / SERVO_DOMAIN;

    public static int DEGREES_180 = 375;
    public static int NUDGE = 15;
    public static int CLAW_VERTICAL = 400;
    public static double NUDGE_POWER = 0.1;
    public static double ROTATE_POWER = 0.3;
    public static double CLAW_VERTICAL_POWER = 0.5;



}
