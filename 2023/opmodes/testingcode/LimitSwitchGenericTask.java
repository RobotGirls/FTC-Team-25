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

package testingcode;

import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class LimitSwitchGenericTask extends RobotTask {

    protected DigitalChannel module;
    protected boolean normallyClosed;
    public String status;

    protected Telemetry.Item moduleTlm;

    // Constructor.
    public LimitSwitchGenericTask(Robot robot, DigitalChannel module)
    {
        super(robot);

        this.module = module;

        this.limitState = new SwitchState();
        this.limitState.switch_closed = false;
        this.limitState.switch_open = false;
        this.limitState.switch_unknown = true;
    }

    public LimitSwitchGenericTask(Robot robot, DigitalChannel module, Telemetry telemetry)
    {
        super(robot);

        this.module = module;

        this.limitState = new SwitchState();
        this.limitState.switch_closed = false;
        this.limitState.switch_open = false;
        this.limitState.switch_unknown = true;

        moduleTlm = telemetry.addData("Switch State","unknown");
    }

    // Instance of SwitchState.
    protected SwitchState limitState;

    // Class: boolean limit states.
    public class SwitchState {
        public boolean switch_open;
        public boolean switch_closed;
        public boolean switch_unknown;
    }

    // Class: events.
    public class LimitSwitchEvent extends RobotEvent {
        public EventKind kind;

        public LimitSwitchEvent(RobotTask task, EventKind k)
        {
            super(task);
            kind = k;
        }
    }

    // Enumeration: events.
    public enum EventKind {
        OPEN,
        CLOSED,
        UNKNOWN,
    }

    public boolean limitSwitchClosed()
    {
        /*
         * TODO: Implement with new hardware.
         */
        return false;
    }

    public void setSwitchMode(boolean normallyClosed)
    {
        this.normallyClosed = normallyClosed;
    }

    @Override
    public void start()
    {
        /*
         * TODO: Implement with new hardware.
         */
    }

    @Override
    public void stop()
    {
    }

    @Override
    public boolean timeslice() {
        /*
         * TODO: Implement with new hardware.
         */
        if (module.getState() == false) {
            robot.queueEvent(new LimitSwitchEvent(this, EventKind.CLOSED));
            limitState.switch_closed = true;
            moduleTlm.setValue("closed");
        } else if (module.getState() == true) {
            robot.queueEvent(new LimitSwitchEvent(this, EventKind.OPEN));
            limitState.switch_closed = false;
            moduleTlm.setValue( "open");
        } else {
            moduleTlm.setValue("in else");
        }

        // This task doesn't stop.
        return false;
    }
}