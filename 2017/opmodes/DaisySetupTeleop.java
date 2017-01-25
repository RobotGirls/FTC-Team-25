package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadmanMotorTask;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 12/3/2016.
 */

@Autonomous(name = "Daisy: Setup for Autonomous", group = "Team25")
@Disabled
public class DaisySetupTeleop extends Robot
{
    /*

    GAMEPAD 1: MECHANISM CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    |
      (L bumper)         (R bumper)     |
                            (y)         | (y) run launcher forward
      arrow pad          (x)   (b)      | (x) run conveyor forward    (b) run conveyor backward
                            (a)         | (a) run launcher backward

    */

    private DcMotor launcher;
    private DcMotor conveyor;
    private DeadmanMotorTask runLauncherBackTask;
    private DeadmanMotorTask runLauncherForwardTask;
    private DeadmanMotorTask runConveyorForwardTask;
    private DeadmanMotorTask runConveyorBackTask;

    @Override
    public void handleEvent(RobotEvent e)
    {
       // Nothing.
    }

    @Override
    public void init()
    {
        conveyor = hardwareMap.dcMotor.get("conveyor");
        launcher = hardwareMap.dcMotor.get("launcher");

        runLauncherForwardTask = new DeadmanMotorTask(this, launcher,  0.1, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_Y);
        runLauncherBackTask    = new DeadmanMotorTask(this, launcher, -0.1, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_A);
        runConveyorForwardTask = new DeadmanMotorTask(this, conveyor,  0.1, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_X);
        runConveyorBackTask    = new DeadmanMotorTask(this, conveyor, -0.1, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_B);
    }

    @Override
    public void start()
    {
        addTask(runLauncherForwardTask);
        addTask(runLauncherBackTask);
        addTask(runConveyorForwardTask);
        addTask(runConveyorBackTask);
    }
}
