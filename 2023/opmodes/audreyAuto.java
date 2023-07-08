//package opmodes;
//
//import com.qualcomm.ftccommon.StackTraceActivity;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.hardware.DcMotor;
//
//import org.firstinspires.ftc.robotcore.external.Telemetry;
//
//import team25core.AudreysFourWheelDriveTrain;
//import team25core.AudreysFourWheelDriveTrain;
//import team25core.DeadReckonPath;
//import team25core.DeadReckonTask;
//import team25core.StandardFourMotorRobot;
//
//@Autonomous (name = "audreyAuto", group = "rnrr")
//public class audreyAuto extends StandardFourMotorRobot {
//
//    //constructors initialize and set memory aside for the program
//    private AudreysFourWheelDriveTrain drivetrain;
//
//    private DeadReckonPath straightPath;
//
//    private Telemetry.Item eventTlm;
//
//
////    private void driveAudreyPath() {
////        this.addTask(new DeadReckonTask(this, ));
////
////    }
//
//
//
//
//    @Override
//    public void init() {
//        super.init();
//
//        //instantiating AudreyDriveTrain class
//        drivetrain = new AudreysFourWheelDriveTrain(frontLeft, frontRight, backLeft, backRight);
//
//        //driveTrain.setCannonicalMotorDirection();
//        // call this method only if the robot is going the opposite direction from expected
//        //uncomment if robot is going opposite direction as method switches robot direction
//
//        drivetrain.encodersOn();
//
//        drivetrain.brakeOnZeroPower();
//
//        drivetrain.resetEncoders();
//        //sets the motor encoder position to zero
//
//        //enumeration: variable type similar to int
//
//        eventTlm = telemetry.addData("pathEvent", "none");
//
//        initPaths();
//
//
//    }
//
//
//    public void initPaths() {
//        DeadReckonPath straightPath = new DeadReckonPath();
//        straightPath.stop();
//        straightPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 1.2, STRAIGHT_SPEED);
//    }
//
//    public void goForward() {
//        this.addTask(new DeadReckonTask(this, straightPath, drivetrain){
//            @Override
//            public void handleEvent(RobotEvent e){
//                DeadReckonEvent pathEvent = (DeadReckonEvent) e;
//                if(pathEvent.kind == EventKind.PATH_DONE){
//                    RobotLog.i("went forward");
//                    eventTlm.setValue("path is done");
//                }
//                else if(pathEvent.kind == EventKind.SEGMENT_DONE){
//                    RobotLog.i("segment completed");
//                    eventTlm.setValue("segment is done");
//                }
//            }
//        });
//    }
//
//    @Override
//    public void start() {
////        driveAudreyPath();
//    }
//}
