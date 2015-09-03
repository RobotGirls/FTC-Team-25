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

public class MotorEncodersTest extends Robot {

    private DcMotor motor1;
    private DcMotor motor2;
    private ModernRoboticsMatrixDcMotorController mc;
    private boolean loopOnce = false;
    private boolean initOnce = false;
    private int battery;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof SingleShotTimerTask.SingleShotTimerEvent) {
            handleTimerEvent(e);
        } else if (e instanceof SwitchMotorControllerModeTask.SwitchMotorControllerModeEvent) {
            handleSwitchModeEvent(e);
        }
    }

    private void handleSwitchModeEvent(RobotEvent e)
    {
        addTask(new MonitorMotorTask(this, motor1));
        addTask(new MonitorMotorTask(this, motor2));
    }

    private void handleTimerEvent(RobotEvent e)
    {
        /*
         * Now switch the controller software to read mode, for the port on the hitechnic controller
         */
        addTask(new SwitchMotorControllerModeTask(this, motor2.getController(), DcMotorController.DeviceMode.READ_ONLY));
    }

    @Override
    public void init()
    {
        motor1 = hardwareMap.dcMotor.get("motor_1");
        motor2 = hardwareMap.dcMotor.get("andynark");
        motor1.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        motor1.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        motor2.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        motor2.getController().setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
    }

    @Override
    public void start()
    {
        motor1.setPower(1.0);
        /*
         * For ModernRoboticsNxtDcMotorController, the reset and the encoder commands can not be in the same loop cycle.
         */
        motor2.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        motor2.setPower(1.0);

        /*
         * Give the robot 200ms to start the motors
         */
        addTask(new SingleShotTimerTask(this, 200));
    }

    public void stop()
    {
        motor1.setPower(0.0);
        motor2.setPower(0.0);
        initOnce = false;
    }
}
