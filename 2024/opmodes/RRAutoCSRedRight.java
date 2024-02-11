package opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

@Config
@Autonomous(name = "RRAutoBlueLeft")
public class RRAutoCSBlueLeft extends LinearOpMode {
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

        TrajectorySequence toSpikes = drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.toRadians(270)))
                // APPROACHING SPIKES
                .forward(26)
                .build();
        TrajectorySequence leftSpike = drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.toRadians(270)))
                // LEFT SPIKE PATH
                .turn(Math.toRadians(90))
                // * deploy purple pixel
                .addDisplacementMarker(() -> {
                    // release purple pixel
                    //drive.intake.setTargetPosition(80);
                    //drive.intake.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    drive.intake.setPower(0.2);
                })
                .forward(-3)
                .addDisplacementMarker(() -> {
                    drive.intake.setPower(0);
                })
                .strafeLeft(25)
                .lineToLinearHeading(new Pose2d(50, 36, Math.toRadians(180)))
                // * deploy yellow pixel
                .addDisplacementMarker(() -> {
                    //drive.linearLift.setTargetPosition(30);
                    //drive.linearLift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    drive.linearLift.setPower(0.6);
                    try {
                        wait(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    drive.linearLift.setPower(0);
                    drive.box.setPosition(0.9);
                    drive.pixelRelease.setPosition(0.7);
                })
                .build();
        TrajectorySequence centerSpike = drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.toRadians(270)))
                // CENTER SPIKE PATH
                .forward(26)
                // * deploy purple pixel
                .forward(-25)
                .lineToLinearHeading(new Pose2d(50, 36, Math.toRadians(180)))
                // * deploy yellow pixel
                .build();
        TrajectorySequence rightSpike = drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.toRadians(270)))
                // RIGHT SPIKE PATH
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
