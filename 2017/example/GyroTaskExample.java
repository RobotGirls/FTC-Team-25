package example;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.GyroTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

@TeleOp(name = "Gyro Task Example", group = "Team25")
public class GyroTaskExample extends Robot
{
    private DcMotor left;
    private DcMotor right;
    private GyroSensor gyro;
    private GyroTask gyroTask;

    @Override
    public void init()
    {
        left = hardwareMap.dcMotor.get("left");
        right = hardwareMap.dcMotor.get("right");
        gyro = hardwareMap.gyroSensor.get("gyro");
        gyro.resetZAxisIntegrator();

        gyroTask = new GyroTask(this, gyro, 180, true);
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        GyroTask.GyroEvent event = (GyroTask.GyroEvent) e;
        if (event.kind == GyroTask.EventKind.HIT_TARGET) {
            left.setPower(0);
            right.setPower(0);
        }
    }

    @Override
    public void start()
    {
        super.start();

        // Spin.
        left.setPower(0.5);
        right.setPower(0.5);

        this.addTask(gyroTask);
    }
}
