/*
 * Copyright (c) September 2017 FTC Teams 25/5218
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted (subject to the limitations in the disclaimer below) provided that
 *  the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list
 *  of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice, this
 *  list of conditions and the following disclaimer in the documentation and/or
 *  other materials provided with the distribution.
 *
 *  Neither the name of FTC Teams 25/5218 nor the names of their contributors may be used to
 *  endorse or promote products derived from this software without specific prior
 *  written permission.
 *
 *  NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 *  LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 *  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.TwoWheelDriveTask;


@TeleOp(name="OrangeCrabDemo")
public class OrangeCrabTeleopDemo extends Robot {

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    };

    private DcMotor left;
    private DcMotor right;
    private DcMotor clawRotate;
    private DcMotor clawVertical;

    private Servo s1;
    private Servo s2;
    private Servo s3;
    private Servo s4;

    private static double SERVO_DOMAIN = 256.0;

    /**
     * Note that S3 sounds like it has a mechanical problem.
     */
    private static double S1_OPEN   = 18  / SERVO_DOMAIN;
    private static double S1_CLOSED = 57  / SERVO_DOMAIN;
    private static double S2_OPEN   = 210 / SERVO_DOMAIN;
    private static double S2_CLOSED = 180 / SERVO_DOMAIN;
    private static double S3_OPEN   = 35  / SERVO_DOMAIN;
    private static double S3_CLOSED = 73  / SERVO_DOMAIN;
    private static double S4_OPEN   = 54  / SERVO_DOMAIN;
    private static double S4_CLOSED = 84  / SERVO_DOMAIN;

    private static int DEGREES_180 = 790;
    private static int NUDGE = 25;
    private static int CLAW_VERTICAL = 400;
    private static double NUDGE_POWER = 0.1;
    private static double ROTATE_POWER = 0.3;
    private static double CLAW_VERTICAL_POWER = 0.4;

    /**
     * Default states for actuators that are toggled.
     */
    private boolean clawDown = true;
    private boolean s1Open = true;
    private boolean s3Open = true;

    private boolean lockout = false;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof RunToEncoderValueTask.RunToEncoderValueEvent) {
            if (((RunToEncoderValueTask.RunToEncoderValueEvent)e).kind == RunToEncoderValueTask.EventKind.DONE) {
                lockout = false;
                RobotLog.i("Done moving motor");
            }
        }
    }

    @Override
    public void init()
    {

        right = hardwareMap.get(DcMotor.class, "right");
        left = hardwareMap.get(DcMotor.class, "left");
        clawRotate = hardwareMap.get(DcMotor.class, "clawRotate");
        clawVertical = hardwareMap.get(DcMotor.class, "clawVertical");

        right.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        right.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        left.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        left.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        left.setDirection(DcMotorSimple.Direction.REVERSE);

        clawRotate.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        clawRotate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        clawVertical.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        clawVertical.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        clawRotate.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        clawVertical.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        s1 = hardwareMap.get(Servo.class, "s1");
        s2 = hardwareMap.get(Servo.class, "s2");
        s3 = hardwareMap.get(Servo.class, "s3");
        s4 = hardwareMap.get(Servo.class, "s4");

        openClaw();
    }

    /**
     * Move claw up and down, making sure we don't over rotate.
     */
    private void toggleClawVertical()
    {
        if (clawDown == true) {
            clawVertical.setDirection(DcMotorSimple.Direction.FORWARD);
            clawDown = false;
        } else {
            clawVertical.setDirection(DcMotorSimple.Direction.REVERSE);
            clawDown = true;
        }
        this.addTask(new RunToEncoderValueTask(this, clawVertical, CLAW_VERTICAL, CLAW_VERTICAL_POWER));
    }

    /**
     * Blindly open both claws and set the state appropriately.
     */
    private void openClaw()
    {
        s1.setPosition(S1_OPEN);
        s2.setPosition(S2_OPEN);
        s3.setPosition(S3_OPEN);
        s4.setPosition(S4_OPEN);

        s1Open = true;
        s3Open = true;
    }

    /**
     * The servos always work in pairs.  S1/S2 and S3/S4.  toggleS1 therefore refers the to the S1/S2 pair.
     */
    private void toggleS1()
    {
        if (s1Open == true) {
            s1.setPosition(S1_CLOSED);
            s2.setPosition(S2_CLOSED);
            s1Open = false;
        } else {
            s1.setPosition(S1_OPEN);
            s2.setPosition(S2_OPEN);
            s1Open = true;
        }
    }

    /**
     * The servos always work in pairs.  S1/S2 and S3/S4.  toggleS3 therefore refers the to the S3/S4 pair.
     */
    private void toggleS3()
    {
        if (s3Open == true) {
            s3.setPosition(S3_CLOSED);
            s4.setPosition(S4_CLOSED);
            s3Open = false;
        } else {
            s3.setPosition(S3_OPEN);
            s4.setPosition(S4_OPEN);
            s3Open = true;
        }
    }

    /**
     * We will spin the claw back and forth, be careful that you alternate directions so that
     * you don't wrap the servo cables around the motor shaft.
     *
     * This motor's movement is not symmetrical, so we'll compensate in one direction.
     */
    private void rotate(Direction direction)
    {
        int distance;

        if (direction == Direction.CLOCKWISE) {
            clawRotate.setDirection(DcMotorSimple.Direction.REVERSE);
            distance = DEGREES_180;
        } else {
            clawRotate.setDirection(DcMotorSimple.Direction.FORWARD);
            distance = (int)(DEGREES_180 * 0.725);
        }
        this.addTask(new RunToEncoderValueTask(this, clawRotate, DEGREES_180, ROTATE_POWER));
    }

    /**
     * Fine alignment for the claw.  Note that there is no deadman motor function specifically to
     * avoid operator error wherein the motor is held on too long and we over rotate thereby
     * damaging the cabling or wire harnesses.
     */
    private void nudge(Direction direction)
    {
        if (direction == Direction.CLOCKWISE) {
            clawRotate.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            clawRotate.setDirection(DcMotorSimple.Direction.FORWARD);
        }
        this.addTask(new RunToEncoderValueTask(this, clawRotate, NUDGE, NUDGE_POWER));
    }

    @Override
    public void start()
    {
        this.addTask(new TwoWheelDriveTask(this, right, left));

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent)e;

                /**
                 * Finish a move before we allow another one.
                 */
                if (lockout == true) {
                    return;
                }

                switch (event.kind) {
                case BUTTON_Y_DOWN:
                    /**
                     * Toggle the claw up and down.
                     */
                    toggleClawVertical();
                    break;
                case BUTTON_A_DOWN:
                    /**
                     * Open the claw
                     */
                     openClaw();
                     break;
                case LEFT_BUMPER_DOWN:
                    /**
                     * Toggle s1/s2
                     */
                    toggleS1();
                    break;
                case RIGHT_BUMPER_DOWN:
                    /**
                     * Toggle s3/s4
                     */
                    toggleS3();
                    break;
                case BUTTON_B_DOWN:
                    /**
                     * Rotate 180 degrees clockwise
                     */
                    lockout = true;
                    rotate(Direction.CLOCKWISE);
                    break;
                case BUTTON_X_DOWN:
                    /**
                     * Rotate 180 degrees counterclockwise
                     */
                    lockout = true;
                    rotate(Direction.COUNTERCLOCKWISE);
                    break;
                case LEFT_TRIGGER_DOWN:
                    /**
                     * Nudge counterclockwise
                     */
                    lockout = true;
                     nudge(Direction.COUNTERCLOCKWISE);
                    break;
                case RIGHT_TRIGGER_DOWN:
                    /**
                     * Nudge clockwise
                     */
                    lockout = true;
                    nudge(Direction.CLOCKWISE);
                    break;
                default:
                }
            }
        });
    }
}
