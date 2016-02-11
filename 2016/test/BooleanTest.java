package test;

//import org.swerverobotics.library.interfaces.Disabled;
//import org.swerverobotics.library.interfaces.TeleOp;

import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by Izzie on 1/14/2016.
 */

//@TeleOp(name = "Boolean Test", group = "AutoTeam25")
//@Disabled
public class BooleanTest extends Robot {
    Robot robot;

    boolean redAlliance;
    boolean blueAlliance;

    @Override
    public void handleEvent(RobotEvent e) {
        // No-op.
    }

    @Override
    public void init() {

        addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

                if (event.kind == GamepadTask.EventKind.BUTTON_X_DOWN) {
                    blueAlliance = true;
                    telemetry.addData("Alliance: ", "blue");
                } else if (event.kind == GamepadTask.EventKind.BUTTON_B_DOWN) {
                    redAlliance = true;
                    telemetry.addData("Alliance: ", "red");
                }
            }
        });
    }
}
