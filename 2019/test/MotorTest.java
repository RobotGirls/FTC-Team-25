package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/*
 * FTC Team 25: Created by Elizabeth Wu, November 06, 2018
 */
@Autonomous(name="Motor Test", group="Team 25")
public class MotorTest extends OpMode {

    private DcMotor motor1;
    private DcMotor motor2;
    private DcMotor motor3;
    private DcMotor motor4;


    public MotorTest() {
        super();
    }

    @Override
    public void init() {
      //  motor1 = hardwareMap.dcMotor.get("frontRight");
     //   motor2 = hardwareMap.dcMotor.get("frontLeft");
        motor3 = hardwareMap.dcMotor.get("rearRight");
       // motor4 = hardwareMap.dcMotor.get("rearLeft");
    }

    @Override
    public void loop() {
      //  motor1.setPower(1);
       // motor2.setPower(-1);
      motor3.setPower(-1);
      //  motor4.setPower(-1);
    }
}
