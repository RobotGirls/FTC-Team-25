package test;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.RobotLog;

import opmodes.DaisyCalibration;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.RangeSensorCriteria;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by elizabeth on 1/5/17.
 */

@Autonomous(name = "Daisy: Range Test", group = "Team25")
@Disabled

public class RangeTest extends Robot {

    public DistanceSensor rangeSensor;
    public RangeSensorCriteria rangeSensorCriteria;
    private DeadReckonPath path;
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private FourWheelDirectDrivetrain drivetrain;

    @Override
    public void init() {
        rangeSensor = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "range");
        rangeSensorCriteria = new RangeSensorCriteria(rangeSensor, 15);

        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);

        path = new DeadReckonPath();
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 40, -0.4);
    }

    @Override
    public void start() {
        addTask(new DeadReckonTask(this, path, drivetrain, rangeSensorCriteria) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent event = (DeadReckonEvent) e;
                if (event.kind == EventKind.SENSOR_SATISFIED) {
                    RobotLog.i("Max distance reached", "stopping path");
                }
            }
        });
    }

    @Override
    public void handleEvent(RobotEvent e) {


    }
}
