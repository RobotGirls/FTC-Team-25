package test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Created by Breanna Chan on 2/15/2018.
 */

@TeleOp(name = "Reed Switch Test", group = "Test")
//@Disabled

public class ReedSwitchTest extends OpMode {

    private Telemetry.Item reedTelemetry;
    private DigitalChannel reedSwitch;

    @Override
    public void init()
    {
        reedSwitch = hardwareMap.digitalChannel.get("limit");

        reedTelemetry = telemetry.addData("State: ", "Unknown");
    }

    @Override
    public void loop()
    {
        if (reedSwitch.getState() == false) {        // when opened, reed switch is off magnet
            reedTelemetry.setValue("Opened");
        } else {                                    // when closed, reed switch is on magnet
            reedTelemetry.setValue("Closed");
        }
    }
}
