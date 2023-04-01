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
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package opmodes.testingcode;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.openftc.apriltag.AprilTagDetection;

import team25core.sensors.color.ColorSensorTask;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.DistanceSensorCriteria;
import team25core.FourWheelDirectDrivetrain;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;
import team25core.sensors.color.RGBColorSensorTask;
import team25core.vision.apriltags.AprilTagDetectionTask;


@Autonomous(name = "strafewithcolorSensor")
//@Disabled
public class ONLYSTACK2WPRELOAD extends Robot {


    //wheels
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private FourWheelDirectDrivetrain drivetrain;


    //mechs
    private Servo umbrella;

    private DcMotor linearLift;
    private OneWheelDirectDrivetrain liftDriveTrain;

    private DcMotor turret;
    private OneWheelDirectDrivetrain turretDrivetrain;


    //sensors
    private DistanceSensor alignerDistanceSensor;
    private DistanceSensorCriteria distanceSensorCriteria;
    private ColorSensor linearColorSensor;
    private ColorSensorTask linearColorSensorTask;



    //paths
    private DeadReckonPath goDropPreLoadPath;
    private DeadReckonPath goToJunctionPath;
    private DeadReckonPath goToStackPath;
    private DeadReckonPath goStrafeToJunction;


    private DeadReckonPath liftMech;
    private DeadReckonPath  lowerMech;


    private DeadReckonPath strafeOutPath;
    private DeadReckonPath deliverConePath;

    //variables for constants
    static final double FORWARD_DISTANCE = 13.5;
    static final double DRIVE_SPEED = 0.5;

    // apriltags detection
    private Telemetry.Item tagIdTlm;
    private Telemetry.Item parkingLocationTlm;
    AprilTagDetection tagObject;
    private AprilTagDetectionTask detectionTask;

    //telemetry
    private Telemetry.Item whereAmI;

    private RunToEncoderValueTask linearLiftTask;

    private RunToEncoderValueTask linearLiftTaskJunction;

    private RunToEncoderValueTask linearLiftTaskStack;

    private static final int DELAY = 5000;

    public String detectValue = "";

    private ColorSensor bottomColorsensor;
    private DeadReckonPath strafetoColorLinePath;
    private RGBColorSensorTask colorSensorTask;
    private Telemetry.Item colorDetectedTlm;
    private Telemetry.Item redDetectedTlm;
    private Telemetry.Item greenDetectedTlm;
    private Telemetry.Item blueDetectedTlm;

    /*
     * The default event handler for the robot.
     */

    @Override
    public void handleEvent(RobotEvent e)
    {
        /*
         * Every time we complete a segment drop a note in the robot log.
         */
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
    }





    public void initPaths()
    {
        goToJunctionPath = new DeadReckonPath();
        goToStackPath = new DeadReckonPath();
        goStrafeToJunction= new DeadReckonPath();

        goDropPreLoadPath= new DeadReckonPath();

        strafeOutPath = new DeadReckonPath();

        goToJunctionPath.stop();
        goToStackPath.stop();
        goStrafeToJunction.stop();

        strafeOutPath.stop();

        liftMech = new DeadReckonPath();
        liftMech.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, -0.5);

