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

package test;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.MonitorGyroTask;
import team25core.MonitorMotorTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.Team25DcMotor;
import team25core.TwoWheelGearedDriveDeadReckon;

public class LameingoDeadReckonSquareTest extends Robot {

    private DcMotor r;
    private DcMotor l;
    private Team25DcMotor frontRight;
    private Team25DcMotor frontLeft;
    private DeadReckonTask deadReckonTask;
    private MonitorMotorTask monitorMotorTask;
    private MonitorGyroTask monitorGyroTask;
    private GyroSensor gyro;
    private final static double MOTOR_SPEED = 0.9;
    private TwoWheelGearedDriveDeadReckon deadReckon;

    @Override
    public void handleEvent(RobotEvent e)
    {

    }

    @Override
    public void init()
    {
        r = hardwareMap.dcMotor.get("rightMotor");
        l = hardwareMap.dcMotor.get("leftMotor");  //
        frontRight = new Team25DcMotor(this, r.getController(), 2);
        frontLeft = new Team25DcMotor(this, l.getController(), 1);
        frontRight.stopPeriodic();
        frontLeft.stopPeriodic();
        gyro = hardwareMap.gyroSensor.get("gyro");
        gyro.calibrate();

        frontLeft.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        frontRight.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        frontLeft.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        frontRight.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

/*
        deadReckon = new TwoWheelGearedDriveDeadReckon(this, 251, gyro, frontLeft, frontRight);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, MOTOR_SPEED);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, MOTOR_SPEED);
*/
        deadReckon = new TwoWheelGearedDriveDeadReckon(this, 251, gyro, frontLeft, frontRight);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, 0.7);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 45, 0.4);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, 0.7);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 45, 0.4);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, 0.7);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 0, 0);

    }

    @Override
    public void start()
    {
        // monitorMotorTask = new MonitorMotorTask(this, frontLeft);
        monitorGyroTask = new MonitorGyroTask(this, gyro);
        deadReckonTask = new DeadReckonTask(this, deadReckon);
        // addTask(monitorMotorTask);
        addTask(monitorGyroTask);
        addTask(deadReckonTask);

    }

    public void stop()
    {
        if (deadReckonTask != null) {
            deadReckonTask.stop();
        }
    }
}
