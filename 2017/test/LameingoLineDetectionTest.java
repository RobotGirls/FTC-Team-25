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

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

import team25core.DeadReckonPath;
import team25core.MRLightSensor;
import team25core.OpticalDistanceSensorCriteria;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TwoWheelDirectDrivetrain;

@Autonomous(name="Lameingo: Line Detection", group="AutoTeam25")
@Disabled
public class LameingoLineDetectionTest extends Robot
{
    private DcMotor frontRight;
    private DcMotor frontLeft;

    private OpticalDistanceSensor opticalDistanceSensor;
    private MRLightSensor ods;

    private DeadReckonTask nearBeaconTask;
    private PersistentTelemetryTask ptt;
    private TwoWheelDirectDrivetrain drivetrain;

    private final static double STRAIGHT_SPEED = LameingoConfiguration.STRAIGHT_SPEED;
    private final static double TURN_SPEED = LameingoConfiguration.TURN_SPEED;
    private final static int TICKS_PER_INCH = LameingoConfiguration.TICKS_PER_INCH;
    private final static int TICKS_PER_DEGREE = LameingoConfiguration.TICKS_PER_DEGREE;

    private DeadReckonPath approachNearBeacon;
    OpticalDistanceSensorCriteria lightCriteria;

    @Override
    public void handleEvent(RobotEvent e)
    {
    }

    @Override
    public void init()
    {
        // Motor setup.
        frontRight = hardwareMap.dcMotor.get("rightMotor");
        frontLeft = hardwareMap.dcMotor.get("leftMotor");

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        drivetrain = new TwoWheelDirectDrivetrain(LameingoConfiguration.TICKS_PER_INCH, frontRight, frontLeft);

        // Path setup.
        approachNearBeacon = new DeadReckonPath();
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        approachNearBeacon.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 40, STRAIGHT_SPEED);

        // Optical Distance Sensor setup.
        opticalDistanceSensor = hardwareMap.opticalDistanceSensor.get("frontLight");
        ods = new MRLightSensor(opticalDistanceSensor);
        lightCriteria = new OpticalDistanceSensorCriteria(ods, LameingoConfiguration.ODS_MIN, LameingoConfiguration.ODS_MAX); // Confirm w/Craig switching to double; 1.3 and 4.5ish.

        // Telemetry setup.
        ptt = new PersistentTelemetryTask(this);
    }

    @Override
    public void start()
    {
        nearBeaconTask = new DeadReckonTask(this, approachNearBeacon, drivetrain, lightCriteria);
        addTask(nearBeaconTask);
    }

    public void stop()
    {
        if (nearBeaconTask != null) {
            nearBeaconTask.stop();
        }
    }
}
