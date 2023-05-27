package opmodes;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.CindysMecanumFourWheelDrivetrain;
import team25core.StandardFourMotorRobot;

@Autonomous(name = "Cindy's Mecanum Auto",group = "rnrr")
public class CodaMecanumAuto extends StandardFourMotorRobot {


    CindysMecanumFourWheelDrivetrain drivetrain;


    @Override
    public void init() {
        super.init(); // taking parent's init (hardware mapping)
        //instantiating the CindysMecanumFourWheelDrivetrain class
        drivetrain = new CindysMecanumFourWheelDrivetrain(frontRight, backRight, frontLeft, backLeft);
        // uncomment the following only if the robot is going in the opposite direction from intended
        //drivetrain.setCanonicalMotorDirection();

        // motor will try to run at the targeted velocity
        //drivetrain.encodersOn();

        // Sets the behavior of the motor when a power level of zero is applied i.e. not moving - when we apply 0 power, the motor brakes
        //drivetrain.brakeOnZeroPower();

        // Sets motor encoder position to 0
        //drivetrain.resetEncoders();
    }

}
