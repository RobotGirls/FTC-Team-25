package test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;

@TeleOp(name = "ServoLinearSlidesPrototype")
//@Disabled
public class ServoLinearSlidesPrototype extends Robot {
    private Servo leftServo;
    private Servo rightServo;
    private Servo monsterRetentionServo;
    private DcMotor liftMotor;
    private final double OPEN_LEFT_SERVO = 85/256; //FIXME
    private final double OPEN_RIGHT_SERVO = 171/256; //FIXME
    private final double CLOSE_LEFT_SERVO = 52/256; //FIXME
    private final double CLOSE_RIGHT_SERVO = 196/256; //FIXME
    private final double OPEN_MONSTER_RETENTION_SERVO = 256/256;
    private final double CLOSE_MONSTER_RETENTION_SERVO = 128/256;

    @Override
    public void handleEvent(RobotEvent e)
    {
    }

    @Override
    public void init()
    {
        liftMotor = hardwareMap.get(DcMotor.class, "liftMotor");
        leftServo = hardwareMap.servo.get("leftServo");
        rightServo = hardwareMap.servo.get("rightServo");
        monsterRetentionServo = hardwareMap.servo.get ("monsterRetentionServo");
        leftServo.setPosition(CLOSE_LEFT_SERVO);
        rightServo.setPosition(CLOSE_RIGHT_SERVO);
        monsterRetentionServo.setPosition(CLOSE_MONSTER_RETENTION_SERVO);

        liftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftMotor.setPower(0.0);
    }

    @Override
    public void start()
    {
        monsterRetentionServo.setPosition(OPEN_MONSTER_RETENTION_SERVO);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            @Override
            public void handleEvent(RobotEvent e) {
                GamepadEvent gamepadEvent = (GamepadEvent)e;

                switch (gamepadEvent.kind) {
                    case LEFT_BUMPER_DOWN:
                        leftServo.setPosition(OPEN_LEFT_SERVO);
                        rightServo.setPosition(OPEN_RIGHT_SERVO);
                        break;
                    case RIGHT_BUMPER_DOWN:
                        leftServo.setPosition(CLOSE_LEFT_SERVO);
                        rightServo.setPosition(CLOSE_RIGHT_SERVO);
                        break;
                    case BUTTON_A_DOWN:
                        liftMotor.setPower(-0.5);
                        break;
                    case BUTTON_Y_DOWN:
                        liftMotor.setPower(0.5);
                        break;
                    case BUTTON_B_DOWN:
                        liftMotor.setPower(0.0);
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
