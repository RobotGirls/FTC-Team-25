
package test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

/**
 * TeleOp Mode
 * <p>
 * Enables control of the robot via the gamepad.
 * NOTE: This op mode will not work with the NXT Motor Controllers. Use an Nxt op mode instead.
 */
public class TankDriveTeleOp extends OpMode {

  DcMotorController wheelController;

  DcMotor motorRight;
  DcMotor motorLeft;

  /*
   * Code to run when the op mode is first enabled goes here
   * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#init()
   */
  @Override
  public void init() {
    motorLeft = hardwareMap.dcMotor.get("motor_3");
    motorRight = hardwareMap.dcMotor.get("motor_4");
    wheelController = hardwareMap.dcMotorController.get("MatrixControllerMotor");

    motorLeft.setDirection(DcMotor.Direction.REVERSE);
    motorLeft.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
    motorRight.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
  }

  /*
   * This method will be called repeatedly in a loop
   * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#loop()
   */
  @Override
  public void loop() {

    float right  = gamepad1.left_stick_y;
    float left = gamepad1.right_stick_y;

    // clip the right/left values so that the values never exceed +/- 1
    right = Range.clip(right, -1, 1);
    left  = Range.clip(left,  -1, 1);

    // write the values to the motors
    motorRight.setPower(right);
    motorLeft.setPower(left);

    telemetry.addData("left motor", motorLeft.getPower());
    telemetry.addData("right motor", motorRight.getPower());
    telemetry.addData("left", left);
    telemetry.addData("right", right);
  }
}
