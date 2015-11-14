package test;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;

import team25core.GyroTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by katie on 11/14/15.
 */
public class GyroTest extends Robot {
    GyroSensor sensor;
    DcMotor leftMotor;
    DcMotor rightMotor;

    @Override
    public void handleEvent(RobotEvent e){
        //See below...
    }

    @Override
    public void init(){
        sensor = hardwareMap.gyroSensor.get("gyroSensor");
        sensor.calibrate();

        leftMotor = hardwareMap.dcMotor.get("leftMotor");
        rightMotor = hardwareMap.dcMotor.get("rightMotor");

        leftMotor.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        rightMotor.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

    }

    @Override
    public void start() {

        leftMotor.setPower(-0.3);
        rightMotor.setPower(-0.3);

        addTask(new GyroTask(this, sensor, 180, true) {
            public void handleEvent(RobotEvent e) {
                GyroEvent event = (GyroEvent) e;

                if (event.kind == EventKind.HIT_TARGET || event.kind == EventKind.PAST_TARGET) {
                    leftMotor.setPower(0);
                    rightMotor.setPower(0);
                } else if (event.kind == EventKind.THRESHOLD_80) {
                    leftMotor.setPower(-0.1);
                    rightMotor.setPower(-0.1);
                } else if (event.kind == EventKind.THRESHOLD_90) {
                    leftMotor.setPower(-0.02);
                    rightMotor.setPower(-0.02);
                }
            }
        });
    }
}
