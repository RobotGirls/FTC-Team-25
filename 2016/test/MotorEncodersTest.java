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

import java.util.HashSet;
import java.util.Set;

public class MotorEncodersTest extends Robot {

    private DcMotor matrixMotor1;
    private DcMotor matrixMotor2;
    private DcMotor matrixMotor3;
    private DcMotor matrixMotor4;
    private DcMotor andymarkMotor;
    private ModernRoboticsMatrixDcMotorController mc;

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
        Set<DcMotor> slaves;

        addTask(new MonitorMotorTask(this, matrixMotor1));
        addTask(new MonitorMotorTask(this, andymarkMotor));

        slaves = new HashSet<DcMotor>();
        slaves.add(matrixMotor2);
        slaves.add(matrixMotor3);
        slaves.add(matrixMotor4);
        addTask(new RunToEncoderValueTask(this, matrixMotor1, slaves, 10000));
        addTask(new RunToEncoderValueTask(this, andymarkMotor, null, 10000));

    }

    private void handleTimerEvent(RobotEvent e)
    {
        /*
         * Now switch the controller software to read mode, for the port on the hitechnic controller
         */
        addTask(new SwitchMotorControllerModeTask(this, andymarkMotor.getController(), DcMotorController.DeviceMode.READ_ONLY));
    }

    @Override
    public void init()
    {
        matrixMotor1 = hardwareMap.dcMotor.get("motor_1");
        matrixMotor2 = hardwareMap.dcMotor.get("motor_2");
        matrixMotor3 = hardwareMap.dcMotor.get("motor_3");
        matrixMotor4 = hardwareMap.dcMotor.get("motor_4");
        andymarkMotor = hardwareMap.dcMotor.get("andynark");
        matrixMotor1.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        matrixMotor1.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        matrixMotor2.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        matrixMotor2.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        matrixMotor3.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        matrixMotor3.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        matrixMotor4.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        matrixMotor4.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        andymarkMotor.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        andymarkMotor.getController().setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
    }

    @Override
    public void start()
    {
        matrixMotor1.setPower(1.0);
        matrixMotor2.setPower(1.0);
        matrixMotor3.setPower(1.0);
        matrixMotor4.setPower(1.0);
        /*
         * For ModernRoboticsNxtDcMotorController, the reset and the encoder commands can not be in the same loop cycle.
         */
        andymarkMotor.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        andymarkMotor.setPower(1.0);

        /*
         * Give the robot 200ms to start the motors
         */
        addTask(new SingleShotTimerTask(this, 200));
    }

    public void stop()
    {
        matrixMotor1.setPower(0.0);
        matrixMotor2.setPower(0.0);
        matrixMotor3.setPower(0.0);
        matrixMotor4.setPower(0.0);

        andymarkMotor.setPower(0.0);
    }
}
