package opmodes;

/**
 * FTC Team 25: Created by Elizabeth Wu (updated by Breanna Chan) on 10/28/2017.
 */

public class VioletConstants
{
    // Autonomous constants.
    //public static final int TICKS_PER_INCH = 36;
    public static final int TICKS_PER_INCH = 350;
    public static final int TICKS_PER_DEGREE = 19;
    public final static double STRAIGHT_SPEED = 0.7;
    public final static double TURN_SPEED = 1;

    private static double SERVO_DOMAIN = 256.0;

    public static double JEWEL_UP     = 175 / SERVO_DOMAIN;
    public static double JEWEL_DOWN   = 35  / SERVO_DOMAIN;
    public static double S1_INIT      = 10  / SERVO_DOMAIN;
    public static double S2_INIT      = 255 / SERVO_DOMAIN;
    public static double S3_INIT      = 224 / SERVO_DOMAIN;
    public static double S4_INIT      = 10  / SERVO_DOMAIN;
    public static double RELIC_INIT   = 116 / SERVO_DOMAIN;
    //public static double S1_OPEN      = 70  / SERVO_DOMAIN;
    public static double S1_OPEN      = 102 / SERVO_DOMAIN;
    public static double S1_CLOSED    = 151 / SERVO_DOMAIN;
    //public static double S2_OPEN      = 172 / SERVO_DOMAIN;
    public static double S2_OPEN      = 166 / SERVO_DOMAIN;
    public static double S2_CLOSED    = 120 / SERVO_DOMAIN;
    //public static double S3_OPEN      = 155 / SERVO_DOMAIN;
    public static double S3_OPEN      = 121 / SERVO_DOMAIN;
    public static double S3_CLOSED    = 85  / SERVO_DOMAIN;
    //public static double S4_OPEN      = 65  / SERVO_DOMAIN;
    public static double S4_OPEN      = 92  / SERVO_DOMAIN;
    public static double S4_CLOSED    = 140 / SERVO_DOMAIN;
    public static double RELIC_OPEN   = 16  / SERVO_DOMAIN;
    public static double RELIC_CLOSED = 100 / SERVO_DOMAIN;
    public static double RELIC_ROTATE_DOWN = 0 / SERVO_DOMAIN;
    public static double RELIC_ROTATE_UP   = 240 / SERVO_DOMAIN;

    //public static int DEGREES_180_CLOCKWISE = 375;
    //public static int DEGREES_180_COUNTERCLOCKWISE = 400;
    public static int DEGREES_180 = 450;
    public static double ROTATE_POWER = 0.3;
    public static int NUDGE = 15;
    public static double NUDGE_POWER = 0.1;
    //public static int VERTICAL_MIN_HEIGHT = 700;
    public static int VERTICAL_MIN_HEIGHT = 1500;
    public static int CLAW_VERTICAL = 700;
    public static double CLAW_VERTICAL_POWER = 0.75;
    // put on actual teleop in order to be able to change power in certain instances (Bella)
    // Hesitant initial values for relic slides.
    public static double RELIC_CEILING = 0.3;
    public static int RELIC_HORIZONTAL = 100; //FIXME
    public static double RELIC_HORIZONTAL_POWER = 0.5;
}
