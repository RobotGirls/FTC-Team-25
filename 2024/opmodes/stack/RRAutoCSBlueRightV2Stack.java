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

@Config
@Autonomous(name = "RR_BLUERIGHT_WORLDS")
public class RRAutoCSBlueRightV2Stack extends LinearOpMode {
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



        Pose2d startPose = new Pose2d(-35, 58, Math.toRadians(90));
        drive.setPoseEstimate(startPose);

        TrajectorySequence toSpikes = drive.trajectorySequenceBuilder(startPose)
                // APPROACHING SPIKES
                //.forward(initialForward)   // going forward
                .lineToLinearHeading(new Pose2d(-35,27, Math.toRadians(90)))
                .build();
        TrajectorySequence leftSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // LEFT SPIKE PATH
                .forward(2)
                .turn(Math.toRadians(90))
                .forward(-3)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(0.5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    drive.linkage.setPosition(0.47);
                    drive.pixelRelease.setPosition(BLOCK_PIXELS);
                })
                .lineToLinearHeading(new Pose2d(-53, 34, Math.toRadians(180)))
                // intake and check for color change from black to yellow
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    while (drive.colorSensor.red()<300 && drive.colorSensor.green()<400 && drive.colorSensor.blue()<300) {
                        // color is black --> intake
                        drive.intake.setPower(-0.9);
                    }
                    // after color is not black (meaning it's yellow), stop intaking
                    drive.intake.setPower(0);})
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(3, () -> {
                    drive.intake.setPower(0);
                })
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    drive.intake.setPower(0.9);
                })
                .UNSTABLE_addTemporalMarkerOffset(1, () -> {drive.intake.setPower(0.9);})
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(4, () -> {drive.intake.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.intake.setPower(-0.9);})
                .setTangent(0)
                .splineToConstantHeading(new Vector2d(-34,55),Math.toRadians(0))
                .lineToLinearHeading(new Pose2d(25,55,Math.toRadians(180)))
                .UNSTABLE_addTemporalMarkerOffset(0.8, () -> {drive.box.setPosition(FLIP_UP);})
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .setTangent(0)
                .splineToConstantHeading(new Vector2d(55, 38), Math.toRadians(0))
                .waitSeconds(3)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .waitSeconds(0.4)
                .forward(3)
                .strafeRight(18)
                .build();
        TrajectorySequence centerSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // CENTER SPIKE PATH
                .forward(4)
                // * deploy purple pixel
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(0.3)
                .forward(5)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linkage.setPosition(0.475);})
                .lineToLinearHeading(new Pose2d(-55, 33, Math.toRadians(180)))
                // * INTAKE FROM STACK
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    while (drive.colorSensor.red()<300 && drive.colorSensor.green()<400 && drive.colorSensor.blue()<300) {
                        // color is black --> intake
                        drive.intake.setPower(-0.9);
                    }
                    // after color is not black (meaning it's yellow), stop intaking
                    drive.intake.setPower(0);
                })
                .waitSeconds(2)
                // outtake while driving backwards
                .UNSTABLE_addTemporalMarkerOffset(3, () -> {
                    drive.intake.setPower(0);
                })
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    drive.intake.setPower(0.9);
                })
                .lineToLinearHeading(new Pose2d(-37,57,Math.toRadians(180)))
                .lineToLinearHeading(new Pose2d(35,57,Math.toRadians(180)))
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(0.85, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_UP);})
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                .setTangent(0)
                .splineToConstantHeading(new Vector2d(56, 38), Math.toRadians(0))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .waitSeconds(1)
                .forward(1)
                .waitSeconds(0.5)
                .forward(2)
                .strafeRight(22)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_DOWN);})
                .build();
        TrajectorySequence rightSpike = drive.trajectorySequenceBuilder(toSpikes.end())
                // RIGHT SPIKE PATH
                .lineToLinearHeading(new Pose2d(-52,20,Math.toRadians(180)))
                .lineToLinearHeading(new Pose2d(-60,36,Math.toRadians(180)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    while (drive.colorSensor.red()<300 && drive.colorSensor.green()<400 && drive.colorSensor.blue()<300) {
                        // color is black --> intake
                        drive.intake.setPower(-0.9);
                    }
                    // after color is not black (meaning it's yellow), stop intaking
                    drive.intake.setPower(0);
                })
                .waitSeconds(0.5)
                // outtake while driving backwards
                .UNSTABLE_addTemporalMarkerOffset(3, () -> {
                    drive.intake.setPower(0);
                })
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    drive.intake.setPower(0.9);
                })
                .lineToLinearHeading(new Pose2d(-55,33,Math.toRadians(180)))
                .waitSeconds(1)
                .UNSTABLE_addTemporalMarkerOffset(1, () -> {drive.purple.setPosition(PURPLE_RELEASE);})
                .waitSeconds(3)
                .forward(1)
                .lineToLinearHeading(new Pose2d(-55,59,Math.toRadians(180)))
                .lineToLinearHeading(new Pose2d(35,59,Math.toRadians(180)))
                .UNSTABLE_addTemporalMarkerOffset(0.85, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(0.4);})
                .waitSeconds(1.3)
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.box.setPosition(FLIP_UP);})
                .waitSeconds(2)
                .UNSTABLE_addTemporalMarkerOffset(0.5, () -> {drive.linearLift.setPower(0);})
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.linearLift.setPower(-0.4);})
                .waitSeconds(1)
                .splineToConstantHeading(new Vector2d(49, 38), Math.toRadians(0))
                // * deploy yellow pixel
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {drive.pixelRelease.setPosition(RELEASE_PIXELS);})
                .waitSeconds(1)
                .forward(2)
                .strafeRight(27)
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

    public String findColor() {
        if (drive.colorSensor.red()<300 && drive.colorSensor.green()<400 && drive.colorSensor.blue()<300) {
            return "black";
        }
        else {
            return "yellow";
        }
    }
}
