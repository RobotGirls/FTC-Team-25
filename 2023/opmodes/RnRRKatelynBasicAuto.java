package opmodes;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.KatelynsFourWheelDrivetrain;
import team25core.StandardFourMotorRobot;

@Autonomous(name = "Katelyn's Auto",group = "rnrr")
public class RnRRKatelynBasicAuto extends StandardFourMotorRobot {


    private KatelynsFourWheelDrivetrain drivetrain;


    @Override
    public void init() {
        super.init(); // taking parent's init (hardware mapping)
        //instantiating the KatelynsFourWheelDrivetrain class
        drivetrain = KatelynsFourWheelDrivetrain(frontRight, backRight, frontLeft, backLeft);

        // uncomment the following only if the robot is going in the opposite direction from intended
        //drivetrain.setCanonicalMotorDirection();

        // motor will try to run at the targeted velocity
        drivetrain.encodersOn();

        // Sets the behavior of the motor when a power level of zero is applied i.e. not moving - when we apply 0 power, the motor brakes
        drivetrain.brakeOnZeroPower();

        // Sets motor encoder position to 0
        drivetrain.resetEncoders();
    }

}