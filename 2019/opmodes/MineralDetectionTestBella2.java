package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.MineralDetectionTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

import static test.MDConstants.*;

@Autonomous(name = "Mineral Detection Test Bella3", group = "Team 25")
@Disabled
public class MineralDetectionTestBella2 extends Robot {

    private final static String TAG = "BLAH MineralDetectionTest";

    MineralDetectionTask mdTask;
    private double left1 = 0;
    private double confidence1;
    private boolean started = false;
    private int stepNum = 0;
    private boolean foundGold = false;

    // TODO: ELIZABETH
    public static double LATCH_OPEN = 130;
    public static double LATCH_CLOSED = 180;

    @Override
    public void handleEvent(RobotEvent e)
    {
    }

    private void setLeft(double leftVal) {
        RobotLog.i("BLAH Mineral setLeft with value BEFORE "+ left1 +"blah");
        left1 = leftVal;
        RobotLog.i("BLAH Mineral setLeft with value "+ left1 +"blah");
        RobotLog.i("BLAH Mineral leftVal with value "+leftVal+"blah");

    }

    @Override
    public void init()
    {
        /* TODO ============================================
        // Hardware mapping.
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        latchArm    = hardwareMap.dcMotor.get("latchArm");
        marker      = hardwareMap.servo.get("marker");

        TODO: ELIZABETH START ----------------------
        // Latch servo used to close claw at end of latch arm
        latchServo      = hardwareMap.servo.get("latchServo");

        latchArm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        latchArm.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Allows for latchArm to hold position when no button is pressed
        latchArm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        latchServo.setPosition(LATCH_CLOSED);
        TODO: ELIZABETH END ------------------------


        // Drive train for driving
        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);


        // Drivetrain for latching arm
        single     = new OneWheelDirectDrivetrain(latchArm);

        TODO: ELIZABETH START -------------------
        drivetrain.resetEncoders();
        drivetrain.encodersOn();

        single.resetEncoders();
        single.encodersOn();

        TODO: ELIZABETH END     --------------------

        // Telemetry for selecting robot position (Y=CRATER, A=MARKER_DEPOT)
        positionItem = telemetry.addData("POSITION", "Unselected (Y/A)");

        // setup gamepad 1 for selecting robot position
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        // Path setup.
        // path for scoring marker
        scoreMarker = new DeadReckonPath();
        // path for lowering robot on latch arm
        detachPath  = new DeadReckonPath();
        // path for unlatching arm
        unlatchScan = new DeadReckonPath();

        // Segment setup.
        setOtherPaths();

        END TODO ==================================================== */

        mdTask = new MineralDetectionTask(this) {
            @Override
            public void handleEvent(RobotEvent e) {
                MineralDetectionEvent event = (MineralDetectionEvent)e;
                confidence1 = event.minerals.get(0).getConfidence();
                left1 = event.minerals.get(0).getLeft();
                //setLeft(left1);
                RobotLog.ii(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>left in init"+left1 +"blah");
                RobotLog.ii(TAG, "Saw: " + event.kind + " Confidence: " + event.minerals.get(0).getConfidence());
                RobotLog.ii(TAG, "Saw: " + event.kind + " LEFT: " + event.minerals.get(0).getLeft());
                /*if (started) {
                    lookForGold();
                }*/
            }
        };
        mdTask.init(telemetry, hardwareMap);
        mdTask.setDetectionKind(MineralDetectionTask.DetectionKind.LARGEST_GOLD);
        //mdTask.setDetectionKind(MineralDetectionTask.DetectionKind.EVERYTHING);
        // Slow the rate to once every quarter of a second
        mdTask.rateLimit(100);
    }



    protected void lookForGold() {

        // TODO: ELIZABETH  perhaps we can add the last segment based on whether we saw the gold the
        // first time, second time, or third time.

        if (LEFT_MIN < left1 && left1 < LEFT_MAX ){
            //in center
            RobotLog.ii(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>LEFT IN CENTER"+left1 +"blah");
            foundGold = true;
        } else {
            RobotLog.ii(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>NOT IN CENTER"+ left1+"blah");
        }
        stepNum += 1;

        if (foundGold) {
            RobotLog.ii(TAG, "lookForGold: found gold! so can go forward to knock off gold mineral" + left1 + "step"+stepNum);
            // select the path that knocks off the gold mineral
            // TODO: ELIZABETH
            // if stepNum == 1:  addSegment for the 2nd part of path after particle is knocked off (since this is leftmost mineral
            // if stepNum == 2   addSegment for the 2nd part
        } else {
            RobotLog.ii(TAG, "lookForGold: gold not seen! so can go to right to look for it. step" +stepNum);
            // select the path the goes right
        }

        // TODO: the temporaryLookForGold is just for testing and should be removed for real competition
        if (!foundGold && stepNum <= 3) {
            temporaryLookForGold();
        }
      /*
        addTask(new DeadReckonTask(this, path, drivetrain) {
            @Override
              public void handleEvent(RobotEvent e) {
                DeadReckonEvent event = (DeadReckonEvent) e;
                switch (event.kind) {
                    case PATH_DONE:
                        RobotLog.ii(TAG, "lookForGold: path done");

                        if (! foundGold ) {
                        }

                }
            }

        }); */
    }

    /* TODO: ELIZABETH
    protected void moveAwayFromLatch() {
        addTask(new DeadReckonTask(this, unlatchScan, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent event = (DeadReckonEvent) e;
                switch (event.kind) {
                    case PATH_DONE:
                        RobotLog.ii(TAG, "moveAwayFromLatch: finished moving away from latch");
                        started = true;
                        lookForGold();

                }
            }
        });
    }
    TODO: ELIZABETH END ------------------------------*/

    /* TODO: ELIZABETH
    protected void unlatchArm() {

        latchServo.setPosition(LATCH_OPEN);


        addTask(new SingleShotTimerTask(this, 5000) {
            @Override
            // FIXME: CINDY add EvenKing.EXPIRED
            public void handleEvent(RobotEvent e) {
                RobotLog.ii(TAG, "unlatchArm: Finished unlatching Arm");
                lowerRobot();
            }
        });

    }
    */

    /* TODO: ELIZABETH START -----------------------------
    protected lowerRobot() {

        // lower robot using latchArm
        addTask(new DeadReckonTask(this, detachPath, single) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent event = (DeadReckonEvent) e;
                switch (event.kind) {
                    case PATH_DONE:
                       RobotLog.ii(TAG, "lowerRobot: finished lowering robot");
                       moveAwayFromLatch();
                }
            }
        });
    }
    TODO: ELIZABETH END ------------------------------*/

    protected void temporaryLookForGold() {
        // TODO: ELIZABETH
        // The following is just temporary, so i can test looking for gold after 5 seconds,
        // but later should be removed and we should loo for gold later
        addTask(new SingleShotTimerTask(this, 5000) {
            @Override
            public void handleEvent(RobotEvent e) {
                RobotLog.ii(TAG, "start: start to look for gold (TEMPORARY will move later)");
                lookForGold();
            }
        });
    }

    @Override
    public void start()
    {
        /* TODO: ELIZABETH START ----------------
        unlatchArm();
           TODO: ELIZABETH END ------------------ */


        addTask(mdTask);
        started = true;

        temporaryLookForGold();

    }
}
