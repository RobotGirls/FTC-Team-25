package team25core;/*
 * FTC Team 25: cmacfarl, January 13, 2016
 */

import java.util.LinkedHashMap;
import java.util.Map;

public class PersistentTelemetryTask extends RobotTask {

    private final Map<String, String> objs = new LinkedHashMap<String, String>();  // linked so as to preserve addition order as iteration order

    public PersistentTelemetryTask(Robot robot)
    {
        super(robot);
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
    }

    public void addData(String key, String value)
    {
        objs.put(key, value);
    }

    @Override
    public boolean timeslice()
    {
        for (Map.Entry<String, String> entry : objs.entrySet()) {
            this.robot.telemetry.addData(entry.getKey(), entry.getValue());
        }

        return false;
    }

}

