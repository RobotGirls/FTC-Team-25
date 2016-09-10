package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name = "GamepadTest")
public class GamepadLinearTest extends LinearOpMode{

    public enum Alliance {
        RED,
        BLUE,
        PURPLE,
    };

    Alliance alliance;

    private boolean done;

    protected Alliance selectAlliance() throws InterruptedException
    {
        Alliance alliance = Alliance.PURPLE;

        while (!done) {
            if (gamepad1.a) {
                alliance = Alliance.RED;
            }
            if (gamepad1.b) {
                alliance = Alliance.BLUE;
            }
            waitOneFullHardwareCycle();
            telemetry.addData("Alliance: ", alliance.toString());
        }

        return alliance;
    }

    @Override
    public void runOpMode() throws InterruptedException {

        done = false;
        alliance = selectAlliance();

        waitForStart();

        telemetry.addData("Alliance: ", alliance.toString());
    }
}
