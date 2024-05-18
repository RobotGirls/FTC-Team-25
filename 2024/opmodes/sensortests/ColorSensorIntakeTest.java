package opmodes.sensortests;

import static org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive.LINKAGE_ONE_PIXEL;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

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

        TrajectorySequence intake = drive.trajectorySequenceBuilder(new Pose2d(0, 0, Math.toRadians(0)))
                .UNSTABLE_addTemporalMarkerOffset(0, () -> { drive.linkage.setPosition(LINKAGE_ONE_PIXEL); })
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
                        // color is black --> intake
                        drive.intake.setPower(-0.9);
                    }
                    // after color is not black (meaning it's yellow), stop intaking
                    drive.intake.setPower(0);
                })
                .waitSeconds(1)
                // outtake while driving backwards
                .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                    drive.intake.setPower(0.9);
                })
                .forward(-10)
                .build();


        waitForStart();
        telemetry.addData("location: ", "started program");
        telemetry.update();
        if (isStopRequested()) return;
        drive.followTrajectorySequence(intake);


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