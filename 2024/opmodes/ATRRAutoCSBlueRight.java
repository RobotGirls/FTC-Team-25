package opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import team25core.GamepadTask;

@Config
@Autonomous(name = "ATRRAutoBlueRight")
public class ATRRAutoCSBlueRight extends LinearOpMode {
    public static double DISTANCE = 30; // in

    private final double BLOCK_NOTHING = 0.05;
    private final double BLOCK_BOTH = 0.8;

    private final double PROP_DIST = 10; // cm
    CenterstageSampleMecanumDrive drive;

    private AprilTagProcessor aprilTag;              // Used for managing the AprilTag detection process.
    private VisionPortal visionPortal;               // Used to manage the video source.
    private static final boolean USE_WEBCAM = true;  // Set true to use a webcam, or false for a phone camera
    private AprilTagDetection desiredTag = null;     // Used to hold the data for a detected AprilTag
    private int desiredTagID = -1;     // Choose the tag you want to approach or set to -1 for ANY tag.
    private static final int BLUE_LEFT_TAG_ID = 1;
    private static final int BLUE_MIDDLE_TAG_ID = 2;
    private static final int BLUE_RIGHT_TAG_ID = 3;

    private boolean foundTarget = false;

    double  myDrive           = 0;        // Desired forward power/speed (-1 to +1)
    double  strafe          = 0;        // Desired strafe power/speed (-1 to +1)
    double  turn            = 0;        // Desired turning power/speed (-1 to +1)
    boolean targetFound     = false;    // Set to true when an AprilTag target is detected
    final double SPEED_GAIN  =  0.02  ;   //  Forward Speed Control "Gain". eg: Ramp up to 50% power at a 25 inch error.   (0.50 / 25.0)
    final double STRAFE_GAIN =  0.015 ;   //  Strafe Speed Control "Gain".  eg: Ramp up to 25% power at a 25 degree Yaw error.   (0.25 / 25.0)
    final double TURN_GAIN   =  0.01  ;   //  Turn Control "Gain".  eg: Ramp up to 25% power at a 25 degree error. (0.25 / 25.0)

    final double MAX_AUTO_SPEED = 0.5;   //  Clip the approach speed to this max value (adjust for your robot)
    final double MAX_AUTO_STRAFE= 0.5;   //  Clip the approach speed to this max value (adjust for your robot)
    final double MAX_AUTO_TURN  = 0.3;   //  Clip the turn speed to this max value (adjust for your robot)
    final double DESIRED_DISTANCE = 4.0; //  this is how close the camera should get to the target (inches)


    @Override
    public void runOpMode() throws InterruptedException {
        drive = new CenterstageSampleMecanumDrive(hardwareMap);
        Telemetry telemetry = new MultipleTelemetry(this.telemetry, FtcDashboard.getInstance().getTelemetry());

        // you can also cast this to a Rev2mDistanceSensor if you want to use added
        // methods associated with the Rev2mDistanceSensor class.
        Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor) drive.distanceSensor1;

        // Initialize the Apriltag Detection process
        initAprilTag();
        //FIXME stalling
//        if (USE_WEBCAM)
//            setManualExposure(6, 250);  // Use low exposure time to reduce motion blur


        // FIXME update lift movement and test purple pixel

        TrajectorySequence toSpikes = drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.toRadians(0)))
                // APPROACHING SPIKES
                .forward(33)
                .build();
        TrajectorySequence leftSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // LEFT SPIKE PATH
                .forward(-2.5)
                .turn(Math.toRadians(90))
                // * deploy purple pixel
                .UNSTABLE_addTemporalMarkerOffset(2, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> {drive.intake.setPower(0.3);})
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.intake.setPower(-0.3);})
                .waitSeconds(3.2)
                .forward(-3)
                .strafeRight(21)
                .forward(50)
                .lineToLinearHeading(new Pose2d(38, 70, Math.toRadians(270)))
                .turn(Math.toRadians(30))                // * deploy yellow pixel
