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

/**
 * A simple example of run to position with a matrix controller.
 */
public class MatrixRTPTestTeleOp extends OpMode {

    private DcMotor motor1;
    private ModernRoboticsMatrixDcMotorController mc;
    private boolean loopOnce = false;
    private boolean initOnce = false;
    private int battery;

    @Override
    public void init()
    {
        /*
         * Note that motors are initialized to a floating state by the underlying
         * infrastructure.  Teams should always initialize the channel mode of
         * each motor to the desired state prior to usage.
         *
         * Also note that before running some tests, I did not realize that init()
         * was called continuously from an event loop.  Hence the flag to prevent
         * spamming the controller.  Note there is some caching so sequential calls
         * to the same mode are discarded, but reset encoders is special and is never
         * discarded and the followon mode can not be discarded.
         */
        if (!initOnce) {
            motor1.setChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
            motor1.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
            motor1.setTargetPosition(1000);
            initOnce = true;
        }
    }

    @Override
    public void start()
    {
        motor1 = hardwareMap.dcMotor.get("motor_1");
        mc = (ModernRoboticsMatrixDcMotorController)hardwareMap.dcMotorController.get("MatrixControllerMotor");
    }

    public void stop()
    {
        motor1.setPowerFloat();
        initOnce = false;
        loopOnce = false;
    }

    @Override
    public void loop()
    {
        int position;

        if (!loopOnce) {
            loopOnce = true;
            motor1.setPower(1.0);
        }

        if (motor1.isBusy()) {
            telemetry.addData("Busy", " yes");
        } else {
            telemetry.addData("Busy", " no");
        }

        position = motor1.getCurrentPosition();
        telemetry.addData("Position: ", position);
        battery = mc.getBattery();
        telemetry.addData("Battery: ", ((float)battery/1000));
    }
}
