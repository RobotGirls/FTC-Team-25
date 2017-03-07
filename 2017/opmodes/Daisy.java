package opmodes;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/12/2016.
 */

public class Daisy
{
    // Autonomous constants.
    public static final int TICKS_PER_INCH = 60;
    public static final int TICKS_PER_DEGREE = 19;
    public final static double STRAIGHT_SPEED = 0.9;
    public final static double TURN_SPEED = 0.6;
    public final static double ODS_MIN = 0.5;
    public final static double ODS_MAX = 4;
    public final static int LAUNCH_POSITION = 1500;
    public final static int RANGE_PORT = 2;
    public final static int COLOR_PORT = 1;
    public final static int COLOR_THRESHOLD = 278;
    public final static int COLOR_MS_DELAY = 700;
    public final static int RED_THRESHOLD = 1300;
    public final static int BLUE_THRESHOLD = 1700;

    // Button pushers.
    public final static double LEFT_DEPLOY_POS = 1;
    public final static double RIGHT_DEPLOY_POS = 1;
    public final static double LEFT_STOW_POS = 0.4;
    public final static double RIGHT_STOW_POS = 0.3;

    // Vuforia constants.
    public final static VuforiaLocalizer.CameraDirection CAMERA_CHOICE = VuforiaLocalizer.CameraDirection.FRONT;
    public final static String KEY = "Afbu2Uv/////AAAAGVouNdSAD0P8la+sq37vCdQ6uLVH8NWrBLnfZ1R5rObJQpVVHJzqvIgMZO5gTqXG6DYJZcgwtSVZXU2g20FAJobxCog9Wc5vtqgJJmrsJ0NOABRbi9vy4Y9IzBVfaDoRsQTmjxxFf62Z9slttsb44KopGpVGTQ83iHnTo/wDvnZBWRhmckG6IKuqkbRYCFD+w1hHvVLuDoIYLgfpa1Rw1Pc7rszP/CDzUfeO9KwodFpEsfZHIZI8KHIYzfRIOhg1Tg0T4eRsLCO8s9vfZd6vfTuUA/sZkID3N7BsrlLaL6vUqheGPvsbPuQQsMqgPNYTqbhvv3KI/SR5WxUaccuVHnpVMhAjkdpruWVliCCZqp1t";
    public final static int CAMERA_FORWARD_DISPLACEMENT  = 0;       // Camera is 0 mm in front of robot center
    public final static int CAMERA_VERTICAL_DISPLACEMENT = 127;     // Camera is 127 mm above ground
    public final static int CAMERA_LEFT_DISPLACEMENT     = 64;      // Camera is 64 mm off the robots center line
    public final static int CAMERA_BLUE_LEFT_DISPLACEMENT = 110;    // What's this?
    public final static OpenGLMatrix PHONE_LOCATION_ON_ROBOT = OpenGLMatrix
                        .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                        .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.YZX, AngleUnit.DEGREES, CAMERA_CHOICE == VuforiaLocalizer.CameraDirection.FRONT ? 90 : -90, 0, 0));
    public final static OpenGLMatrix BLUE_PHONE_LOCATION_ON_ROBOT = OpenGLMatrix
                        .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_BLUE_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                        .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.YZX, AngleUnit.DEGREES, CAMERA_CHOICE == VuforiaLocalizer.CameraDirection.FRONT ? 90 : -90, 0, 0));
}
