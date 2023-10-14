package opmodes.testingcode;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;


import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import com.acmerobotics.roadrunner.trajectorysequence.TrajectorySequence;
import com.acmerobotics.roadrunner.drive.DriveConstants;
import com.acmerobotics.roadrunner.drive.SampleMecanumDrive;
import com.acmerobotics.roadrunner.util.Timing;
import com.acmerobotics.roadrunner.drive.SampleMecanumDrive;




@Autonomous(name = "BlueWarehouse6", group = "Blue Warehouse")
public class BlueWarehouse extends LinearOpMode {

    SampleMecanumDrive drive;
    TrajectorySequence preloadTraj;
    TrajectorySequence preloadTrajMid;
    TrajectorySequence preloadTrajLow;

    TrajectorySequence firstCycleWareEntryTop;
    TrajectorySequence firstCycleWareEntryMid;
    TrajectorySequence firstCycleWareEntryLow;
    TrajectorySequence firstCycleWareCollect;
    TrajectorySequence firstCycleScore;

    TrajectorySequence secondCycleWareEntry;
    TrajectorySequence secondCycleWareCollect;
    TrajectorySequence secondCycleScore;

    TrajectorySequence thirdCycleWareEntry;
    TrajectorySequence thirdCycleWareCollect;
    TrajectorySequence thirdCycleScore;

    TrajectorySequence fourthCycleWareEntry;
    TrajectorySequence fourthCycleWareCollect;
    TrajectorySequence fourthCycleScore;

    TrajectorySequence fifthCycleWareEntry;
    TrajectorySequence fifthCycleWareCollect;
    TrajectorySequence fifthCycleScore;

    TrajectorySequence sixthCycleWareEntry;
    TrajectorySequence sixthCycleWareCollect;
    TrajectorySequence sixthCycleScore;

    Trajectory traj1;
    Trajectory traj2Top;
    Trajectory traj2Mid;
    Trajectory traj2Low;
    Trajectory traj3Low;
    Trajectory traj3Mid;
    Trajectory traj3Top;
    Trajectory traj4;
    Trajectory traj5;
    Trajectory traj6;
    TrajectorySequence traj7;
    TrajectorySequence traj8;
    Trajectory traj9;
    Trajectory traj10;
    Trajectory traj11;
    TrajectorySequence traj12;
    TrajectorySequence traj13;
    Trajectory traj15;
    Trajectory traj16;
    Trajectory traj17;
    TrajectorySequence traj18;
    TrajectorySequence traj19;
    Trajectory traj20;
    Trajectory traj21;
    TrajectorySequence traj23;
    TrajectorySequence traj24;
    Trajectory traj25;
    TrajectorySequence traj26;
    TrajectorySequence traj27;
    TrajectorySequence traj28;
    TrajectorySequence traj29;
    Trajectory traj30;
    Trajectory traj31;
    Trajectory traj32;
    Trajectory traj33;
    Trajectory traj34;

    TrajectorySequence entryTraj;

    Pose2d startPose;
    Pose2d newPose;
    Pose2d warehouseEntryPose;

    private DcMotor lb;
    private DcMotor lf;
    private DcMotor rb;
    private DcMotor rf;

    private DcMotor intakeMotor;
    private DcMotor duckMotor;
    private DcMotor leftSlideMotor;
    private DcMotor rightSlideMotor;

    private Servo depositServo;
    private Servo iLifterServo;
    private Servo gbServoRight;
    private Servo gbServoLeft;
    private Servo tseArmServo;
    private Servo tseClawServo;

    private RevColorSensorV3 colorSensor;
    private DistanceSensor distanceSensorLeft;
    private DistanceSensor distanceSensorRight;
    private DistanceSensor distanceSensorFront;

    private BNO055IMU imu;
    private Orientation angles;

    private Thread slideTopThread;
    private Thread slideMidThread;
    private Thread slideLowThread;
    private Thread slideIntermediateThread;
    private Thread scoreThread;
    private Thread scoreThreadFinal;
    private Thread intakeDown;
    private Thread scoreThread2;
    private Thread CaseC;
    private Thread sensorOuttake;
    private Thread scoreThreadMid;

    public double depositOpen = 0.02;
    public double depositClose = 0.34;
    public double depositRamp = 0.16;
    public double depositIntermediate = 0.12;

    public double rightOffset = 0;
    public double getHeadingOffset = 0;

