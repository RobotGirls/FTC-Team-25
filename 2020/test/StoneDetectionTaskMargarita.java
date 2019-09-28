package test;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.ArrayList;
import java.util.List;

import team25core.Robot;
import team25core.RobotEvent;
import team25core.RobotTask;
import team25core.VuforiaConstants;

import static org.firstinspires.ftc.robotcore.external.tfod.TfodSkyStone.TFOD_MODEL_ASSET;

public class StoneDetectionTaskMargarita extends RobotTask {

    public enum EventKind {
        OBJECTS_DETECTED,
    }

    protected ElapsedTime timer;

    public class StoneDetectionEvent extends RobotEvent {

        public EventKind kind;
        public List<Recognition> stones;

        //this is constructor for stone detection event
        public StoneDetectionEvent(RobotTask task, EventKind kind, List<Recognition> m)
        {
            super(task);
            this.kind = kind;
            this.stones = new ArrayList<>(m.size());
            this.stones.addAll(m);
        }

        public String toString()
        {
            return kind.toString();
        }
    }

    private VuforiaLocalizer vuforia;
    private Telemetry telemetry;
    private TFObjectDetector tfod;

    public static final String LABEL_STONE = "Stone";
    private static final String LABEL_SKY_STONE = "SkyStone";
    private int rateLimitMs;
    private DetectionKind detectionKind;
    private String cameraName;

    public enum DetectionKind {
        EVERYTHING,
        STONE_DETECTED,
        SKY_STONE_DETECTED,
        LARGEST_STONE_DETECTED,
        UNKNOWN_DETECTED,
    }
     public enum StoneKind {
            SKY_STONE_KIND,
            STONE_KIND,
            UNKNOWN_KIND,
     };


    //for phone camera constructor
    public StoneDetectionTaskMargarita(Robot robot)
    {
        super(robot);

        rateLimitMs = 0;
        detectionKind = DetectionKind.EVERYTHING;
    }
    //for webcamera construtor
    public StoneDetectionTaskMargarita(Robot robot, String cameraName)
    {
        super(robot);
        rateLimitMs = 0;
        detectionKind = DetectionKind.EVERYTHING;
        this.cameraName = cameraName;
    }

    private void initVuforia(HardwareMap hardwareMap) {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        //new=your own copy of vuforia parameters
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        //if webcam
        if (cameraName != null) {
            parameters.vuforiaLicenseKey = VuforiaConstants.WEBCAM_VUFORIA_KEY;
            parameters.cameraName = hardwareMap.get(WebcamName.class, cameraName);

        } else {   // if phonecam
            parameters.vuforiaLicenseKey = VuforiaConstants.VUFORIA_KEY;
        }

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    private void initTfod(HardwareMap hardwareMap)
    {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        //concept tensor flow object detection had minimum confidence
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_STONE, LABEL_SKY_STONE);
    }

    public void init(Telemetry telemetry, HardwareMap hardwareMap)
    {
        initVuforia(hardwareMap);

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod(hardwareMap);
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }
    }

    public void rateLimit(int ms)
    {
        this.rateLimitMs = ms;
    }

    public void setDetectionKind(DetectionKind detectionKind)
    {
        this.detectionKind = detectionKind;
    }
    //this will start tfod activation and start detecting
    @Override
    public void start()
    {
        tfod.activate();

        if (rateLimitMs != 0) {
            timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
        }
    }

    @Override
    public void stop()
    {
        tfod.deactivate();
        robot.removeTask(this);
    }

    public static StoneKind isStone(Recognition object)
    {
        if (object.getLabel().equals(LABEL_STONE)) {
            return StoneKind.STONE_KIND;
        } else if (object.getLabel().equals(LABEL_SKY_STONE)) {
            return StoneKind.SKY_STONE_KIND;
        } else {
            return StoneKind.UNKNOWN_KIND;
        }
    }
    //if recognize anything will add to que
    protected void processEverything(List<Recognition> objects)
    {
        if (objects.size() > 0) {
            robot.queueEvent(new StoneDetectionEvent(this, EventKind.OBJECTS_DETECTED, objects));
        }
    }
    //only adds stones which will make event and add to que
    protected void processStone(List<Recognition> objects)
    {
        List<Recognition> stones = new ArrayList<>();
        for (Recognition object : objects) {
            if (isStone(object) == StoneKind.STONE_KIND) {
                stones.add(object);
            }
        }

        if (!stones.isEmpty()) {
            robot.queueEvent(new StoneDetectionEvent(this, EventKind.OBJECTS_DETECTED, stones));
        }
    }

    protected void processSkyStone(List<Recognition> objects)
    {
        List<Recognition> skystones = new ArrayList<>();
        for (Recognition object : objects) {
            if (isStone(object) == StoneKind.SKY_STONE_KIND) {
                skystones.add(object);
            }
        }

        if (!skystones.isEmpty()) {
            robot.queueEvent(new StoneDetectionEvent(this, EventKind.OBJECTS_DETECTED, skystones));
        }

    }

    protected void processLargestSkyStone(List<Recognition> objects)
    {
        if (objects.isEmpty()) {
            return;
        }

        Recognition largest = null;
        List<Recognition> singleton;

        for (Recognition object : objects) {
            if (isStone(object) == StoneKind.SKY_STONE_KIND) {
                if (largest == null) {
                    largest = object;
                } else if ((largest.getHeight() * largest.getWidth()) < (object.getWidth() * object.getHeight())) {
                    largest = object;
                }
            }
        }

        if (largest != null) {
            singleton = new ArrayList<>();
            singleton.add(largest);
            robot.queueEvent(new StoneDetectionEvent(this, EventKind.OBJECTS_DETECTED, singleton));
        }
    }

    //timeslice calls to get information from recognition
    protected void processDetectedObjects(List<Recognition> objects)
    {
        if (objects == null || objects.isEmpty()) {
            return;
        }

        switch (detectionKind) {
            case EVERYTHING:
                processEverything(objects);
                break;
            case SKY_STONE_DETECTED:
                processStone(objects);
                break;
            case STONE_DETECTED:
                processSkyStone(objects);
                break;
            case LARGEST_STONE_DETECTED:
                processLargestSkyStone(objects);
                break;
        }
    }

    @Override
    public boolean timeslice()

    {
     //timeslice set to 0 do when it gets called
        if (rateLimitMs != 0) {
            if (timer.time() < rateLimitMs) {
                return false;
            }
        }
        //shows location of stone
        processDetectedObjects(tfod.getUpdatedRecognitions());

        if (rateLimitMs != 0) {
            timer.reset();
        }

        return false;
    }
}