        lowerMech =  new DeadReckonPath();
        lowerMech.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 0.5);

        deliverConePath  = new DeadReckonPath();
        deliverConePath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5.5,  -DRIVE_SPEED);


        goDropPreLoadPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 32, DRIVE_SPEED);


        //drive path 1
        goToStackPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 32, DRIVE_SPEED);
        goToStackPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE + 1, -0.25);

        //drive path 2
        goToJunctionPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE + 7, DRIVE_SPEED);

        //drive path 3
        goStrafeToJunction.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,1.75,-0.25);

        linearLiftTask = new RunToEncoderValueTask(this,linearLift,2000,-0.5);
        linearLiftTaskJunction = new RunToEncoderValueTask(this,linearLift,3100,-0.5);
        linearLiftTaskStack = new RunToEncoderValueTask(this,linearLift,2000,0.5);

        strafetoColorLinePath = new DeadReckonPath();
        strafetoColorLinePath.stop();
        strafetoColorLinePath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 30, 0.2);


        strafeOutPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,2,0.25);
        strafeOutPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 23, -0.25);

    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        umbrella=hardwareMap.servo.get("umbrella");



        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, backRight, frontLeft, backLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();

        whereAmI = telemetry.addData("location in code", "init");
        tagIdTlm = telemetry.addData("tagId","none");
        parkingLocationTlm = telemetry.addData("parking location: ","none");

        linearLift=hardwareMap.get(DcMotor.class, "linearLift");
        linearLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        liftDriveTrain = new OneWheelDirectDrivetrain(linearLift);
        liftDriveTrain.resetEncoders();
        liftDriveTrain.encodersOn();

        turret = hardwareMap.get(DcMotor.class, "turret");
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        turretDrivetrain = new OneWheelDirectDrivetrain(turret);
        turretDrivetrain.resetEncoders();
        turretDrivetrain.encodersOn();

        //open umbrella & lock cone
        //umbrella.setPosition(0.55);
        umbrella.setPosition(0);

        colorDetectedTlm = telemetry.addData("color detected", "unknown");
        blueDetectedTlm = telemetry.addData("blue color sensor value", 0);
        redDetectedTlm = telemetry.addData("red color sensor value", 0);
        greenDetectedTlm = telemetry.addData("green color sensor value", 0);


        linearColorSensor = hardwareMap.get(RevColorSensorV3.class, "liftColorSensor");
        alignerDistanceSensor = hardwareMap.get(Rev2mDistanceSensor.class, "alignerDistanceSensor");

        bottomColorsensor = hardwareMap.get(RevColorSensorV3.class, "bottomColorSensor");

        turret.setTargetPosition(0);
        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        turret.setPower(0.5);


        initPaths();


    }


    //method will be called in start


    public void setAprilTagDetection() {
        detectionTask = new AprilTagDetectionTask(this, "Webcam 1") {
            @Override
            public void handleEvent(RobotEvent e) {
                TagDetectionEvent event = (TagDetectionEvent) e;
                tagObject = event.tagObject;
                tagIdTlm.setValue(tagObject.id);
                whereAmI.setValue("in handleEvent");

                if (tagObject.id == 0) {
                    detectValue = "leftpark";
                }
                if (tagObject.id == 6) {
                    detectValue = "rightpark";
                }
                if (tagObject.id == 19) {
                    detectValue = "middlepark";
                }
                goDropPreLoad();



            }
        };
        whereAmI.setValue("setAprilTagDetection");
        detectionTask.init(telemetry, hardwareMap);
    }

    public void goDropPreLoad()
    {
        parkingLocationTlm.setValue("went to middle target zone");

        this.addTask(new DeadReckonTask(this, goDropPreLoadPath,drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to middle target zone");
                    whereAmI.setValue("went to middle target zone");
                    delayAndDrop0(2000);


                }
            }
        });
    }

    private void delayAndDrop0(int delayInMsec) {
        this.addTask(new SingleShotTimerTask(this, delayInMsec) {
            @Override
            public void handleEvent(RobotEvent e) {
                SingleShotTimerEvent event = (SingleShotTimerEvent) e;
                if (event.kind == EventKind.EXPIRED ) {
                    whereAmI.setValue("in delay task");
                    dropCone0();

                }
            }
        });

    }
    private void dropCone0() {
        umbrella.setPosition(0);
        gotoStack();
    }

    public void gotoStack()
    {
        parkingLocationTlm.setValue("went to middle target zone");

        this.addTask(new DeadReckonTask(this, goToStackPath,drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to middle target zone");
                    whereAmI.setValue("went to middle target zone");
                    delayAndDrop(2000);


                }
            }
        });
    }





    private void delayAndDrop(int delayInMsec) {
        this.addTask(new SingleShotTimerTask(this, delayInMsec) {
            @Override
            public void handleEvent(RobotEvent e) {
                SingleShotTimerEvent event = (SingleShotTimerEvent) e;
                if (event.kind == EventKind.EXPIRED ) {
                    whereAmI.setValue("in delay task");
                    golowerMech();

                }
            }
        });

    }

    private void golowerMech() {
        this.addTask(new DeadReckonTask(this, lowerMech, liftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    whereAmI.setValue("lifted linear lift");
                    grabcone();

                }
            }
        });
    }

    private void grabcone() {
        umbrella.setPosition(0.55);
        whereAmI.setValue("grabbed the cone");
        goliftMech();
    }


    private void goliftMech() {
        this.addTask(new DeadReckonTask(this, liftMech, liftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    whereAmI.setValue("lifted linear lift");
                    goToJunction();
                    addTask(linearLiftTaskJunction);



                }
            }
        });
    }

    public void goToJunction()
    {
        parkingLocationTlm.setValue("went to middle target zone");

        this.addTask(new DeadReckonTask(this, goToJunctionPath,drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to middle target zone");
                    whereAmI.setValue("went to middle target zone");
                    goTurnTurret();



                }
            }
        });
    }

    public void goTurnTurret()
    {
        turret.setTargetPosition(-800);
        turret.setPower(0.5);
        goStrafeToJunction();

    }


    public void goStrafeToJunction()
    {
        parkingLocationTlm.setValue("went to middle target zone");

        this.addTask(new DeadReckonTask(this, goStrafeToJunction,drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to middle target zone");
                    whereAmI.setValue("went to middle target zone");
                    delayAndDrop2(3000);

                }
            }
        });
    }



    private void delayAndDrop2(int delayInMsec) {
        this.addTask(new SingleShotTimerTask(this, delayInMsec) {
            @Override
            public void handleEvent(RobotEvent e) {
                SingleShotTimerEvent event = (SingleShotTimerEvent) e;
                if (event.kind == EventKind.EXPIRED ) {
                    whereAmI.setValue("in delay task");
                    dropCone();
                }
            }
        });

    }

    private void dropCone() {
        umbrella.setPosition(0);
        whereAmI.setValue("dropped the cone");

        delayAndDrop3(1000);

    }

    private void delayAndDrop3(int delayInMsec) {
        this.addTask(new SingleShotTimerTask(this, delayInMsec) {
            @Override
            public void handleEvent(RobotEvent e) {
                SingleShotTimerEvent event = (SingleShotTimerEvent) e;
                if (event.kind == EventKind.EXPIRED ) {
                    whereAmI.setValue("in delay task");
                    goTurnTurretOG();

                }
            }
        });

    }

    public void goTurnTurretOG()
    {
        turret.setTargetPosition(0);
        turret.setPower(0.5);
        goToStackAgain();
        addTask(linearLiftTaskStack);

    }

    public void goToStackAgain()
    {
        parkingLocationTlm.setValue("went to middle target zone");

        this.addTask(new DeadReckonTask(this, strafeOutPath,drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to middle target zone");
                    whereAmI.setValue("went to middle target zone");
                    golowerMech();

                }
            }
        });
    }






    @Override
    public void start()
    {
//        whereAmI.setValue("in Start");
//        setAprilTagDetection();
//        addTask(linearLiftTask);

       strafetoColorLine();


    }


    public void strafetoColorLine()
    {

        handleColorSensor();
        parkingLocationTlm.setValue("in strafetoColorLine Method");
        this.addTask(new DeadReckonTask(this, strafetoColorLinePath,drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {

                    whereAmI.setValue("after strafe path");
                    // goTurnTurret();

                }
            }
        });
    }


    public void handleColorSensor () {

        whereAmI.setValue("in handleColorSensor");
        colorSensorTask = new RGBColorSensorTask(this, bottomColorsensor) {
            public void handleEvent(RobotEvent e) {

                whereAmI.setValue("in handleColorSensor handle Event");
                RGBColorSensorTask.ColorSensorEvent event = (RGBColorSensorTask.ColorSensorEvent) e;
                // sets threshold for blue, red, and green to ten thousand
                // FIXME seems redundant, possibly remove; said twice
                colorArray = colorSensorTask.getColors();
                // shows the values of blue, red, green on the telemetry
                blueDetectedTlm.setValue(colorArray[0]);
                redDetectedTlm.setValue(colorArray[1]);
                switch(event.kind) {
                    // red is at the end
                    case RED_DETECTED:
                        drivetrain.stop();
                        robot.removeTask(colorSensorTask);
                        this.resume();
                        colorDetectedTlm.setValue("red");
                        whereAmI.setValue("red");
                        break;
                    case BLUE_DETECTED:
                        drivetrain.stop();
                        robot.removeTask(colorSensorTask);
                        this.resume();
                        colorDetectedTlm.setValue("blue");
                        whereAmI.setValue("blue");
                        break;
                    default:
                        colorDetectedTlm.setValue("none");
                        whereAmI.setValue("none");
                        break;

                }
                whereAmI.setValue("detected strafeing color");
            }
        };
        colorSensorTask.setThresholds(10000, 10000, 5000);
        colorSensorTask.setDrivetrain(drivetrain);
        addTask(colorSensorTask);
    }

}