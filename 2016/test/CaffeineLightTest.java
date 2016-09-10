package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.LightSensor;

import opmodes.NeverlandMotorConstants;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TwoWheelGearedDriveDeadReckon;

/**
 * Created by Izzie on 2/26/2016.
 */

@Autonomous(name = "TEST Display Color")
public class CaffeineLightTest extends Robot {

    protected double frontMinimum;
    protected double backMinimum;
    protected double frontMaximum;
    protected double backMaximum;

    protected int TICKS_PER_INCH = NeverlandMotorConstants.ENCODER_TICKS_PER_INCH;
    protected int TICKS_PER_DEGREE = NeverlandMotorConstants.ENCODER_TICKS_PER_DEGREE;

    protected DcMotor leftTread;
    protected DcMotor rightTread;
    protected LightSensor frontLight;
    protected LightSensor backLight;
    protected DeadReckon deadReckonStraight;
    protected DeadReckonTask deadReckonStraightTask;

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init() {
        rightTread = hardwareMap.dcMotor.get("rightTread");
        leftTread = hardwareMap.dcMotor.get("leftTread");
        frontLight = hardwareMap.lightSensor.get("frontLight");
        backLight = hardwareMap.lightSensor.get("backLight");

        deadReckonStraight = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckonStraight.addSegment(DeadReckon.SegmentType.STRAIGHT, 15, 0.251);

        deadReckonStraightTask = new DeadReckonTask(this, deadReckonStraight);
    }

    @Override
    public void start() {

        double currentFront = frontLight.getRawLightDetected();
        double currentBack = backLight.getRawLightDetected();

        telemetry.addData("FRONT: ", currentFront);
        telemetry.addData("BACK: ", currentBack);

        if (currentFront < frontMinimum) {
            frontMinimum = currentFront;
        } else if (currentFront > frontMaximum) {
            frontMaximum = currentFront;
        } else if (currentBack < backMinimum) {
            backMinimum = currentBack;
        } else if (currentBack > backMaximum) {
            backMaximum = currentBack;
        }

        telemetry.addData("FRONT Minimum: ", frontMinimum);
        telemetry.addData("BACK Minimum: ", backMinimum);

        addTask(deadReckonStraightTask);
    }
}
