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


import android.util.EventLog;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.MecanumWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Elizabeth Wu on 11/1/17.
 */
@TeleOp(name = "Team 25 Teleop", group = "Team25")
public class VioletTeleop extends Robot {

     /*

    GAMEPAD 1: DRIVETRAIN CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    |  // FOR FUTURE..needs to be programmed
                                        |  (LT) bward left diagonal    (RT) bward right diagonal
      (L bumper)         (R bumper)     |  (LB) fward left diagonal    (RB) fward right diagonal
                            (y)         |
      arrow pad          (x)   (b)      |
                            (a)         |

    GAMEPAD 2: MECHANISM CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    | (LT) rotate block left      (RT) lower relic holder
      (L bumper)         (R bumper)     | (LB) rotate block right     (RB) raise relic holder
                            (y)         |  (y)
      arrow pad          (x)   (b)      |  (b)
                            (a)         |  (a)

    */

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor rotate;
    private DcMotor linear;
    private DcMotor slide;

    private Servo topRight;
    private Servo bottomRight;
    private Servo topLeft;
    private Servo bottomLeft;
    private Servo jewel;
    private Servo relic;

    private FourWheelDirectDrivetrain drivetrain;
    private MecanumWheelDriveTask drive;

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init()
    {
        // Hardware mapping.
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        rotate  = hardwareMap.dcMotor.get("rotate");
        linear  = hardwareMap.dcMotor.get("linear");

        topRight    = hardwareMap.servo.get("topRight");
        bottomRight = hardwareMap.servo.get("bottomRight");
        topLeft     = hardwareMap.servo.get("topLeft");
        bottomLeft  = hardwareMap.servo.get("bottomLeft");
        jewel       = hardwareMap.servo.get("jewel");
        relic       = hardwareMap.servo.get("relic");


        // Reset encoders.
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);



        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
    }

    @Override
    public void start()
    {
        drive = new MecanumWheelDriveTask(this, frontLeft, frontRight, rearLeft, rearRight);
        this.addTask(drive);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_A_DOWN) {
                    topLeft.setPosition(0.5);
                    topRight.setPosition(0.5);
                    bottomLeft.setPosition(0.5);
                    bottomRight.setPosition(0.5);
                } else if (event.kind == EventKind.BUTTON_A_UP) {
                    topLeft.setPosition(0.0);
                    topRight.setPosition(0.0);
                    bottomLeft.setPosition(0.0);
                    bottomRight.setPosition(0.0);
                } else if (event.kind == EventKind.RIGHT_TRIGGER_DOWN) {
                    linear.setPower(-1.0);
                } else if (event.kind == EventKind.RIGHT_BUMPER_DOWN) {
                    linear.setPower(1.0);
                } else if (event.kind == EventKind.LEFT_TRIGGER_DOWN) {
                    rotate.setPower(-1.0);
                } else if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    rotate.setPower(1.0);
                }

            }
        });
    }
}