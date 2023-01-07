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
package opmodes;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.openftc.apriltag.AprilTagDetection;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.DistanceSensorCriteria;
import team25core.FourWheelDirectDrivetrain;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.vision.apriltags.AprilTagDetectionTask;


@Autonomous(name = "ONLYPARKAUTO")
//@Disabled
public class PowerPlayDetectAuto extends Robot {


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


    //paths
    private DeadReckonPath leftPath;
    private DeadReckonPath middlePath;
    private DeadReckonPath rightPath;


    private DeadReckonPath liftMech;
    private DeadReckonPath  lowerMech;

    private DeadReckonPath turretTurnOrangePath;
    private DeadReckonPath turretTurnBluePath;

    private DeadReckonPath deliverConePath;

    //variables for constants
    static final double FORWARD_DISTANCE = 13.5;
    static final double DRIVE_SPEED = 0.25;

    // apriltags detection
    private Telemetry.Item tagIdTlm;
    private Telemetry.Item parkingLocationTlm;
    AprilTagDetection tagObject;
    private AprilTagDetectionTask detectionTask;

    //telemetry
    private Telemetry.Item whereAmI;

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

    public void setAprilTagDetection() {
        detectionTask = new AprilTagDetectionTask(this, "Webcam 1") {
            @Override
            public void handleEvent(RobotEvent e) {
                TagDetectionEvent event = (TagDetectionEvent) e;
                tagObject = event.tagObject;
                tagIdTlm.setValue(tagObject.id);
                whereAmI.setValue("in handleEvent");

                if (tagObject.id == 0) {
                    gotoLeftPark();
                }
                if (tagObject.id == 6) {
                    gotoRightPark();
                }
                if (tagObject.id == 19) {
                    gotoMiddlePark();
                }

            }
        };
        whereAmI.setValue("setAprilTagDetection");
        detectionTask.init(telemetry, hardwareMap);
    }



    public void initPaths()
    {
        leftPath = new DeadReckonPath();
        middlePath = new DeadReckonPath();
        rightPath= new DeadReckonPath();


        leftPath.stop();
        middlePath.stop();
        rightPath.stop();

        liftMech = new DeadReckonPath();
        liftMech.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 68, -0.5);

        lowerMech =  new DeadReckonPath();
        lowerMech.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 1.5, -0.01);

        deliverConePath  = new DeadReckonPath();
        deliverConePath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5.5,  -DRIVE_SPEED);

        //going forward then to the left
        leftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE, -DRIVE_SPEED);
        leftPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 13.5, DRIVE_SPEED);
        leftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -DRIVE_SPEED);



        //going forward
        middlePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 17, -DRIVE_SPEED);


        //going forward then right
        rightPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,FORWARD_DISTANCE+1.5,-DRIVE_SPEED);
        rightPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,13.5,-DRIVE_SPEED);

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

        turretDrivetrain = new OneWheelDirectDrivetrain(turret);
        turretDrivetrain.resetEncoders();
        turretDrivetrain.encodersOn();

        //open umbrella & lock cone
        umbrella.setPosition(0.5);


        linearColorSensor = hardwareMap.get(RevColorSensorV3.class, "liftColorSensor");
        alignerDistanceSensor = hardwareMap.get(Rev2mDistanceSensor.class, "alignerDistanceSensor");


        initPaths();


    }




    //  lifting & dropping paths --------------------------------------

    private void goliftMech() {
        this.addTask(new DeadReckonTask(this, liftMech, liftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    whereAmI.setValue("lifted linear lift");
                   // dropCone();



                }
            }
        });
    }

    private void dropCone() {
        umbrella.setPosition(0);
        golowerMech();

    }

    private void golowerMech() {
        this.addTask(new DeadReckonTask(this, lowerMech, liftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    whereAmI.setValue("lifted linear lift");




                }
            }
        });
    }

    public void goDeliverCone()
    {

        parkingLocationTlm.setValue("went to right target zone");

        this.addTask(new DeadReckonTask(this, deliverConePath ,drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to right target zone");
                    whereAmI.setValue("went to right target zone");

                }
            }
        });
    }

    // parking paths -----------------------------------

    public void gotoRightPark()
    {

        parkingLocationTlm.setValue("went to right target zone");

        this.addTask(new DeadReckonTask(this, rightPath,drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to right target zone");
                    whereAmI.setValue("went to right target zone");

                }
            }
        });
    }

    public void gotoMiddlePark()
    {
        parkingLocationTlm.setValue("went to middle target zone");

        this.addTask(new DeadReckonTask(this, middlePath,drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to middle target zone");
                    whereAmI.setValue("went to middle target zone");
                    //goliftMech();

                }
            }
        });
    }

    public void gotoLeftPark()
    {


        parkingLocationTlm.setValue("went to left target zone");


        this.addTask(new DeadReckonTask(this, leftPath,drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went to left target zone");
                    whereAmI.setValue("went to left target zone");

                }
            }
        });


    }

    @Override
    public void start()
    {
        whereAmI.setValue("in Start");
        setAprilTagDetection();
        addTask(detectionTask);


    }
}
