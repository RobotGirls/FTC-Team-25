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

@Config
@Autonomous(name = "RRAutoRedLeft")
public class RRAutoCSRedLeft extends LinearOpMode {
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

        // FIXME update lift movement and test purple pixel, test left spike
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
                .forward(-1)
                .strafeRight(25)
                .forward(-60)
                .lineToLinearHeading(new Pose2d(25, -86.5, Math.toRadians(90))) // x 26
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(0.6, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(2)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(0.9);})
                .waitSeconds(2)
                .UNSTABLE_addTemporalMarkerOffset(2, () -> {drive.pixelRelease.setPosition(0.95);})
                .waitSeconds(1.5)
                .forward(1)
                .waitSeconds(2.5)
                .forward(2)
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
                .strafeLeft(15)
                .forward(40)
                .strafeRight(75)
                .lineToLinearHeading(new Pose2d(24.5, -86.5, Math.toRadians(90))) // x 26
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(0.6, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(2)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(0.9);})
                .waitSeconds(2)
                .UNSTABLE_addTemporalMarkerOffset(2, () -> {drive.pixelRelease.setPosition(0.95);})
                .forward(1)
                .waitSeconds(2.5)
                .forward(3)
                .strafeRight(15)
                .build();
        TrajectorySequence rightSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // RIGHT SPIKE PATH
                .forward(-4)
                .turn(Math.toRadians(-90))
                .UNSTABLE_addTemporalMarkerOffset(2.9, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(1.7, () -> {drive.intake.setPower(0.2);})
                .UNSTABLE_addTemporalMarkerOffset(0.9, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.intake.setPower(-0.26);})
                .waitSeconds(3.9)
                .forward(-0.8)
                .strafeLeft(26)
                .forward(60)
                .lineToLinearHeading(new Pose2d(23.5, -86.5, Math.toRadians(90)))
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(0.6, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(2)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(0.9);})
                .waitSeconds(2)
                .UNSTABLE_addTemporalMarkerOffset(2, () -> {drive.pixelRelease.setPosition(0.95);})
                .forward(1)
                .waitSeconds(2.5)
                .forward(2)
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
