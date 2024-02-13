package opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

import java.util.Queue;

@Config
@Autonomous(name = "RRAutoRedRight")
public class RRAutoCSRedRight extends LinearOpMode {
    public static double DISTANCE = 30; // in

    private final double BLOCK_NOTHING = 0.05;
    private final double BLOCK_BOTH = 0.8;

    private final double PROP_DIST = 10; // cm
    CenterstageSampleMecanumDrive drive;


    @Override
    public void runOpMode() throws InterruptedException {
        drive = new CenterstageSampleMecanumDrive(hardwareMap);
        Telemetry telemetry = new MultipleTelemetry(this.telemetry, FtcDashboard.getInstance().getTelemetry());

        // you can also cast this to a Rev2mDistanceSensor if you want to use added
        // methods associated with the Rev2mDistanceSensor class.
        Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor) drive.distanceSensor1;

        TrajectorySequence toSpikes = drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.toRadians(90)))
                // APPROACHING SPIKES
                .forward(32)
                .build();
        TrajectorySequence leftSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // LEFT SPIKE PATH
                .turn(Math.toRadians(90))
                // * deploy purple pixel
                .UNSTABLE_addTemporalMarkerOffset(1, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.intake.setPower(0.2);})
                .waitSeconds(1)
                .forward(-3)
                .strafeLeft(25)
                .lineToLinearHeading(new Pose2d(50, -33, Math.toRadians(180)))
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(1.5, () -> {drive.linearLift.setPower(0);})
                .addDisplacementMarker(() -> {
                    drive.linearLift.setPower(0.6);
                    drive.box.setPosition(0.9);
                    drive.pixelRelease.setPosition(0.7);
                })
                .build();
        TrajectorySequence centerSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // CENTER SPIKE PATH
                .forward(-6)
                // * deploy purple pixel
                .UNSTABLE_addTemporalMarkerOffset(1, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.intake.setPower(0.2);})
                .waitSeconds(1)
                .forward(-20)
                .lineToLinearHeading(new Pose2d(50, -33, Math.toRadians(180)))
                // * deploy yellow pixel
                .build();
        TrajectorySequence rightSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // RIGHT SPIKE PATH
                .forward(26)
                .turn(Math.toRadians(-90))
                .forward(-3)
                .lineToLinearHeading(new Pose2d(50, 36, Math.toRadians(180)))
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
            telemetry.addData("ID", String.format("%x", sensorTimeOfFlight.getModelID()));
            telemetry.addData("did time out", Boolean.toString(sensorTimeOfFlight.didTimeoutOccur()));

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
        if (drive.distanceSensor1.getDistance(DistanceUnit.CM) < PROP_DIST) {
            // prop is on the left spike
            return "left";
        } else if (drive.distanceSensor2.getDistance(DistanceUnit.CM) < PROP_DIST){
            return "right";
        } else {
            return "center";
        }
    }
}
