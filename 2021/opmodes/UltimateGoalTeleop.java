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
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.GamepadTask;
import team25core.RobotEvent;
import team25core.SingleGamepadControlScheme;
import team25core.SingleShotTimerTask;
import team25core.StandardFourMotorRobot;
import team25core.TeleopDriveTask;

@TeleOp(name = "UltimateGoalTeleop")
//@Disabled
public class UltimateGoalTeleop extends StandardFourMotorRobot {

    private Telemetry.Item linearPos;
    private Telemetry.Item linearEncoderVal;

    private TeleopDriveTask drivetask;
    private DcMotor launchMechLeft;
    private DcMotor launchMechRight;

    private DcMotor wobbleLift;
    private Servo wobbleGrab;
    private boolean wobbleGrabIsOpen = true;

    private DcMotor ringLift; //hd hex 40
    private Servo ringDispenser; //regular servo
    private boolean ringDispenserExtended = false;

    private static final int TICKS_PER_INCH = 79;
    private final double OPEN_WOBBLE_SERVO = (float) 244.0 / 256.0;
    private final double CLOSE_WOBBLE_SERVO = (float) 140.0 / 256.0;
    private final double DISPENSE_RING = (float) 245.0 / 256.0;
    private final double RETURN_RING_DISPENSER = (float) 140.0 / 256.0;

    //private FourWheelDirectDrivetrain drivetrain;
    //private MechanumGearedDrivetrain drivetrain;

    @Override
    public void handleEvent(RobotEvent e) {
    }

    @Override
    public void init() {

        super.init();

        //mapping the wheels
        frontLeft = hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        backLeft = hardwareMap.get(DcMotorEx.class, "backLeft");
        backRight = hardwareMap.get(DcMotorEx.class, "backRight");

        //mapping wobble goal mechanism
        wobbleGrab = hardwareMap.servo.get("wobbleGrabServo");
        wobbleLift = hardwareMap.get(DcMotor.class, "wobbleLift");

        //mapping the intake mech
        launchMechLeft = hardwareMap.get(DcMotor.class, "intakeMechLeft");
        launchMechRight = hardwareMap.get(DcMotor.class, "intakeMechRight");

        //mapping the ring elevator mechanism
        ringLift = hardwareMap.get(DcMotor.class, "ringLift");
        ringDispenser = hardwareMap.servo.get("ringDispenser");

        // using encoders to record ticks
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launchMechLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launchMechRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        wobbleLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        ringLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

       /* launch = new OneWheelDirectDrivetrain(launchMech);
        launch.resetEncoders();
        launch.encodersOn();

        intake = new OneWheelDirectDrivetrain(intakeMech);
        intake.resetEncoders();
        intake.encodersOn();

        */

        //code for forward mechanum drivetrain:
        //drivetrain = new MechanumGearedDrivetrain(360, frontRight, rearRight, frontLeft, rearLeft);
    }

    @Override
    public void start() {

        SingleGamepadControlScheme scheme = new SingleGamepadControlScheme(gamepad1);

        drivetask = new TeleopDriveTask(this, scheme, frontLeft, frontRight, backLeft, backRight);

        this.addTask(drivetask);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            //@Override
            public void handleEvent(RobotEvent e) {
                GamepadEvent gamepadEvent = (GamepadEvent) e;

                switch (gamepadEvent.kind) {
                    //launching system
                    case BUTTON_X_DOWN:
                        //shooting Servo open or closed depending on boolean toggle
                        if (ringDispenserExtended) {
                            ringDispenser.setPosition(RETURN_RING_DISPENSER);
                            ringDispenserExtended = false;
                        } else {
                            ringDispenser.setPosition(DISPENSE_RING);
                            ringDispenserExtended = true;
                        }
                        break;
                    case BUTTON_Y_DOWN:
                        //activate launching mech
                        launchMechLeft.setPower(0.5);
                        launchMechRight.setPower(-0.15);
                    case BUTTON_Y_UP:
                        // stop the launching mech
                        launchMechLeft.setPower(0);
                        launchMechRight.setPower(0);
                        break;
                    case LEFT_TRIGGER_DOWN:
                        //lift ring elevator UP
                        ringLift.setPower(0.1);
                        break;
                    case RIGHT_TRIGGER_DOWN:
                        //ring elevator DOWN
                        ringLift.setPower(-0.1);
                        break;
                    case LEFT_TRIGGER_UP:
                    case RIGHT_TRIGGER_UP:
                        ringLift.setPower(0);
                        break;
                    //wobble goal system
                    case BUTTON_B_DOWN:
                        //wobble servo close OR open depending on boolean toggle;
                        if (wobbleGrabIsOpen) {
                            wobbleGrab.setPosition(CLOSE_WOBBLE_SERVO);
                            wobbleGrabIsOpen = false;
                        } else {
                            wobbleGrab.setPosition(OPEN_WOBBLE_SERVO);
                            wobbleGrabIsOpen = true;
                        }
                        break;
                    case DPAD_UP_DOWN:
                        //wobble lift up
                        wobbleLift.setPower(0.5);
                        break;
                    case DPAD_DOWN_DOWN:
                        //wobble lift down
                        wobbleLift.setPower(-0.5);
                        break;
                    case DPAD_UP_UP:
                    case DPAD_DOWN_UP:
                        wobbleLift.setPower(0);
                        break;

                    /*
                    BUTTON A: reserved for intake
                    BUTTON B: wobble grabbing servo
                    BUTTON X: dispense ring OR retract dispenser
                    BUTTON Y: activate launching motors

                    L TRIGGER: ring elevator rises
                    R TRIGGER: ring elevator descends

                    DPAD UP: raise wobble goal
                    DPAD DOWN: lower wobble goal

                    EMPTY BUTTONS: L DPAD, R DPAD, L BUMPER, R BUMPER
                     */
                }
            }
        });
    }
}

