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

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.FourWheelDirectDrivetrain;
import team25core.ObjectDetectionTask;
import team25core.ObjectImageInfo;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

@Autonomous(name = "QT1NewAutoWR3")
//@Disabled
//red side
public class QT1AutoWR extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private Servo intakeDrop;

    private static double INTAKEDROP_OPEN = 180 / 256.0;
    private static double INTAKEDROP_OUT = 1 / 256.0;
    private final static int PAUSE_TIMER = 1000;

    //private Servo teamElementServo;
    private OneWheelDirectDrivetrain carouselDriveTrain;
    private DcMotor carouselMech;

    private OneWheelDirectDrivetrain gravelLiftDriveTrain;
    private DcMotor gravelLift;

    private OneWheelDirectDrivetrain intakeMechDriveTrain;
    private DcMotor freightIntake;


    private DeadReckonPath goToShippingHubPath;

    private DeadReckonPath outTakePath;
    private DeadReckonPath goParkInWareHousePath;

    private DeadReckonPath moveToShippingHub;

    private DeadReckonPath goMoveForwardTopPath;
    private DeadReckonPath liftMechPathTop;
    private DeadReckonPath lowerMechPathTop;

    private DeadReckonPath liftMechPathMiddle;
    private DeadReckonPath lowerMechPathMiddle;

    private DeadReckonPath liftMechPathBottom;
    private DeadReckonPath lowerMechPathBottom;

    private DeadReckonPath goliftMechInitalPath;

    SingleShotTimerTask rtTask;

    private String whichPause = "unknown";


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
        /**
         * Every time we complete a segment drop a note in the robot log.
         */
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent) e).segment_num);
        }
    }

    public void initPath() {
        // going to shipping hub
        goToShippingHubPath = new DeadReckonPath();
        goToShippingHubPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 17, 0.25);
        goToShippingHubPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 9, -0.25); //red

        //outtaking object
        outTakePath = new DeadReckonPath();
        outTakePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, 1.0);

        // strafe to shipping hub after detection
        moveToShippingHub = new DeadReckonPath();
        moveToShippingHub.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 7, 1.0);

        // forward to shipping hub

        goliftMechInitalPath = new DeadReckonPath();
        goliftMechInitalPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3, -0.25);

        //turning drivetrain for top position of hub
        goMoveForwardTopPath = new DeadReckonPath();
        goMoveForwardTopPath.addSegment(DeadReckonPath.SegmentType.TURN, 5.5, 0.25);
        goMoveForwardTopPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 0.3, -0.25);

        //top

        liftMechPathTop = new DeadReckonPath();
        liftMechPathTop.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7.5, 0.10);

        lowerMechPathTop = new DeadReckonPath();
        lowerMechPathTop.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7.5, -0.07);

        //middle

        liftMechPathMiddle = new DeadReckonPath();
        liftMechPathMiddle.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3.5, 0.07);

        lowerMechPathMiddle = new DeadReckonPath();
        lowerMechPathMiddle.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3.5, -0.07);

        //bottom

        liftMechPathBottom = new DeadReckonPath();
        liftMechPathBottom.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 1.7, 0.07);

        lowerMechPathBottom = new DeadReckonPath();
        lowerMechPathBottom.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 1.7, -0.07);

        //end parking in warehouse

        goParkInWareHousePath = new DeadReckonPath();
        goParkInWareHousePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, 0.50);
        goParkInWareHousePath.addSegment(DeadReckonPath.SegmentType.TURN, 27, 0.5);
        goParkInWareHousePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 30, 0.7);

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

        carouselMech = hardwareMap.get(DcMotor.class, "carouselMech");
        carouselDriveTrain = new OneWheelDirectDrivetrain(carouselMech);
        carouselDriveTrain.resetEncoders();
        carouselDriveTrain.encodersOn();

        gravelLift = hardwareMap.get(DcMotor.class, "gravelLift");
        gravelLiftDriveTrain = new OneWheelDirectDrivetrain(gravelLift);
        gravelLiftDriveTrain.resetEncoders();
        gravelLiftDriveTrain.encodersOn();

        intakeDrop = hardwareMap.servo.get("intakeDrop");
        intakeDrop.setPosition(INTAKEDROP_OPEN);

        //break for flipover
        gravelLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        freightIntake = hardwareMap.get(DcMotor.class, "freightIntake");
        intakeMechDriveTrain = new OneWheelDirectDrivetrain(freightIntake);
        intakeMechDriveTrain.resetEncoders();
        intakeMechDriveTrain.encodersOn();

        // initializing paths


        pathTlm = telemetry.addData("path status", "unknown");

        objectImageInfo = new ObjectImageInfo();
        objectImageInfo.displayTelemetry(this.telemetry);

        currentLocationTlm = telemetry.addData("current location", "init");
        objectDetectedTlm = telemetry.addData("Object detected", "unknown");

        positionTlm = telemetry.addData("Position:", "unknown");


        initPath();
        setObjectDetection();
        addTask(rdTask);
        //rdTask.start();


    }

    public void startPauseTimer() {
        pathTlm.setValue("in starting timer");
        rtTask = new SingleShotTimerTask(this, PAUSE_TIMER)
        {
            @Override
            public void handleEvent(RobotEvent e)
            {
                SingleShotTimerTask.SingleShotTimerEvent event = (SingleShotTimerEvent) e;

                if(event.kind == EventKind.EXPIRED)
                {

                    pathTlm.setValue("timer expired");
                    intakeDrop.setPosition(INTAKEDROP_OPEN);
                    if ( capStonePos == "bottom")
                    {

                        golowerMechBottom();
                    }
                    else if ( capStonePos == "middle")
                    {


                        golowerMechMiddle();

                    }


                    //no need for servo timer for top pos


                }
            }
        };

        addTask(rtTask);
    }

    public void setObjectDetection() {
        rdTask = new ObjectDetectionTask(this, "Webcam1") {
            @Override
            public void handleEvent(RobotEvent e) {

                ObjectDetectionEvent event = (ObjectDetectionEvent) e;
                capPositionLeft = event.objects.get(0).getLeft();
                capMidpoint = (event.objects.get(0).getWidth() / 2.0) + capPositionLeft;
                capImageWidth = event.objects.get(0).getImageWidth();
                if (event.kind == EventKind.OBJECTS_DETECTED) {
                    objectDetectedTlm.setValue(event.objects.get(0).getLabel());
                    currentLocationTlm.setValue(capMidpoint);
                    capLocation = capMidpoint;
                }
            }
        };
        rdTask.init(telemetry, hardwareMap);
        rdTask.setDetectionKind(ObjectDetectionTask.DetectionKind.EVERYTHING);

    }

    public void goToShippingHub(String capPosition) {
        this.addTask(new DeadReckonTask(this, goToShippingHubPath, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("arrived at carousel");
                    intakeDrop.setPosition(INTAKEDROP_OPEN);

                    if ( capPosition == "bottom")
                    {
                        goliftMechBottom();
                    }
                    else if ( capPosition == "middle")
                    {
                        goliftMechMiddle();

                    }
                    else if ( capPosition == "top")
                    {
                        goMoveForwardTop();
                    }


                }
            }
        });

    }