    private double fourBarTopPos = 0.74;
    private double fourBarMidPos = 0.74;
    private double fourBarLowPos = 0.80;
    private double fourBarIntakePos = 0.01;
    private double fourBarIntermediatePos = 0.13;
    private double fourBarIntermediateScorePos = 0.04;

    private int slideLevel3Pos = 1550;
    private int slideLevel2Pos = 710;
    private int slideLevel1Pos = 278;
    private int slideIntermediate = 540;
    private int slideIntakePos = 0;

    double headingOffset;

    private double initPos = 0;
    private double intakePos = 0.27;

    private Timing.Timer scoreTimer;

   // private BarcodeUtil detector;
   // private BarCodeDetection.BarcodePosition barcodePosition = BarCodeDetection.BarcodePosition.NOT_FOUND;

    public void runOpMode() {
        drive = new SampleMecanumDrive(hardwareMap);

        lf = hardwareMap.get(DcMotor.class, "lf");
        rf = hardwareMap.get(DcMotor.class, "rf");
        lb = hardwareMap.get(DcMotor.class, "lb");
        rb = hardwareMap.get(DcMotor.class, "rb");

        intakeMotor = hardwareMap.get(DcMotor.class, "intakeMotor");
        duckMotor = hardwareMap.get(DcMotor.class, "duckMotor");
        leftSlideMotor = new Motor(hardwareMap, "leftSlideMotor");
        rightSlideMotor = new Motor(hardwareMap, "rightSlideMotor");

        depositServo = hardwareMap.get(Servo.class, "depositServo");
        iLifterServo = hardwareMap.get(Servo.class, "iLifterServo");
        gbServoLeft = hardwareMap.get(Servo.class, "gbServoLeft");
        gbServoRight = hardwareMap.get(Servo.class, "gbServoRight");
        tseArmServo = hardwareMap.get(Servo.class, "tseArmServo");
        tseClawServo = hardwareMap.get(Servo.class, "tseClawServo");

        colorSensor = hardwareMap.get(RevColorSensorV3.class, "cupSensor");
        distanceSensorLeft = hardwareMap.get(DistanceSensor.class, "distanceSensorLeft");
        distanceSensorRight = hardwareMap.get(DistanceSensor.class, "distanceSensorRight");

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        imu.initialize(parameters);

        leftSlideMotor.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        rightSlideMotor.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        leftSlideMotor.resetEncoder();
        rightSlideMotor.resetEncoder();

        // Invert motors
        intakeMotor.setDirection(DcMotor.Direction.REVERSE);
        duckMotor.setDirection(DcMotor.Direction.REVERSE);
        rightSlideMotor.setInverted(true);

        depositServo.setDirection(Servo.Direction.REVERSE);
        gbServoLeft.setDirection(Servo.Direction.REVERSE);
        tseArmServo.setDirection(Servo.Direction.REVERSE);

        iLifterServo.setPosition(0);
        depositServo.setPosition(depositClose);
        gbServoLeft.setPosition(0.022);
        gbServoRight.setPosition(0.022);
        tseArmServo.setPosition(0.02);
        tseClawServo.setPosition(0);

//        BarcodeUtil detector = new BarcodeUtil(hardwareMap, "Webcam 1", telemetry, 1);
//        detector.init();


        slideTopThread = new Thread(() -> {
            iLifterServo.setPosition(0.23);
            depositServo.setPosition(depositClose);

            scoreTimer = new Timing.Timer(100);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();
            intakeMotor.setPower(-0.35);
            moveFourBarIntermediate();
            moveSlideTop();
            moveFourBarTop();
            scoreTimer = new Timing.Timer(300);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();
            intakeMotor.setPower(0);
        });

        slideMidThread = new Thread(() -> {
            iLifterServo.setPosition(0.23);
            depositServo.setPosition(depositClose);

            scoreTimer = new Timing.Timer(100);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();
            intakeMotor.setPower(-0.35);
            moveFourBarIntermediate();
            moveSlideMid();
            moveFourBarMiddle();
            scoreTimer = new Timing.Timer(300);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();
            intakeMotor.setPower(0);
        });

        slideIntermediateThread = new Thread(() -> {
            iLifterServo.setPosition(0.23);
            depositServo.setPosition(depositClose);

            scoreTimer = new Timing.Timer(100);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();
            intakeMotor.setPower(-0.35);
            moveFourBarIntermediate();
            moveSlideIntermediate();
            scoreTimer = new Timing.Timer(300);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();
        });

        slideLowThread = new Thread(() -> {
            iLifterServo.setPosition(0.23);
            depositServo.setPosition(depositClose);

            scoreTimer = new Timing.Timer(100);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();
            intakeMotor.setPower(-0.1);
            moveFourBarIntermediate();
            moveSlideLow();
            moveFourBarLow();
            scoreTimer = new Timing.Timer(300);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();
            intakeMotor.setPower(0);
        });

        scoreThread2 = new Thread(() -> {
            depositServo.setPosition(depositRamp);

            scoreTimer = new Timing.Timer(1000);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            moveFourBarIntermediateScore();

            scoreTimer = new Timing.Timer(450);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            moveSlideIntake();
            moveFourBarIntake();

            scoreTimer = new Timing.Timer(200);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            intakeMotor.setPower(1);
            depositServo.setPosition(depositIntermediate);
        });

        scoreThread = new Thread(() -> {
            depositServo.setPosition(depositOpen);

            scoreTimer = new Timing.Timer(200);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            moveFourBarIntermediateScore();

            scoreTimer = new Timing.Timer(150);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            moveSlideIntake();
            moveFourBarIntake();

            scoreTimer = new Timing.Timer(200);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            intakeMotor.setPower(1);
            depositServo.setPosition(depositIntermediate);
        });

        scoreThreadMid = new Thread(() -> {
            depositServo.setPosition(depositOpen);

            scoreTimer = new Timing.Timer(200);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            moveFourBarIntake();

            scoreTimer = new Timing.Timer(500);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            moveSlideIntake();


            scoreTimer = new Timing.Timer(200);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            intakeMotor.setPower(1);
            depositServo.setPosition(depositIntermediate);
        });

        scoreThreadFinal = new Thread(() -> {
            depositServo.setPosition(depositOpen);

            scoreTimer = new Timing.Timer(200);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            moveFourBarIntermediateScore();

            scoreTimer = new Timing.Timer(150);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            moveSlideIntake();
            tseArmServo.setPosition(0.02);
            moveFourBarIntake();

            scoreTimer = new Timing.Timer(200);
            scoreTimer.start();
            while (!scoreTimer.done()) {

            }
            scoreTimer.pause();

            intakeMotor.setPower(1);
            depositServo.setPosition(depositIntermediate);
        });

        sensorOuttake = new Thread(() -> {
            depositServo.setPosition(depositClose);
            sleep(300);
            intakeMotor.setPower(-0.1);
        });

        intakeDown = new Thread(() -> {
            intakeDown();
        });

        startPose = new Pose2d(11, 61.63, -Math.toRadians(270));

        traj1 = drive.trajectoryBuilder(startPose)
                .back(5)
                .build();

        traj2Top = drive.trajectoryBuilder(traj1.end())
                .lineToLinearHeading(new Pose2d(-5, 47, -Math.toRadians(295)),
                        SampleMecanumDrive.getVelocityConstraint(35, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj2Mid = drive.trajectoryBuilder(traj1.end())
                .lineToLinearHeading(new Pose2d(-5, 43.5, -Math.toRadians(302)),
                        SampleMecanumDrive.getVelocityConstraint(40, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj2Low = drive.trajectoryBuilder(traj1.end())
                .lineToLinearHeading(new Pose2d(-5.5, 42, -Math.toRadians(295)),
                        SampleMecanumDrive.getVelocityConstraint(40, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj3Low = drive.trajectoryBuilder(traj2Low.end())
                .splineToLinearHeading(new Pose2d(17, 64, Math.toRadians(360)), -Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(60, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj3Mid = drive.trajectoryBuilder(traj2Mid.end())
                .splineToLinearHeading(new Pose2d(17, 64, Math.toRadians(360)), -Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj3Top = drive.trajectoryBuilder(traj2Top.end())
                .splineToLinearHeading(new Pose2d(17, 64, Math.toRadians(360)), -Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj4 = drive.trajectoryBuilder(traj3Low.end())
                .splineToLinearHeading(new Pose2d(43, 64), -Math.toRadians(360))
                .build();

        traj5 = drive.trajectoryBuilder(traj4.end())
                .lineToLinearHeading(new Pose2d(48, 64, -Math.toRadians(360)),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();


        traj7 = drive.trajectorySequenceBuilder(traj4.end())
                .setReversed(true)
                .lineToSplineHeading(new Pose2d(22, 66, -Math.toRadians(360)))
                .splineTo(new Vector2d(-5, 44.2), Math.toRadians(260),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj8 = drive.trajectorySequenceBuilder(traj7.end())
                .setReversed(false)
                .splineToSplineHeading(new Pose2d(17, 64, Math.toRadians(360)), Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj9 = drive.trajectoryBuilder(traj8.end())
                .splineToLinearHeading(new Pose2d(43.5, 64), -Math.toRadians(360))
                .build();

        traj10 = drive.trajectoryBuilder(traj9.end())
                .lineToLinearHeading(new Pose2d(51, 63, Math.toRadians(350)),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();


        traj12 = drive.trajectorySequenceBuilder(traj9.end())
                .setReversed(true)
                .lineToSplineHeading(new Pose2d(22, 66, -Math.toRadians(360)))
                .splineTo(new Vector2d(-5, 44), Math.toRadians(260),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj13 = drive.trajectorySequenceBuilder(traj12.end())
                .setReversed(false)
                .splineToSplineHeading(new Pose2d(17, 64, Math.toRadians(360)), -Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj15 = drive.trajectoryBuilder(traj13.end())
                .splineToLinearHeading(new Pose2d(45, 64), -Math.toRadians(360))
                .build();

        traj16 = drive.trajectoryBuilder(traj15.end())
                .lineToLinearHeading(new Pose2d(55, 64, -Math.toRadians(360)),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();


        traj18 = drive.trajectorySequenceBuilder(traj15.end())
                .setReversed(true)
                .lineToSplineHeading(new Pose2d(22, 66, -Math.toRadians(360)))
                .splineTo(new Vector2d(-5, 43.5), Math.toRadians(257),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj19 = drive.trajectorySequenceBuilder(traj18.end())
                .setReversed(false)
                .splineToSplineHeading(new Pose2d(17, 64, Math.toRadians(360)), -Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj20 = drive.trajectoryBuilder(traj19.end())
                .splineToLinearHeading(new Pose2d(48, 64, -Math.toRadians(360)), -Math.toRadians(360))
                .build();

        traj21 = drive.trajectoryBuilder(traj20.end())
                .lineToLinearHeading(new Pose2d(55, 60, -Math.toRadians(380)),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj23 = drive.trajectorySequenceBuilder(traj20.end())
                .setReversed(true)
                .lineToSplineHeading(new Pose2d(22, 66, -Math.toRadians(360)))
                .splineTo(new Vector2d(-5, 43.5), Math.toRadians(258),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj24 = drive.trajectorySequenceBuilder(traj23.end())
                .setReversed(false)
                .splineToSplineHeading(new Pose2d(18, 64, Math.toRadians(360)), -Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj25 = drive.trajectoryBuilder(traj24.end())
                .splineToLinearHeading(new Pose2d(48, 64, -Math.toRadians(370)), -Math.toRadians(360))
                .build();

        traj26 = drive.trajectorySequenceBuilder(traj25.end())
                .lineToLinearHeading(new Pose2d(56, 60, -Math.toRadians(380)),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj27 = drive.trajectorySequenceBuilder(traj25.end())
                .setReversed(true)
                .lineToSplineHeading(new Pose2d(22, 66, -Math.toRadians(360)))
                .splineTo(new Vector2d(-5, 43.5), Math.toRadians(259),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        traj28 = drive.trajectorySequenceBuilder(traj27.end())
                .setReversed(false)
                .splineToSplineHeading(new Pose2d(17, 64, Math.toRadians(360)), -Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .splineToLinearHeading(new Pose2d(44, 64, -Math.toRadians(360)), -Math.toRadians(360))
                .build();


        /*preloadTraj = drive.trajectorySequenceBuilder(startPose)
                .back(5)
                .addTemporalMarker(0, () -> {
                    slideTopThread.start();
                })
                .build();

        preloadTrajMid = drive.trajectorySequenceBuilder(startPose)
                .back(3)
                .addTemporalMarker(0, () -> {
                    slideMidThread.start();
                })
                .build();

        preloadTrajLow = drive.trajectorySequenceBuilder(startPose)
                .back(3.5)
                .addTemporalMarker(0, () -> {
                    slideLowThread.start();
                })
                .build();

        firstCycleWareEntryTop = drive.trajectorySequenceBuilder(preloadTraj.end())
                .lineToLinearHeading(new Pose2d(-5, -41, Math.toRadians(290)),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .waitSeconds(0.2)
                .addTemporalMarker(1.1, () -> {
                    scoreThread.start();
                })
                .splineToSplineHeading(new Pose2d(20, -58.5, Math.toRadians(360)), Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .splineToLinearHeading(new Pose2d(42, -59), Math.toRadians(360))
                .build();

        firstCycleWareEntryMid = drive.trajectorySequenceBuilder(preloadTraj.end())
                .lineToLinearHeading(new Pose2d(-0, -39.7, Math.toRadians(295)),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .waitSeconds(0.2)
                .addTemporalMarker(0.7, () -> {
                    scoreThread.start();
                })
                .splineToSplineHeading(new Pose2d(20, -58, Math.toRadians(360)), Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .splineToLinearHeading(new Pose2d(42, -59), Math.toRadians(360))
                .build();

        firstCycleWareEntryLow = drive.trajectorySequenceBuilder(preloadTraj.end())
                .lineToLinearHeading(new Pose2d(-5, -41.2, Math.toRadians(295)),
                        SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .waitSeconds(0.2)
                .addTemporalMarker(1, () -> {
                    scoreThread2.start();
                })
                .splineToSplineHeading(new Pose2d(20, -58, Math.toRadians(360)), Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .waitSeconds(0.25)
                .splineToLinearHeading(new Pose2d(42, -59), Math.toRadians(360))
                .build();


        firstCycleWareCollect = drive.trajectorySequenceBuilder(firstCycleWareEntryTop.end())
                .lineToLinearHeading(new Pose2d(50, -58.5, Math.toRadians(360)),
                        SampleMecanumDrive.getVelocityConstraint(20, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();

        firstCycleScore = drive.trajectorySequenceBuilder(firstCycleWareCollect.end())
                .lineToLinearHeading(new Pose2d(35, -59, Math.toRadians(360)))
                .setReversed(true)
                .splineTo(new Vector2d(5, -36.5), Math.toRadians(135),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .addTemporalMarker(0.6, () -> slideTopThread.start())
                .addTemporalMarker(4.7, () -> scoreThread.start())
                .build();

        secondCycleWareEntry = drive.trajectorySequenceBuilder(firstCycleScore.end())
                .setReversed(false)
                .splineToSplineHeading(new Pose2d(20, -58, Math.toRadians(360)), Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(51, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .splineToLinearHeading(new Pose2d(43, -58.5), Math.toRadians(360))
                .build();*/

        /*secondCycleWareCollect = drive.trajectorySequenceBuilder(secondCycleWareEntry.end())
                .lineToLinearHeading(new Pose2d(51, -57, Math.toRadians(360)),
                        SampleMecanumDrive.getVelocityConstraint(20, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();*/

        /*secondCycleScore = drive.trajectorySequenceBuilder(secondCycleWareCollect.end())
                .lineToLinearHeading(new Pose2d(35, -59.5, Math.toRadians(360)))
                .setReversed(true)
                .splineTo(new Vector2d(5, -36), Math.toRadians(135),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .addTemporalMarker(0.6, () -> slideTopThread.start())
                .addTemporalMarker(4.5, () -> scoreThread.start())
                .build();*/

        /*thirdCycleWareEntry = drive.trajectorySequenceBuilder(secondCycleScore.end())
                .setReversed(false)
                .splineToSplineHeading(new Pose2d(20, -58, Math.toRadians(360)), Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .splineToLinearHeading(new Pose2d(44, -59), Math.toRadians(360))
                .build();*/

        /*thirdCycleWareCollect = drive.trajectorySequenceBuilder(thirdCycleWareEntry.end())
                .lineToLinearHeading(new Pose2d(55, -55, Math.toRadians(380)),
                        SampleMecanumDrive.getVelocityConstraint(20, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();*/

        /*thirdCycleScore = drive.trajectorySequenceBuilder(thirdCycleWareCollect.end())
                .lineToLinearHeading(new Pose2d(35, -59, Math.toRadians(360)))
                .setReversed(true)
                .splineTo(new Vector2d(2, -36), Math.toRadians(133),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .addTemporalMarker(0.6, () -> slideTopThread.start())
                .addTemporalMarker(4.5, () -> scoreThread.start())
                .build();*/

        /*fourthCycleWareEntry = drive.trajectorySequenceBuilder(thirdCycleScore.end())
                .setReversed(false)
                .splineToSplineHeading(new Pose2d( 18, -58.5, Math.toRadians(360)), Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .splineToLinearHeading(new Pose2d(45, -58.5, Math.toRadians(380)), -Math.toRadians(360))
                .build();*/

        /*fourthCycleWareCollect = drive.trajectorySequenceBuilder(fourthCycleWareEntry.end())
                .lineToLinearHeading(new Pose2d(55, -54, Math.toRadians(380)),
                        SampleMecanumDrive.getVelocityConstraint(20, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();*/

        /*fourthCycleScore = drive.trajectorySequenceBuilder(fourthCycleWareCollect.end())
                .lineToLinearHeading(new Pose2d(35, -59, Math.toRadians(360)))
                .setReversed(true)
                .splineTo(new Vector2d(2, -37), Math.toRadians(127),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .addTemporalMarker(0.6, () -> slideTopThread.start())
                .addTemporalMarker(2.4, () -> scoreThread.start())
                .setReversed(false)
                .build();*/

        /*fifthCycleWareEntry = drive.trajectorySequenceBuilder(fourthCycleScore.end())
                .splineToSplineHeading(new Pose2d(18, -58.5, Math.toRadians(360)), Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .splineToLinearHeading(new Pose2d(5`0, -58, Math.toRadians(380)), -Math.toRadians(360))
                .build();*/

        /*fifthCycleWareCollect = drive.trajectorySequenceBuilder(fifthCycleWareEntry.end())
                .lineToLinearHeading(new Pose2d(59, -53, Math.toRadians(380)),
                        SampleMecanumDrive.getVelocityConstraint(20, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .build();*/

        /*fifthCycleScore = drive.trajectorySequenceBuilder(fifthCycleWareCollect.end())
                .lineToLinearHeading(new Pose2d(35, -59, Math.toRadians(360)))
                .setReversed(true)
                .splineTo(new Vector2d(2, -35), Math.toRadians(130),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .addTemporalMarker(0.6, () -> slideTopThread.start())
                .addTemporalMarker(3, () -> scoreThreadFinal.start())
                .setReversed(false)
                .build();*/

        /*sixthCycleWareEntry = drive.trajectorySequenceBuilder(fifthCycleScore.end())
                .splineToSplineHeading(new Pose2d(17, -59, Math.toRadians(360)), Math.toRadians(360),
                        SampleMecanumDrive.getVelocityConstraint(55, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .splineToLinearHeading(new Pose2d(45, -59), Math.toRadians(360))
                .addTemporalMarker(1, () -> tseArmServo.setPosition(0.05))
                .build();*/

        ///// BARCOCE  COMMENT OUT

//        while (!isStopRequested() && !isStarted()) {
//            telemetry.addData("Element position", detector.getBarcodePosition());
//            telemetry.update();
//            barcodePosition = detector.getBarcodePosition();
//        }
//
//        Thread stopCamera = new Thread(() -> detector.stopCamera());
//        stopCamera.start();
//
//        waitForStart();
//        tseArmServo.setPosition(0.3);
//
//        if(!isStopRequested()) {
//            if(barcodePosition == BarCodeDetection.BarcodePosition.LEFT)
//                CaseA(drive);
//            else if(barcodePosition == BarCodeDetection.BarcodePosition.MIDDLE)
//                CaseB(drive);
//            else
//                CaseC(drive);
//
//            intakeMotor.setPower(0);
//            sleep(30000);
//        }
//    }

        private void CaseC (SampleMecanumDrive drive){
            drive.setPoseEstimate(startPose);

            drive.followTrajectory(traj1);

            if (!isStopRequested())
                slideTopThread.start();

            drive.followTrajectory(traj2Top);
            sleep(100);

            if (!isStopRequested())
                scoreThread.start();

            drive.followTrajectory(traj3Top);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj4);

            drive.followTrajectoryAsync(traj5);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();

            drive.followTrajectorySequence(traj7);
            sleep(25);
            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj8);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj9);

            drive.followTrajectoryAsync(traj10);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj12);
            sleep(25);
            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj13);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj15);

            drive.followTrajectoryAsync(traj16);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj18);
            sleep(25);

            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj19);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj20);

            drive.followTrajectoryAsync(traj21);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj23);
            sleep(50);
            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj24);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj25);
            drive.followTrajectorySequenceAsync(traj26);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }
            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj27);
            if (!isStopRequested())
                scoreThreadFinal.start();
            drive.followTrajectorySequence(traj28);
        }

        private void CaseB (SampleMecanumDrive drive){
            drive.setPoseEstimate(startPose);

            drive.followTrajectory(traj1);

            if (!isStopRequested())
                slideMidThread.start();

            drive.followTrajectory(traj2Mid);
            sleep(100);

            if (!isStopRequested())
                scoreThreadMid.start();

            sleep(100);

            drive.followTrajectory(traj3Mid);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj4);

            drive.followTrajectoryAsync(traj5);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();

            drive.followTrajectorySequence(traj7);
            sleep(25);
            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj8);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj9);

            drive.followTrajectoryAsync(traj10);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj12);
            sleep(25);
            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj13);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj15);

            drive.followTrajectoryAsync(traj16);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj18);
            sleep(25);

            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj19);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj20);

            drive.followTrajectoryAsync(traj21);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj23);
            sleep(50);
            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj24);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj25);
            drive.followTrajectorySequenceAsync(traj26);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }
            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj27);
            if (!isStopRequested())
                scoreThreadFinal.start();
            drive.followTrajectorySequence(traj28);
        }

        private void CaseA (SampleMecanumDrive drive){
            drive.setPoseEstimate(startPose);

            drive.followTrajectory(traj1);

            if (!isStopRequested())
                slideLowThread.start();

            drive.followTrajectory(traj2Low);

            if (!isStopRequested())
                scoreThreadMid.start();

            sleep(200);

            drive.followTrajectory(traj3Low);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj4);

            drive.followTrajectoryAsync(traj5);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();

            drive.followTrajectorySequence(traj7);
            sleep(25);
            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj8);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj9);

            drive.followTrajectoryAsync(traj10);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj12);
            sleep(25);
            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj13);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj15);

            drive.followTrajectoryAsync(traj16);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj18);
            sleep(25);

            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj19);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj20);

            drive.followTrajectoryAsync(traj21);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }

            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj23);
            sleep(50);
            if (!isStopRequested())
                scoreThread.start();
            drive.followTrajectorySequence(traj24);

            if (distanceSensorLeft.getDistance(DistanceUnit.INCH) <= 80) {
                drive.update();
                newPose = drive.getPoseEstimate();
                drive.setPoseEstimate(new Pose2d(newPose.getX(), 70 - distanceSensorLeft.getDistance(DistanceUnit.INCH) - 6.161, -Math.toRadians(270) + imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle));
                drive.update();
            }

            drive.followTrajectory(traj25);
            drive.followTrajectorySequenceAsync(traj26);
            while (drive.isBusy() && opModeIsActive()) {
                if (depositgetDistance() < 4.5) {
                    drive.breakFollowing();
                    drive.setDrivePower(new Pose2d());
                    sensorOuttake.start();
                }

                drive.update();
            }
            if (!isStopRequested())
                slideTopThread.start();
            drive.followTrajectorySequence(traj27);
            if (!isStopRequested())
                scoreThreadFinal.start();
            drive.followTrajectorySequence(traj28);
        }

        public void moveSlideTop () {
            leftSlideMotor.setRunMode(Motor.RunMode.PositionControl);
            rightSlideMotor.setRunMode(Motor.RunMode.PositionControl);

            // set the target position
            leftSlideMotor.setTargetPosition(slideLevel3Pos); // an integer representing desired tick count
            rightSlideMotor.setTargetPosition(slideLevel3Pos);

            leftSlideMotor.set(0);
            rightSlideMotor.set(0);

            // set the tolerance
            leftSlideMotor.setPositionTolerance(13.6);   // allowed maximum error
            rightSlideMotor.setPositionTolerance(13.6);

            // perform the control loop
            while (!leftSlideMotor.atTargetPosition() && !isStopRequested()) {
                leftSlideMotor.set(0.8);
                rightSlideMotor.set(0.8);
            }
            leftSlideMotor.stopMotor();
            rightSlideMotor.stopMotor();// stop the motor
        }

        public void moveSlideMid () {
            leftSlideMotor.setRunMode(Motor.RunMode.PositionControl);
            rightSlideMotor.setRunMode(Motor.RunMode.PositionControl);

            // set the target position
            leftSlideMotor.setTargetPosition(slideLevel2Pos); // an integer representing desired tick count
            rightSlideMotor.setTargetPosition(slideLevel2Pos);

            leftSlideMotor.set(0);
            rightSlideMotor.set(0);

            // set the tolerance
            leftSlideMotor.setPositionTolerance(13.6);   // allowed maximum error
            rightSlideMotor.setPositionTolerance(13.6);

            // perform the control loop
            while (!leftSlideMotor.atTargetPosition() && !isStopRequested()) {
                leftSlideMotor.set(1);
                rightSlideMotor.set(1);
            }
            leftSlideMotor.stopMotor();
            rightSlideMotor.stopMotor();// stop the motor
        }

        public void moveSlideLow () {
            leftSlideMotor.setRunMode(Motor.RunMode.PositionControl);
            rightSlideMotor.setRunMode(Motor.RunMode.PositionControl);

            // set the target position
            leftSlideMotor.setTargetPosition(slideLevel1Pos); // an integer representing desired tick count
            rightSlideMotor.setTargetPosition(slideLevel1Pos);

            leftSlideMotor.set(0);
            rightSlideMotor.set(0);

            // set the tolerance
            leftSlideMotor.setPositionTolerance(13.6);   // allowed maximum error
            rightSlideMotor.setPositionTolerance(13.6);

            // perform the control loop
            while (!leftSlideMotor.atTargetPosition() && !isStopRequested()) {
                leftSlideMotor.set(1);
                rightSlideMotor.set(1);
            }
            //leftSlideMotor.stopMotor();
           // rightSlideMotor.stopMotor();// stop the motor
        }

        public void moveSlideIntake () {
            //leftSlideMotor.setRunMode(Motor.RunMode.PositionControl);
           // rightSlideMotor.setRunMode(Motor.RunMode.PositionControl);

            // set the target position
            leftSlideMotor.setTargetPosition(slideIntakePos); // an integer representing desired tick count
            rightSlideMotor.setTargetPosition(slideIntakePos);

            leftSlideMotor.set(0);
            rightSlideMotor.set(0);

            // set the tolerance
            leftSlideMotor.setPositionTolerance(13.6);   // allowed maximum error
            rightSlideMotor.setPositionTolerance(13.6);

            // perform the control loop
            while (!leftSlideMotor.atTargetPosition() && !isStopRequested()) {
                leftSlideMotor.set(1);
                rightSlideMotor.set(1);
            }
            leftSlideMotor.stopMotor();
            rightSlideMotor.stopMotor();// stop the motor
        }

        public void moveSlideIntermediate () {
            leftSlideMotor.setRunMode(Motor.RunMode.PositionControl);
            rightSlideMotor.setRunMode(Motor.RunMode.PositionControl);

            // set the target position
            leftSlideMotor.setTargetPosition(slideIntermediate); // an integer representing desired tick count
            rightSlideMotor.setTargetPosition(slideIntermediate);

            leftSlideMotor.set(0);
            rightSlideMotor.set(0);

            // set the tolerance
            leftSlideMotor.setPositionTolerance(13.6);   // allowed maximum error
            rightSlideMotor.setPositionTolerance(13.6);

            // perform the control loop
            while (!leftSlideMotor.atTargetPosition() && !isStopRequested()) {
                leftSlideMotor.set(1);
                rightSlideMotor.set(1);
            }
            leftSlideMotor.stopMotor();
            rightSlideMotor.stopMotor();// stop the motor
        }

        public void moveFourBarTop () {
            gbServoRight.setPosition(fourBarTopPos);
            gbServoLeft.setPosition(fourBarTopPos);
        }

        public void moveFourBarMiddle() {
            gbServoRight.setPosition(fourBarMidPos);
            gbServoLeft.setPosition(fourBarMidPos);
        }

        public void moveFourBarLow() {
            gbServoRight.setPosition(fourBarLowPos);
            gbServoLeft.setPosition(fourBarLowPos);
        }


        public void moveFourBarIntermediate () {
            gbServoRight.setPosition(fourBarIntermediatePos);
            gbServoLeft.setPosition(fourBarIntermediatePos);
        }

        public void moveFourBarIntermediateScore () {
            gbServoLeft.setPosition(fourBarIntermediateScorePos);
            gbServoRight.setPosition(fourBarIntermediateScorePos);
        }

        public void moveFourBarIntake () {
            gbServoRight.setPosition(fourBarIntakePos);
            gbServoLeft.setPosition(fourBarIntakePos);
        }

        public void intakeDown () {
            iLifterServo.setPosition(0);
        }

        public void rampDeposit () {
            depositServo.setPosition(depositRamp);
        }

        public double getRawExternalHeading () {
            return imu.getAngularOrientation().firstAngle;
        }

        public double rightOffset () {
            return distanceSensorRight.getDistance(DistanceUnit.INCH);
        }
        public double headingOffset () {
            return headingOffset = 270 + angles.firstAngle;
        }

        private double depositgetDistance () {
            return 2.0;
            //colorSensor.getDistance(DistanceUnit.CM);
        }
        private void driveTime ( int milliseconds){
            Timing.Timer scoreTimer = new Timing.Timer(milliseconds);
            scoreTimer.start();
            while (!scoreTimer.done()) {
                lb.setPower(-0.5);
                lf.setPower(0.5);
                rf.setPower(-0.5);
                rb.setPower(0.5);
            }
            scoreTimer.pause();
            lf.setPower(0);
            rf.setPower(0);
            lb.setPower(0);
            rb.setPower(0);
        }


    }
}