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
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;
import team25core.vision.apriltags.AprilTagDetectionTask;


@Autonomous(name = "autoName")
//@Disabled

//if any terms in the program are unknown to you, right click and press Go To > Declarations and Usages
public class autoTemplate extends Robot {


    //wheels
    //all variables labeled private are declarations to their corresponding type
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private FourWheelDirectDrivetrain drivetrain;


    //mechs
    private Servo servoMech;
    private DcMotor motorMech;
    private OneWheelDirectDrivetrain motorDrivetrain;


    //sensors
    private DistanceSensor distanceSensor;
    private DistanceSensorCriteria distanceSensorCriteria;
    private ColorSensor colorSensor;


    //paths
    private DeadReckonPath path1;
    private DeadReckonPath path2;
    private DeadReckonPath path3;

    private DeadReckonPath motorMechPath;


    //variables for constants
    //these constants CANNOT be changed unless edited in this declaration and initialization
    static final double FORWARD_DISTANCE = 0;
    static final double RIGHT_DISTANCE = 0;
    static final double LEFT_DISTANCE = 0;
    static final double DRIVE_SPEED = 0;


    //telemetry
    private Telemetry.Item whereAmI;
    private RunToEncoderValueTask motorMechTask;
    private static final int DELAY = 5000;
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
        path1 = new DeadReckonPath();
        path2 = new DeadReckonPath();
        path3 = new DeadReckonPath();

        //removes or clears the action of the paths
        path1.stop();
        path2.stop();
        path3.stop();


        motorMechPath = new DeadReckonPath();
        motorMechPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 0, 0);

        //addSegment adds a new segment or direction the robot moves into

        //drive path 1
        //drive path 1 moves the robot forward and then strafes left
        path1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE, DRIVE_SPEED);
        path1.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, LEFT_DISTANCE, DRIVE_SPEED);

        //drive path 2
        //drive path 2 moves the robot forward
        path2.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE, DRIVE_SPEED);

        //drive path 3
        //drive path 3 moves the robot forward then strafe to the right
        path3.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE, DRIVE_SPEED);
        path3.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, RIGHT_DISTANCE, DRIVE_SPEED);

        //initializes motorMechTask
        motorMechTask = new RunToEncoderValueTask(this, motorMech, 0, 0);
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
        servoMech = hardwareMap.servo.get("servoMech");


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
        motorMech = hardwareMap.get(DcMotor.class, "motorMech");
        motorMech.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorMech.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorMech.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //initializes the motor drivetrain, resets encoders, and prepares motor(s) to run on the encoders
        motorDrivetrain = new OneWheelDirectDrivetrain(motorMech);
        motorDrivetrain.resetEncoders();
        motorDrivetrain.encodersOn();


        //initializes the color sensor and distance sensor for usage
        colorSensor = hardwareMap.get(RevColorSensorV3.class, "colorSensor");
        distanceSensor = hardwareMap.get(Rev2mDistanceSensor.class, "distanceSensor");

        //calls method to start the initialization
        initPaths();


    }

    //method that starts moving robot for path1 indicated above
    public void goToPath1()
    {

        this.addTask(new DeadReckonTask(this, path1, drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("Drove to the left");
                    whereAmI.setValue("Parked on the left");
                    delay(0);


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
    private void goMoveMotorMech() {
        this.addTask(new DeadReckonTask(this, motorMechPath, motorDrivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    whereAmI.setValue("moved motor mech");

                }
            }
        });
    }

    //provides certain movement for servo mechanism and displays telemetry stating robot
    //executed the servo task
    private void setServoMech() {
        servoMech.setPosition(0);
        whereAmI.setValue("servo moved");
    }



    //method that executes when the driver presses start on the driver station
    @Override
    public void start()
    {
        whereAmI.setValue("in Start");
        goToPath1();
        addTask(motorMechTask);



    }
}