package org.firstinspires.ftc.teamcode.drive;

import com.acmerobotics.roadrunner.drive.SampleMecanumDrive;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;

public class RoadrunnerTrajectoriesClass {

    SampleMecanumDrive driveTrain;

    public RoadrunnerTrajectoriesClass (SampleMecanumDrive dT) {
        driveTrain = dT;
    }

    public Trajectory forwardByInches (double x, double y, double heading, double inches){

        Trajectory forwards = driveTrain.trajectoryBuilder(new Pose2d (x, y, heading))
                .forward(inches)
                 .build();

        return forwards;
    }

    public Trajectory backwardsByInches (double x, double y, double heading, double inches){
        Trajectory backwards = driveTrain.trajectoryBuilder(new Pose2d (x, y, heading))
                .back(inches)
                .build();

        return backwards;
    }

    public Trajectory strafeLeft (double x, double y, double heading, double inches){

        Trajectory left = driveTrain.trajectoryBuilder(new Pose2d (x, y, heading))
                .strafeLeft(inches)
                .build();

        return left;
    }

    public Trajectory strafeRight (double x, double y, double heading, double inches){

        Trajectory right = driveTrain.trajectoryBuilder(new Pose2d (x, y, heading))
                .strafeRight(inches)
                .build();

        return right;
    }

    public Trajectory lineTo (double currentX, double currentY, double currentHeading, double goalX, double goalY){

        Trajectory lineToPosition = driveTrain.trajectoryBuilder(new Pose2d (currentX, currentY, currentHeading))
                .lineTo(new Vector2d(goalX, goalY))
                .build();

        return lineToPosition;
    }

    public Trajectory strafeTo (double currentX, double currentY, double currentHeading, double goalX, double goalY){

        Trajectory strafeToPosition = driveTrain.trajectoryBuilder(new Pose2d (currentX, currentY, currentHeading))
                .strafeTo(new Vector2d(goalX, goalY))
                .build();

        return strafeToPosition;
    }

    public Trajectory splineTo (double currentX, double currentY, double currentHeading, double goalX, double goalY, double goalHeading){

        Trajectory splineToPosition = driveTrain.trajectoryBuilder(new Pose2d(currentX, currentY, currentHeading))
                .splineTo(new Vector2d(goalX, goalY), Math.toRadians(goalHeading))
                .build();

        return splineToPosition;
    }

}