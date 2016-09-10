package team25core;

/*
 * FTC Team 25: cmacfarl, August 28, 2016
 */

import android.content.Context;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbDcMotorController;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.util.SerialNumber;

public class Team25DcMotorControllerWrapper {

    DcMotor motor1;
    DcMotor motor2;
    DcMotorController mc;

    Team25DcMotorControllerWrapper(DcMotor motor1, DcMotor motor2, DcMotorController mc)
    {
        this.motor1 = motor1;
        this.motor2 = motor2;
        this.mc = mc;
    }

    public DcMotor getMotor(int port)
    {
        switch (port) {
            case 1:
                return motor1;
            case 2:
                return motor2;
            default:
        }
        return null;
    }
}
