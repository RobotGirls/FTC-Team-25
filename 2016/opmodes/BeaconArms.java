package opmodes;

/*
 * FTC Team 25: Izzie_Express, December 05, 2015
 */

import com.qualcomm.robotcore.hardware.Servo;

public class BeaconArms {
    Servo right;
    Servo left;

    public boolean sensorOnLeft;

    public BeaconArms (Servo rightServo, Servo leftServo, boolean isSensorOnLeft) {
        this.right = rightServo;
        this.left = leftServo;
        this.sensorOnLeft = isSensorOnLeft;
    }

    public void colorDeploy() {
        // Depends on robot. This position is for 5218's robot.
        if (sensorOnLeft) {
            leftDeploy();
        } else {
            rightDeploy();
        }
    }

    public void leftDeploy() {
        left.setPosition(NeverlandServoConstants.LEFT_PUSHER_DEPLOYED);
    }

    public void rightDeploy() {
        right.setPosition(NeverlandServoConstants.RIGHT_PUSHER_DEPLOYED);
    }

    public void rightStow() {
        right.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
    }

    public void leftStow() {
        left.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
    }

    public void allStow() {
        left.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        right.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
    }
}
