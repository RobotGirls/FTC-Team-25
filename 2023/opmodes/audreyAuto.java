package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.AudreysFourWheelDriveTrain;
import team25core.AudreysFourWheelDriveTrain;
import team25core.StandardFourMotorRobot;

@Autonomous (name = "audreyAuto", group = "java")
public class audreyAuto extends StandardFourMotorRobot {

    //constructors initialize and set memory aside for the program
    private AudreysFourWheelDriveTrain drivetrain;


    @Override
    public void init() {
        super.init();

        //instantiating AudreyDriveTrain class
        drivetrain = new AudreysFourWheelDriveTrain(frontLeft, frontRight, backLeft, backRight);

        //driveTrain.setCannonicalMotorDirection();
        // call this method only if the robot is going the opposite direction from expected
        //uncomment if robot is going opposite direction as method switches robot direction

        drivetrain.encodersOn();

        drivetrain.brakeOnZeroPower();

        drivetrain.resetEncoders();
        //sets the motor encoder position to zero

        //enumeration: variable type similar to int


    }
}
