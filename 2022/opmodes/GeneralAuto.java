/*
Copyright (c) September 2017 FTC Teams 25/5218

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of FTC Teams 25/5218 nor the names of their contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package opmodes;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
//import team25core.ObjectDetectionTask;
//import team25core.ObjectImageInfo;
import team25core.ObjectDetectionTask;
import team25core.ObjectImageInfo;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;


/*

Create by  Ruchi Bondre 2022

 */

@Autonomous(name = "Generalauto")
//@Disabled
//red side
public class GeneralAuto extends Robot {

    // for drivetrain
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;

    private Servo servo1;

    //for path selection
    private enum AllianceColor {
        BLUE, // Button X
        RED, // Button B
        DEFAULT
    }

    private enum RobotPosition {
        RIGHT, // Button Y
        LEFT, // Button A
        DEFAULT
    }

    private Telemetry.Item allianceTlm;
    private Telemetry.Item robotPosTlm;
    private GamepadTask gamepad;
    protected AllianceColor allianceColor;
    protected RobotPosition robotPosition;


    // for timer
    private static double SERVO_OPEN = 180 / 256.0;
    private static double SERVO_OUT = 1 / 256.0;
    private final static int PAUSE_TIMER = 1000;
    SingleShotTimerTask rtTask;
    private String whichPause = "unknown";

    // paths for mechanisms
    private OneWheelDirectDrivetrain mech1drivetrain;
    private DcMotor mech1;


    // path declerations for wheels
    private DeadReckonPath blueRightPath;
    private DeadReckonPath blueLeftPath;
    private DeadReckonPath redRightPath;
    private DeadReckonPath redLeftPath;

    // path declerations for mechs
    private DeadReckonPath goliftMech1;
    private DeadReckonPath goliftDownMech1;



    //detection
    private double capPositionLeft;
    private double capMidpoint;
    private double capImageWidth;

    private String capStonePos;
    private double capLocation;

    ObjectDetectionTask rdTask;
    ObjectImageInfo objectImageInfo;

    private Telemetry.Item currentLocationTlm;
    private Telemetry.Item pathTlm;
    private Telemetry.Item positionTlm;
    private Telemetry.Item objectDetectedTlm;

    private FourWheelDirectDrivetrain drivetrain;




    /**
     * The default event handler for the robot.
     */
    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
        //decide what alliance and position of robot
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;
            switch (event.kind) {
                case BUTTON_X_DOWN:
                    allianceColor = AllianceColor.BLUE;
                    allianceTlm.setValue("BLUE");
                    break;
                case BUTTON_B_DOWN:
                    allianceColor = AllianceColor.RED;
                    allianceTlm.setValue("RED");
                    break;
                case BUTTON_Y_DOWN:
                    robotPosition = RobotPosition.RIGHT;
                    positionTlm.setValue("RIGHT");
                    break;
                case BUTTON_A_DOWN:
                    robotPosition = RobotPosition.LEFT;
                    positionTlm.setValue("LEFT");
                    break;

            }
        }
    }

    public void initPath() {

        blueRightPath = new DeadReckonPath();
        blueRightPath.stop();
        blueRightPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -1.0);

        blueLeftPath = new DeadReckonPath();
        blueLeftPath.stop();
        blueLeftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, 1.0);

        redRightPath = new DeadReckonPath();
        redRightPath.stop();
        redRightPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -1.0);

        redLeftPath = new DeadReckonPath();
        redLeftPath.stop();
        redLeftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, 1.0);

//        goliftMech1 = new DeadReckonPath();
//        goliftMech1.stop();
//        goliftMech1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 60, -1.0);
//
//        goliftDownMech1 = new DeadReckonPath();
//        goliftDownMech1.stop();
//        goliftDownMech1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 60, -1.0);


    }

    @Override
    public void init() {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        rearLeft = hardwareMap.get(DcMotor.class, "backLeft");
        rearRight = hardwareMap.get(DcMotor.class, "backRight");

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();

        /*  UNCOMMENT IF YOU HAVE A MECH


        mech1 = hardwareMap.get(DcMotor.class, "Mech");
        mech1drivetrain = new OneWheelDirectDrivetrain(mech1);
        mech1drivetrain.resetEncoders();
        mech1drivetrain.encodersOn();


        servo1 = hardwareMap.servo.get("servo1");
        servo1.setPosition(SERVO_OPEN);

        //break for mech
        mech1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

         */



        // Telemetry for choosing paths
        telemetry.setAutoClear(false);
        allianceTlm = telemetry.addData("ALLIANCE", "Unselected (X-blue /B-red)");
        robotPosTlm = telemetry.addData("POSITION", "Unselected (Y-build/A-depot)");



        pathTlm = telemetry.addData("path status", "unknown");

//        For detection

        objectImageInfo = new ObjectImageInfo();
        objectImageInfo.displayTelemetry(this.telemetry);

        currentLocationTlm = telemetry.addData("current location", "init");
        objectDetectedTlm = telemetry.addData("Object detected", "unknown");

        positionTlm = telemetry.addData("Position:", "unknown");


        initPath();



    }




    public void goMoveBlueRight()
    {
        this.addTask(new DeadReckonTask(this, blueRightPath, drivetrain){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    pathTlm.setValue("arrived at hub");


                }
            }
        });

    }

    private void goMoveBlueLeft() {
        this.addTask(new DeadReckonTask(this, blueLeftPath, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done lifting");


                }
            }
        });
    }

    private void goMoveRedRight() {
        this.addTask(new DeadReckonTask(this, redRightPath, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done lowering");



                }
            }
        });
    }

    private void goMoveRedLeft() {
        this.addTask(new DeadReckonTask(this, redLeftPath, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done lowering");



                }
            }
        });
    }








    @Override
    public void start() {


        // detection selection
        if (capLocation < 340) {
            positionTlm.setValue("Bottom Position");
            capStonePos = "bottom";


        } else if (capLocation < 580) {
            positionTlm.setValue("Middle Position");
            capStonePos = "middle";

        } else {
            positionTlm.setValue("Top Position");
            capStonePos = "top";
        }


        // choosing path based on selection
        if (allianceColor == AllianceColor.BLUE)
        {
            if (robotPosition == RobotPosition.RIGHT)
            {
                goMoveBlueRight();

            } else if (robotPosition == RobotPosition.LEFT)
            {
                goMoveBlueLeft();
            }

        }
        else if (allianceColor == AllianceColor.RED)
        {
            if (robotPosition == RobotPosition.RIGHT)
            {
                goMoveRedRight();

            } else if (robotPosition == RobotPosition.LEFT)
            {
                goMoveRedLeft();
            }

        }











    }



}
