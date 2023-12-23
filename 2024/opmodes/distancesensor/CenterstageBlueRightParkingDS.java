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
package opmodes.distancesensor;

//import com.acmerobotics.dashboard.FtcDashboard;
//import com.acmerobotics.dashboard.config.Config;
//import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.DistanceSensorTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;

//@Config
@Autonomous(name = "CenterstageBlueRightParkingDS")

//if any terms in the program are unknown to you, right click and press Go To > Declarations and Usages
public class CenterstageBlueRightParkingDS extends Robot {


    //wheels
    //all variables labeled private are declarations to their corresponding type
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private FourWheelDirectDrivetrain drivetrain;

    private DistanceSensorTask distanceTask;
    private final static String TAG = "PROP";
    private DistanceSensor rightSensor;
    private DistanceSensor leftSensor;
    private Telemetry.Item tagIdTlm;
   // private Telemetry.Item rightSensorTlm;
   // private Telemetry.Item leftSensorTlm;

    private Servo box;
    private Servo pixelRelease;
    private DcMotor linearLift;
    private DcMotor outtake;
    private OneWheelDirectDrivetrain outtakeDrivetrain;
    private OneWheelDirectDrivetrain liftDrivetrain;

    double rightDistance;
    double leftDistance;

    double minDistance;
    double maxDistance;

    //paths
    private DeadReckonPath goToParkFromMiddle;
    private DeadReckonPath goToParkFromRight;
    private DeadReckonPath goToParkFromLeft;

    private DeadReckonPath goMiddleToObject;
    private DeadReckonPath goRightToObject;
    private DeadReckonPath goLeftToObject;

    private DeadReckonPath outtakePath;

    private DeadReckonPath liftPath;
    private DeadReckonPath downLiftPath;

    private DeadReckonPath forwardPath;

    private DeadReckonPath backstageOuttake;


    //variables for constants
    //these constants CANNOT be changed unless edited in this declaration and initialization
    public static double FORWARD_DISTANCE = 14;
    public static double RIGHT_DISTANCE = 10;
    public static double LEFT_DISTANCE = 11;
    public static double DRIVE_SPEED = 0.6;
    public static double OUTTAKE_DISTANCE = 3;
    public static double OUTTAKE_SPEED = 0.3;


    //telemetry
    private Telemetry.Item whereAmI;
    private RunToEncoderValueTask outtakeTask;
    //integer 5000 represents 5000 milliseconds-change according to how long delay should be
    private static final int DELAY = 5000;

    public String objectDetectDirection;


    static double cX = 0;
    static double cY = 0;
    static double width = 0;

    private OpenCvCamera controlHubCam;  // Use OpenCvCamera class from FTC SDK

    private static final int CAMERA_WIDTH = 640; // width  of wanted camera resolution
    private static final int CAMERA_HEIGHT = 360; // height of wanted camera resolution

    // Calculate the distance using the formula
    public static final double objectWidthInRealWorldUnits = 3.75;  // Replace with the actual width of the object in real-world units
    public static final double focalLength = 728;  // Replace with the focal length of the camera in pixels

    public String position;
    public String DSPosition;

    private Telemetry.Item locationTlm;

    public String finalPos;

    private Servo releaseHanger;

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
        goToParkFromMiddle = new DeadReckonPath();
        goToParkFromRight = new DeadReckonPath();
        goToParkFromLeft = new DeadReckonPath();

        goMiddleToObject = new DeadReckonPath();
        goRightToObject = new DeadReckonPath();
        goLeftToObject = new DeadReckonPath();

        //removes or clears the action of the paths
        goToParkFromMiddle.stop();
        goToParkFromRight.stop();
        goToParkFromLeft.stop();

        goMiddleToObject.stop();
        goRightToObject.stop();
        goLeftToObject.stop();

        backstageOuttake = new DeadReckonPath();
        backstageOuttake.stop();
        backstageOuttake.addSegment(DeadReckonPath.SegmentType.STRAIGHT, OUTTAKE_DISTANCE, -OUTTAKE_SPEED);

