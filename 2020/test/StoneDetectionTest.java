package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.List;

import team25core.Robot;
import team25core.RobotEvent;
import team25core.StoneDetectionTask;

@Autonomous(name = "SkyStones Detection Unit Test", group = "Team 25")
@Disabled
public class Stonedetectiontest extends Robot {

    private final static String TAG = "holly";
    private Telemetry.Item stonePositionTlm;
    private Telemetry.Item stoneTlm;
    private Telemetry.Item stoneConfidTlm;
    private Telemetry.Item stoneTypeTlm;
    private Telemetry.Item numStonesSeenTlm;
    private Telemetry.Item skystoneIndexTlm;
    private Telemetry.Item imageWidthTlm;
    private Telemetry.Item stoneWidthTlm;
    private final float NO_STONES_SEEN_FLOAT = (float) -1;
    private final int NO_STONES_SEEN_INT = -1;
    private final float FIRST_SKYSTONE_SEEN = (float) -1;

    private double confidence;
    private double left;
    private float stoneWidth;
    private float imageWidth;
    private String stoneType;
    private int numStonesSeen;
    private float foundSkystoneLeft = NO_STONES_SEEN_FLOAT;
    private float previousSkystoneLeft = FIRST_SKYSTONE_SEEN;
    private float currentSkystoneLeft = FIRST_SKYSTONE_SEEN;
    private int index = NO_STONES_SEEN_INT;
    StoneDetectionTask mdTask;
    private String depotColor = "RED";

    @Override
    public void handleEvent(RobotEvent e)
    {

    }

    public boolean currSkystoneIsBetter(float currentLeft, float previousLeft) {
        boolean isBetter = false;
        if (depotColor == "RED") {
            if (currentLeft > previousLeft) {
                isBetter = true;
            }
        } else {  // depotColor is BLUE
            if (currentLeft < previousLeft) {
                isBetter = true;
            }
        }
        return(isBetter);
    }

    public int checkSkystonePosition(List<Recognition> stones)
    {
        int foundSkystoneIndex = NO_STONES_SEEN_INT;
        numStonesSeen = stones.size();
        numStonesSeenTlm.setValue(numStonesSeen);
        /*
        for (Recognition recognition : stones) {
            index++;
            confidence = stones.get(index).getConfidence();
            left = stones.get(index).getLeft();
            stonePositionTlm.setValue(left);

            stoneWidth = stones.get(index).getWidth();
            imageWidth = stones.get(index).getImageWidth();
            RobotLog.w("Holly stoneWidth" + stoneWidth);
            RobotLog.w("Holly imageWidth" + imageWidth);
            RobotLog.w("Holly left" + left);
            RobotLog.w("Holly confidence" + confidence);
            if (recognition.getLabel().equals(StoneDetectionTask.LABEL_SKY_STONE)) {
                if (previousSkystoneLeft == FIRST_SKYSTONE_SEEN){
                    foundSkystoneIndex = index;
                    foundSkystoneLeft = recognition.getLeft();
                } else { //not first skystone, 2nd
                    previousSkystoneLeft = foundSkystoneLeft;
                    currentSkystoneLeft = recognition.getLeft();
                    if (currSkystoneIsBetter(currentSkystoneLeft, previousSkystoneLeft)) {
                        foundSkystoneIndex = index;
                        foundSkystoneLeft = recognition.getLeft();
                    }
                }

            }
        }

         */
        return(foundSkystoneIndex);
    }

    public void startStoneDetection()
    {
        mdTask = new StoneDetectionTask(this, "Webcam1") {
            @Override
            public void handleEvent(RobotEvent e) {
                int skystoneIndex = NO_STONES_SEEN_INT;
                StoneDetectionEvent event = (StoneDetectionEvent)e;

                left = event.stones.get(0).getLeft();
                stonePositionTlm.setValue(left);
                confidence = event.stones.get(0).getConfidence();
                stoneConfidTlm.setValue(confidence);
                stoneType = event.stones.get(0).getLabel();
                stoneTypeTlm.setValue(stoneType);
                RobotLog.ii("holly", "Saw: " + event.kind + " Confidence: " + confidence);
                imageWidth = event.stones.get(0).getImageWidth();
                imageWidthTlm.setValue(imageWidth);
                stoneWidth = event.stones.get(0).getWidth();
                stoneWidthTlm.setValue(stoneWidth);
                skystoneIndex = checkSkystonePosition(event.stones);
                //skystoneIndexTlm.setValue(skystoneIndex);
            }
        };

        mdTask.init(telemetry, hardwareMap);
        mdTask.setDetectionKind(StoneDetectionTask.DetectionKind.LARGEST_SKY_STONE_DETECTED); //previously EVERYTHING
    }

    @Override
    public void init()
    {
        stoneConfidTlm = telemetry.addData("Confidence", 0);
        stonePositionTlm = telemetry.addData("StoneLeftCoord", 0);
        stoneTypeTlm = telemetry.addData("StoneType","unknown");
        imageWidthTlm = telemetry.addData("imageWidth", (float)0.0);
        stoneWidthTlm = telemetry.addData("stoneWidth", (float)0.0);
        numStonesSeenTlm = telemetry.addData("numStonesSeen", (int)0);
        //skystoneIndexTlm.addData("skystone index", 0);
        startStoneDetection();
    }

    @Override
    public void start()
    {
        addTask(mdTask);
    }
}
