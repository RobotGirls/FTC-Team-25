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

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RobotTask;

public class ServoCalibrateTask extends RobotTask {

    protected GamepadTask gamepadTask;
    protected ElapsedTime timer;
    protected int position;
    protected int direction;
    protected Servo servo;

    public static int speed = 251;

    public ServoCalibrateTask(Robot robot, Servo servo, int startPoint)
    {
        super(robot);
        this.position = startPoint;
        this.direction = 1;
        this.servo = servo;
    }

    public ServoCalibrateTask(Robot robot, Servo servo)
    {
        super(robot);
        this.position = 128;
        this.direction = 1;
        this.servo = servo;
    }

    @Override
    public void start()
    {
        timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

        gamepadTask = new GamepadTask(robot, GamepadTask.GamepadNumber.GAMEPAD_1) {
            @Override
            public void handleEvent(RobotEvent e) {
                GamepadEvent ge = (GamepadEvent)e;

                if (ge.kind == EventKind.BUTTON_X_DOWN) {
                    direction = -1;
                } else if (ge.kind == EventKind.BUTTON_B_DOWN) {
                    direction = 1;
                } else if (ge.kind == EventKind.BUTTON_Y_DOWN) {
                    direction = 0;
                }
            }
        };
        robot.addTask(gamepadTask);
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean timeslice()
    {
        // Toggle speed.
        robot.addTask(new GamepadTask(robot, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e)
            {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    speed = 100;
                } else if (event.kind == EventKind.LEFT_TRIGGER_DOWN) {
                    speed = 500;
                }
            }
        });

        if (timer.time() > speed) {
            if (position >= 256) {
                direction = -1;
            } else if (position <= 0) {
                direction = 1;
            }

            position += direction;
            servo.setPosition((float)position/(float)256.0);
            timer.reset();
        }
        robot.telemetry.addData("Position: ", position);
        return false;
    }
}
