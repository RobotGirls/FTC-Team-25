package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.MineralDetectionTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

import static test.MDConstants.*;

@Autonomous(name = "Mineral Detection Test Bella", group = "Team 25")
@Disabled
public class MineralDectectionTestBella extends Robot {

    private final static String TAG = "MineralDetectionTest";

    MineralDetectionTask mdTask;
    public double left;
    public double confidence;

    @Override
    public void handleEvent(RobotEvent e)
    {
    }

    @Override
    public void init()
    {
        mdTask = new MineralDetectionTask(this) {
            @Override
            public void handleEvent(RobotEvent e) {
                MineralDetectionEvent event = (MineralDetectionEvent)e;
                confidence = event.minerals.get(0).getConfidence();
                left = event.minerals.get(0).getLeft();
                RobotLog.ii(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>left in init"+left +"blah");
                RobotLog.ii(TAG, "Saw: " + event.kind + " Confidence: " + event.minerals.get(0).getConfidence());
                RobotLog.ii(TAG, "Saw: " + event.kind + " LEFT: " + event.minerals.get(0).getLeft());
            }
        };
        mdTask.init(telemetry, hardwareMap);
        mdTask.setDetectionKind(MineralDetectionTask.DetectionKind.LARGEST_GOLD);
    }

    public void dostuff() {

        if (LEFT_MIN < left && left < LEFT_MAX ){
            //in center
            RobotLog.ii(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>LEFT IN CENTER"+left +"blah");
        } else {
            RobotLog.ii(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>NOT IN CENTER"+ left+"blah");
        }
      /*
        addTask(new MineralDetectionTask(this) {
            @Override
            public void handleEvent(RobotEvent e){
                if (LEFT_MIN < left && left < LEFT_MAX && CONF_THRESH < confidence){
                    //in center
                    RobotLog.ii(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>LEFT IN CENTER");
                }
            }
        }); */
    }

    @Override
    public void start()
    {
        addTask(mdTask);
        RobotLog.ii(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>left in start"+left +"blah");

        addTask(new SingleShotTimerTask(this, 10) {
            @Override
            public void handleEvent(RobotEvent e) {
                RobotLog.ii(TAG,"Bella testing");
                RobotLog.ii(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>left in SingleShot"+left +"blah");
                dostuff();
            }
        });


    }
}
