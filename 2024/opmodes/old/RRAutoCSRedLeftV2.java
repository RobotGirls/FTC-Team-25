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
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

@Config
@Autonomous(name = "RR_REDLEFT")
@Disabled
public class RRAutoCSRedLeftV2 extends LinearOpMode {
    public static double DISTANCE = 30; // in

    private final double BLOCK_NOTHING = 0.05;
    private final double BLOCK_BOTH = 0.8;

    private final double PROP_DIST = 10; // cm

    private final double PURPLE_RELEASE = 0.05;
    CenterstageSampleMecanumDrive drive;

    private final double initialForward = -33;

    @Override
    public void runOpMode() throws InterruptedException {
        drive = new CenterstageSampleMecanumDrive(hardwareMap);
        Telemetry telemetry = new MultipleTelemetry(this.telemetry, FtcDashboard.getInstance().getTelemetry());

        // you can also cast this to a Rev2mDistanceSensor if you want to use added
        // methods associated with the Rev2mDistanceSensor class.
        Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor) drive.distanceSensor1;

        TrajectorySequence toSpikes = drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.toRadians(0)))
                // APPROACHING SPIKES
                //.forward(initialForward)   // going forward
                .lineToLinearHeading(new Pose2d(-30, 0, Math.toRadians(0)))
                .build();
        TrajectorySequence leftSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // LEFT SPIKE PATH
                .forward(2)
                .turn(Math.toRadians(90))
                .forward(-0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(0.5)
                .forward(2.5)
                .lineToLinearHeading(new Pose2d(-50, 11, Math.toRadians(90))) // strafe to center
                .lineToLinearHeading(new Pose2d(-50, 45, Math.toRadians(90))) // go across
                .lineToLinearHeading(new Pose2d(-24, 89, Math.toRadians(270))) //  --- constant name :
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(0.85, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_UP);})
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0.6, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .waitSeconds(1)
                .forward(2)
                .strafeRight(18)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_DOWN);})
                .build();
        TrajectorySequence centerSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // CENTER SPIKE PATH
                .forward(2.5)
                // * deploy purple pixel
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(0.5)
                .forward(8)
                .lineToLinearHeading(new Pose2d(-22, -15, Math.toRadians(0)))
                .lineToLinearHeading(new Pose2d(-52.5, -15, Math.toRadians(270)))
                //.turn(Math.toRadians(-90))
                .lineToLinearHeading(new Pose2d(-52, 59, Math.toRadians(270)))
                .lineToLinearHeading(new Pose2d(-15, 59, Math.toRadians(270)))
                .lineToLinearHeading(new Pose2d(-22, 83, Math.toRadians(270)))
                //.lineToLinearHeading(new Pose2d(-22, 83, Math.toRadians(270))) // diagonal to backdrop
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(0.85, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_UP);})
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .waitSeconds(1)
                .forward(3)
                .strafeLeft(16)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_DOWN);})
                .build();
        TrajectorySequence rightSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // RIGHT SPIKE PATH
                .forward(3.5)
                .turn(Math.toRadians(-90))
                // * release purple pixel
                .forward(-1.3)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(0.5)
                .forward(1.3)
                .strafeRight(27)
                .lineToLinearHeading(new Pose2d(-47, 55, Math.toRadians(270)))
                .lineToLinearHeading(new Pose2d(-34, 89, Math.toRadians(270)))
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(0.85, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(1.3)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_UP);})
                .waitSeconds(2)
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .forward(1)
                .waitSeconds(1)
                .forward(2)
                .strafeRight(32)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_DOWN);})
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
            telemetry.addData("deviceName", drive.distanceSensor1.getDeviceName() );
            telemetry.addData("range", String.format("%.01f mm", drive.distanceSensor1.getDistance(DistanceUnit.MM)));
            telemetry.addData("range", String.format("%.01f cm", drive.distanceSensor1.getDistance(DistanceUnit.CM)));
            telemetry.addData("range", String.format("%.01f m", drive.distanceSensor1.getDistance(DistanceUnit.METER)));
            telemetry.addData("range", String.format("%.01f in", drive.distanceSensor1.getDistance(DistanceUnit.INCH)));

            // Rev2mDistanceSensor specific methods.
            telemetry.addData("ID", String.format("%x", sensorTimeOfFlight.getModelID()));
            telemetry.addData("did time out", Boolean.toString(sensorTimeOfFlight.didTimeoutOccur()));

            telemetry.update();
        }
    }

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
