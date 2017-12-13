/*
Copyright (c) September 2017 FTC Teams 25/5218

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of FTC Teams 25/5218 nor the names of their contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Bella Heinrichs on 12/9/2017.
 */

@Autonomous(name = "Glyph Paths Testing")
//@Disabled
public class GlyphPaths extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;


    private FourWheelDirectDrivetrain drivetrain;

    /**
     * The default event handler for the robot.
     */
    @Override
    public void handleEvent(RobotEvent e)
    {
        /**
         * Every time we complete a segment drop a note in the robot log.
         */
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "rearLeft");
        backRight = hardwareMap.get(DcMotor.class, "rearRight");

        drivetrain = new FourWheelDirectDrivetrain(frontRight, backRight, frontLeft, backLeft);
    }

    @Override
    public void start()
    {
        DeadReckonPath redFrontP1 = new DeadReckonPath();
        DeadReckonPath redFrontP2 = new DeadReckonPath();
        DeadReckonPath redFrontP3 = new DeadReckonPath();
        DeadReckonPath redBackP1 = new DeadReckonPath();
        DeadReckonPath redBackP2 = new DeadReckonPath();
        DeadReckonPath redBackP3 = new DeadReckonPath();
        DeadReckonPath blueFrontP1 = new DeadReckonPath();
        DeadReckonPath blueFrontP2 = new DeadReckonPath();
        DeadReckonPath blueFrontP3 = new DeadReckonPath();
        DeadReckonPath blueBackP1 = new DeadReckonPath();
        DeadReckonPath blueBackP2 = new DeadReckonPath();
        DeadReckonPath blueBackP3 = new DeadReckonPath();

        //path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 1.0);


        //Red Front Position 1 Path
        redFrontP1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, GlyphConstants.STRAIGHT_SPEED);
        redFrontP1.addSegment(DeadReckonPath.SegmentType.TURN, 90, GlyphConstants.TURN_SPEED);
        redFrontP1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);

        //Red Front Position 2 Path
        redFrontP2.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 18, GlyphConstants.STRAIGHT_SPEED);
        redFrontP2.addSegment(DeadReckonPath.SegmentType.TURN,90 , GlyphConstants.TURN_SPEED);
        redFrontP2.addSegment(DeadReckonPath.SegmentType.STRAIGHT , 4, GlyphConstants.STRAIGHT_SPEED);

        //Red Front Position 3 Path
        redFrontP3.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 21, GlyphConstants.STRAIGHT_SPEED);
        redFrontP3.addSegment(DeadReckonPath.SegmentType.TURN, 90, GlyphConstants.TURN_SPEED);
        redFrontP3.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);

        //----------------------------------------------------------------

        //Red Back Position 1 Path
        redBackP1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, GlyphConstants.STRAIGHT_SPEED);
        redBackP1.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 8, GlyphConstants.STRAIGHT_SPEED );
        redBackP1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);

        //Red Back Position 2 Path
        redBackP2.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, GlyphConstants.STRAIGHT_SPEED);
        redBackP2.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 11, GlyphConstants.STRAIGHT_SPEED);
        redBackP2.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);

        //Red back Position 3 Path
        redBackP3.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, GlyphConstants.STRAIGHT_SPEED);
        redBackP3.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 14, GlyphConstants.STRAIGHT_SPEED);
        redBackP3.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);

        //-----------------------------------------------------------------

        //Blue Front Position 1 Path
        blueFrontP1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, -15, GlyphConstants.STRAIGHT_SPEED);
        blueFrontP1.addSegment(DeadReckonPath.SegmentType.TURN, 90, GlyphConstants.TURN_SPEED);
        blueFrontP1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);

        //Blue Front Position 2 Path
        blueFrontP2.addSegment(DeadReckonPath.SegmentType.STRAIGHT, -18, GlyphConstants.STRAIGHT_SPEED);
        blueFrontP2.addSegment(DeadReckonPath.SegmentType.TURN, 90, GlyphConstants.TURN_SPEED);
        blueFrontP2.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);

        //Blue Front Position 3 Path
        blueFrontP3.addSegment(DeadReckonPath.SegmentType.STRAIGHT, -21, GlyphConstants.STRAIGHT_SPEED);
        blueFrontP3.addSegment(DeadReckonPath.SegmentType.TURN, 90, GlyphConstants.TURN_SPEED);
        blueFrontP3.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);

        //----------------------------------------------------------------

        //Blue Back Position 1 Path
        blueBackP1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, -15, GlyphConstants.STRAIGHT_SPEED);
        blueBackP1.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, -8, 1.0);
        blueBackP1.addSegment(DeadReckonPath.SegmentType.TURN, 180, GlyphConstants.TURN_SPEED);
        blueBackP1.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);

        //Blue Back Position 2 Path
        blueBackP2.addSegment(DeadReckonPath.SegmentType.STRAIGHT, -15, GlyphConstants.STRAIGHT_SPEED);
        blueBackP2.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, -11, 1.0);
        blueBackP2.addSegment(DeadReckonPath.SegmentType.TURN, 180, GlyphConstants.TURN_SPEED);
        blueBackP2.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);

        //Blue Back Position 3 Path
        blueBackP3.addSegment(DeadReckonPath.SegmentType.STRAIGHT, -15, GlyphConstants.STRAIGHT_SPEED);
        blueBackP3.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, -14, 1.0);
        blueBackP3.addSegment(DeadReckonPath.SegmentType.TURN, 180, GlyphConstants.TURN_SPEED);
        blueBackP3.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 4, GlyphConstants.STRAIGHT_SPEED);


        /**
         * Alternatively, this could be an anonymous class declaration that implements
         * handleEvent() for task specific event handlers.
         */
        this.addTask(new DeadReckonTask(this, redFrontP1, drivetrain));

    }

}
