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

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

@Config
@Autonomous(name = "ColorSensorIntakeTest")
public class ColorSensorIntakeTest extends LinearOpMode {
    public static double DISTANCE = 30; // in

    private final double BLOCK_NOTHING = 0.05;
    private final double BLOCK_BOTH = 0.8;

    private final double PURPLE_RELEASE = 0.05;

    private final double PROP_DIST = 10; // cm

    public String COLOR_DETECTED = "black";
    CenterstageSampleMecanumDrive drive;


    @Override
    public void runOpMode() throws InterruptedException {
        drive = new CenterstageSampleMecanumDrive(hardwareMap);
        Telemetry telemetry = new MultipleTelemetry(this.telemetry, FtcDashboard.getInstance().getTelemetry());

        // you can also cast this to a Rev2mDistanceSensor if you want to use added
        // methods associated with the Rev2mDistanceSensor class.
        Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor) drive.distanceSensor1;
        Rev2mDistanceSensor sensorTimeOfFlight2 = (Rev2mDistanceSensor) drive.distanceSensor2;

        TrajectorySequence intake = drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.toRadians(0)))
                // APPROACHING SPIKES
                .forward(1)
                /*
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    while (true) {
                        if (drive.colorSensor.red()<300 && drive.colorSensor.green()<400 && drive.colorSensor.blue()<300) {
                            COLOR_DETECTED = "black";
                            drive.intake.setPower(-0.9);
                        }
                        else {
                            COLOR_DETECTED = "yellow";
                            drive.intake.setPower(0.9);
                        }
                    }
                })
                .UNSTABLE_addTemporalMarkerOffset(10, () -> {drive.intake.setPower(0);})

                 */
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    while (drive.colorSensor.red()<300 && drive.colorSensor.green()<400 && drive.colorSensor.blue()<300) {
                        // color is black
                        drive.intake.setPower(-0.9);
                    }
                    drive.intake.setPower(0);
                })
                .build();


        waitForStart();
        telemetry.addData("location: ", "started program");
        telemetry.update();
        if (isStopRequested()) return;
        telemetry.addData("location: ", "starting toSpikes");
        telemetry.update();
        drive.followTrajectorySequence(intake);
        telemetry.update();


        while (!isStopRequested() && opModeIsActive()) {
            // generic DistanceSensor methods.
            telemetry.addData("deviceName", drive.distanceSensor1.getDeviceName());
            telemetry.addData("range", String.format("%.01f in", drive.distanceSensor1.getDistance(DistanceUnit.INCH)));
/*
            if (drive.colorSensor.red()<300 && drive.colorSensor.green()<400 && drive.colorSensor.blue()<300) {
                telemetry.addData("Color: ", "Black");
                COLOR_DETECTED = "black";
                drive.intake.setPower(-0.9);
            }
            else {
                telemetry.addData("Color: ","Yellow");
                COLOR_DETECTED = "yellow";
                drive.intake.setPower(0.9);
            }
 */
            telemetry.update();
        }
    }
}