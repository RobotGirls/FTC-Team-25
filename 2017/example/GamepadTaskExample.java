package example;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;

@TeleOp(name = "Gamepad Task Example", group = "Team25")
public class GamepadTaskExample extends Robot
{
        private Servo leftArm;
        private Servo rightArm;

        @Override
        public void init()
        {
            leftArm = hardwareMap.servo.get("left");
            leftArm.setPosition(1);

            rightArm = hardwareMap.servo.get("right");
            rightArm.setPosition(1);

        }

        @Override
        public void handleEvent(RobotEvent e)
        {
            // No-op.
        }

        @Override
        public void start()
        {
            super.start();

            this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
                public void handleEvent(RobotEvent e) {
                    GamepadEvent event = (GamepadEvent) e;

                    if (event.kind == EventKind.BUTTON_Y_DOWN) {
                        leftArm.setPosition(1);
                        rightArm.setPosition(1);
                    } else if (event.kind == EventKind.BUTTON_X_DOWN) {
                        leftArm.setPosition(0);
                        rightArm.setPosition(0);
                    }

                }
            });
        }
}
