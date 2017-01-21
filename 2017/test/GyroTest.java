
package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;

import team25core.GyroTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by katie on 11/14/15.
 */
@Autonomous(name = "Gyro Test", group = "Team 25")
public class GyroTest extends Robot {
    GyroSensor sensor;
    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor rearLeft;
    DcMotor rearRight;

    @Override
    public void handleEvent(RobotEvent e){
        //See below...
    }

    @Override
    public void init(){
        sensor = hardwareMap.gyroSensor.get("gyroSensor");
        sensor.calibrate();

        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    }

    @Override
    public void start() {
        /*
        frontLeft.setPower(-0.3);
        frontRight.setPower(0.3);

        addTask(new GyroTask(this, sensor, -180, true) {
            public void handleEvent(RobotEvent e) {
                GyroEvent event = (GyroEvent) e;

                if (event.kind == EventKind.HIT_TARGET || event.kind == EventKind.PAST_TARGET) {
                    frontLeft.setPower(0);
                    frontRight.setPower(0);
                } else if (event.kind == EventKind.THRESHOLD_80) {
                    frontLeft.setPower(-0.1);
                    frontRight.setPower(0.1);
                } else if (event.kind == EventKind.THRESHOLD_90) {
                    frontLeft.setPower(-0.10);
                    frontRight.setPower(0.10);
                }
            }
        });
        */

        frontLeft.setPower(1.0);
        rearLeft.setPower(1.0);
        frontRight.setPower(1.0);
        rearRight.setPower(1.0);

        // Display: gyro.
        GyroTask displayGyro = new GyroTask(this, sensor, 90, true);
        this.addTask(displayGyro);
    }
}
