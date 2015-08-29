/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.qualcomm.ftcrobotcontroller.opmodes;

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
