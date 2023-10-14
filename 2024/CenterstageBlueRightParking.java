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

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
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

import opmodes.testingcode.CenterstageBlueLeftDetect;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.DistanceSensorCriteria;
import team25core.FourWheelDirectDrivetrain;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;


@Autonomous(name = "CenterstageBlueRightParking")
//@Disabled

//if any terms in the program are unknown to you, right click and press Go To > Declarations and Usages
public class CenterstageBlueRightParking extends Robot {


    //wheels
    //all variables labeled private are declarations to their corresponding type
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private FourWheelDirectDrivetrain drivetrain;


    //mechs
    //private Servo servoMech;
    private DcMotor outtake;
    private OneWheelDirectDrivetrain outtakeDrivetrain;


    //sensors
    //private DistanceSensor distanceSensor;
    //private DistanceSensorCriteria distanceSensorCriteria;
    //private ColorSensor colorSensor;


    //paths
    private DeadReckonPath goToPark;
    private DeadReckonPath goStraightToObject;
    private DeadReckonPath goLeftToObject;
    private DeadReckonPath goRightToObject;
    private DeadReckonPath outtakePath;

    //variables for constants
    //these constants CANNOT be changed unless edited in this declaration and initialization
    static final double FORWARD_DISTANCE = 28;
    static final double RIGHT_DISTANCE = 60;
    static final double LEFT_DISTANCE = 60;
    static final double DRIVE_SPEED = 0.3;


    //telemetry
    private Telemetry.Item whereAmI;
    //private RunToEncoderValueTask motorMechTask;
    private static final int DELAY = 5000;
    //integer 5000 represents 5000 milliseconds-change according to how long delay should be

    //OPENCV DETECTION

    double cX = 0;
    double cY = 0;
    double width = 0;

    private OpenCvCamera controlHubCam;  // Use OpenCvCamera class from FTC SDK
    private static final int CAMERA_WIDTH = 640; // width  of wanted camera resolution
    private static final int CAMERA_HEIGHT = 360; // height of wanted camera resolution

    // Calculate the distance using the formula
    public static final double objectWidthInRealWorldUnits = 3.75;  // Replace with the actual width of the object in real-world units
    public static final double focalLength = 728;  // Replace with the focal length of the camera in pixels
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
        goLeftToObject = new DeadReckonPath();
        goRightToObject = new DeadReckonPath();

        //removes or clears the action of the paths
        goToPark.stop();
        goStraightToObject.stop();
        goLeftToObject.stop();
        goRightToObject.stop();


