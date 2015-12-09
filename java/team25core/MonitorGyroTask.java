package team25core;

/*
 * FTC Team 25: cmacfarl, September 01, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;

public class MonitorGyroTask extends RobotTask {

    /*
     * No Events.
     */

    protected Robot robot;
    protected GyroSensor gyro;

    public MonitorGyroTask(Robot robot, GyroSensor gyro)
    {
        super(robot);

        this.gyro = gyro;
        this.robot = robot;
    }

    @Override
    public void start()
    {

    }

    @Override
    public void stop()
    {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        robot.telemetry.addData("Gyro Heading: ", gyro.getHeading());

        /*
         * Never stops.
         */
        return false;
    }
}
