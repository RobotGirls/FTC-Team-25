package test;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.RobotLog;

import opmodes.DaisyConfiguration;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.MecanumGearedDriveDeadReckon;
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
    private MecanumGearedDriveDeadReckon path;
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;

    @Override
    public void init() {
        rangeSensor = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "range");
        rangeSensorCriteria = new RangeSensorCriteria(rangeSensor, 15);

        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        path = new MecanumGearedDriveDeadReckon(this, DaisyConfiguration.TICKS_PER_INCH, DaisyConfiguration.TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        path.addSegment(DeadReckon.SegmentType.STRAIGHT, 40, -0.4);
    }

    @Override
    public void start() {
        addTask(new DeadReckonTask(this, path, rangeSensorCriteria) {
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
