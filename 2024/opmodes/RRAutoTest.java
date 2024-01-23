package opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

/*
 * This is a simple routine to test translational drive capabilities.
 */
@Config
@Autonomous(name = "RRAutoTest")
public class RRAutoTest extends LinearOpMode {
    public static double DISTANCE = 30; // in
    private DcMotor intake;

    @Override
    public void runOpMode() throws InterruptedException {
        Telemetry telemetry = new MultipleTelemetry(this.telemetry, FtcDashboard.getInstance().getTelemetry());

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        intake = hardwareMap.get(DcMotor.class, "testMotor");
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        intake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);



        TrajectorySequence traj1 = drive.trajectorySequenceBuilder(new Pose2d(0,0,0))
                .splineTo(new Vector2d(36, 36), Math.toRadians(0))
                .addDisplacementMarker(25, () -> {
                    intake.setPower(0.5);
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

        while (!isStopRequested() && opModeIsActive()) ;
    }

    public void testMotor() {
        intake.setPower(0.5);
    }
}
