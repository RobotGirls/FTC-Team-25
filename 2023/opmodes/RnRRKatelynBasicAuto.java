package opmodes;


import static team25core.IMUTableConfiguration.STRAIGHT_SPEED;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.KatelynsFourWheelDrivetrain;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;

@Autonomous(name = "Katelyn's Auto",group = "rnrr")
public class RnRRKatelynBasicAuto extends StandardFourMotorRobot {


    private KatelynsFourWheelDrivetrain drivetrain;
    private DeadReckonPath straightPath;

    @Override
    public void init() {
        super.init(); // taking parent's init (hardware mapping)
        //instantiating the KatelynsFourWheelDrivetrain class
        drivetrain = new KatelynsFourWheelDrivetrain(frontRight, backRight, frontLeft, backLeft);
        // uncomment the following only if the robot is going in the opposite direction from intended
        //drivetrain.setCanonicalMotorDirection();

        // motor will try to run at the targeted velocity
        drivetrain.encodersOn();

        // Sets the behavior of the motor when a power level of zero is applied i.e. not moving - when we apply 0 power, the motor brakes
        drivetrain.brakeOnZeroPower();

        // Sets motor encoder position to 0
        drivetrain.resetEncoders();

        initPaths();
    }

    public void initPaths() {
        DeadReckonPath straightPath = new DeadReckonPath();
        straightPath.stop();
        straightPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,1.2, STRAIGHT_SPEED);
    }

    public void goForward() {
        this.addTask(new DeadReckonTask(this, straightPath, drivetrain){
            @Override
            public void handleEvent (RobotEvent e){
                // could use a little clarification on this - what is the DeadReckonEvent line doing?
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("went forward");
                }

            }
        });
    }

    public void start() {
        // what do I need to add to this method?
    }

}