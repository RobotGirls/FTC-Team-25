package opmodes.colorsensor;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive;

@Config
@Autonomous(group = "drive")
public class ColorSensorTest extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        Telemetry telemetry = new MultipleTelemetry(this.telemetry, FtcDashboard.getInstance().getTelemetry());

        CenterstageSampleMecanumDrive drive = new CenterstageSampleMecanumDrive(hardwareMap);

        waitForStart();

        if (isStopRequested()) return;

        while (!isStopRequested() && opModeIsActive()) {
            if (drive.colorSensor.red()<300 && drive.colorSensor.green()<400 && drive.colorSensor.blue()<300) {
                telemetry.addData("Color: ", "Black");
            }
            else {
                telemetry.addData("Color: ","Yellow");
            }
            telemetry.addData("Red: ", drive.colorSensor.red());
            telemetry.addData("Green: ", drive.colorSensor.green());
            telemetry.addData("Blue: ", drive.colorSensor.blue());
            telemetry.update();
        }
    }
}
