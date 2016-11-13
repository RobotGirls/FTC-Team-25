package opmodes;

/*
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/8/2016. Wahoo!
 */

import com.qualcomm.robotcore.hardware.Servo;

public class GeneralBeaconArms
{
    Servo right;
    Servo left;
    double leftDeployPos;
    double rightDeployPos;
    double leftStowPos;
    double rightStowPos;
    boolean sensorOnLeft;

    public GeneralBeaconArms(Servo left, Servo right, double leftDeployPos, double rightDeployPos,
                             double leftStowPos, double rightStowPos, boolean isSensorOnLeft)
    {
        this.left = left;
        this.right = right;
        this.leftDeployPos = leftDeployPos;
        this.rightDeployPos = rightDeployPos;
        this.leftStowPos = leftStowPos;
        this.rightStowPos = rightStowPos;
        this.sensorOnLeft = isSensorOnLeft;
    }

    // Alternatively, you could pass in your alliance color (as a boolean isRed or something)
    // to the constructor and pass a color to deploy()... not sure which is better. For now, this:
    public void deploy(boolean sensedMyAlliance)
    {
        // If your alliance color is sensed (e.g. red alliance, red is sensed) and
        // your sensor is on the left, deploy the left arm, and so on.
        if ((sensedMyAlliance && sensorOnLeft) || (!sensedMyAlliance && !sensorOnLeft)) {
            deployLeft();
            stowRight();
        } else if (sensedMyAlliance && !sensorOnLeft) {
            deployRight();
            stowLeft();
        } else if (!sensedMyAlliance && sensorOnLeft) {
            deployRight();
            stowLeft();
        }
    }

    public void deployLeft()
    {
        left.setPosition(leftDeployPos);
    }

    public void deployRight()
    {
        right.setPosition(rightDeployPos);
    }

    public void stowLeft()
    {
        left.setPosition(leftStowPos);
    }

    public void stowRight()
    {
        right.setPosition(rightStowPos);
    }

    public void stowAll()
    {
        left.setPosition(leftStowPos);
        right.setPosition(rightStowPos);
    }
}
