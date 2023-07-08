package opmodes;


import static team25core.IMUTableConfiguration.STRAIGHT_SPEED;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.KatelynsFourWheelDrivetrain;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;

@Autonomous(name = "KatelynAuto",group = "rnrr")
public class RnRRKatelynBasicAuto extends StandardFourMotorRobot {


    //private KatelynsFourWheelDrivetrain drivetrain;
    private FourWheelDirectDrivetrain drivetrain;
    private DeadReckonPath straightPath;

    private DeadReckonPath strafePath;

    private DeadReckonPath turnPath;

    private Telemetry.Item eventTlm;

    private static String TAG = "KatelynAuto";

    @Override
    public void init() {
        super.init(); // taking parent's init (hardware mapping)
        //instantiating the KatelynsFourWheelDrivetrain class
        //drivetrain = new KatelynsFourWheelDrivetrain(frontRight, backRight, frontLeft, backLeft);
        drivetrain = new FourWheelDirectDrivetrain(frontRight, backRight, frontLeft, backLeft);
        // uncomment the following only if the robot is going in the opposite direction from intended
        //drivetrain.setCanonicalMotorDirection();

        // motor will try to run at the targeted velocity
        drivetrain.encodersOn();

        // Sets the behavior of the motor when a power level of zero is applied i.e. not moving - when we apply 0 power, the motor brakes
        //drivetrain.brakeOnZeroPower();

        // Sets motor encoder position to 0
        drivetrain.resetEncoders();

        eventTlm = telemetry.addData("pathEvent", "none");

        initPaths();
    }

    public void initPaths() {
        straightPath = new DeadReckonPath();
        strafePath = new DeadReckonPath();
        turnPath = new DeadReckonPath();

        straightPath.stop();
        strafePath.stop();
        turnPath.stop();

        straightPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,1.2, 0.3);
        strafePath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 2, 0.3);
        turnPath.addSegment(DeadReckonPath.SegmentType.TURN, 1.5, 0.3);
    }

    public void goForward() {
        this.addTask(new DeadReckonTask(this, straightPath, drivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                DeadReckonEvent pathEvent = (DeadReckonEvent) e;
                if (pathEvent.kind == EventKind.PATH_DONE)
                {
                    RobotLog.ii(TAG, "went forward");
                    eventTlm.setValue("path is done");
                    strafe();
                }
                else if (pathEvent.kind == EventKind.SEGMENT_DONE) {
                    RobotLog.ii(TAG, "segment completed");
                    eventTlm.setValue("segment is done");
                }
            }
        });
    }

    public void strafe() {
        this.addTask(new DeadReckonTask(this, strafePath, drivetrain){
            @Override
            public void handleEvent (RobotEvent e) {
                DeadReckonEvent pathEvent = (DeadReckonEvent) e;
                if (pathEvent.kind == EventKind.PATH_DONE) {
                    RobotLog.ii(TAG, "strafed");
                    eventTlm.setValue("path is done");
                    turnRight();
                }
                else if (pathEvent.kind == EventKind.SEGMENT_DONE) {
                    RobotLog.ii(TAG, "segment completed");
                    eventTlm.setValue("segment is done");
                }
            }
        });
    }

    public void turnRight() {
        this.addTask(new DeadReckonTask(this, turnPath, drivetrain) {
            @Override
            public void handleEvent (RobotEvent e) {
                DeadReckonEvent pathEvent = (DeadReckonEvent) e;
                if (pathEvent.kind == EventKind.PATH_DONE) {
                    RobotLog.ii(TAG, "turned");
                    eventTlm.setValue("path is done");
                }
                else if (pathEvent.kind == EventKind.SEGMENT_DONE) {
                    RobotLog.ii(TAG, "segment completed");
                    eventTlm.setValue("segment is done");
                }
            }
        });
    }


    @Override
    public void start() {
        goForward();
        //strafe();
        //turnRight();
    }

}