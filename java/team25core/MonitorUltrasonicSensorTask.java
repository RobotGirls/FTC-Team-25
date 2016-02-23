package team25core;

import com.qualcomm.robotcore.hardware.UltrasonicSensor;

/*
 *
 */
public class MonitorUltrasonicSensorTask extends RobotTask {

    protected Robot robot;
    protected UltrasonicSensor ultrasound;
    protected double value;

    public MonitorUltrasonicSensorTask(Robot robot, UltrasonicSensor sensor) {
        super(robot);
        this.robot = robot;
        this.ultrasound = sensor;
    }

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public boolean timeslice() {
        value = ultrasound.getUltrasonicLevel();
        robot.telemetry.addData(ultrasound.getConnectionInfo() + " Value: ", value);
        return false;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {

    }
}
