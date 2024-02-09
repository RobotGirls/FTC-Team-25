package opmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.drive.CenterstageSampleMecanumDrive;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

@Config
@Autonomous(name = "RRAutoBlueLeft")
public class RRAutoCSBase extends LinearOpMode {
    public static double DISTANCE = 30; // in
    private DistanceSensor distanceSensor;
    private DistanceSensor distanceSensor2;

    private String propPosition = "center";

    private final double BLOCK_NOTHING = 0.05;
    private final double BLOCK_BOTH = 0.8;

    // FIXME distance sensor code here (figuring out which spike the prop is on)

    @Override
    public void runOpMode() throws InterruptedException {
        Telemetry telemetry = new MultipleTelemetry(this.telemetry, FtcDashboard.getInstance().getTelemetry());

        CenterstageSampleMecanumDrive drive = new CenterstageSampleMecanumDrive(hardwareMap);

        distanceSensor = hardwareMap.get(Rev2mDistanceSensor.class, "distanceSensor");

        // you can also cast this to a Rev2mDistanceSensor if you want to use added
        // methods associated with the Rev2mDistanceSensor class.
        Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor) distanceSensor;

        TrajectorySequence leftSpike = drive.trajectorySequenceBuilder(new Pose2d(0,0,Math.toRadians(270)))
                // LEFT SPIKE PATH
                .build();
        TrajectorySequence centerSpike = drive.trajectorySequenceBuilder(new Pose2d(0,0,Math.toRadians(270)))
                // CENTER SPIKE PATH
                .build();
        TrajectorySequence rightSpike = drive.trajectorySequenceBuilder(new Pose2d(0,0,Math.toRadians(270)))
                // RIGHT SPIKE PATH
                .build();


        waitForStart();

        if (isStopRequested()) return;

        if (propPosition == "left") {
            drive.followTrajectorySequence(leftSpike);
        }
        else if (propPosition == "center") {
            drive.followTrajectorySequence(centerSpike);
        }
        else {
            drive.followTrajectorySequence(rightSpike);
        }


        while (!isStopRequested() && opModeIsActive()) {
            // generic DistanceSensor methods.
            telemetry.addData("deviceName", distanceSensor.getDeviceName() );
            telemetry.addData("range", String.format("%.01f mm", distanceSensor.getDistance(DistanceUnit.MM)));
            telemetry.addData("range", String.format("%.01f cm", distanceSensor.getDistance(DistanceUnit.CM)));
            telemetry.addData("range", String.format("%.01f m", distanceSensor.getDistance(DistanceUnit.METER)));
            telemetry.addData("range", String.format("%.01f in", distanceSensor.getDistance(DistanceUnit.INCH)));

            // Rev2mDistanceSensor specific methods.
            telemetry.addData("ID", String.format("%x", sensorTimeOfFlight.getModelID()));
            telemetry.addData("did time out", Boolean.toString(sensorTimeOfFlight.didTimeoutOccur()));

            telemetry.update();
        }
    }
}
