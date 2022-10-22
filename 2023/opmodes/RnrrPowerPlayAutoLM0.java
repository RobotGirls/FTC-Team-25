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

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;
//import org.openftc.apriltag.AprilTagDetection;

import team25core.vision.apriltags.AprilTagDetectionTask;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.MechanumGearedDrivetrain;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;


@Autonomous(name = "aprilTagsAuto", group="Team-25")
//@Disabled
public class RnrrPowerPlayAutoLM0 extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;


    private FourWheelDirectDrivetrain drivetrain;

    private DeadReckonPath leftPath;
    private DeadReckonPath middlePath;
    private DeadReckonPath rightPath;

    static final double FORWARD_DISTANCE = 6;
    static final double DRIVE_SPEED = -0.5;


    private Telemetry.Item tagIdTlm;

    //AprilTagDetection tagObject;
    private AprilTagDetectionTask detectionTask;

    private Telemetry.Item whereAmITlm;

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

//    public void setAprilTagDetection() {
//        whereAmITlm.setValue("before detectionTask");
//        detectionTask = new AprilTagDetectionTask(this, "Webcam 1") {
//            @Override
//            public void handleEvent(RobotEvent e) {
//               TagDetectionEvent event = (TagDetectionEvent) e;
//               tagObject = event.tagObject;
//               //tagIdTlm.setValue(tagObject.id);
//                whereAmI.setValue("in handleEvent");
//            }
//        };
//        whereAmI.setValue("setAprilTagDetection");
//        detectionTask.init(telemetry, hardwareMap);
//    }



    public void driveToSignalZone(DeadReckonPath signalPath)
    {
        whereAmITlm.setValue("in driveToSignalZone");
        RobotLog.i("drives straight onto the launch line");


        //starts when you have stone and want to move
        this.addTask(new DeadReckonTask(this, signalPath, drivetrain){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("finished parking");

                }
            }
        });
    }


    public void initPaths()
    {
        leftPath = new DeadReckonPath();
        middlePath = new DeadReckonPath();
        rightPath= new DeadReckonPath();


        leftPath.stop();
        middlePath.stop();
        rightPath.stop();

        //going forward then to the left
        leftPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE, DRIVE_SPEED);
        leftPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, FORWARD_DISTANCE, -DRIVE_SPEED);
        //going forward
        middlePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, FORWARD_DISTANCE, DRIVE_SPEED);
        //going forward then right
        rightPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,FORWARD_DISTANCE,DRIVE_SPEED);
        rightPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,FORWARD_DISTANCE,DRIVE_SPEED);

    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");


        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, backRight, frontLeft, backLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();

        whereAmITlm = telemetry.addData("location in code", "init");
        tagIdTlm = telemetry.addData("tagId","none");
        initPaths();


    }

    @Override
    public void start()
    {
        driveToSignalZone(rightPath);
        //whereAmI.setValue("in Start");
        //setAprilTagDetection();
        //addTask(detectionTask);
    }
}
