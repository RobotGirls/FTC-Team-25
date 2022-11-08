package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.DeadReckonPath;
import team25core.MechanumGearedDrivetrain;
import team25core.Robot;
import team25core.FourWheelDirectDrivetrain;
import team25core.RobotEvent;
import team25core.TankDriveTask;
import team25core.TwoWheelDirectDrivetrain;


/**
 * FTC Team 25: Created by Bella Heinrichs on 12/12/17.
 */

public class GlyphAutonomousPathUtility {

    public enum TargetColumn {
        LEFT,
        RIGHT,
        CENTER,
    }

    public enum StartStone {
        RED_NEAR,
        RED_FAR,
        BLUE_NEAR,
        BLUE_FAR,
        BLUE_FAR_2,
    }

    /**
     * Declare all of the combinations of DeadReckPath members here.
     */

    DeadReckonPath[][] paths = new DeadReckonPath[20][20];

    public GlyphAutonomousPathUtility()
    {
        /**
         * Initialize all of your paths here.  Note this wastes memory, but puts all your path
         * definitions in one nice location for ease of reading and modification.  If we were sending
         * this robot to mars and we had limited available memory we'd likely do something different.
         *
         * e.g.
         *   paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()] = new DeadReckonPath();
         *   paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(...);
         *   paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(...);
         *   paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(...);
         *
         *   paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.LEFT.ordinal()] = new DeadReckonPath();
         *   paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(...);
         *
         *   etc...
         */


        //RED NEAR RIGHT  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()] = new DeadReckonPath();
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 24, GlyphConstants.STRAIGHT_SPEED);
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_90, GlyphConstants.TURN_SPEED);
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyph, GlyphConstants.STRAIGHT_SPEED);

        //RED NEAR CENTER  -------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.CENTER.ordinal()] = new DeadReckonPath();
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 33, GlyphConstants.STRAIGHT_SPEED);
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_90, GlyphConstants.TURN_SPEED);
        //paths[StartStone.RED_NEAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT , GlyphConstants.placeGlyph, GlyphConstants.STRAIGHT_SPEED);
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT , GlyphConstants.placeGlyph, GlyphConstants.STRAIGHT_SPEED);

        //RED NEAR LEFT  --------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.LEFT.ordinal()] = new DeadReckonPath();
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 42.15, GlyphConstants.STRAIGHT_SPEED);
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_90, GlyphConstants.TURN_SPEED);
        paths[StartStone.RED_NEAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyph, GlyphConstants.STRAIGHT_SPEED);

        //RED FAR RIGHT  ----------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.RIGHT.ordinal()] = new DeadReckonPath();
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 22, GlyphConstants.STRAIGHT_SPEED_FAR);
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, 2, -GlyphConstants.TURN_SPEED);
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 10.5, -GlyphConstants.SIDEWAYS_SPEED);
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyphFAR, GlyphConstants.STRAIGHT_SPEED);

        //RED FAR CENTER  --------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.CENTER.ordinal()] = new DeadReckonPath();
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 22, GlyphConstants.STRAIGHT_SPEED_FAR);
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, 2, -GlyphConstants.TURN_SPEED);
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 19, -GlyphConstants.SIDEWAYS_SPEED);
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyphFAR, GlyphConstants.STRAIGHT_SPEED);

        //RED FAR LEFT  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.LEFT.ordinal()] = new DeadReckonPath();
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 22, GlyphConstants.STRAIGHT_SPEED_FAR);
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, 2, -GlyphConstants.TURN_SPEED);
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 28.5, -GlyphConstants.SIDEWAYS_SPEED);
        paths[StartStone.RED_FAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyphFAR, GlyphConstants.STRAIGHT_SPEED);

        //BLUE NEAR LEFT  --------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.LEFT.ordinal()] = new DeadReckonPath();
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 28.5, -GlyphConstants.STRAIGHT_SPEED);
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_90_BLUE + 1, GlyphConstants.TURN_SPEED);
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyph, GlyphConstants.STRAIGHT_SPEED);

        //BLUE NEAR CENTER  ------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.CENTER.ordinal()] = new DeadReckonPath();
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 35.5, -GlyphConstants.STRAIGHT_SPEED);
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_90_BLUE_C + 1, GlyphConstants.TURN_SPEED);
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyph, GlyphConstants.STRAIGHT_SPEED);

        //BLUE NEAR RIGHT  -------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()] = new DeadReckonPath();
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 45, -GlyphConstants.STRAIGHT_SPEED);
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_90_BLUE + 1, GlyphConstants.TURN_SPEED);
        paths[StartStone.BLUE_NEAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyph, GlyphConstants.STRAIGHT_SPEED);

        //BLUE FAR LEFT  ---------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.LEFT.ordinal()] = new DeadReckonPath();
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 25, -GlyphConstants.STRAIGHT_SPEED_FAR);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 4, -GlyphConstants.SIDEWAYS_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_180_P1, GlyphConstants.TURN_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3.5, -GlyphConstants.STRAIGHT_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_180_P2, GlyphConstants.TURN_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 0.25, GlyphConstants.SIDEWAYS_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.LEFT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyphFAR, GlyphConstants.STRAIGHT_SPEED);

        //BLUE FAR CENTER  -------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.CENTER.ordinal()] = new DeadReckonPath();
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 25, -GlyphConstants.STRAIGHT_SPEED_FAR);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 4, -GlyphConstants.SIDEWAYS_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_180_P1, GlyphConstants.TURN_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3.5, -GlyphConstants.STRAIGHT_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_180_P2, GlyphConstants.TURN_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 7, GlyphConstants.SIDEWAYS_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.CENTER.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyphFAR, GlyphConstants.STRAIGHT_SPEED);
        //BLUE FAR RIGHT  --------------------------------------------------------------------------------------------------------------------------------------------------------------
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.RIGHT.ordinal()] = new DeadReckonPath();
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 25, -GlyphConstants.STRAIGHT_SPEED_FAR);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 4, -GlyphConstants.SIDEWAYS_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_180_P1, GlyphConstants.TURN_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3.5, -GlyphConstants.STRAIGHT_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.TURN, GlyphConstants.TURN_ANGLE_180_P2, GlyphConstants.TURN_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 14, GlyphConstants.SIDEWAYS_SPEED);
        paths[StartStone.BLUE_FAR.ordinal()][TargetColumn.RIGHT.ordinal()].addSegment(DeadReckonPath.SegmentType.STRAIGHT, GlyphConstants.placeGlyphFAR + 1, GlyphConstants.STRAIGHT_SPEED);

    }

    public DeadReckonPath getPath(TargetColumn column, StartStone stone)
    {
        RobotLog.i("506 Stone is " + stone);
        RobotLog.i("506 Target Column is " + column);
        return paths[stone.ordinal()][column.ordinal()];
    }
}