//                .UNSTABLE_addTemporalMarkerOffset(0.85, () -> {drive.linearLift.setPower(0);})
//                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
//                .waitSeconds(2)
//                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(0.9);})
//                .waitSeconds(2)
//                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> {drive.linearLift.setPower(0);})
//                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
//                .waitSeconds(2)
//                .UNSTABLE_addTemporalMarkerOffset(2, () -> {drive.pixelRelease.setPosition(0.95);})
//                .waitSeconds(1.5)
//                .forward(1)
//                .waitSeconds(2.5)
//                .forward(2)
                .build();
        TrajectorySequence centerSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // CENTER SPIKE PATH
                .forward(-6.5)
                // * deploy purple pixel
                .UNSTABLE_addTemporalMarkerOffset(2.25, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> {drive.intake.setPower(0.22);})
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.intake.setPower(-0.3);})
                .waitSeconds(3.4)
                .forward(-10)
                .strafeRight(15)
                .forward(36)
                .strafeLeft(70)
                .lineToLinearHeading(new Pose2d(38, 70, Math.toRadians(270)))
                .turn(Math.toRadians(30))                // * deploy yellow pixel
//                .UNSTABLE_addTemporalMarkerOffset(0.85, () -> {drive.linearLift.setPower(0);})
//                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
//                .waitSeconds(2)
//                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(0.9);})
//                .waitSeconds(2)
//                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> {drive.linearLift.setPower(0);})
//                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
//                .waitSeconds(2)
//                .UNSTABLE_addTemporalMarkerOffset(2, () -> {drive.pixelRelease.setPosition(0.95);})
//                .forward(1)
//                .waitSeconds(2.5)
//                .forward(3)
//                .strafeRight(15)
                .build();
        TrajectorySequence rightSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // RIGHT SPIKE PATH
                .forward(-3)
                .turn(Math.toRadians(-90))
                .UNSTABLE_addTemporalMarkerOffset(3, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(1.7, () -> {drive.intake.setPower(0.2);})
                .UNSTABLE_addTemporalMarkerOffset(0.9, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.intake.setPower(-0.26);})
                .waitSeconds(3.9)
                .forward(-0.3)
                .strafeLeft(18)
                .forward(-50)
                .lineToLinearHeading(new Pose2d(38, 70, Math.toRadians(270)))
                .turn(Math.toRadians(30))
                // * deploy yellow pixel
