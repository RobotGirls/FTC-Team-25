package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.ReneeFourWheelDrivetrain;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;

@Autonomous(name="reneebasicauto2", group="rnrr")
public class RnrrReneeBasicAuto extends StandardFourMotorRobot {

    private static String TAG = "Renee";
    //private ReneeFourWheelDrivetrain drivetrain;
    private FourWheelDirectDrivetrain drivetrain;
    private DeadReckonPath reneePath;

    private Telemetry.Item eventTlm;

    @Override
    public void start() {
        RobotLog.ii(TAG,"start");
        eventTlm.setValue("im in start");
        driveReneePath();
    }

    public void init() {
        super.init(); //calling parent init for hardware mapping

        //initiating the ReneeFourWheelDrivetrain class
        //drivetrain = new ReneeFourWheelDrivetrain(frontRight, backRight, frontLeft, backLeft);
        drivetrain = new FourWheelDirectDrivetrain(frontRight, backRight, frontLeft, backLeft);

        //uncomment the following only if the robot is going in the opposite direction from what you expect
        //drivetrain.setCanonicalMotorDirection();

        //motor will try to run at target velocity
        drivetrain.encodersOn();

        //sets the behavior when power level of 0 is applied (i.e., the motor is not moving) then we apply the brakes
       // drivetrain.brakeOnZeroPower();

        //sets the motor encoder position to zero
        drivetrain.resetEncoders();

        eventTlm = telemetry.addData("drive event info", "none");

        initPaths();
    }

    private void initPaths() {
        reneePath = new DeadReckonPath();//DeadReckonPath is the class and the constructor is the same name as the class
        reneePath.stop(); //clears segments
        reneePath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 15, -0.5);
        reneePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 1);
        //reneePath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 10, 1);
        //reneePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, -1);
//do we need to end the path?


    }


    private void driveReneePath() {
        RobotLog.ii(TAG,"now in driveReneePath");
        eventTlm.setValue("now in driveReneePath");

        this.addTask(new DeadReckonTask(this, reneePath, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                RobotLog.ii(TAG,"driveReneePath's handleEvent is called");
                eventTlm.setValue("now in driveReneePath handleEvent");

                if (path.kind == EventKind.PATH_DONE) {
                    eventTlm.setValue("path done");
                    RobotLog.ii(TAG, "intpathdone");
                }
                if (path.kind == EventKind.SEGMENT_DONE) {
                    eventTlm.setValue("segment done");
                }
            }
        });
    }
}