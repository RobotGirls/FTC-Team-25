/* Copyright (c) 2015 Craig MacFarlane

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Craig MacFarlane nor the names of its contributors
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

import com.qualcomm.modernrobotics.ModernRoboticsMatrixDcMotorController;
import com.qualcomm.modernrobotics.ModernRoboticsMatrixServoController;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * A simple example of run to position with a matrix controller.
 */
public class MatrixMotorServoTestTeleOp extends OpMode {

    private ElapsedTime motorOscTimer = new ElapsedTime(0);
    private ElapsedTime servoOscTimer = new ElapsedTime(0);
    private ElapsedTime spamPrevention = new ElapsedTime(0);
    private DcMotor motor1;
    private DcMotor motor2;
    private DcMotor motor3;
    private DcMotor motor4;
    private Servo servo1;
    private Servo servo2;
    private Servo servo3;
    private Servo servo4;
    private ModernRoboticsMatrixDcMotorController mc;
    private ModernRoboticsMatrixServoController sc;
    private boolean initOnce = false;
    private boolean loopOnce = false;
    private boolean firstMotors = true;
    private boolean firstServos = true;
    private boolean firstBattery = true;
    private int battery;
    private final static double MOTOR_OSC_FREQ = 5.0;
    private final static double SERVO_OSC_FREQ = 1.0;
    private final static double SPAM_PREVENTION_FREQ = 1.0;

    private double motorPower = 1.0;
    private double servoPosition = 0.0;

    @Override
    public void init()
    {
        if (!initOnce) {
            motor1 = hardwareMap.dcMotor.get("motor_1");
            motor2 = hardwareMap.dcMotor.get("motor_2");
            motor3 = hardwareMap.dcMotor.get("motor_3");
            motor4 = hardwareMap.dcMotor.get("motor_4");
            servo1 = hardwareMap.servo.get("servo_1");
            servo2 = hardwareMap.servo.get("servo_2");
            servo3 = hardwareMap.servo.get("servo_3");
            servo4 = hardwareMap.servo.get("servo_4");
            mc = (ModernRoboticsMatrixDcMotorController)hardwareMap.dcMotorController.get("MatrixControllerMotor");
            sc = (ModernRoboticsMatrixServoController)hardwareMap.servoController.get("MatrixControllerServo");
            motor1.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            motor2.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            motor3.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            motor4.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            sc.pwmEnable();
            initOnce = true;
        }
    }

    @Override
    public void start()
    {
        motorOscTimer.reset();
        servoOscTimer.reset();
        spamPrevention.reset();
    }

    public void stop()
    {
        motor1.setPower(0.0);
        motor2.setPower(0.0);
        motor3.setPower(0.0);
        motor4.setPower(0.0);
        sc.pwmDisable();
    }

    protected void handleMotors()
    {
        if ((firstMotors) || (motorOscTimer.time() > MOTOR_OSC_FREQ)) {
            motorPower = -motorPower;
            motor1.setPower(motorPower);
            motor2.setPower(motorPower);
            motor3.setPower(motorPower);
            motor4.setPower(motorPower);
            motorOscTimer.reset();
            firstMotors = false;
        }
    }

    protected void handleServos()
    {
        if ((firstServos) || (servoOscTimer.time() > SERVO_OSC_FREQ)) {
            if (servoPosition == 0.0) {
                servoPosition = 1.0;
            } else {
                servoPosition = 0.0;
            }
            servo1.setPosition(servoPosition);
            servo2.setPosition(servoPosition);
            servo3.setPosition(servoPosition);
            servo4.setPosition(servoPosition);
            servoOscTimer.reset();
            firstServos = false;
        }
    }

    @Override
    public void loop()
    {
        handleMotors();
        handleServos();

        if ((firstBattery) && (spamPrevention.time() > SPAM_PREVENTION_FREQ)) {
            battery = mc.getBattery();
            spamPrevention.reset();
            firstBattery = false;
        }
        telemetry.addData("Battery: ", ((float)battery/1000));
    }
}
