package opmodes;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import team25core.KatelynsFourWheelDrivetrain;
import team25core.StandardFourMotorRobot;

@Autonomous(name = "Katelyn's Auto",group = "rnrr")
public class RnRRKatelynBasicAuto extends StandardFourMotorRobot {


    KatelynsFourWheelDrivetrain drivetrain;


    @Override
    public void init() {
        super.init(); // taking parent's init (hardware mapping)

    }
}