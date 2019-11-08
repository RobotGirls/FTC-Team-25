package test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.ServoCalibrateTask;

@TeleOp
        (name = "ServoCalibrateUtility2")
//@Disabled
public class ServoCalibrate extends Robot {

    List<Servo> servoList;
    Map<Servo, Telemetry.Item> servoItemMap;
    Iterator<Map.Entry<Servo, Telemetry.Item>> iterator = null;

    Servo activeServo;
    int numServos;

    @Override
    public void handleEvent(RobotEvent e)
    {
    }

    protected String getServoName(Servo servo)
    {
        Set<String> names = hardwareMap.getNamesOf(servo);

        /**
         * Yes, this looks strange, but if a device has more than one name, we'll
         * just use the first one.  In all practical usage I don't think there's
         * ever more than one name.  And since Set is unordered there's no first()
         * or last() operations.  Hence the funky non-iteration.
         */
        for (String name : names) {
            return name;
        }

        return "NoName";
    }

    protected Servo getNextInMap()
    {
        if ((iterator == null) || (iterator.hasNext() == false)) {
            iterator = servoItemMap.entrySet().iterator();
        }

        return iterator.next().getKey();
    }

    protected void clearMark(Telemetry.Item item)
    {
        item.setValue("");
    }

    protected void setMark(Telemetry.Item item)
    {
        item.setValue(" *");
    }

    @Override
    public void init()
    {
        servoList = hardwareMap.getAll(Servo.class);
        numServos = servoList.size();
        servoItemMap = new LinkedHashMap<>();

        if (numServos == 0) {
            Telemetry.Item none = telemetry.addData("No servos", "");
            return;
        }

        for (Servo servo : servoList) {
            String name = getServoName(servo);
            Telemetry.Item servoTelemetry = telemetry.addData(name, "");
            servoItemMap.put(servo, servoTelemetry);
        }

        activeServo = ((Servo)servoItemMap.keySet().toArray()[0]);

        addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            @Override
            public void handleEvent(RobotEvent e) {
                GamepadEvent gamepadEvent = (GamepadEvent)e;

                switch (gamepadEvent.kind) {
                    case LEFT_BUMPER_DOWN:
                    case RIGHT_BUMPER_DOWN:
                        clearMark(servoItemMap.get(activeServo));
                        activeServo = getNextInMap();
                        setMark(servoItemMap.get(activeServo));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void start()
    {
        addTask(new ServoCalibrateTask(this, activeServo));
    }
}
