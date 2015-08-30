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

        FourWheelDriveDeadReckon(int encoderTicksPerInch, double encoderTicksPerDegree)
        {
            super(encoderTicksPerInch, encoderTicksPerDegree);
        }

        @Override
        protected void resetEncoders(int ticks)
        {
            rearLeft.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
            rearRight.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
            frontLeft.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
            frontRight.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);

            rearLeft.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            rearRight.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            frontLeft.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            frontRight.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

            rearLeft.setTargetPosition(ticks);
            rearRight.setTargetPosition(ticks);
            frontLeft.setTargetPosition(ticks);
            frontRight.setTargetPosition(ticks);
        }

        protected void motorStraight(double speed)
        {
            frontRight.setPower(speed);
            frontLeft.setPower(speed);
            rearRight.setPower(speed);
            rearLeft.setPower(speed);
        }

        @Override
        protected void motorTurn(double speed)
        {
            frontRight.setPower(speed);
            rearRight.setPower(speed);
            frontLeft.setPower(-speed);
            rearLeft.setPower(-speed);
        }

        @Override
        protected boolean isBusy()
        {
            return (Math.abs(rearLeft.getCurrentPosition()) < Math.abs(rearLeft.getTargetPosition()));
        }
    }

    private final static int TICKS_PER_INCH = 50;
    private final static double TICKS_PER_DEGREE = 6;
    private final static double MOTOR_SPEED = 0.2;

    private FourWheelDriveDeadReckon deadReckon = new FourWheelDriveDeadReckon(TICKS_PER_INCH, TICKS_PER_DEGREE);
    private DcMotor frontRight;
    private DcMotor frontLeft;
    private DcMotor rearRight;
    private DcMotor rearLeft;
    private ModernRoboticsMatrixDcMotorController mc;
    private int battery;

    @Override
    public void init()
    {
        frontRight = hardwareMap.dcMotor.get("motor_1");
        frontLeft = hardwareMap.dcMotor.get("motor_2");  //
        rearRight = hardwareMap.dcMotor.get("motor_3");
        rearLeft = hardwareMap.dcMotor.get("motor_4");
        mc = (ModernRoboticsMatrixDcMotorController)hardwareMap.dcMotorController.get("MatrixControllerMotor");
        frontRight.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
        frontLeft.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
        rearRight.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
        rearLeft.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        rearLeft.setDirection(DcMotor.Direction.REVERSE);
    }

    @Override
    public void start()
    {
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, MOTOR_SPEED);
    }

    public void stop()
    {
        deadReckon.stop();
    }

    @Override
    public void loop()
    {
        int position;
        int target;

        deadReckon.runPath();

        position = rearLeft.getCurrentPosition();
        target = rearLeft.getTargetPosition();
        telemetry.addData("Position: ", Math.abs(position));
        telemetry.addData("Target: ", Math.abs(target));
    }
}
