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
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;


import team25core.DeadReckonPath;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.LimitSwitchCriteria;
import team25core.OneWheelDirectDrivetrain;
import team25core.OneWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;
import team25core.TankMechanumControlScheme;
import team25core.TeleopDriveTask;
import team25core.VioletOneWheelDriveTask;

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
                            (y)         |   (y)
      arrow pad          (x)   (b)      |   (x)                        (b)
                            (a)         |   (a) toggle slowness
                                        |   (DPad - UP) extend relic   (DPad - DOWN) bring relic in

    GAMEPAD 2: MECHANISM CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    | (LT) nudge block left       (RT) nudge block right
      (L bumper)         (R bumper)     | (LB) toggle top servos      (RB) toggle bottom servos
                            (y)         |  (y) toggle relic servo
      arrow pad          (x)   (b)      |  (x)                        (b) rotate glyph
                            (a)         |  (a) rotate relic

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

    private DigitalChannel limitSwitch;
    private LimitSwitchCriteria limitSwitchCriteria;

    private DeadReckonPath rotatePath;

    private FourWheelDirectDrivetrain drivetrain;
    private OneWheelDirectDrivetrain rotateDrivetrain;
    private TeleopDriveTask drive;
    //private OneWheelDriveTask controlLinear;
    //private OneWheelDriveTask controlSlide;
    private VioletOneWheelDriveTask controlLinear;
    private VioletOneWheelDriveTask controlSlide;
    //private DeadmanMotorTask runSlideOutTask;
    //private DeadmanMotorTask runSlideInTask;

    private boolean slow = false;
    private boolean s1Open= true;
    private boolean s3Open = true;
    private boolean relicOpen = false;
    private boolean relicDown = true;
    private boolean rotateLeft = true;
    private Telemetry.Item speed;
    private Telemetry.Item encoderRelic;
    private Telemetry.Item encoderLift;
    private boolean rotated180 = false;
    private boolean lockout = false;

    public Direction currentDirection;
    public int currentEncoder;
    public boolean goDown = false;

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

        limitSwitch = hardwareMap.digitalChannel.get("limit");
        limitSwitchCriteria = new LimitSwitchCriteria(limitSwitch);

        rotatePath = new DeadReckonPath();

        // Sets position of jewel for teleop  (moved to start)
        //jewel.setPosition(VioletConstants.JEWEL_UP);

        //runSlideOutTask = new DeadmanMotorTask(this, slide, 0.75, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.BUTTON_Y);
        //runSlideInTask = new DeadmanMotorTask(this, slide, -0.75, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.BUTTON_A);

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
        slide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Allows for rotate, linear, and slide motor to hold position when no button is pressed
        rotate.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        linear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        rotateDrivetrain = new OneWheelDirectDrivetrain(rotate);

        // Sets initial positions for claw servos, relic grab servo, and relicRotate servo (moved to start)
        //openClaw();
        //relic.setPosition(VioletConstants.RELIC_INIT);
        //relicRotate.setPosition(VioletConstants.RELIC_ROTATE_DOWN);
    }

    /**
     * Blindly open both claws completely and sets to initial state in order to fit in box.
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
        if (s1Open) {
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
        if (s3Open) {
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
     * Move claw up or down. Uses a 60 motor.
     */
    private void moveClaw(Direction direction)
    {
        if (direction == Direction.CLOCKWISE)
            linear.setDirection(DcMotorSimple.Direction.REVERSE);
        else
            linear.setDirection(DcMotorSimple.Direction.FORWARD);

        this.addTask(new RunToEncoderValueTask(this, linear, VioletConstants.VERTICAL_MIN_HEIGHT, VioletConstants.CLAW_VERTICAL_POWER));
    }

    /**
     * Lift claw up. Uses a 60 motor.
     */
    private void liftClawUp()
    {
        linear.setDirection(DcMotorSimple.Direction.FORWARD);

        this.addTask(new RunToEncoderValueTask(this, linear, VioletConstants.CLAW_VERTICAL, VioletConstants.CLAW_VERTICAL_POWER));
    }

    /**
     * Lower claw down. Uses a 60 motor.
     */
    private void lowerClawDown()
    {
        linear.setDirection(DcMotorSimple.Direction.REVERSE);
        RobotLog.e("Beginning LowerClawDown: %d", linear.getCurrentPosition());

        this.addTask(new RunToEncoderValueTask(this, linear, VioletConstants.VERTICAL_MIN_HEIGHT, VioletConstants.CLAW_VERTICAL_POWER) {
            @Override
            public void handleEvent(RobotEvent e) {
                RunToEncoderValueTask.RunToEncoderValueEvent lowerClaw = (RunToEncoderValueTask.RunToEncoderValueEvent) e;
                // note that because we reversed direction, when we start going down, the encoder values are actually
                // increasing as we go down instead of decreasing
                RobotLog.e("LowerClawDown Handler: %d", linear.getCurrentPosition());
                if (lowerClaw.kind == RunToEncoderValueTask.EventKind.DONE) {
                    RobotLog.e("LowerClawDown Handler DONE***");
                    linear.setDirection(DcMotorSimple.Direction.FORWARD);
                    RobotLog.e("Before clear encoder: %d", linear.getCurrentPosition());
                    //linear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    RobotLog.e("After clear encoder: %d", linear.getCurrentPosition());
                }
            }
        });
    }

    /**
     * This dead reckon path rotates the glyph mechanism off the switch. Runs at a faster speed.
     */
    private DeadReckonPath moveOffSwitchPath()
    {
        DeadReckonPath path = new DeadReckonPath();
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 1, 0.2);
        return path;
    }

    /**
     * This dead reckon path rotates the glyph mechanism until it senses the limit switch.
     * Runs at a slower speed so we don't miss the switch.
     */
    private DeadReckonPath moveToSwitchPath()
    {
        DeadReckonPath path = new DeadReckonPath();
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, 0.1);
        return path;
    }

    /**
     * We will spin the claw back and forth, be careful that you alternate directions so that
     * you don't wrap the servo cables around the motor shaft.
     *
     * This motor's movement is not symmetrical, so we'll compensate using nudge.
     */
    private void rotateGlyph(Direction direction)
    {
        RobotLog.e("251: in rotate glyph.");
        if (direction == Direction.CLOCKWISE) {
            rotate.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            rotate.setDirection(DcMotorSimple.Direction.FORWARD);
        }
        this.addTask(new RunToEncoderValueTask(this, rotate, VioletConstants.DEGREES_180, VioletConstants.ROTATE_POWER));
        /**
        this.addTask(new DeadReckonTask(this, moveOffSwitchPath(), rotateDrivetrain) {
            @Override
            public void handleEvent(RobotEvent event) {
                RobotLog.e("504 251 Rotate: in handleEvent");
                rotateDrivetrain.stop();
                robot.addTask(new DeadReckonTask(robot, moveToSwitchPath(), rotateDrivetrain, limitSwitchCriteria) {
                    @Override
                    public void handleEvent (RobotEvent event) {
                        DeadReckonEvent e = (DeadReckonEvent) event;
                        RobotLog.e("504 251 added limitSwitch deadReckonTask");

                        if (e.kind == EventKind.SENSOR_SATISFIED) {
                            RobotLog.e("504 251 SENSOR SATISFIED");
                            rotateDrivetrain.stop();
                        }
                    }
                });
                RobotLog.e("504 251 stopping first path task");
            }
        }); */
    }

    /**
     * Alternates the 180 degree rotation of the glyph mechanism. rotateLeft is initialized
     * to false at beginning. Also controls the alternating of controls for the top and bottom
     * servo claw pairs.
     */

    private void alternateRotate()
    {
        if (rotateLeft) {       // happens first
            RobotLog.e("251: in alternate rotate glyph, rotateLeft true. Clockwise");
            rotateGlyph(Direction.CLOCKWISE);
            rotateLeft = false;
            rotated180 = true; // rotated180 equal to true means that S3 and S4 servo pair is on top

        } else {
            RobotLog.e("251: in alternate rotate glyph, rotateLeft false. Counterclockwise");
            rotateGlyph(Direction.COUNTERCLOCKWISE);
            rotateLeft = true;
            rotated180 = false; // rotated180 equal to false means that S1 and S2 servo pair is on top
        }
    }

    /**
     * Fine alignment for the claw.  Note that there is no deadman motor function specifically to
     * avoid operator error wherein the motor is held on too long and we over rotate thereby
     * damaging the cabling or wire harnesses.
     */
    private void nudgeGlyph(Direction direction)
    {
        if (direction == Direction.CLOCKWISE) {
            RobotLog.e("251: in nudgle glyph. Clockwise");
            rotate.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            RobotLog.e("251: in nudgle glyph. Counterclockwise");
            rotate.setDirection(DcMotorSimple.Direction.FORWARD);
        }
        this.addTask(new RunToEncoderValueTask(this, rotate, VioletConstants.NUDGE, VioletConstants.NUDGE_POWER));
    }

    /**
     * Extend relic mechanism out. Uses a 60 motor.
     */
    private void extendRelic()
    {
        slide.setDirection(DcMotorSimple.Direction.FORWARD);

        this.addTask(new RunToEncoderValueTask(this, slide, VioletConstants.RELIC_HORIZONTAL, VioletConstants.RELIC_HORIZONTAL_POWER));
    }

    /**
     * Bring relic mechanism back in. Uses a 60 motor.
     */
    private void contractRelic()
    {
        slide.setDirection(DcMotorSimple.Direction.REVERSE);

        this.addTask(new RunToEncoderValueTask(this, slide, VioletConstants.RELIC_HORIZONTAL, VioletConstants.RELIC_HORIZONTAL_POWER));
    }

    /**
     * Open and close relic claw servo.
     */
    private void toggleRelicClaw()
    {
        if (relicOpen) {
            relic.setPosition(VioletConstants.RELIC_CLOSED);
            relicOpen = false;
        } else {
            relic.setPosition(VioletConstants.RELIC_OPEN);
            relicOpen = true;
        }
    }

    /**
     * Rotate relic rotate servo.
     */
    private void rotateRelic()
    {
        if (relicDown) {
            relicRotate.setPosition(VioletConstants.RELIC_ROTATE_UP);
            relicDown = false;
        } else {
            relicRotate.setPosition(VioletConstants.RELIC_ROTATE_DOWN);
            relicDown = true;
        }
    }

    @Override
    public void start()
    {
        // Sets position of jewel for teleop
        jewel.setPosition(VioletConstants.JEWEL_UP);

        // Sets initial positions for claw servos, relic claw servo, and relicRotate servo
        openClaw();
        relic.setPosition(VioletConstants.RELIC_INIT);
        relicRotate.setPosition(VioletConstants.RELIC_ROTATE_DOWN);

        TankMechanumControlScheme scheme = new TankMechanumControlScheme(gamepad1);

        drive = new TeleopDriveTask(this, scheme, frontLeft, frontRight, rearLeft, rearRight);

        // Left joystick (Gamepad 2) controls lifting and lowering of glyph mechanism
        linear.setDirection(DcMotorSimple.Direction.FORWARD); // To run glyph mechanism up
        //controlLinear = new OneWheelDriveTask(this, linear, true);
        controlLinear = new VioletOneWheelDriveTask(this, linear, true, 0.5);

        // Right joystick (Gamepad 2) controls running out of relic mechanism
        slide.setDirection(DcMotorSimple.Direction.REVERSE); //FIXME?
        //controlSlide = new OneWheelDriveTask(this, slide, false);
        controlSlide = new VioletOneWheelDriveTask(this, slide, false, 1.0);
        controlSlide.useCeiling(VioletConstants.RELIC_CEILING);

        this.addTask(drive);
        this.addTask(controlLinear);
        this.addTask(controlSlide);
        //this.addTask(runSlideOutTask);
        //this.addTask(runSlideInTask);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;


                // Finish a move before we allow another one.

                if (lockout == true) {
                    return;
                }

                if (event.kind == EventKind.BUTTON_Y_DOWN) {
                    // Toggle relic claw servo

                    toggleRelicClaw();
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    // Rotate 180 degrees

                    lockout = true;
                    RobotLog.e("251: in button b.");
                    alternateRotate();
                } else if (event.kind == EventKind.BUTTON_A_DOWN) {
                    // Rotate relic

                    rotateRelic();
                } /**else if (event.kind == EventKind.DPAD_UP_DOWN) {
                    // Extends relic slide out

                    extendRelic();
                } else if (event.kind == EventKind.DPAD_DOWN_DOWN) {
                    // Contracts relic slide in

                    contractRelic();
                } */else if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    // Toggle top servo pair

                    RobotLog.e("251: in left bumper for nudge");
                    if (rotated180)
                        toggleS3();
                    else
                        toggleS1();
                } else if (event.kind == EventKind.RIGHT_BUMPER_DOWN) {
                    // Toggle bottom servo pair

                    RobotLog.e("251: in right bumper for nudge");
                    if (rotated180)
                        toggleS1();
                    else
                        toggleS3();
                } else if (event.kind == EventKind.LEFT_TRIGGER_DOWN) {
                    // Nudge counterclockwise looking from behind robot

                    lockout = true;
                    nudgeGlyph(Direction.COUNTERCLOCKWISE);
                } else if (event.kind == EventKind.RIGHT_TRIGGER_DOWN) {
                    // Nudge clockwise looking from behind robot

                    lockout = true;
                    nudgeGlyph(Direction.CLOCKWISE);
                }
            }
        });

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;


                if (event.kind == EventKind.BUTTON_X_DOWN) {
                    // Toggle relic claw servo

                    toggleRelicClaw();
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    // Rotate relic

                    rotateRelic();
                } else if (event.kind == EventKind.BUTTON_A_DOWN) {
                    // If slow, then normal speed. If fast, then slow speed of motors.

                    if (slow) {
                        drive.slowDown(1.0);
                        slow = false;
                        speed = telemetry.addData("SLOW", "false");
                    } else {
                        drive.slowDown(0.3);
                        slow = true;
                        speed = telemetry.addData("SLOW", "true");
                    }
                } /**else if (event.kind == EventKind.DPAD_UP_DOWN) {
                    // Extends relic slide out

                    extendRelic();
                } else if (event.kind == EventKind.DPAD_DOWN_DOWN) {
                    // Contracts relic slide out

                    contractRelic();
                }*/
            }
        });

    }
}
