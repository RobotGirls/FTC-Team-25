package team25core;

/*
 * FTC Team 25: cmacfarl, August 31, 2015
 */

import com.qualcomm.robotcore.hardware.Gamepad;

public class GamepadTask extends RobotTask {

    public enum GamepadNumber {
        GAMEPAD_1,
        GAMEPAD_2,
    };

    public enum EventKind {
        BUTTON_A_DOWN,
        BUTTON_A_UP,
        BUTTON_B_DOWN,
        BUTTON_B_UP,
        BUTTON_X_DOWN,
        BUTTON_X_UP,
        BUTTON_Y_DOWN,
        BUTTON_Y_UP,
        LEFT_BUMPER_DOWN,
        LEFT_BUMPER_UP,
        RIGHT_BUMPER_DOWN,
        RIGHT_BUMPER_UP,
    };

    public class GamepadEvent extends RobotEvent {

        EventKind kind;

        public GamepadEvent(RobotTask task, EventKind k)
        {
            super(task);
            kind = k;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "Gamepad Event " + kind);
        }
    }

    protected class ButtonState {
        public boolean a_pressed;
        public boolean b_pressed;
        public boolean x_pressed;
        public boolean y_pressed;
    }

    protected GamepadNumber gamepadNum;
    protected ButtonState buttonState;

    public GamepadTask(Robot robot, GamepadNumber gamepadNum)
    {
        super(robot);

        this.buttonState = new ButtonState();
        this.buttonState.a_pressed = false;
        this.buttonState.b_pressed = false;
        this.buttonState.x_pressed = false;
        this.buttonState.y_pressed = false;

        this.gamepadNum = gamepadNum;
    }

    @Override
    public void start()
    {
        // TODO: ??
    }

    @Override
    public void stop()
    {
        // TODO: ??
    }

    /*
     * Process gamepad actions and send them to the robot as events.
     *
     * Note that these are not state changes, but is designed to send a
     * continual stream of events as long as the button is pressed (hmmm,
     * this may not be a good idea if software can't keep up).
     */
    @Override
    public boolean timeslice()
    {
        Gamepad gamepad;

        /*
         * I thought Java passed objects by reference, but oddly enough if you cache
         * the gamepad in the task's contstructor, it will never update.  Hence this.
         */
        if (gamepadNum == GamepadNumber.GAMEPAD_1) {
            gamepad = robot.gamepad1;
        } else {
            gamepad = robot.gamepad2;
        }

        if ((gamepad.a) && (buttonState.a_pressed == false)) {
            robot.queueEvent(new GamepadEvent(this, EventKind.BUTTON_A_DOWN));
            buttonState.a_pressed = true;
        } else if ((!gamepad.a) && (buttonState.a_pressed == true)) {
            robot.queueEvent(new GamepadEvent(this, EventKind.BUTTON_A_UP));
            buttonState.a_pressed = false;
        }

        if ((gamepad.b) && (buttonState.b_pressed == false)) {
            robot.queueEvent(new GamepadEvent(this, EventKind.BUTTON_B_DOWN));
            buttonState.b_pressed = true;
        } else if ((!gamepad.b) && (buttonState.b_pressed == true)) {
            robot.queueEvent(new GamepadEvent(this, EventKind.BUTTON_B_UP));
            buttonState.b_pressed = false;
        }

        if ((gamepad.x) && (buttonState.x_pressed == false)) {
            robot.queueEvent(new GamepadEvent(this, EventKind.BUTTON_X_DOWN));
            buttonState.x_pressed = true;
        } else if ((!gamepad.x) && (buttonState.x_pressed == true)) {
            robot.queueEvent(new GamepadEvent(this, EventKind.BUTTON_X_UP));
            buttonState.x_pressed = false;
        }

        if ((gamepad.y) && (buttonState.y_pressed == false)) {
            robot.queueEvent(new GamepadEvent(this, EventKind.BUTTON_Y_DOWN));
            buttonState.y_pressed = true;
        } else if ((!gamepad.y) && (buttonState.y_pressed == true)) {
            robot.queueEvent(new GamepadEvent(this, EventKind.BUTTON_Y_UP));
            buttonState.y_pressed = false;
        }

        /*
         * This task lives forever.
         */
        return false;
    }
}
