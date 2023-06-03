package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.ReneeFourWheelDrivetrain;
import team25core.StandardFourMotorRobot;

@Autonomous(name="reneebasicauto", group="rnrr")
public class RnrrReneeBasicAuto extends StandardFourMotorRobot {

    private ReneeFourWheelDrivetrain drivetrain;


    @Override
    public void init() {
        super.init();

        //initiating the ReneeFourWheelDrivetrain class
        drivetrain = new ReneeFourWheelDrivetrain(frontRight, backRight, frontLeft, backLeft);

        //uncomment the following only if the robot is going in the opposite direction from what you expect
        //@drivetrain.setCanonicalMotorDirection();

        //motor will try to run at target velocity
        drivetrain.encodersOn();

        //sets the behavior when power level of 0 is applied (i.e., the motor is not moving) then we apply the brakes
        drivetrain.brakeOnZeroPower();

        //sets the motor encoder position to zero
        drivetrain.resetEncoders();





    }

}
