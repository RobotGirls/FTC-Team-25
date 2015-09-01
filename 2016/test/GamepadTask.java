package com.qualcomm.ftcrobotcontroller.opmodes;

/*
 * FTC Team 25: cmacfarl, August 31, 2015
 */

import com.qualcomm.robotcore.hardware.Gamepad;

public class GamepadTask implements RobotTask {

    public enum EventKind {
        BUTTON_A_DOWN,
        BUTTON_A_UP,
        BUTTON_B_DOWN,
        BUTTON_B_UP,
        BUTTON_X_DOWN,
        BUTTON_X_UP,
        BUTTON_Y_DOWN,
        BUTTON_Y_UP,
    };

    public class GamepadEvent extends RobotEvent {

        EventKind kind;

        public GamepadEvent(Robot r, EventKind k)
        {
            super(r);
            kind = k;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "Gamepad Event " + kind);
        }
    }

    protected Robot robot;

    protected class ButtonState {
        public boolean a_pressed;
        public boolean b_pressed;
        public boolean x_pressed;
        public boolean y_pressed;
    }

    protected ButtonState buttonState;

    public GamepadTask(Robot robot, Gamepad gamepad)
    {
        this.robot = robot;
        this.buttonState = new ButtonState();
        this.buttonState.a_pressed = false;
        this.buttonState.b_pressed = false;
        this.buttonState.x_pressed = false;
        this.buttonState.y_pressed = false;
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
         *
         * TODO: Choose the right gamepad (pass an enumerated value into the constructor.
         */
        gamepad = robot.gamepad1;

        if ((gamepad.a) && (buttonState.a_pressed == false)) {
            robot.queueEvent(new GamepadEvent(robot, EventKind.BUTTON_A_DOWN));
            buttonState.a_pressed = true;
        } else if ((!gamepad.a) && (buttonState.a_pressed == true)) {
            robot.queueEvent(new GamepadEvent(robot, EventKind.BUTTON_A_UP));
            buttonState.a_pressed = false;
        }

        if ((gamepad.b) && (buttonState.b_pressed == false)) {
            robot.queueEvent(new GamepadEvent(robot, EventKind.BUTTON_B_DOWN));
            buttonState.b_pressed = true;
        } else if ((!gamepad.b) && (buttonState.b_pressed == true)) {
            robot.queueEvent(new GamepadEvent(robot, EventKind.BUTTON_B_UP));
            buttonState.b_pressed = false;
        }

        if ((gamepad.x) && (buttonState.x_pressed == false)) {
            robot.queueEvent(new GamepadEvent(robot, EventKind.BUTTON_X_DOWN));
            buttonState.x_pressed = true;
        } else if ((!gamepad.x) && (buttonState.x_pressed == true)) {
            robot.queueEvent(new GamepadEvent(robot, EventKind.BUTTON_X_UP));
            buttonState.x_pressed = false;
        }

        if ((gamepad.y) && (buttonState.y_pressed == false)) {
            robot.queueEvent(new GamepadEvent(robot, EventKind.BUTTON_Y_DOWN));
            buttonState.y_pressed = true;
        } else if ((!gamepad.y) && (buttonState.y_pressed == true)) {
            robot.queueEvent(new GamepadEvent(robot, EventKind.BUTTON_Y_UP));
            buttonState.y_pressed = false;
        }

        /*
         * This task lives forever.
         */
        return false;
    }
}
