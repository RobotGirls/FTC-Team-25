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
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.LinkedList;
import java.util.Queue;

public class MatrixDriveSquareTest extends OpMode {

    private class FourWheelDriveDeadReckon extends DeadReckon {

        FourWheelDriveDeadReckon(int encoderTicksPerInch, int encoderTicksPerDegree)
        {
            super(encoderTicksPerInch, encoderTicksPerDegree);
        }

        @Override
        protected void resetEncoders(int ticks)
        {
            motor1.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
            motor1.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
            motor1.setTargetPosition(ticks);
        }

        protected void motorStraight(double speed)
        {
            motor1.setPower(speed);
            motor2.setPower(speed);
            motor3.setPower(speed);
            motor4.setPower(speed);
        }

        @Override
        protected void motorTurn(double speed)
        {
            motor1.setPower(speed);
            motor2.setPower(speed);
            motor3.setPower(-speed);
            motor4.setPower(-speed);
        }

        @Override
        protected boolean isBusy()
        {
            return motor1.isBusy();
        }
    }

    private FourWheelDriveDeadReckon deadReckon = new FourWheelDriveDeadReckon(100, 100);
    private DcMotor motor1;
    private DcMotor motor2;
    private DcMotor motor3;
    private DcMotor motor4;
    private ModernRoboticsMatrixDcMotorController mc;
    private boolean initOnce = false;
    private int battery;

    @Override
    public void init()
    {
        if (!initOnce) {
            motor1 = hardwareMap.dcMotor.get("motor_1");
            motor2 = hardwareMap.dcMotor.get("motor_2");
            motor3 = hardwareMap.dcMotor.get("motor_3");
            motor4 = hardwareMap.dcMotor.get("motor_4");
            mc = (ModernRoboticsMatrixDcMotorController)hardwareMap.dcMotorController.get("MatrixControllerMotor");
            motor1.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
            motor2.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            motor3.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            motor4.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            initOnce = true;

            deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, 1.0);
            deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, 1.0);
            deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, 1.0);
            deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, 1.0);
            deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, 1.0);
            deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, 1.0);
            deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, 1.0);
            deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, 1.0);
        }
    }

    @Override
    public void start()
    {
    }

    public void stop()
    {
        motor1.setPower(0.0);
        motor2.setPower(0.0);
        motor3.setPower(0.0);
        motor4.setPower(0.0);
    }

    @Override
    public void loop()
    {
        deadReckon.runPath();
        battery = mc.getBattery();
        telemetry.addData("Battery: ", ((float)battery/1000));
    }
}
