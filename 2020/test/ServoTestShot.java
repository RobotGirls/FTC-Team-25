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

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TankDriveTask;

@Autonomous(name = "ServoTestShot")
//@Disabled
public class ServoTestShot extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private Servo rightServo;
    private Servo leftServo;

    private final double OPEN_RIGHT_SERVO = 0;
    private final double OPEN_LEFT_SERVO = 0;

    private final double CLOSE_RIGHT_SERVO = 100;
    private final double CLOSE_LEFT_SERVO = 100;


    private FourWheelDirectDrivetrain drivetrain;

    private static final int TICKS_PER_INCH = 79;

    @Override
    public void handleEvent(RobotEvent e)
    {
       if (e instanceof GamepadTask.GamepadEvent)  {
           GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent)  e;

           switch (event.kind)  {
               case BUTTON_X_DOWN:
                   rightServo.setPosition(OPEN_RIGHT_SERVO);
                   leftServo.setPosition(OPEN_LEFT_SERVO);
                   break;
               case BUTTON_B_DOWN:
                   rightServo.setPosition(CLOSE_RIGHT_SERVO);
                   leftServo.setPosition(CLOSE_LEFT_SERVO);
               case RIGHT_TRIGGER_DOWN:
                   rightServo.setPosition(OPEN_RIGHT_SERVO);
                   leftServo.setPosition(CLOSE_LEFT_SERVO);
               default:
                   RobotLog.i("no button pressed");

           }
       }
    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

                                        //in green is on configuration
        rightServo = hardwareMap.servo.get ("rightServo");
        leftServo = hardwareMap.servo.get("leftServo");



        rightServo.setPosition(0);
        leftServo.setPosition(0);

        GamepadTask gamepad = new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1);
        addTask(gamepad);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, backRight, frontLeft, backLeft);
    }

    @Override
    public void start()
    {
        this.addTask(new TankDriveTask(this, drivetrain));
    }

}