//    private void goliftMechInital() {
//        this.addTask(new DeadReckonTask(this, goliftMechInitalPath, gravelLiftDriveTrain) {
//            @Override
//            public void handleEvent(RobotEvent e) {
//                DeadReckonEvent path = (DeadReckonEvent) e;
//                if (path.kind == EventKind.PATH_DONE) {
//                    pathTlm.setValue("done lifting");
//                    goToShippingHub(capStonePos);
//
//
//                }
//            }
//        });
//    }

    /////////////////////////////////////////////////// Top Methods /////////////////////////////////////////////////////////////////


    private void goliftMechTop() {
        this.addTask(new DeadReckonTask(this, liftMechPathTop, gravelLiftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done lifting");
                    golowerMechTop();

                }
            }
        });
    }

    public void goMoveForwardTop() {
        this.addTask(new DeadReckonTask(this, goMoveForwardTopPath, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("arrived at carousel");
                    goliftMechTop();

                }
            }
        });

    }

    private void golowerMechTop() {
        this.addTask(new DeadReckonTask(this, lowerMechPathTop, gravelLiftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done lowering");
                    goParkInWareHouse();


                }
            }
        });
    }

    /////////////////////////////////////////////////// Middle Methods /////////////////////////////////////////////////////////////////


    private void goliftMechMiddle() {
        this.addTask(new DeadReckonTask(this, liftMechPathMiddle, gravelLiftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done lifting");
                    intakeDrop.setPosition(INTAKEDROP_OUT);
                    startPauseTimer();




                }
            }
        });
    }

    private void golowerMechMiddle() {
        this.addTask(new DeadReckonTask(this, lowerMechPathMiddle, gravelLiftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done lowering");
                    goParkInWareHouse();



                }
            }
        });
    }

    /////////////////////////////////////////////////// Bottom Methods /////////////////////////////////////////////////////////////////

//    public void goMoveForwardBottom() {
//        this.addTask(new DeadReckonTask(this, goMoveForwardBottomPath, drivetrain) {
//            @Override
//            public void handleEvent(RobotEvent e) {
//                DeadReckonEvent path = (DeadReckonEvent) e;
//                if (path.kind == EventKind.PATH_DONE) {
//                    pathTlm.setValue("arrived at carousel");
//                    goliftMechBottom();
//
//
//                }
//            }
//        });
//
//    }

    private void goliftMechBottom() {
        this.addTask(new DeadReckonTask(this, liftMechPathBottom, gravelLiftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done lifting");
                    intakeDrop.setPosition(INTAKEDROP_OUT);
                    startPauseTimer();


                }
            }
        });
    }

    private void golowerMechBottom() {
        this.addTask(new DeadReckonTask(this, lowerMechPathBottom, gravelLiftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done lowering");
                    goParkInWareHouse();


                }
            }
        });
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//    private void goOuttakePreloaded() {
//        this.addTask(new DeadReckonTask(this, outTakePath, intakeMechDriveTrain) {
//            @Override
//            public void handleEvent(RobotEvent e) {
//                DeadReckonEvent path = (DeadReckonEvent) e;
//                if (path.kind == EventKind.PATH_DONE) {
//                    pathTlm.setValue("done lifting");
//                    goParkInWareHouse();
//
//
//
//
//                }
//            }
//        });
//    }


    public void goParkInWareHouse() {
        this.addTask(new DeadReckonTask(this, goParkInWareHousePath, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("parked in Warehouse");



                }
            }
        });

    }


    @Override
    public void start() {

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
        goToShippingHub(capStonePos);
        //goliftMechInital();




    }



}