        outtakePath = new DeadReckonPath();
        outtakePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, -1, 0.75);

        //addSegment adds a new segment or direction the robot moves into

        //drive path goToPark
        //drive path goToPark moves the robot forward and then strafes left
        goToPark.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE, DRIVE_SPEED);
        goToPark.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, LEFT_DISTANCE, DRIVE_SPEED);
        goToPark.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, LEFT_DISTANCE, DRIVE_SPEED);
        goToPark.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, LEFT_DISTANCE, DRIVE_SPEED);
        goToPark.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, LEFT_DISTANCE, DRIVE_SPEED);
        outtakePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE, DRIVE_SPEED);

        //initializes motorMechTask
        //motorMechTask = new RunToEncoderValueTask(this, motorMech, 0, 0);
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
        //servoMech = hardwareMap.servo.get("servoMech");


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
        //motorMech = hardwareMap.get(DcMotor.class, "motorMech");
        //motorMech.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //motorMech.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //motorMech.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //initializes the outtake drivetrain, resets encoders, and prepares motor(s) to run on the encoders
        outtakeDrivetrain = new OneWheelDirectDrivetrain(outtake);
        outtakeDrivetrain.resetEncoders();
        outtakeDrivetrain.encodersOn();


        //initializes the color sensor and distance sensor for usage
        //colorSensor = hardwareMap.get(RevColorSensorV3.class, "colorSensor");
        //distanceSensor = hardwareMap.get(Rev2mDistanceSensor.class, "distanceSensor");

        //openCv
        initOpenCV();
        FtcDashboard dashboard = FtcDashboard.getInstance();
        telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
        FtcDashboard.getInstance().startCameraStream(controlHubCam, 30);

        //calls method to start the initialization
        initPaths();


    }
    //method that turns robot positioning depending on results from openCV
    /*public void objectDetectionPath()
    {
        if (findPosition().equals("right")) {
            //drive path goRightToObject
            //drive path goRightToObject turns the robot right and moves forward
            goRightToObject.addSegment(DeadReckonPath.SegmentType.TURN, FORWARD_DISTANCE, DRIVE_SPEED);
            goRightToObject.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, RIGHT_DISTANCE, -DRIVE_SPEED);
        }
        else if (findPosition().equals("left")) {
            //drive path goLeftToObject
            //drive path goLeftToObject turns the robot left and moves forward
            goLeftToObject.addSegment(DeadReckonPath.SegmentType.TURN, FORWARD_DISTANCE, -DRIVE_SPEED);
            goLeftToObject.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, RIGHT_DISTANCE, DRIVE_SPEED);

        }
        else {
            //drive path goStraightToObject
            //drive path goStraightToObject moves the robot forward
            goStraightToObject.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE, DRIVE_SPEED);

        }

    }*/
    //method that starts moving robot for goToPark indicated above
    public void goToPark()
    {
        this.addTask(new DeadReckonTask(this, goToPark, drivetrain ){
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
    /*private void goMoveMotorMech() {
        this.addTask(new DeadReckonTask(this, outtake, outtakeDrivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    whereAmI.setValue("moved motor mech");

                }
            }
        });
    }*/

    //provides certain movement for servo mechanism and displays telemetry stating robot
    //executed the servo task
    /*private void setServoMech() {
        servoMech.setPosition(0);
        whereAmI.setValue("servo moved");
    }*/



    //method that executes when the driver presses start on the driver station
    @Override
    public void start()
    {
        whereAmI.setValue("in Start");
        goToPark();
        //addTask(motorMechTask);

        telemetry.addData("Coordinate", "(" + (int) cX + ", " + (int) cY + ")");
        telemetry.addData("Distance in Inch", (getDistance(width)));
        telemetry.addData("Position: ", findPosition());
        telemetry.update();

    }
    /////////////////////////////////// OPENCV STUFF ///////////////////////////////////////////////

    private void initOpenCV() {

        // Create an instance of the camera
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        // Use OpenCvCameraFactory class from FTC SDK to create camera instance
        controlHubCam = OpenCvCameraFactory.getInstance().createWebcam(
                hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        controlHubCam.setPipeline(new CenterstageBlueLeftDetect.RedBlobDetectionPipeline());

        controlHubCam.openCameraDevice();
        controlHubCam.startStreaming(CAMERA_WIDTH, CAMERA_HEIGHT, OpenCvCameraRotation.UPRIGHT);
    }

    class RedBlobDetectionPipeline extends OpenCvPipeline {
        @Override
        public Mat processFrame(Mat input) {
            // Preprocess the frame to detect red regions
            Mat redMask = preprocessFrame(input);

            // Find contours of the detected red regions
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(redMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Find the largest red contour (blob)
            MatOfPoint largestContour = findLargestContour(contours);

            if (largestContour != null) {
                // Draw a red outline around the largest detected object
                Imgproc.drawContours(input, contours, contours.indexOf(largestContour), new Scalar(255, 0, 0), 2);
                // Calculate the width of the bounding box
                width = calculateWidth(largestContour);

                String widthLabel = "Width: " + (int) width + " pixels";
                String distanceLabel = "Distance: " + String.format("%.2f", getDistance(width)) + " inches";

                // Calculate the centroid of the largest contour
                Moments moments = Imgproc.moments(largestContour);
                cX = moments.get_m10() / moments.get_m00();
                cY = moments.get_m01() / moments.get_m00();

                String posLabel = "Position: " + findPosition();

                // Draw a dot at the centroid
                String label = "(" + (int) cX + ", " + (int) cY + ")";
                Imgproc.putText(input, label, new Point(cX + 10, cY), Imgproc.FONT_HERSHEY_COMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                Imgproc.circle(input, new Point(cX, cY), 5, new Scalar(0, 255, 0), -1);

                //DISPLAY TO RIGHT OF OBJECT
                // Display the width next to the label
                Imgproc.putText(input, widthLabel, new Point(cX + 10, cY + 20), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                //Display the Distance
                Imgproc.putText(input, distanceLabel, new Point(cX + 10, cY + 60), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                // Display the position
                Imgproc.putText(input, posLabel, new Point(cX + 10, cY + 75), Imgproc.FONT_HERSHEY_COMPLEX, 0.5, new Scalar(0, 255, 0), 2);
            }

            return input;
        }

        private Mat preprocessFrame(Mat frame) {
            Mat hsvFrame = new Mat();
            Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV);

            Scalar lowerRed = new Scalar(100, 100, 100);
            Scalar upperRed = new Scalar(180, 255, 255);


            Mat redMask = new Mat();
            Core.inRange(hsvFrame, lowerRed, upperRed, redMask);

            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
            Imgproc.morphologyEx(redMask, redMask, Imgproc.MORPH_OPEN, kernel);
            Imgproc.morphologyEx(redMask, redMask, Imgproc.MORPH_CLOSE, kernel);

            return redMask;
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
    private String findPosition(){
        if (cX > 400) {
            return "right";
        }
        else if (cX <= 400 && cX >= 200) {
            return "center";
        }
        else {
            return "left";
        }
    }
}