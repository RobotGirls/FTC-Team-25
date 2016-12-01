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
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.LightSensorCriteria;
import team25core.MRLightSensor;
import team25core.OpticalDistanceSensorCriteria;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TwoWheelGearedDriveDeadReckon;

@Autonomous(name="Lameingo: Autonomous", group="AutoTeam25")
public class LameingoAutonomous extends Robot
{
    private DcMotor frontRight;
    private DcMotor frontLeft;

    private OpticalDistanceSensor frontOds;
    private OpticalDistanceSensor backOds;
    private MRLightSensor frontLight;
    private MRLightSensor backLight;


    private DeadReckonTask nearBeaconTask;
    private DeadReckonTask lineDetectTurnTask;
    private DeadReckonTask farBeaconTask;

    private PersistentTelemetryTask ptt;

    private final static double STRAIGHT_SPEED = LameingoConfiguration.STRAIGHT_SPEED;
    private final static double TURN_SPEED = LameingoConfiguration.TURN_SPEED;
    private final static int TICKS_PER_INCH = LameingoConfiguration.TICKS_PER_INCH;
    private final static int TICKS_PER_DEGREE = LameingoConfiguration.TICKS_PER_DEGREE;
    private static int TURN_MULTIPLER = 1;

    private TwoWheelGearedDriveDeadReckon approachNearBeacon;
    private TwoWheelGearedDriveDeadReckon approachFarBeacon;
    private TwoWheelGearedDriveDeadReckon lineDetectTurnPath;
    private TwoWheelGearedDriveDeadReckon beaconAlignTurn;

    OpticalDistanceSensorCriteria lightCriteria;
    OpticalDistanceSensorCriteria backLightCriteria;

    public enum Alliance {
        RED,
        BLUE
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            if (event.kind == GamepadTask.EventKind.BUTTON_X_DOWN) {
                selectAlliance(Alliance.BLUE);
            } else if (event.kind == GamepadTask.EventKind.BUTTON_B_DOWN) {
                selectAlliance(Alliance.RED);
            }
        }


    }



    public void selectAlliance(Alliance color)
    {
       if (color == Alliance.BLUE) {
           // do blue setup.
           ptt.addData("Alliance:", "Blue");
           TURN_MULTIPLER = -1;
       } else {
           // do red setup.
           ptt.addData("Alliance:", "Red");
           TURN_MULTIPLER = 1;
       }
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

        // Path setup.
        approachNearBeacon = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight);
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        approachNearBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 40, STRAIGHT_SPEED);

        // Line detect turn path setup.
        lineDetectTurnPath = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight);
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        lineDetectTurnPath.addSegment(DeadReckon.SegmentType.TURN, 60,  TURN_SPEED);


        // Optical Distance Sensor (front) setup.
        frontOds = hardwareMap.opticalDistanceSensor.get("frontLight");
        frontLight = new MRLightSensor(frontOds);
        lightCriteria = new OpticalDistanceSensorCriteria(frontLight, LameingoConfiguration.ODS_MIN, LameingoConfiguration.ODS_MAX); // Confirm w/Craig switching to double; 1.3 and 4.5ish.

        // Optical Distance Sensor (back) setup.
        backOds = hardwareMap.opticalDistanceSensor.get("backLight");
        backLight = new MRLightSensor(backOds);
        backLightCriteria = new OpticalDistanceSensorCriteria(backLight, LameingoConfiguration.ODS_MIN, LameingoConfiguration.ODS_MAX);

        // Telemetry setup.
        ptt = new PersistentTelemetryTask(this);
        ptt.addData("Press (x) to select", "BLUE alliance!");
        ptt.addData("Press (b) to select", "RED alliance!");

        // Alliance selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));
    }

    @Override
    public void start()
    {
        this.addTask(new DeadReckonTask(this, approachNearBeacon, backLightCriteria) {
            @Override
            public void handleEvent(RobotEvent e) {
                if (e instanceof DeadReckonTask.DeadReckonEvent) {
                    DeadReckonTask.DeadReckonEvent drEvent = (DeadReckonTask.DeadReckonEvent) e;

                    if (drEvent.kind == DeadReckonTask.EventKind.SENSOR_SATISFIED) {
                        doTurnOnLine();

                    }
                }
            }

            private void doTurnOnLine() {
                robot.addTask(new DeadReckonTask(robot, lineDetectTurnPath, lightCriteria) {
                    @Override
                    public void handleEvent(RobotEvent e)
                    {
                        DeadReckonEvent drEvent = (DeadReckonEvent) e;

                        if (drEvent.kind == EventKind.SENSOR_SATISFIED) {
                            robot.addTask(new DeadReckonTask(robot, beaconAlignTurn, backLightCriteria));
                        }
                    }
                });
            }
        });
    }

    public void stop()
    {
        if (nearBeaconTask != null) {
            nearBeaconTask.stop();
        }
    }
}
