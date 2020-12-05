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

package test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.GamepadTask;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;
import team25core.TankMechanumControlSchemeReverse;
import team25core.TeleopDriveTask;

@TeleOp(name = "UltimateGoalTeleop")
//@Disabled
public class UltimateGoalTeleop extends StandardFourMotorRobot {

    private Telemetry.Item linearPos;
    private Telemetry.Item linearEncoderVal;

    private TeleopDriveTask drivetask;
    private DcMotor launchMech;
    private DcMotor intakeMechLeft;
    private DcMotor intakeMechRight;

    private DcMotor wobbleLift;
    private Servo wobbleGrab;
    private boolean wobbleGrabIsOpen = true;

    private static final int TICKS_PER_INCH = 79;
    private final double OPEN_WOBBLE_SERVO = (float) 244.0 / 256.0;
    private final double CLOSE_WOBBLE_SERVO = (float) 140.0 / 256.0;

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

        //mapping wobble grab servo
        wobbleGrab = hardwareMap.servo.get("wobbleGrabServo");

        //mapping the launch mech and intake mech
        launchMech = hardwareMap.get(DcMotor.class, "launchMech");
        intakeMechLeft = hardwareMap.get(DcMotor.class, "intakeMechLeft");
        intakeMechRight = hardwareMap.get(DcMotor.class, "intakeMechRight");

        //mapping wobble lift motor
        wobbleLift = hardwareMap.get(DcMotor.class, "wobbleLift");

        // using encoders to record ticks
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launchMech.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        intakeMechLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        intakeMechRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        wobbleLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

       /* launch = new OneWheelDirectDrivetrain(launchMech);
        launch.resetEncoders();
        launch.encodersOn();

        intake = new OneWheelDirectDrivetrain(intakeMech);
        intake.resetEncoders();
        intake.encodersOn();

        */

        TankMechanumControlSchemeReverse scheme = new TankMechanumControlSchemeReverse(gamepad1);

        //code for forward mechanum drivetrain:
        //drivetrain = new MechanumGearedDrivetrain(360, frontRight, rearRight, frontLeft, rearLeft);
    }

    @Override
    public void start() {

        TankMechanumControlSchemeReverse scheme = new TankMechanumControlSchemeReverse(gamepad1);

        drivetask = new TeleopDriveTask(this, scheme, frontLeft, frontRight, backLeft, backRight);

        //=== continue from here ===
        this.addTask(drivetask);

        //gamepad 1
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            //@Override
            public void handleEvent(RobotEvent e) {
                GamepadEvent gamepadEvent = (GamepadEvent) e;

                switch (gamepadEvent.kind) {
                    case BUTTON_X_DOWN:
                        // enable the launch mech
                        launchMech.setPower(1);
                        break;
                    case BUTTON_X_UP:
                        // stop the launch mech
                        launchMech.setPower(0);
                        break;
                    case BUTTON_Y_DOWN:
                        //enable the intake mech
                        intakeMechLeft.setPower(-1); //lines 151+152 and 155+156 might need power vals to be switched (or reduced)
                        intakeMechRight.setPower(1);
                    case BUTTON_A_DOWN:
                        //enable the outtake mech
                        intakeMechLeft.setPower(1);
                        intakeMechRight.setPower(-1);
                        break;
                    case BUTTON_Y_UP:
                    case BUTTON_A_UP:
                        // stop the intake/outtake mech
                        intakeMechLeft.setPower(0);
                        intakeMechRight.setPower(0);
                        break;
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
                        wobbleLift.setPower(1); //might have to be changed based on testing
                        break;
                    case DPAD_DOWN_DOWN:
                        //wobble lift down
                        wobbleLift.setPower(-1); //might have to be changed based on testing
                        break;
                    case DPAD_UP_UP:
                    case DPAD_DOWN_UP:
                        wobbleLift.setPower(0);
                        break;
                }
            }
        });
    }
}

