package team25core;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

/*
 * FTC Team 25: cmacfarl, September 26, 2015
 */

public class DeadmanMotorTask extends RobotTask {

    public enum DeadmanButton {
        BUTTON_A,
        BUTTON_B,
        BUTTON_X,
        BUTTON_Y,
        LEFT_BUMPER,
        RIGHT_BUMPER,
        LEFT_TRIGGER,
        RIGHT_TRIGGER,
    };

    public enum EventKind {
        DEADMAN_BUTTON_DOWN,
        DEADMAN_BUTTON_UP,
    }

    public class DeadmanMotorEvent extends RobotEvent {
        public EventKind kind;

        public DeadmanMotorEvent (RobotTask task, EventKind k){
            super(task);
            kind = k;
        }
    }

    GamepadTask.GamepadNumber gamepad;
    DcMotor motor;
    double power;
    DeadmanButton button;
    boolean done;
    boolean buttonDown;

    public DeadmanMotorTask(Robot robot, DcMotor motor, double power, GamepadTask.GamepadNumber gamepad, DeadmanButton button)
    {
        super(robot);

        this.motor = motor;
        this.gamepad = gamepad;
        this.button = button;
        this.power = power;
        done = false;
    }

    protected boolean isButtonTracked(GamepadTask.EventKind kind)
    {
        boolean ret;

        ret = false;

        if (((kind == GamepadTask.EventKind.BUTTON_A_DOWN) || (kind == GamepadTask.EventKind.BUTTON_A_UP)) && (button == DeadmanButton.BUTTON_A)) {
            ret = true;
        } else if (((kind == GamepadTask.EventKind.BUTTON_B_DOWN) || (kind == GamepadTask.EventKind.BUTTON_B_UP)) && (button == DeadmanButton.BUTTON_B)) {
            ret = true;
        } else if (((kind == GamepadTask.EventKind.BUTTON_X_DOWN) || (kind == GamepadTask.EventKind.BUTTON_X_UP)) && (button == DeadmanButton.BUTTON_X)) {
            ret = true;
        } else if (((kind == GamepadTask.EventKind.BUTTON_Y_DOWN) || (kind == GamepadTask.EventKind.BUTTON_Y_UP)) && (button == DeadmanButton.BUTTON_Y)) {
            ret = true;
        } else if (((kind == GamepadTask.EventKind.LEFT_BUMPER_DOWN) || (kind == GamepadTask.EventKind.LEFT_BUMPER_UP)) && (button == DeadmanButton.LEFT_BUMPER)) {
            ret = true;
        } else if (((kind == GamepadTask.EventKind.RIGHT_BUMPER_DOWN) || (kind == GamepadTask.EventKind.RIGHT_BUMPER_UP)) && (button == DeadmanButton.RIGHT_BUMPER)) {
            ret = true;
        } else if (((kind == GamepadTask.EventKind.LEFT_TRIGGER_DOWN) || (kind == GamepadTask.EventKind.LEFT_TRIGGER_UP)) && (button == DeadmanButton.LEFT_TRIGGER)) {
            ret = true;
        } else if (((kind == GamepadTask.EventKind.RIGHT_TRIGGER_DOWN) || (kind == GamepadTask.EventKind.RIGHT_TRIGGER_UP)) && (button == DeadmanButton.RIGHT_TRIGGER)) {
            ret = true;
        }
        return ret;
    }

    protected void toggleMotor(GamepadTask.EventKind kind)
    {
        switch (kind) {
        case BUTTON_A_DOWN:
        case BUTTON_B_DOWN:
        case BUTTON_X_DOWN:
        case BUTTON_Y_DOWN:
        case LEFT_BUMPER_DOWN:
        case RIGHT_BUMPER_DOWN:
        case LEFT_TRIGGER_DOWN:
        case RIGHT_TRIGGER_DOWN:
            motor.setPower(power);
            robot.queueEvent(new DeadmanMotorEvent(this, EventKind.DEADMAN_BUTTON_DOWN));
            break;
        case BUTTON_A_UP:
        case BUTTON_B_UP:
        case BUTTON_X_UP:
        case BUTTON_Y_UP:
        case LEFT_BUMPER_UP:
        case RIGHT_BUMPER_UP:
        case LEFT_TRIGGER_UP:
        case RIGHT_TRIGGER_UP:
            motor.setPower(0.0);
            robot.queueEvent(new DeadmanMotorEvent(this, EventKind.DEADMAN_BUTTON_UP));
            break;
        }
    }

    @Override
    public void start()
    {
        motor.setPower(0.0);

        robot.addTask(new GamepadTask(robot, gamepad) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                GamepadEvent event = (GamepadEvent)e;

                if (isButtonTracked(event.kind)) {
                    toggleMotor(event.kind);
                }

            }
        });
    }

    @Override
    public void stop()
    {
        motor.setPower(0.0);
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice() {
       return done;
    }
}
