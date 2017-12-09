package opmodes;

/**
 * FTC Team 25: Created by Elizabeth Wu (updated by Breanna Chan) on 10/28/2017.
 */

public class VioletConstants
{
    // Autonomous constants.
    public static final int TICKS_PER_INCH = 36;
    public static final int TICKS_PER_DEGREE = 19;
    public final static double STRAIGHT_SPEED = 0.7;
    public final static double TURN_SPEED = 1;
    public final static int turnMultiplier = -1;
    public final static int COLOR_PORT = 1;
    public final static int COLOR_THRESHOLD = 278;
    public final static int RED_THRESHOLD = 1300;
    public final static int BLUE_THRESHOLD = 1700;

    private static double SERVO_DOMAIN = 256.0;

    public static double S1_OPEN      = 10  / SERVO_DOMAIN;
    public static double S1_CLOSED    = 150  / SERVO_DOMAIN;
    public static double S2_OPEN      = 255 / SERVO_DOMAIN;
    public static double S2_CLOSED    = 120 / SERVO_DOMAIN;
    public static double S3_OPEN      = 224  / SERVO_DOMAIN;
    public static double S3_CLOSED    = 85  / SERVO_DOMAIN;
    public static double S4_OPEN      = 10 / SERVO_DOMAIN;
    public static double S4_CLOSED    = 140  / SERVO_DOMAIN;
    public static double JEWEL_INIT   = 145 / SERVO_DOMAIN;
    // Hesitant initial values. Will have to calibrate servo later.
    public static double RELIC_OPEN   = 100 / SERVO_DOMAIN;
    public static double RELIC_CLOSED = 170 / SERVO_DOMAIN;
    public static double RELIC_ROTATE_DOWN = 128 / SERVO_DOMAIN;
    public static double RELIC_ROTATE_UP = 135 / SERVO_DOMAIN;

    //public static int DEGREES_180_CLOCKWISE = 375;
    //public static int DEGREES_180_COUNTERCLOCKWISE = 400;
    public static int DEGREES_180 = 450;
    public static double ROTATE_POWER = 0.3;
    public static int NUDGE = 15;
    public static double NUDGE_POWER = 0.1;
    public static int CLAW_VERTICAL = 400;
    public static double CLAW_VERTICAL_POWER = 0.75;
    // put on actual teleop in order to be able to change power in certain instances (Bella)
    // Hesitant initial values for relic slides.
    public static int RELIC_HORIZONTAL = 400;
    public static double RELIC_HORIZONTAL_POWER = 0.75;
}
