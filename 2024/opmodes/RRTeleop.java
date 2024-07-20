// field centric teleop with linearopmode
// FIXME figure out how/where to add mechanisms/gamepad controls

package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive;

@TeleOp(name = "Roadrunner Teleop")
public class RRTeleop extends LinearOpMode {

    private final double BLOCK_NOTHING = 0.25;
    private final double BLOCK_BOTH = 0.05;

    @Override
    public void runOpMode() throws InterruptedException {
        CenterstageSampleMecanumDrive drive = new CenterstageSampleMecanumDrive(hardwareMap);
        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        DcMotor rightHang;
        DcMotor leftHang;
        DcMotor linearLift;

        boolean intakeOn = false;
        boolean outtakeOn = false;

        rightHang = hardwareMap.get(DcMotor.class, "rightHang");
        rightHang.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightHang.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightHang.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        leftHang = hardwareMap.get(DcMotor.class, "leftHang");
        leftHang.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftHang.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftHang.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        linearLift = hardwareMap.get(DcMotor.class, "linearLift");
        linearLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        linearLift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        linearLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        Pose2d startPose = new Pose2d(0, 0, Math.toRadians(0));

        drive.setPoseEstimate(startPose);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive() && !isStopRequested()) {
            // Read pose
            Pose2d poseEstimate = drive.getPoseEstimate();

            // Create a vector from the gamepad x/y inputs
            // Then, rotate that vector by the inverse of that heading
            Vector2d input = new Vector2d(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x
            ).rotated(-poseEstimate.getHeading());

            // Pass in the rotated input + right stick value for rotation
            // Rotation is not part of the rotated input thus must be passed in separately
            drive.setWeightedDrivePower(
                    new Pose2d(
                            input.getX(),
                            input.getY(),
                            -gamepad1.right_stick_x
                    )
            );

            if (gamepad2.dpad_up) {
                drive.pixelRelease.setPosition(BLOCK_BOTH);
            }
            else if (gamepad2.dpad_down) {
                // pixel box is open
                drive.pixelRelease.setPosition(BLOCK_NOTHING);
            }
            else if (gamepad2.y) {
                leftHang.setPower(1);
                rightHang.setPower(-1);
            }
            else if (gamepad2.a) {
                leftHang.setPower(-1);
                rightHang.setPower(1);
            }
            else if (gamepad2.x) {
                leftHang.setPower(1);
            }
            else if (gamepad2.b) {
                leftHang.setPower(-1);
            }
            else if (!gamepad2.y && !gamepad2.a) {
                rightHang.setPower(0);
                leftHang.setPower(0);
            }
            else if (!gamepad2.y && !gamepad2.a && !gamepad2.x && !gamepad2.b) {
                leftHang.setPower(0);
                rightHang.setPower(0);
            }
            else if (gamepad2.right_bumper) {
                drive.linkage.setPosition(drive.LINKAGE_UP);
            }
            else if (gamepad2.left_bumper) {
                drive.linkage.setPosition(drive.LINKAGE_DOWN);
            }
            else if (gamepad2.dpad_right) {
                //turn off intake
                drive.intake.setPower(0);
                intakeOn = false;
            }
            drive.linearLift.setPower(gamepad2.left_stick_y);

            if (gamepad1.right_bumper) {

                // turn on intake if it's currently off; turn off intake if it's currently on
                if(intakeOn == false) {
                    drive.intake.setPower(-0.9);
                    intakeOn = true;
                }
                else {
                    drive.intake.setPower(0);
                    intakeOn = false;
                }
            }
            else if (gamepad1.left_bumper) {
                //outtake pixels
                if(outtakeOn == false) {
                    drive.intake.setPower(0.8);
                    outtakeOn = true;
                }
                else {
                    drive.intake.setPower(0);
                    outtakeOn = false;
                }

                drive.box.setPosition(0.465);
                drive.pixelRelease.setPosition(BLOCK_BOTH);
            }
            else if (gamepad1.dpad_down) {
                // flip box to original position block pixels from falling
                drive.pixelRelease.setPosition(BLOCK_BOTH);
                drive.box.setPosition(0.89);
            }
            else if (gamepad1.dpad_up) {
                // box up to score and block pixels
                drive.box.setPosition(0.465);
                drive.pixelRelease.setPosition(BLOCK_BOTH);
            }

            // Update everything. Odometry. Etc.
            drive.update();

            // Print pose to telemetry
            telemetry.addData("x", poseEstimate.getX());
            telemetry.addData("y", poseEstimate.getY());
            telemetry.addData("heading", poseEstimate.getHeading());
            telemetry.update();
        }
    }
}