        outtakePath = new DeadReckonPath();
        outtakePath.stop();
        outtakePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, OUTTAKE_DISTANCE, OUTTAKE_SPEED);

        forwardPath = new DeadReckonPath();
        forwardPath.stop();
        forwardPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 14, 0.4);

        liftPath = new DeadReckonPath();
        liftPath.stop();
        liftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, 0.6);

        downLiftPath = new DeadReckonPath();
        downLiftPath.stop();
        downLiftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, 0.6);

        //addSegment adds a new segment or direction the robot moves into
        //robot moves to the object in the right
        goRightToObject.addSegment(DeadReckonPath.SegmentType.TURN, 42, DRIVE_SPEED);

        //robot moves to the object in the middle
        goMiddleToObject.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 1, -0.3);

        //robot moves to the object in the left
        goLeftToObject.addSegment(DeadReckonPath.SegmentType.TURN, 45, -DRIVE_SPEED);

        //after robot places pixel in the middle position, drives to the parking spot in backstage
        goToParkFromMiddle.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 1, -DRIVE_SPEED);
        goToParkFromMiddle.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 9, DRIVE_SPEED);
        goToParkFromMiddle.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, DRIVE_SPEED);
        goToParkFromMiddle.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 40, -DRIVE_SPEED);
        goToParkFromMiddle.addSegment(DeadReckonPath.SegmentType.TURN, 42, DRIVE_SPEED);
        goToParkFromMiddle.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 11, DRIVE_SPEED);
        goToParkFromMiddle.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 16, -DRIVE_SPEED);

        //after robot places pixel in the right position, drives to the parking spot in backstage
        goToParkFromRight.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, LEFT_DISTANCE, -DRIVE_SPEED);
        goToParkFromRight.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 40, -DRIVE_SPEED); //backwards
        goToParkFromRight.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 9, DRIVE_SPEED);
        goToParkFromRight.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 6, -DRIVE_SPEED);

        //after robot places pixel in the left position, drives to the parking spot in backstage
        goToParkFromLeft.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -DRIVE_SPEED);
        goToParkFromLeft.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 10, DRIVE_SPEED);
        goToParkFromLeft.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 25, DRIVE_SPEED);
        goToParkFromLeft.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 6, -DRIVE_SPEED);
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

        rightSensor = hardwareMap.get(DistanceSensor.class, "rightSensor");
        leftSensor = hardwareMap.get(DistanceSensor.class, "leftSensor");

        box = hardwareMap.servo.get("pixelBox");
        box.setPosition(0.95);

        pixelRelease = hardwareMap.servo.get("pixelRelease");
        pixelRelease.setPosition(0.5);

        releaseHanger = hardwareMap.servo.get("releaseHanger");
        releaseHanger.setPosition(1);

        linearLift = hardwareMap.get(DcMotor.class, "linearLift");
        linearLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        linearLift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        linearLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        liftDrivetrain = new OneWheelDirectDrivetrain(linearLift);
        liftDrivetrain.resetEncoders();
        liftDrivetrain.encodersOn();

        detectPropDS();

        locationTlm = telemetry.addData("prop position", "none");
        /*
        initOpenCV();
        FtcDashboard dashboard = FtcDashboard.getInstance();
        telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
        FtcDashboard.getInstance().startCameraStream(controlHubCam, 30);
        telemetry.addData("Coordinate", "(" + (int) cX + ", " + (int) cY + ")");
        telemetry.addData("Distance in Inch", (getDistance(width)));
        telemetry.addData("Position: ", findPositionOpenCV());
        telemetry.update();

         */
        //calls method to start the initialization
        initPaths();

    }

    private void initOpenCV() {

        // Create an instance of the camera
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        // Use OpenCvCameraFactory class from FTC SDK to create camera instance
        controlHubCam = OpenCvCameraFactory.getInstance().createWebcam(
                hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        controlHubCam.setPipeline(new BlueBlobDetectionPipeline());

        controlHubCam.openCameraDevice();
        controlHubCam.startStreaming(CAMERA_WIDTH, CAMERA_HEIGHT, OpenCvCameraRotation.UPRIGHT);
    }

    public void outtakeInBackstage() {
        this.addTask(new DeadReckonTask(this, backstageOuttake, outtakeDrivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {

                }
            }
        });
    }

    //moves to detected object and releases the pixel
    public void moveToObjectAndReleasePixel(DeadReckonPath path)
    {

        this.addTask(new DeadReckonTask(this, path, drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("Drove to the object");
                    whereAmI.setValue("At the object");
                    releaseOuttake();
                }
            }
        });
    }

    public void approachSpikes() {
        this.addTask(new DeadReckonTask(this, forwardPath, drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("Drove to the object");
                    whereAmI.setValue("At the object");
                    addTask(distanceTask);
                }
            }
        });
    }

    public void detectPropDS() {
        distanceTask = new DistanceSensorTask(this, rightSensor, leftSensor, telemetry, 0, 12, 15 ,
                2,false) {
            @Override
            public void handleEvent(RobotEvent e) {
                DistanceSensorEvent event = (DistanceSensorEvent) e;
                switch (event.kind) {
                    case LEFT_DISTANCE:
                        DSPosition = "left";
                        locationTlm.setValue("left");
                        break;
                    case RIGHT_DISTANCE:
                        DSPosition = "right";
                        locationTlm.setValue("right");
                        break;
                    case UNKNOWN:
                        DSPosition = "center";
                        locationTlm.setValue("center");
                        break;
                }
               telemetry.addData("DS Position: ", DSPosition);
                telemetry.update();
                chooseSpike();
            }
        };
    }


    public void chooseSpike()
    {
        if((position.equals("right")&&DSPosition.equals("right")) || (position.equals("left")&&DSPosition.equals("right")) || (position.equals("center")&&DSPosition.equals("right")))
        {
            finalPos = "right";
            moveToObjectAndReleasePixel(goRightToObject);

        }
        else if((position.equals("center")&&DSPosition.equals("center")) || (position.equals("left")&&DSPosition.equals("center")) || (position.equals("right")&&DSPosition.equals("center")))
        {
            finalPos = "center";
            moveToObjectAndReleasePixel(goMiddleToObject);
        }
        else
        {
            finalPos = "left";
            moveToObjectAndReleasePixel(goLeftToObject);
        }
    }
    //robot goes to the backstage parking
    public void goToPark(DeadReckonPath path)
    {

        this.addTask(new DeadReckonTask(this, path, drivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    lift();
                }
            }
        });
    }

    public void lift() {
        this.addTask(new DeadReckonTask(this, liftPath, liftDrivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    box.setPosition(0);
                    pixelRelease.setPosition(0);
                    delay(500);
                    pixelRelease.setPosition(0.5);
                    box.setPosition(0.94);
                    liftDown();
                }
            }
        });
    }
    public void liftDown() {
        this.addTask(new DeadReckonTask(this, downLiftPath, liftDrivetrain ){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {

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
                    if(finalPos.equals("right"))
                    {
                        delay(1000);
                        goToPark(goToParkFromRight);
                    }
                    else if(finalPos.equals("center"))
                    {
                        delay(1000);
                        goToPark(goToParkFromMiddle);
                    }
                    else
                    {
                        delay(1000);
                        goToPark(goToParkFromLeft);
                    }

                }
            }
        });
    }

    //executes parking and releases pixel
    @Override
    public void start()
    {
        whereAmI.setValue("in Start");
        telemetry.addData("Position: ", findPositionOpenCV());
        telemetry.update();
        approachSpikes();
    }


    public static class BlueBlobDetectionPipeline extends OpenCvPipeline
    {
        Mat hierarchy = new Mat();
        Mat hsvFrame = new Mat();
        Mat blueMask = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        @Override
        public Mat processFrame(Mat input) {
            // Preprocess the frame to detect blue regions
            blueMask = preprocessFrame(input);


            // Find contours of the detected blue regions
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(blueMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Find the largest blue contour (blob)
            MatOfPoint largestContour = findLargestContour(contours);

            if (largestContour != null) {
                // Draw a blue outline around the largest detected object
                Imgproc.drawContours(input, contours, contours.indexOf(largestContour), new Scalar(255, 0, 0), 2);

                // Calculate the width of the bounding box
                width = calculateWidth(largestContour);

                String widthLabel = "Width: " + (int) width + " pixels";
                String distanceLabel = "Distance: " + String.format("%.2f", getDistance(width)) + " inches";

                // Calculate the centroid of the largest contour
                Moments moments = Imgproc.moments(largestContour);
                cX = moments.get_m10() / moments.get_m00();
                cY = moments.get_m01() / moments.get_m00();

                // Draw a dot at the centroid
                String label = "(" + (int) cX + ", " + (int) cY + ")";
                Imgproc.putText(input, label, new Point(cX + 10, cY), Imgproc.FONT_HERSHEY_COMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                Imgproc.circle(input, new Point(cX, cY), 5, new Scalar(0, 255, 0), -1);

                //DISPLAY TO RIGHT OF OBJECT
                // Display the width next to the label
                Imgproc.putText(input, widthLabel, new Point(cX + 10, cY + 20), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                //Display the Distance
                Imgproc.putText(input, distanceLabel, new Point(cX + 10, cY + 60), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
            }
            hierarchy.release();
            blueMask.release();
            return input;
        }

        private Mat preprocessFrame(Mat frame) {
            Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV);

            Scalar lowerRed = new Scalar(120, 100, 100);
            Scalar upperRed = new Scalar(180, 255, 255);


            Core.inRange(hsvFrame, lowerRed, upperRed, blueMask);

            Imgproc.morphologyEx(blueMask, blueMask, Imgproc.MORPH_OPEN, kernel);
            Imgproc.morphologyEx(blueMask, blueMask, Imgproc.MORPH_CLOSE, kernel);

            return blueMask;
        }

        private MatOfPoint findLargestContour(List<MatOfPoint> contours) {
            double maxArea = 0;
            MatOfPoint largestContour = null;

            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area > maxArea) {
                    maxArea = area;
                    largestContour = contour;
                }
            }

            return largestContour;
        }
        private double calculateWidth(MatOfPoint contour) {
            Rect boundingRect = Imgproc.boundingRect(contour);
            return boundingRect.width;
        }

    }
    private static double getDistance(double width){
        double distance = (objectWidthInRealWorldUnits * focalLength) / width;
        return distance;
    }
    private String findPositionOpenCV(){
        if (cX > 400) {
            position = "right";
            return position;
        }
        else if (cX <= 400 && cX >= 200) {
            position = "center";
            return position;
        }
        else {
            position = "left";
            return position;
        }
    }
}