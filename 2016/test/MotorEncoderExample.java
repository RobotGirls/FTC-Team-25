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
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class MotorEncoderExample extends Robot {

    private DcMotor matrixMotor1;
    private DcMotor matrixMotor2;
    private DcMotor matrixMotor3;
    private DcMotor matrixMotor4;
    private ModernRoboticsMatrixDcMotorController mc;
    private Set<DcMotor> motorSet = new HashSet<DcMotor>();

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof RunToEncoderValueTask.RunToEncoderValueEvent) {
            handleEncoderEvent((RunToEncoderValueTask.RunToEncoderValueEvent)e);
        }

    }

    /*
     * We are going to do a poor man's PID and ramp down the speed as we approach the target.
     */
    private void handleEncoderEvent(RunToEncoderValueTask.RunToEncoderValueEvent e)
    {
        telemetry.addDataPersist("Threshold ", e.kind.toString());

        if (e.kind == RunToEncoderValueTask.EventKind.THRESHOLD_80) {
                mc.setMotorPower(motorSet, 0.7);
        } else if (e.kind == RunToEncoderValueTask.EventKind.THRESHOLD_90) {
            mc.setMotorPower(motorSet, 0.3);
        } else if (e.kind == RunToEncoderValueTask.EventKind.THRESHOLD_95) {
            mc.setMotorPower(motorSet, 0.1);
        } else if (e.kind == RunToEncoderValueTask.EventKind.THRESHOLD_98) {
            mc.setMotorPower(motorSet, 0.05);
        }
    }

    private RobotTask monitorMotor(DcMotor motor)
    {
        RobotTask task;
        EnumSet<MonitorMotorTask.DisplayProperties> props = EnumSet.of(MonitorMotorTask.DisplayProperties.POSITION);

        task = new MonitorMotorTask(this, motor, props);
        addTask(task);

        return task;
    }

    @Override
    public void init()
    {
        matrixMotor1 = hardwareMap.dcMotor.get("motor_1");
        matrixMotor2 = hardwareMap.dcMotor.get("motor_2");
        matrixMotor3 = hardwareMap.dcMotor.get("motor_3");
        matrixMotor4 = hardwareMap.dcMotor.get("motor_4");
        matrixMotor1.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        matrixMotor1.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        matrixMotor2.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        matrixMotor2.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        matrixMotor3.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        matrixMotor3.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        matrixMotor4.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        matrixMotor4.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        mc = (ModernRoboticsMatrixDcMotorController)matrixMotor1.getController();

        motorSet.add(matrixMotor1);
        motorSet.add(matrixMotor2);
        motorSet.add(matrixMotor3);
        motorSet.add(matrixMotor4);
    }

    @Override
    public void start()
    {
        Set<DcMotor> slaves = new HashSet<DcMotor>();

        mc.setMotorPower(motorSet, 1.0);

        slaves.addAll(motorSet);
        slaves.remove(matrixMotor1);

        monitorMotor(matrixMotor1);
        addTask(new RunToEncoderValueTask(this, matrixMotor1, slaves, 10000));
    }

    public void stop()
    {
        mc.setMotorPower(motorSet, 0.0);
    }
}
