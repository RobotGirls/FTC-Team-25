package opmodes.stack;

import static org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive.BLOCK_PIXELS;
import static org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive.FLIP_DOWN;
import static org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive.FLIP_UP;
import static org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive.RELEASE_PIXELS;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;


// COORDINATE SYSTEM:
/*
audience-backdrop axis - X
red-blue axis - Y
 */

@Config
@Autonomous(name = "RR_BLUELEFT_NOSTACK")
public class RRAutoCSBlueLeftV2NOSTACK extends LinearOpMode {
    public static double DISTANCE = 30; // in

    private final double BLOCK_NOTHING = 0.05;

    private final double PROP_DIST = 10; // cm

    private final double PURPLE_RELEASE = 0.05;
    CenterstageSampleMecanumDrive drive;

    @Override
    public void runOpMode() throws InterruptedException {
        drive = new CenterstageSampleMecanumDrive(hardwareMap);
        Telemetry telemetry = new MultipleTelemetry(this.telemetry, FtcDashboard.getInstance().getTelemetry());

        // you can also cast this to a Rev2mDistanceSensor if you want to use added
        // methods associated with the Rev2mDistanceSensor class.
        Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor) drive.distanceSensor1;

        Pose2d startPose = new Pose2d(12, 60, Math.toRadians(90));

        drive.setPoseEstimate(startPose);

        TrajectorySequence toSpikes = drive.trajectorySequenceBuilder(startPose)
                // APPROACHING SPIKES
                .lineToLinearHeading(new Pose2d(12, 29, Math.toRadians(90)))
                .waitSeconds(0.5)
                .build();
        TrajectorySequence leftSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // LEFT SPIKE PATH
                .forward(3)
                .turn(Math.toRadians(90))
                .forward(-4)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(0.4)
                .forward(2)
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> {drive.box.setPosition(FLIP_UP);})
                .UNSTABLE_addTemporalMarkerOffset(0.6, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0.6, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                .lineToLinearHeading(new Pose2d(14,40,Math.toRadians(180)))
                .splineToConstantHeading(new Vector2d(55, 40), Math.toRadians(360))
                // * deploy yellow pixel
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_DOWN);})
                .forward(5)
                .strafeRight(30)
                .forward(-5)
                .build();
        TrajectorySequence centerSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // CENTER SPIKE PATH
                .forward(4)
                // * deploy purple pixel
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(0.5)
                .forward(8)
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> {drive.box.setPosition(FLIP_UP);})
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0.6, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                .lineToLinearHeading(new Pose2d(55, 36, Math.toRadians(180)))
                // * deploy yellow pixel
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_DOWN);})
                .waitSeconds(0.5)
                .forward(5)
                .strafeRight(32)
                //.strafeLeft()
                .forward(-5)
                .build();
        TrajectorySequence rightSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // RIGHT SPIKE PATH
                .forward(3.5)
                .turn(Math.toRadians(-90))
                .forward(-2)
                // * release purple pixel
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(0.5)
                .forward(3)
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> {drive.box.setPosition(FLIP_UP);})
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(0.5)
                .lineToLinearHeading(new Pose2d(55, 30.4, Math.toRadians(180)))
                .UNSTABLE_addTemporalMarkerOffset(0.4, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                // * deploy yellow pixel
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .waitSeconds(1)
                .forward(5)
                .strafeRight(32)
                //.strafeLeft(18)
                .forward(-5)
                .build();
        waitForStart();

        if (isStopRequested()) return;

        drive.followTrajectorySequence(toSpikes);

        if (detectProp() == "left") {
            drive.followTrajectorySequence(leftSpike);
            telemetry.addData("dist sensor 1 range", String.format("%.01f cm", drive.distanceSensor1.getDistance(DistanceUnit.CM)));
            telemetry.addData("dist sensor 2 range", String.format("%.01f cm", drive.distanceSensor2.getDistance(DistanceUnit.CM)));
        }
        else if (detectProp() == "center") {
            drive.followTrajectorySequence(centerSpike);
            telemetry.addData("dist sensor 1 range", String.format("%.01f cm", drive.distanceSensor1.getDistance(DistanceUnit.CM)));
            telemetry.addData("dist sensor 2 range", String.format("%.01f cm", drive.distanceSensor2.getDistance(DistanceUnit.CM)));
        }
        else {
            drive.followTrajectorySequence(rightSpike);
            telemetry.addData("dist sensor 1 range", String.format("%.01f cm", drive.distanceSensor1.getDistance(DistanceUnit.CM)));
            telemetry.addData("dist sensor 2 range", String.format("%.01f cm", drive.distanceSensor2.getDistance(DistanceUnit.CM)));
        }


        while (!isStopRequested() && opModeIsActive()) {

            // generic DistanceSensor methods.
            telemetry.addData("dist sensor 1 range", String.format("%.01f cm", drive.distanceSensor1.getDistance(DistanceUnit.CM)));
            telemetry.addData("dist sensor 2 range", String.format("%.01f cm", drive.distanceSensor2.getDistance(DistanceUnit.CM)));
            // Rev2mDistanceSensor specific methods.
            telemetry.addData("ID", String.format("%x", sensorTimeOfFlight.getModelID()));
            telemetry.addData("did time out", Boolean.toString(sensorTimeOfFlight.didTimeoutOccur()));

            telemetry.update();
        }
    }

    // detect which spike mark the team prop is on using the distance sensors
    public String detectProp() {
        if (drive.distanceSensor2.getDistance(DistanceUnit.CM) < PROP_DIST) {
            // prop is on the left spike
            return "left";
        } else if (drive.distanceSensor1.getDistance(DistanceUnit.CM) < PROP_DIST){
            return "right";
        } else {
            return "center";
        }
    }

}