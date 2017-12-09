/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadmanMotorTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.MecanumWheelDriveTask;
import team25core.OneWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

/**
 * FTC Team 25: Created by Breanna Chan and Bella Heinrichs on 11/1/17.
 */
@TeleOp(name = "Violet Teleop", group = "Team25")
public class VioletTeleop extends Robot {

     /*

    GAMEPAD 1: DRIVETRAIN CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    |  (LT) bward left diagonal    (RT) bward right diagonal                                        |
      (L bumper)         (R bumper)     |  (LB) fward left diagonal    (RB) fward right diagonal
                            (y)         |   (y) toggle slowness
      arrow pad          (x)   (b)      |   (x) toggle relic servo      (b) rotate relic
                            (a)         |   (a)
                                        |   (DPad - UP) extend relic   (DPad - DOWN) bring relic in

    GAMEPAD 2: MECHANISM CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    | (LT) nudge block left       (RT) nudge block right
      (L bumper)         (R bumper)     | (LB) toggle servos 1/2      (RB) toggle servos 3/4
                            (y)         |  (y)
      arrow pad          (x)   (b)      |  (x) rotate block left      (b) rotate block right
                            (a)         |  (a)

    */

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor rotate;
    private DcMotor linear;
    private DcMotor slide;

    private Servo s2;
    private Servo s4;
    private Servo s1;
    private Servo s3;
    private Servo jewel;
    private Servo relic;
    private Servo relicRotate;

    private FourWheelDirectDrivetrain drivetrain;
    private MecanumWheelDriveTask drive;
    private OneWheelDriveTask controlLinear;
    //private OneWheelDriveTask controlSlide;
    private DeadmanMotorTask runSlideOutTask;
    private DeadmanMotorTask runSlideInTask;

    private boolean slow;
    //private boolean clawDown = true;
    private boolean s1Open;
    private boolean s3Open;
    private boolean relicOpen;
    private boolean relicDown;
    private Telemetry.Item speed;

    private boolean rotated180 = false;
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
        // Hardware mapping.
        frontLeft  = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft   = hardwareMap.dcMotor.get("rearLeft");
        rearRight  = hardwareMap.dcMotor.get("rearRight");
        rotate     = hardwareMap.dcMotor.get("rotate");
        linear     = hardwareMap.dcMotor.get("linear");
        slide      = hardwareMap.dcMotor.get("slide");

        s2    = hardwareMap.servo.get("s2");
        s4    = hardwareMap.servo.get("s4");
        s1    = hardwareMap.servo.get("s1");
        s3    = hardwareMap.servo.get("s3");
        jewel       = hardwareMap.servo.get("jewel");
        relic       = hardwareMap.servo.get("relic");
        relicRotate = hardwareMap.servo.get("relicRotate");

        // Sets position of jewel for teleop
        jewel.setPosition(VioletConstants.JEWEL_INIT);

        runSlideOutTask = new DeadmanMotorTask(this, slide, 0.75, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.BUTTON_Y);
        runSlideInTask = new DeadmanMotorTask(this, slide, -0.75, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.BUTTON_A);