//                .UNSTABLE_addTemporalMarkerOffset(0.85, () -> {drive.linearLift.setPower(0);})
//                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
//                .waitSeconds(1.5)
//                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(0.9);})
//                .waitSeconds(2)
//                .UNSTABLE_addTemporalMarkerOffset(0.6, () -> {drive.linearLift.setPower(0);})
//                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
//                .waitSeconds(1)
//                .UNSTABLE_addTemporalMarkerOffset(2, () -> {drive.pixelRelease.setPosition(0.95);})
//                .waitSeconds(2.2)
//                .forward(2)
                .build();

        waitForStart();

        if (isStopRequested()) return;

        drive.followTrajectorySequence(toSpikes);


        if (detectProp() == "left") {
            drive.followTrajectorySequence(leftSpike);
        }
        else if (detectProp() == "center") {
            drive.followTrajectorySequence(centerSpike);
        }
        else {
            drive.followTrajectorySequence(rightSpike);
        }


        while (!isStopRequested() && opModeIsActive()) {
            // generic DistanceSensor methods.
//            telemetry.addData("deviceName", drive.distanceSensor1.getDeviceName() );
//            telemetry.addData("range", String.format("%.01f mm", drive.distanceSensor1.getDistance(DistanceUnit.MM)));
//            telemetry.addData("range", String.format("%.01f cm", drive.distanceSensor1.getDistance(DistanceUnit.CM)));
//            telemetry.addData("range", String.format("%.01f m", drive.distanceSensor1.getDistance(DistanceUnit.METER)));
//            telemetry.addData("range", String.format("%.01f in", drive.distanceSensor1.getDistance(DistanceUnit.INCH)));

            // Rev2mDistanceSensor specific methods.
//            telemetry.addData("ID", String.format("%x", sensorTimeOfFlight.getModelID()));
//            telemetry.addData("did time out", Boolean.toString(sensorTimeOfFlight.didTimeoutOccur()));

//            telemetry.update();

            foundTarget = findAprilTag(desiredTagID);
            driveToAprilTag(foundTarget);
            telemetry.update();
        }
    }

    public String detectProp() {
        if (drive.distanceSensor1.getDistance(DistanceUnit.CM) < PROP_DIST) {
            desiredTagID = BLUE_LEFT_TAG_ID;
            // prop is on the left spike
            return "left";
        } else if (drive.distanceSensor2.getDistance(DistanceUnit.CM) < PROP_DIST){
            desiredTagID = BLUE_RIGHT_TAG_ID;
            return "right";
        } else {
            desiredTagID = BLUE_MIDDLE_TAG_ID;
            return "center";
        }
    }

    /**
     * Initialize the AprilTag processor.
     */
    private void initAprilTag() {
        // Create the AprilTag processor by using a builder.
        aprilTag = new AprilTagProcessor.Builder().build();

        // Adjust Image Decimation to trade-off detection-range for detection-rate.
        // eg: Some typical detection data using a Logitech C920 WebCam
        // Decimation = 1 ..  Detect 2" Tag from 10 feet away at 10 Frames per second
        // Decimation = 2 ..  Detect 2" Tag from 6  feet away at 22 Frames per second
        // Decimation = 3 ..  Detect 2" Tag from 4  feet away at 30 Frames Per Second
        // Decimation = 3 ..  Detect 5" Tag from 10 feet away at 30 Frames Per Second
        // Note: Decimation can be changed on-the-fly to adapt during a match.
        aprilTag.setDecimation(2);

        // Create the vision portal by using a builder.
        if (USE_WEBCAM) {
            visionPortal = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                    .addProcessor(aprilTag)
                    .build();
        } else {
            visionPortal = new VisionPortal.Builder()
                    .setCamera(BuiltinCameraDirection.BACK)
                    .addProcessor(aprilTag)
                    .build();
        }
    }

    /*
    Manually set the camera gain and exposure.
    This can only be called AFTER calling initAprilTag(), and only works for Webcams;
   */
    private void setManualExposure(int exposureMS, int gain) {
        // Wait for the camera to be open, then use the controls

        if (visionPortal == null) {
            return;
        }

        // Make sure camera is streaming before we try to set the exposure controls
        if (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            telemetry.addData("Camera", "Waiting");
            telemetry.update();
            while (!isStopRequested() && (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING)) {
                sleep(20);
            }
            telemetry.addData("Camera", "Ready");
            telemetry.update();
        }

        // Set camera controls unless we are stopping.
        if (!isStopRequested())
        {
            ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
            if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
                exposureControl.setMode(ExposureControl.Mode.Manual);
                sleep(50);
            }
            exposureControl.setExposure((long)exposureMS, TimeUnit.MILLISECONDS);
            sleep(20);
            GainControl gainControl = visionPortal.getCameraControl(GainControl.class);
            gainControl.setGain(gain);
            sleep(20);
        }
    }

    public boolean findAprilTag(int myDesiredTagID) {

        targetFound = false;
        desiredTag  = null;

        // Step through the list of detected tags and look for a matching tag
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        for (AprilTagDetection detection : currentDetections) {
            // Look to see if we have size info on this tag.
            if (detection.metadata != null) {
                //  Check to see if we want to track towards this tag.
                if ((myDesiredTagID < 0) || (detection.id == myDesiredTagID)) {
                    // Yes, we want to use this tag.
                    targetFound = true;
                    desiredTag = detection;
                    break;  // don't look any further.
                } else {
                    // This tag is in the library, but we do not want to track it right now.
                    telemetry.addData("Skipping", "Tag ID %d is not desired", detection.id);
                }
            } else {
                // This tag is NOT in the library, so we don't have enough information to track to it.
                telemetry.addData("Unknown", "Tag ID %d is not in TagLibrary", detection.id);
            }
        }

        // Tell the driver what we see, and what to do.
        if (targetFound) {
            telemetry.addData("\n>","HOLD Left-Bumper to Drive to Target\n");
            telemetry.addData("Found", "ID %d (%s)", desiredTag.id, desiredTag.metadata.name);
            telemetry.addData("Range",  "%5.1f inches", desiredTag.ftcPose.range);
            telemetry.addData("Bearing","%3.0f degrees", desiredTag.ftcPose.bearing);
            telemetry.addData("Yaw","%3.0f degrees", desiredTag.ftcPose.yaw);
        } else {
            telemetry.addData("\n>","Drive using joysticks to find valid target\n");
        }

        return targetFound;
    }

    public void driveToAprilTag(boolean myTargetFound) {
        // If Left Bumper is being pressed, AND we have found the desired target, Drive to target Automatically .
        if (gamepad1.left_bumper && targetFound) {

            // Determine heading, range and Yaw (tag image rotation) error so we can use them to control the robot automatically.
            double  rangeError      = (desiredTag.ftcPose.range - DESIRED_DISTANCE);
            double  headingError    = desiredTag.ftcPose.bearing;
            double  yawError        = desiredTag.ftcPose.yaw;

            // Use the speed and turn "gains" to calculate how we want the robot to move.
            myDrive  = Range.clip(rangeError * SPEED_GAIN, -MAX_AUTO_SPEED, MAX_AUTO_SPEED);
            turn   = Range.clip(headingError * TURN_GAIN, -MAX_AUTO_TURN, MAX_AUTO_TURN) ;
            strafe = Range.clip(-yawError * STRAFE_GAIN, -MAX_AUTO_STRAFE, MAX_AUTO_STRAFE);

            telemetry.addData("Auto","Drive %5.2f, Strafe %5.2f, Turn %5.2f ", myDrive, strafe, turn);
        } else {

            // drive using manual POV Joystick mode.  Slow things down to make the robot more controlable.
            myDrive  = -gamepad1.left_stick_y  / 2.0;  // Reduce drive rate to 50%.
            strafe = -gamepad1.left_stick_x  / 2.0;  // Reduce strafe rate to 50%.
            turn   = -gamepad1.right_stick_x / 3.0;  // Reduce turn rate to 33%.
            telemetry.addData("Manual","Drive %5.2f, Strafe %5.2f, Turn %5.2f ", myDrive, strafe, turn);
        }
        telemetry.update();

        // Apply desired axes motions to the drivetrain.
        moveRobot(myDrive, strafe, turn);
//        sleep(10);
    }

    /**
     * Move robot according to desired axes motions
     * <p>
     * Positive X is forward
     * <p>
     * Positive Y is strafe left
     * <p>
     * Positive Yaw is counter-clockwise
     */
    public void moveRobot(double x, double y, double yaw) {
        // Calculate wheel powers.
        double leftFrontPower    =  x -y -yaw;
        double rightFrontPower   =  x +y +yaw;
        double leftBackPower     =  x +y -yaw;
        double rightBackPower    =  x -y +yaw;

        // Normalize wheel powers to be less than 1.0
        double max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
        max = Math.max(max, Math.abs(leftBackPower));
        max = Math.max(max, Math.abs(rightBackPower));

        if (max > 1.0) {
            leftFrontPower /= max;
            rightFrontPower /= max;
            leftBackPower /= max;
            rightBackPower /= max;
        }

        // Send powers to the wheels.
        drive.setMotorPowers(leftFrontPower, leftBackPower, rightBackPower, rightFrontPower);
//        leftFrontDrive.setPower(leftFrontPower);
//        rightFrontDrive.setPower(rightFrontPower);
//        leftBackDrive.setPower(leftBackPower);
//        rightBackDrive.setPower(rightBackPower);
    }

}
