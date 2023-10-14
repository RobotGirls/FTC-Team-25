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

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.motors.RevRoboticsCoreHexMotor;
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
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;
import team25core.vision.apriltags.AprilTagDetectionTask;

@Config
@Autonomous(name = "CenterstageRedRightParking")
//@Disabled

//if any terms in the program are unknown to you, right click and press Go To > Declarations and Usages
public class CenterstageRedRightParking extends Robot {


    //wheels
    //all variables labeled private are declarations to their corresponding type
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private FourWheelDirectDrivetrain drivetrain;


    //mechs
//    private Servo servoMech;
    private DcMotor outtake;
    private OneWheelDirectDrivetrain outtakeDrivetrain;


    //sensors
//    private DistanceSensor distanceSensor;
//    private DistanceSensorCriteria distanceSensorCriteria;
//    private ColorSensor colorSensor;


    //paths
    private DeadReckonPath goToPark;
    private DeadReckonPath goStraightToObject;
    private DeadReckonPath goRightToObject;
    private DeadReckonPath goLeftToObject;

    private DeadReckonPath outtakePath;


    //variables for constants
    //these constants CANNOT be changed unless edited in this declaration and initialization
    public static final double FORWARD_DISTANCE = 0;
    public static final double RIGHT_DISTANCE = 28;
    public static final double LEFT_DISTANCE = 0;
    public static final double DRIVE_SPEED = 0.6;
    public static final double OUTTAKE_DISTANCE = 1;
    public static final double OUTTAKE_SPEED = 0.6;


    //telemetry
    private Telemetry.Item whereAmI;
    private static final int DELAY = 5000;

    private String objectDetectDirection;
    //integer 5000 represents 5000 milliseconds-change according to how long delay should be

    /*
     * The default event handler for the robot.
     */

    //method displays telemetry(: prints status of robot) on the driver station
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




    //initializes declared paths/tasks for the robot to do
    public void initPaths()
    {   //initializes the paths
        goToPark = new DeadReckonPath();
        goStraightToObject = new DeadReckonPath();
        goRightToObject = new DeadReckonPath();
        goLeftToObject = new DeadReckonPath();

        //removes or clears the action of the paths
        goToPark.stop();
        goStraightToObject.stop();
        goRightToObject.stop();
        goLeftToObject.stop();


        outtakePath = new DeadReckonPath();
        outtakePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, OUTTAKE_DISTANCE, OUTTAKE_SPEED);

        //addSegment adds a new segment or direction the robot moves into

        //robot moves to the right into the backstage parking area
        goToPark.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, RIGHT_DISTANCE, DRIVE_SPEED);

        //robot moves to the object in the right
        goRightToObject.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, DRIVE_SPEED);
        goRightToObject.addSegment(DeadReckonPath.SegmentType.TURN, 4, DRIVE_SPEED);

        //robot moves to the object in the front
        goStraightToObject.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, DRIVE_SPEED);

        //robot moves to the object in the left
        goLeftToObject.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, DRIVE_SPEED);
        goLeftToObject.addSegment(DeadReckonPath.SegmentType.TURN, 4, -DRIVE_SPEED);

    }

    //initializes the declared motors and servos
    @Override
    public void init()
    {
        //initializes the motors for the wheels
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        //initializes the servo
        //servos are not in initPaths() because they do not get tasks unless a task is created for them in a specified method
//        servoMech = hardwareMap.servo.get("servoMech");


        //sets wheel motors to run using the encoders
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //initializes drivetrain, clears the encoder values, and prepares motors to run on the encoders
        drivetrain = new FourWheelDirectDrivetrain(frontRight, backRight, frontLeft, backLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();

        //displays telemetry of robot location
        whereAmI = telemetry.addData("location in code", "init");

        //initializes motor mechanism, returns what motor would do if 0 power behavior was implemented on it,
        //rests encoder, and prepares motors to run on the encoders
        outtake = hardwareMap.get(DcMotor.class, "outtake");
        outtake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        outtake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        outtake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //initializes the motor drivetrain, resets encoders, and prepares motor(s) to run on the encoders
        outtakeDrivetrain = new OneWheelDirectDrivetrain(outtake);
        outtakeDrivetrain.resetEncoders();
        outtakeDrivetrain.encodersOn();


        //initializes the color sensor and distance sensor for usage
//        colorSensor = hardwareMap.get(RevColorSensorV3.class, "colorSensor");
//        distanceSensor = hardwareMap.get(Rev2mDistanceSensor.class, "distanceSensor");

        //calls method to start the initialization
        initPaths();


    }



    //robot goes to the backstage parking in the corner and releases a pixel in backstage
    public void goToParkAndReleasePixel()
    {

        this.addTask(new DeadReckonTask(this, goToPark, drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("Drove to the left");
                    whereAmI.setValue("Parked in the backstage");
                    releaseOuttake();
                   // delay(0);


                }
            }
        });
    }

    //creates a delay for robot task and sets telemetry to display that robot is in delay task
    private void delay(int delayInMsec) {
        this.addTask(new SingleShotTimerTask(this, delayInMsec) {
            @Override
            public void handleEvent(RobotEvent e) {
                SingleShotTimerEvent event = (SingleShotTimerEvent) e;
                if (event.kind == EventKind.EXPIRED ) {
                    whereAmI.setValue("in delay task");

                }
            }
        });

    }

    //provides a certain task movement for the motor mech and displays telemetry stating robot is
    //executing the motor mech task
    private void releaseOuttake() {
        this.addTask(new DeadReckonTask(this, outtakePath, outtakeDrivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    whereAmI.setValue("released purple pixel");

                }
            }
        });
    }

    //provides certain movement for servo mechanism and displays telemetry stating robot
    //executed the servo task
//    private void setServoMech() {
//        servoMech.setPosition(0);
//        whereAmI.setValue("servo moved");
//    }



    //executes parking and releases pixel
    @Override
    public void start()
    {
        whereAmI.setValue("in Start");
        goToParkAndReleasePixel();
//        releaseOuttake();



    }
}