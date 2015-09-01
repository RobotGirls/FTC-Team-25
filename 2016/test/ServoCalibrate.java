package com.qualcomm.ftcrobotcontroller.opmodes;

/*
 * FTC Team 25: cmacfarl, August 31, 2015
 */

import com.qualcomm.modernrobotics.ModernRoboticsMatrixServoController;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.util.RobotLog;

public class ServoCalibrate extends Robot {

    protected Servo servo;
    protected ServoController sc;
    ServoCalibrateTask st;
    GamepadTask gt;

    protected void handleServoEvent(ServoCalibrateTask.ServoEvent event)
    {
        if (event.kind == ServoCalibrateTask.EventKind.SERVO_DONE) {
            telemetry.addData("Servo Position: ", event.currentPos);
        }
    }

    protected void handleGamepadEvent(GamepadTask.GamepadEvent event)
    {
        switch (event.kind) {
        case BUTTON_A_DOWN:
            st.stop();
            break;
        case BUTTON_Y_DOWN:
            st.reverse();
            break;
        case BUTTON_A_UP:
        case BUTTON_B_UP:
        case BUTTON_B_DOWN:
        case BUTTON_X_DOWN:
        case BUTTON_X_UP:
        case BUTTON_Y_UP:
            break;
        }
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        RobotLog.i("Received event " + e.toString());
        if (e instanceof ServoCalibrateTask.ServoEvent) {
            handleServoEvent((ServoCalibrateTask.ServoEvent)e);
        } else if (e instanceof GamepadTask.GamepadEvent) {
            handleGamepadEvent((GamepadTask.GamepadEvent) e);
        } else {
            RobotLog.i("Unknown event " + e.toString());
        }
    }

    @Override
    public void init()
    {
        servo = hardwareMap.servo.get("servo_1");
        sc = hardwareMap.servoController.get("MatrixControllerServo");
        sc.pwmEnable();
        servo.setPosition(((double)250/2)/250);
    }

    @Override
    public void start()
    {
        st = new ServoCalibrateTask(this, servo, 0, 250);
        gt = new GamepadTask(this, gamepad1);
        addTask(st);
        addTask(gt);
    }
}
