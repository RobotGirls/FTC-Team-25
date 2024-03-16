package opmodes;

import static org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive.FLIP_DOWN;
import static org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive.FLIP_UP;
import static org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive.RELEASE_PIXELS;

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

@Config
@Autonomous(name = "RR_REDRIGHT")
public class RRAutoCSRedRightV2 extends LinearOpMode {
    public static double DISTANCE = 30; // in

    private final double BLOCK_NOTHING = 0.05;
    private final double BLOCK_BOTH = 0.8;

    private final double PURPLE_RELEASE = 0.05;

    private final double PROP_DIST = 10; // cm
    CenterstageSampleMecanumDrive drive;

    private AprilTagProcessor aprilTag;              // Used for managing the AprilTag detection process.
    private VisionPortal visionPortal;               // Used to manage the video source.
    private static final boolean USE_WEBCAM = true;  // Set true to use a webcam, or false for a phone camera
    private AprilTagDetection desiredTag = null;     // Used to hold the data for a detected AprilTag
    private int desiredTagID = -1;     // Choose the tag you want to approach or set to -1 for ANY tag.
    private static final int RED_LEFT_TAG_ID = 4;
    private static final int RED_MIDDLE_TAG_ID = 5;
    private static final int RED_RIGHT_TAG_ID = 6;

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
        Rev2mDistanceSensor sensorTimeOfFlight2 = (Rev2mDistanceSensor) drive.distanceSensor2;

        TrajectorySequence toSpikes = drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.toRadians(0)))
                // APPROACHING SPIKES
                .lineToLinearHeading(new Pose2d(-29, 0, Math.toRadians(0)))
                .build();
        TrajectorySequence leftSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // LEFT SPIKE PATH
                .forward(2)
                .turn(Math.toRadians(90))
                .forward(-0.5)
                // * deploy purple pixel
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(1)
                .forward(3)
                .lineToLinearHeading(new Pose2d(-30, 41, Math.toRadians(270))) // x 26
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(0.85, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_UP);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0.45, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .forward(3)
                .waitSeconds(1)
                .forward(3)
                .strafeLeft(27)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_DOWN);})
                .build();
        TrajectorySequence centerSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // CENTER SPIKE PATH
                // * deploy purple pixel
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(1)
                .forward(6)
                .UNSTABLE_addTemporalMarkerOffset(0.85, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .lineToLinearHeading(new Pose2d(-24.5, 40, Math.toRadians(270))) // x 26
                // * deploy yellow pixel
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_UP);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0.45, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .forward(2)
                .waitSeconds(1)
                .forward(2)
                .strafeLeft(20)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_DOWN);})
                .build();
        TrajectorySequence rightSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // RIGHT SPIKE PATH
                // back up from spike, outtake pixel, push
                // pixel onto spike
                .forward(2)
                .turn(Math.toRadians(-90))
                .forward(-0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(0.5)
                .forward(4)
                .lineToLinearHeading(new Pose2d(-13, -2, Math.toRadians(270)))
                .lineToLinearHeading(new Pose2d(-19, 41, Math.toRadians(270)))
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(0.9, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_UP);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0.45, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .waitSeconds(0.5)
                .forward(5)
                .strafeLeft(20)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_DOWN);})
                .build();


        waitForStart();
        telemetry.addData("location: ", "started program");
        telemetry.update();
        if (isStopRequested()) return;
        telemetry.addData("location: ", "starting toSpikes");
        telemetry.update();
        drive.followTrajectorySequence(toSpikes);
        telemetry.addData("location: ", "approached spikes");

        telemetry.update();

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
            telemetry.addData("deviceName", drive.distanceSensor1.getDeviceName() );
            telemetry.addData("range", String.format("%.01f in", drive.distanceSensor1.getDistance(DistanceUnit.INCH)));

            // Rev2mDistanceSensor specific methods.
            telemetry.addData("ID 1", String.format("%x", sensorTimeOfFlight.getModelID()));
            telemetry.addData("did time out", Boolean.toString(sensorTimeOfFlight.didTimeoutOccur()));

            telemetry.addData("ID 2", String.format("%x", sensorTimeOfFlight2.getModelID()));

            telemetry.addData("prop position", detectProp());
/*
            Pose2d poseEstimate = drive.getPoseEstimate();
            telemetry.addData("x", poseEstimate.getX());
            telemetry.addData("y", poseEstimate.getY());
            telemetry.addData("heading", poseEstimate.getHeading());

*/

            telemetry.update();
        }
    }

    public String detectProp() {
        if (drive.distanceSensor2.getDistance(DistanceUnit.CM) < PROP_DIST) {
            // prop is on the left spike
            desiredTagID = RED_LEFT_TAG_ID;
            return "left";
        } else if (drive.distanceSensor1.getDistance(DistanceUnit.CM) < PROP_DIST){
            desiredTagID = RED_RIGHT_TAG_ID;
            return "right";
        } else {
            desiredTagID = RED_MIDDLE_TAG_ID;
            return "center";
        }
    }
}