        // Reset encoders.
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rotate.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rotate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        linear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        linear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //slide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //slide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Allows for rotate, linear, and slide motor to hold position when no button is pressed
        rotate.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        linear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);

        // Sets claw servos to open position
        openClaw();
    }

    /**
     * Move claw up. Uses a 60 motor.
     */
    private void toggleClawUp()
    {
        linear.setDirection(DcMotorSimple.Direction.FORWARD);

        this.addTask(new RunToEncoderValueTask(this, linear, VioletConstants.CLAW_VERTICAL, VioletConstants.CLAW_VERTICAL_POWER));
    }
    /**
     * Move claw down. Uses a 60 motor.
     */
    private void toggleClawDown()
    {
        linear.setDirection(DcMotorSimple.Direction.REVERSE);

        this.addTask(new RunToEncoderValueTask(this, linear, VioletConstants.CLAW_VERTICAL, VioletConstants.CLAW_VERTICAL_POWER));
    }

    /**
     * Blindly open both claws completely and sets to initial state appropriately.
     */
    private void openClaw()
    {
        s1.setPosition(VioletConstants.S1_INIT);
        s2.setPosition(VioletConstants.S2_INIT);
        s3.setPosition(VioletConstants.S3_INIT);
        s4.setPosition(VioletConstants.S4_INIT);

        s1Open = true;
        s3Open = true;
    }

    /**
     * The servos always work in pairs.  S1/S2 and S3/S4.  toggleS1 therefore refers the to the S1/S2 pair.
     */
    private void toggleS1() //pair on top at beginning
    {
        if (s1Open == true) {
            s1.setPosition(VioletConstants.S1_CLOSED);
            s2.setPosition(VioletConstants.S2_CLOSED);
            s1Open = false;
        } else {
            s1.setPosition(VioletConstants.S1_OPEN);
            s2.setPosition(VioletConstants.S2_OPEN);
            s1Open = true;
        }
    }

    /**
     * The servos always work in pairs.  S1/S2 and S3/S4.  toggleS3 therefore refers the to the S3/S4 pair.
     */
    private void toggleS3() //pair on bottom at beginning
    {
        if (s3Open == true) {
            s3.setPosition(VioletConstants.S3_CLOSED);
            s4.setPosition(VioletConstants.S4_CLOSED);
            s3Open = false;
        } else {
            s3.setPosition(VioletConstants.S3_OPEN);
            s4.setPosition(VioletConstants.S4_OPEN);
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
        //int distance;

        if (direction == Direction.CLOCKWISE) {
            rotate.setDirection(DcMotorSimple.Direction.REVERSE);
            //distance = VioletConstants.DEGREES_180;
        } else {
            rotate.setDirection(DcMotorSimple.Direction.FORWARD);
            //distance = VioletConstants.DEGREES_180;
        }

        this.addTask(new RunToEncoderValueTask(this, rotate, VioletConstants.DEGREES_180, VioletConstants.ROTATE_POWER));
    }

    /**
     * Fine alignment for the claw.  Note that there is no deadman motor function specifically to
     * avoid operator error wherein the motor is held on too long and we over rotate thereby
     * damaging the cabling or wire harnesses.
     */
    private void nudge(Direction direction)
    {
        if (direction == Direction.CLOCKWISE) {
            rotate.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            rotate.setDirection(DcMotorSimple.Direction.FORWARD);
        }
        this.addTask(new RunToEncoderValueTask(this, rotate, VioletConstants.NUDGE, VioletConstants.NUDGE_POWER));
    }

    /**
     * Extend relic mechanism out. Uses a 40 motor.
     */
    private void extendRelic()
    {
        slide.setDirection(DcMotorSimple.Direction.FORWARD);

        this.addTask(new RunToEncoderValueTask(this, slide, VioletConstants.RELIC_HORIZONTAL, VioletConstants.RELIC_HORIZONTAL_POWER));
    }

    /**
     * Bring relic mechanism back in. Uses a 40 motor.
     */
    private void contractRelic()
    {
        slide.setDirection(DcMotorSimple.Direction.REVERSE);

        this.addTask(new RunToEncoderValueTask(this, slide, VioletConstants.RELIC_HORIZONTAL, VioletConstants.RELIC_HORIZONTAL_POWER));
    }

    /**
     * Opens and closes relic claw servo.
     */
    private void toggleRelic()
    {
        if (relicOpen == true) {
            relic.setPosition(VioletConstants.RELIC_CLOSED);
            relicOpen = false;
        } else {
            relic.setPosition(VioletConstants.RELIC_OPEN);
            relicOpen = true;
        }
    }

    /**
     * Rotates relic rotate servo. NEED TO FIGURE OUT.
     */
    private void rotateRelic()
    {
        if (relicDown == true) {
            relicRotate.setPosition(VioletConstants.RELIC_ROTATE_UP);
            relicOpen= false;
        } else {
            relicRotate.setPosition(VioletConstants.RELIC_ROTATE_DOWN);
            relicDown = true;
        }
    }

    //

    @Override
    public void start()
    {
        drive = new MecanumWheelDriveTask(this, frontLeft, frontRight, rearLeft, rearRight);
        // Left joystick (Gamepad 2) controls lifting and lowering of glyph mechanism
        controlLinear = new OneWheelDriveTask(this, linear, true);
        // Right joystick (Gamepad 2) controls extending and contracting of relic mechanism
        //controlSlide = new OneWheelDriveTask(this, slide, false);
        this.addTask(drive);
        this.addTask(controlLinear);
        //this.addTask(controlSlide);
        this.addTask(runSlideOutTask);
        this.addTask(runSlideInTask);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;


                // Finish a move before we allow another one.

                if (lockout == true) {
                    return;
                }

                if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    // Toggle top servo pair

                    if (rotated180)
                        toggleS3();
                    else
                        toggleS1();
                } else if (event.kind == EventKind.RIGHT_BUMPER_DOWN) {
                    // Toggle bottom servo pair

                    if (rotated180)
                        toggleS1();
                    else
                        toggleS3();
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    // Rotate 180 degrees clockwise looking from behind robot

                    lockout = true;
                    rotate(Direction.CLOCKWISE);
                    rotated180 = false;
                } else if (event.kind == EventKind.BUTTON_X_DOWN) {
                    // Rotate 180 degrees counterclockwise looking from behind robot

                    lockout = true;
                    rotate(Direction.COUNTERCLOCKWISE);
                    rotated180 = true;
                } else if (event.kind == EventKind.LEFT_TRIGGER_DOWN) {
                    // Nudge counterclockwise looking from behind robot

                    lockout = true;
                    nudge(Direction.COUNTERCLOCKWISE);
                } else if (event.kind == EventKind.RIGHT_TRIGGER_DOWN) {
                    // Nudge clockwise looking from behind robot

                    lockout = true;
                    nudge(Direction.CLOCKWISE);
                }
            }
        });

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;


                if (event.kind == EventKind.BUTTON_X_DOWN) {
                    // Toggle relic claw servo

                    toggleRelic();
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    // Rotate relic NEEDS TO BE CALIBRATED

                    rotateRelic();
                } else if (event.kind == EventKind.DPAD_UP_DOWN) {
                    // Extends Relic slide out

                    extendRelic();
                } else if (event.kind == EventKind.DPAD_DOWN_DOWN) {
                    // Contracts Relic slide out

                    contractRelic();
                } else if (event.kind == EventKind.BUTTON_A_DOWN) {
                    // Toggles slowness of motors

                    if (!slow) {
                        drive.slowDown(0.3);
                        slow = true;
                        speed = telemetry.addData("SLOW", "true");
                    } else {
                        drive.slowDown(false);
                        slow = false;
                        speed = telemetry.addData("SLOW", "false");
                    }
                }
            }
        });

    }
}
