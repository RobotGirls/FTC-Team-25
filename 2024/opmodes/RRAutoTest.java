package opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

/*
 * This is a simple routine to test translational drive capabilities.
 */
@Config
@Autonomous(name = "RRAutoTest")
public class RRAutoTest extends LinearOpMode {
    public static double DISTANCE = 30; // in
    private DcMotorEx intake;
    private DistanceSensor distanceSensor;
    private DistanceSensor distanceSensor2;

    @Override
    public void runOpMode() throws InterruptedException {
        Telemetry telemetry = new MultipleTelemetry(this.telemetry, FtcDashboard.getInstance().getTelemetry());

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        intake = hardwareMap.get(DcMotorEx.class, "testMotor");
        intake.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        intake.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        intake.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        distanceSensor = hardwareMap.get(Rev2mDistanceSensor.class, "distanceSensor");

        // you can also cast this to a Rev2mDistanceSensor if you want to use added
        // methods associated with the Rev2mDistanceSensor class.
        Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor) distanceSensor;

        TrajectorySequence traj1 = drive.trajectorySequenceBuilder(new Pose2d(0,0,0))
                .splineTo(new Vector2d(36, 36), Math.toRadians(0))
                .addDisplacementMarker(25, () -> {
                    intake.setPower(0.5);
                    while (intake.getCurrent(CurrentUnit.MILLIAMPS) < 300 && opModeIsActive()) {

                    }
                    try {
                        Thread.sleep(350);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    intake.setPower(0);

                })
                .setReversed(true)
                .splineTo(new Vector2d(0, 0), Math.toRadians(180))
                .addDisplacementMarker(75, () -> {
                    intake.setPower(0);
                })
                .build();

        waitForStart();

        if (isStopRequested()) return;

        drive.followTrajectorySequence(traj1);
/*
        Pose2d poseEstimate = drive.getPoseEstimate();
        telemetry.addData("finalX", poseEstimate.getX());
        telemetry.addData("finalY", poseEstimate.getY());
        telemetry.addData("finalHeading", poseEstimate.getHeading());
        telemetry.update();
 */

        while (!isStopRequested() && opModeIsActive()) {
            // generic DistanceSensor methods.
            telemetry.addData("deviceName", distanceSensor.getDeviceName() );
            telemetry.addData("range", String.format("%.01f mm", distanceSensor.getDistance(DistanceUnit.MM)));
            telemetry.addData("range", String.format("%.01f cm", distanceSensor.getDistance(DistanceUnit.CM)));
            telemetry.addData("range", String.format("%.01f m", distanceSensor.getDistance(DistanceUnit.METER)));
            telemetry.addData("range", String.format("%.01f in", distanceSensor.getDistance(DistanceUnit.INCH)));

            // Rev2mDistanceSensor specific methods.
            telemetry.addData("ID", String.format("%x", sensorTimeOfFlight.getModelID()));
            telemetry.addData("did time out", Boolean.toString(sensorTimeOfFlight.didTimeoutOccur()));

            telemetry.update();
        }
    }

    public void testMotor() {
        intake.setPower(0.5);
    }
}
