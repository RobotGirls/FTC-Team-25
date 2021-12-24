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
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;

@Autonomous(name = "FrenzyAutoLM3B")
//@Disabled
// Auto that turns carousel then drops preloaded then parks on BLUE SIDE
public class FrenzyAutoLM2CarB2 extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;

    private DcMotor flipOver;
    private DeadReckonPath liftFlipOverPath;
    private DeadReckonPath lowerFlipOverPath;
    private OneWheelDirectDrivetrain liftDriveTrain;

    private OneWheelDirectDrivetrain intakeMechDriveTrain;
    private DcMotor freightIntake;

    //private Servo teamElementServo;
    private OneWheelDirectDrivetrain carouselDriveTrain;
    private DcMotor carouselMech;

    private DeadReckonPath turningCarouselPath;
    private DeadReckonPath goToCarouselPath;
    private DeadReckonPath goStrafeLeftPath;
    private DeadReckonPath outTakePath;

    private DeadReckonPath goParkPath;




    private Telemetry.Item pathTlm;

    private FourWheelDirectDrivetrain drivetrain;


    /**
     * The default event handler for the robot.
     */
    @Override
    public void handleEvent(RobotEvent e)
    {
        /**
         * Every time we complete a segment drop a note in the robot log.
         */
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
    }




    private void liftFlipOver()
    {
        this.addTask(new DeadReckonTask(this, liftFlipOverPath, liftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
//                    pathTlm.setValue("done spinning carousel");
                    goOuttakePreloaded();

                }
            }
        });
    }

    private void goOuttakePreloaded()
    {
        this.addTask(new DeadReckonTask(this, outTakePath, intakeMechDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done lifting");
                    lowerArm();



                }
            }
        });
    }

    private void lowerArm()
    {
        this.addTask(new DeadReckonTask(this, lowerFlipOverPath, liftDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
//                    pathTlm.setValue("done spinning carousel");
                    goPark();

                }
            }
        });
    }

    public void initPath()
    {
        // 1
        goToCarouselPath = new DeadReckonPath();
        //goToCarouselPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 6, -1.0);
        goToCarouselPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8.5, 0.5);


        // 2
        turningCarouselPath = new DeadReckonPath();
        turningCarouselPath.stop();
        turningCarouselPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 60, -1.0);

        //3
//        goBackOriginalLocationPath = new DeadReckonPath();
//        goBackOriginalLocationPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7.5, 1.0);

        goStrafeLeftPath = new DeadReckonPath();
        //goStrafeLeftPath.addSegment(DeadReckonPath.SegmentType.TURN, 5, -0.5);
        goStrafeLeftPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 22.5 , -0.25);
        goStrafeLeftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4.3, -0.5);



        liftFlipOverPath = new DeadReckonPath();
        liftFlipOverPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, -0.25);

        outTakePath = new DeadReckonPath();
        outTakePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, 1.0);

        lowerFlipOverPath = new DeadReckonPath();
        lowerFlipOverPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 0.25);

        goParkPath = new DeadReckonPath();
        goParkPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 6.5, 0.5);
        goParkPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 9 , 0.25);





    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        rearLeft = hardwareMap.get(DcMotor.class, "backLeft");
        rearRight = hardwareMap.get(DcMotor.class, "backRight");

        carouselMech = hardwareMap.get(DcMotor.class, "carouselMech");

        carouselDriveTrain = new OneWheelDirectDrivetrain(carouselMech);
        carouselDriveTrain.resetEncoders();
        carouselDriveTrain.encodersOn();

        flipOver = hardwareMap.get(DcMotor.class, "flipOver");
        flipOver.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flipOver.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        liftDriveTrain = new OneWheelDirectDrivetrain(flipOver);
        liftDriveTrain.resetEncoders();
        liftDriveTrain.encodersOn();

        freightIntake = hardwareMap.get(DcMotor.class, "freightIntake");
        intakeMechDriveTrain = new OneWheelDirectDrivetrain(freightIntake);
        intakeMechDriveTrain.resetEncoders();
        intakeMechDriveTrain.encodersOn();

        flipOver.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        // initializing paths
        initPath();

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();



        pathTlm = telemetry.addData("path status","unknown");
    }

    public void goToCarousel()
    {
        this.addTask(new DeadReckonTask(this, goToCarouselPath, drivetrain){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    pathTlm.setValue("arrived at carousel");
                    spinCarousel();

                }
            }
        });

    }

    private void spinCarousel()
    {
        this.addTask(new DeadReckonTask(this, turningCarouselPath, carouselDriveTrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    pathTlm.setValue("done spinning carousel");
                    //goBackOriginalLocation();
                    goStrafeLeft();

                }
            }
        });
    }


//    public void goBackOriginalLocation()
//    {
//        this.addTask(new DeadReckonTask(this, goBackOriginalLocationPath, drivetrain){
//            @Override
//            public void handleEvent(RobotEvent e) {
//                DeadReckonEvent path = (DeadReckonEvent) e;
//                if (path.kind == EventKind.PATH_DONE)
//                {
//                    pathTlm.setValue("went back to original positions");
//                    //goStrafeLeft();
//                    //goParkInWarehouse();
//
//
//                }
//            }
//        });
//
//    }

    public void goStrafeLeft()
    {
        this.addTask(new DeadReckonTask(this, goStrafeLeftPath, drivetrain){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    pathTlm.setValue("strafe left done");
                    liftFlipOver();



                }
            }
        });

    }

    public void goPark()
    {
        this.addTask(new DeadReckonTask(this, goParkPath, drivetrain){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    pathTlm.setValue("strafe left done");




                }
            }
        });

    }




//    public void goParkInWarehouse()
//    {
//        this.addTask(new DeadReckonTask(this, goParkInStoragePath, drivetrain){
//            @Override
//            public void handleEvent(RobotEvent e) {
//                DeadReckonEvent path = (DeadReckonEvent) e;
//                if (path.kind == EventKind.PATH_DONE)
//                {
//                    pathTlm.setValue("parked in Storage");
//
//
//                }
//            }
//        });
//
//    }



    @Override
    public void start()
    {
        DeadReckonPath path = new DeadReckonPath();
        goToCarousel();



    }



}