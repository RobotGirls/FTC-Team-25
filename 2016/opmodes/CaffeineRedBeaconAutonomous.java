package opmodes;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.Robot;

/*
 * FTC Team 5218: izzielau, November 02, 2015
 */

@Autonomous(name="RED Beacon", group = "AutoTeam25")
@Disabled
public class CaffeineRedBeaconAutonomous extends OpMode {

    public Robot robot;

    public static final int TICKS_PER_INCH = 318;
    public static final int TICKS_PER_DEGREE = 45;

    private DcMotorController mc;
    private DcMotor leftTread;
    private DcMotor rightTread;
    private DcMotor hook;
    private DeviceInterfaceModule core;
    private ModernRoboticsI2cGyro gyro;
    private ColorSensor color;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;
    private Servo climber;

    public BeaconArms beacon;

    private int state = 1;

    public boolean beaconRed = false;

    @Override
    public void init() {
        // Treads.
        rightTread = hardwareMap.dcMotor.get("rightTread");
        leftTread = hardwareMap.dcMotor.get("leftTread");

        leftTread.setDirection(DcMotor.Direction.REVERSE);

        rightTread.setChannelMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        leftTread.setChannelMode(DcMotor.RunMode.RUN_USING_ENCODERS);

        // Servos.
        rightPusher = hardwareMap.servo.get("rightPusher");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightBumper = hardwareMap.servo.get("rightBumper");
        leftBumper = hardwareMap.servo.get("leftBumper");
        climber = hardwareMap.servo.get("climber");

        rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
        leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);
        climber.setPosition(NeverlandServoConstants.CLIMBER_STORE);

        // Beacon arms.
        BeaconArms beacon = new BeaconArms(rightPusher, leftPusher, true);
    }

    public void moveState(double power, double inches, int state, int change) {
        int stateMove = 1;
        telemetry.addData("Move state: ", stateMove);

        switch(stateMove) {
            case 1:
                leftTread.setPower(power);
                rightTread.setPower(power);
                if (Math.abs(leftTread.getCurrentPosition()) >= (inches * TICKS_PER_INCH)) {
                    stateMove = 2;
                }
                break;
            case 2:
                leftTread.setPower(0.0);
                rightTread.setPower(0.0);
                stateMove = 3;
                break;
            case 3:
                leftTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                rightTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                stateMove = 4;
                break;
            case 4:
                if ((leftTread.getCurrentPosition() == 0) && (rightTread.getCurrentPosition() == 0)) {
                    leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                    rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                }
                state = change;
                break;
            default:
                // End of switch (or bug).
                break;
        }
    }

    public void turnState(double power, double degrees, int state, int change) {
        int stateTurn = 1;
        telemetry.addData("Turn state: ", stateTurn);

        switch(stateTurn) {
            case 1:
                leftTread.setPower(power);
                rightTread.setPower(-power);
                if (Math.abs(leftTread.getCurrentPosition()) >= (degrees * TICKS_PER_DEGREE)) {
                    stateTurn = 2;
                }
                break;
            case 2:
                leftTread.setPower(0.0);
                rightTread.setPower(0.0);
                stateTurn = 3;
                break;
            case 3:
                leftTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                rightTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                stateTurn = 4;
                break;
            case 4:
                if ((leftTread.getCurrentPosition() == 0) && (rightTread.getCurrentPosition() == 0)) {
                    leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                    rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                }
                state = change;
                break;
            default:
                // End of switch (or bug).
                break;
        }
    }

    @Override
    public void loop () {
        telemetry.addData("LOOP STATE: ", state);
        switch(state) {
            // Case 1: Move 38.5 inches forward.
            case 1:
                moveState(1.0, 38, state, 2);
                break;
            // Case 2: Turn right 45 degrees.
            case 2:
                turnState(1.0, 45, state, 3);
                break;
            // Case 3: Move 44 inches forward.
            case 3:
                moveState(1.0, 44, state, 4);
                break;
            // Case 4: Turn 45 inches to the right.
            case 4:
                turnState(1.0, 45, state, 5);
                break;
            // Case 5: Move 3 inches forward.
            case 5:
                moveState(1.0, 3, state, 6);
                break;
            // Case 6: Deploy color-sensing pusher.
            case 6:
                beacon.leftDeploy();
                if (leftPusher.getPosition() == NeverlandServoConstants.LEFT_PUSHER_DEPLOYED) {
                    // Change state ONLY IF servo has reached deployed position.
                    state = 7;
                }
                break;
            // Case 7:
            case 7:
                // handleBeacon(): Find beacon color, push button, and dispense climbers.
                handleBeacon();
                break;
        }
    }

    public void dispenseClimbers(int beaconstate, int stateChange) {
        climber.setPosition(NeverlandServoConstants.CLIMBER_SCORE);
        if (climber.getPosition() == NeverlandServoConstants.CLIMBER_SCORE) {
            beaconstate = stateChange;
        }
    }

    public void handleBeacon() {
        int colorState = 1;
        switch(colorState) {
            case 1:
                // Find color of beacon.
                if (color.red() > color.blue()) {
                    // Color is red.
                    beaconRed = true;
                    // Deploy left pusher.
                    beacon.rightStow();
                    beacon.colorDeploy();
                } else {
                    // Color is blue.
                    beaconRed = false;
                    // Deploy right pusher.
                    beacon.leftStow();
                    beacon.rightDeploy();
                }
                colorState = 2;
                break;
            case 2:
                // Make sure pushers are in correct positions.
                if (beaconRed) {
                    boolean leftDeployed = (leftPusher.getPosition() == NeverlandServoConstants.LEFT_PUSHER_DEPLOYED);
                    boolean rightStowed = (rightPusher.getPosition() == NeverlandServoConstants.RIGHT_PUSHER_STOWED);

                    if (leftDeployed && rightStowed) {
                        colorState = 3;
                    }
                } else {
                    boolean leftStowed = (leftPusher.getPosition() == NeverlandServoConstants.LEFT_PUSHER_STOWED);
                    boolean rightDeployed = (rightPusher.getPosition() == NeverlandServoConstants.RIGHT_PUSHER_DEPLOYED);

                    if (leftStowed && rightDeployed) {
                        colorState = 3;
                    }
                }
                break;
            // Case 3: Press button, dispense climbers, and move backwards.
            case 3:
                int beacon = 1;
                switch(beacon) {
                    case 1:
                        moveState(1.0, 8, beacon, 2);
                        break;
                    case 2:
                        dispenseClimbers(beacon, 3);
                        break;
                    case 3:
                        moveState(-1.0, 8, beacon, 4);
                        break;
                    default:
                        // End of switch (or bug).
                        break;
                }
                break;
            default:
                // End or bug.
                break;
        }
    }
}
