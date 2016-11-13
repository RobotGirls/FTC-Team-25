package test;

/*
 * FTC Team 25: izzielau, October 27, 2015
 */

import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;

import team25core.LimitSwitchTask;
import team25core.Robot;
import team25core.RobotEvent;

public class CoreLimitSwitchTest extends Robot {

    public DeviceInterfaceModule interfaceModule;

    @Override
    public void handleEvent(RobotEvent e)
    {

    }

    @Override
    public void init() {
        interfaceModule = hardwareMap.deviceInterfaceModule.get("interface");
    }

    @Override
    public void start() {
        // public LimitSwitchTask(Robot robot, DeviceInterfaceModule module, int limitPort)
        addTask(new LimitSwitchTask(this, interfaceModule, 0) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                LimitSwitchEvent event = (LimitSwitchEvent)e;

                if (event.kind == EventKind.CLOSED) {
                    telemetry.addData("Status: ", "closed");
                } else if (event.kind == EventKind.OPEN) {
                    telemetry.addData("Status: ", "open");
                } else {
                    telemetry.addData("Status: ", "unknown");
                }
            }
        });
    }
}
