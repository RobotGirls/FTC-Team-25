package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;

import opmodes.BeaconHelper;
import opmodes.DaisyConfiguration;
import opmodes.GeneralBeaconArms;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by elizabeth on 1/8/17.
 */
@Autonomous(name = "Beacon Test 2", group = "Team25")
public class BeaconTest2 extends Robot
{

    private Servo leftPusher;
    private Servo rightPusher;
    private BeaconHelper helper;
    private GeneralBeaconArms pushers;
    private ColorSensor color;
    private DeviceInterfaceModule cdim;

    @Override
    public void init()
    {
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightPusher = hardwareMap.servo.get("rightPusher");
        color = hardwareMap.colorSensor.get("color");
        cdim = hardwareMap.deviceInterfaceModule.get("cdim");

        pushers = new GeneralBeaconArms(leftPusher, rightPusher, DaisyConfiguration.LEFT_DEPLOY_POS, DaisyConfiguration.RIGHT_DEPLOY_POS, DaisyConfiguration.LEFT_STOW_POS, DaisyConfiguration.RIGHT_STOW_POS, false);
        helper = new BeaconHelper(this, BeaconHelper.Alliance.RED, pushers, color, cdim);
    }

    @Override
    public void start()
    {
        super.start();
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        helper.doBeaconWork();
    }
}
