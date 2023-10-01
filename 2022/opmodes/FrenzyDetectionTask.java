package opmodes;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

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


public class FrenzyDetectionTask extends RobotTask {

    public enum EventKind {
        OBJECTS_DETECTED,
    }

    protected ElapsedTime timer;

    public class ObjectDetectionEvent extends RobotEvent {

        public EventKind kind;
        public List<Recognition> objects;

        //this is constructor for object detection event
        public ObjectDetectionEvent(RobotTask task, EventKind kind, List<Recognition> m)
        {
            super(task);
            this.kind = kind;
            this.objects = new ArrayList<>(m.size());
            this.objects.addAll(m);
        }

        public String toString()
        {
            return kind.toString();
        }
    }

    private VuforiaLocalizer vuforia;
    private Telemetry telemetry;
    private TFObjectDetector tfod;

//    public static final String LABEL_TOPCAP = "capStoneTop";
//    private static final String LABEL_MIDDLECAP = "capStoneMid";
//    private static final String LABEL_BOTTOMCAP = "capStoneBtm";
    public static final String LABEL_TOPCAP = "capStone";
    private static final String LABEL_MIDDLECAP = "element";
    private static final String LABEL_BOTTOMCAP = "nothing";
    private static final String TFOD_MODEL_ASSET = "frenzyCapstone1.tflite";
    private int rateLimitMs;
    private DetectionKind detectionKind;
    private String cameraName;
    private Telemetry.Item objTlm;

    public enum DetectionKind {
        EVERYTHING, //this may go away
        TOPCAP_DETECTED,
        MIDDLECAP_DETECTED,
        BOTTOMCAP_DETECTED,
        UNKNOWN_DETECTED,
    }
    public enum ObjectKind {
        TOPCAP_KIND,
        MIDDLECAP_KIND,
        BOTTOMCAP_KIND,
        UNKNOWN_KIND,
    };

    //for phone camera constructor
    public FrenzyDetectionTask(Robot robot)
    {
        super(robot);

        rateLimitMs = 0;
        detectionKind = DetectionKind.EVERYTHING;
    }
    //for webcamera constructor

    public FrenzyDetectionTask(Robot robot, String cameraName, Telemetry.Item tlm)
    {
        super(robot);
        rateLimitMs = 0;
        detectionKind = DetectionKind.EVERYTHING;
        this.cameraName = cameraName;
        objTlm = tlm;
        objTlm.setValue("in FrenzyDetectionTask constructor");
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
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565,true);
        vuforia.setFrameQueueCapacity(1);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    private void initTfod(HardwareMap hardwareMap)
    {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.5f;
        tfodParameters.isModelTensorFlow2 = true;
        tfodParameters.inputSize = 320;
        //tfodParameters.minimumConfidence = 0.6; //the example in the Ultimate Goal Tensor flow example defaults to a MinimumConfidence of 0.8f
        //concept tensor flow object detection had minimum confidence
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_TOPCAP);
    }

    public void init(Telemetry telemetry, HardwareMap hardwareMap)
    {
        initVuforia(hardwareMap);

        // if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
        initTfod(hardwareMap);
//        } else {
//            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
//        }
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

    public static ObjectKind isObject(Recognition object)
    {
        if (object.getLabel().equals(LABEL_TOPCAP)) {
            return ObjectKind.TOPCAP_KIND;
        } else if (object.getLabel().equals(LABEL_MIDDLECAP)) {
            return ObjectKind.MIDDLECAP_KIND;
        } else if (object.getLabel().equals(LABEL_BOTTOMCAP)) {
            return ObjectKind.BOTTOMCAP_KIND;
        } else {
            return ObjectKind.UNKNOWN_KIND;
        }
    }
    //if recognize anything will add to que
    protected void processEverything(List<Recognition> objects)
    {
        if (objects.size() > 0) {
            robot.queueEvent(new ObjectDetectionEvent(this, EventKind.OBJECTS_DETECTED, objects));
        }
    }
    //only adds objects which will make event and add to que
    protected void processObject1(List<Recognition> objects)
    {
        List<Recognition> objectList = new ArrayList<>();
        for (Recognition object : objects) {
            if (isObject(object) == ObjectKind.TOPCAP_KIND) {
                objectList.add(object);
            }
        }

        if (!objectList.isEmpty()) {
            robot.queueEvent(new ObjectDetectionEvent(this, EventKind.OBJECTS_DETECTED, objectList));
        }
    }

    protected void processObject2(List<Recognition> objects)
    {
        List<Recognition> objectList = new ArrayList<>();
        for (Recognition object : objects) {
            if (isObject(object) == ObjectKind.MIDDLECAP_KIND) {
                objectList.add(object);
            }
        }

        if (!objectList.isEmpty()) {
            robot.queueEvent(new ObjectDetectionEvent(this, EventKind.OBJECTS_DETECTED, objectList));
        }
    }

    protected void processObject3(List<Recognition> objects)
    {
        List<Recognition> objectList = new ArrayList<>();
        for (Recognition object : objects) {
            if (isObject(object) == ObjectKind.BOTTOMCAP_KIND) {
                objectList.add(object);
            }
        }

        if (!objectList.isEmpty()) {
            robot.queueEvent(new ObjectDetectionEvent(this, EventKind.OBJECTS_DETECTED, objectList));
        }
    }

    //timeslice calls to get information from recognition
    protected void processDetectedObjects(List<Recognition> objects)
    {
        if (objects == null || objects.isEmpty()) {
            objTlm.setValue("in processDetectedObject object NULL");
            return;
        }
        objTlm.setValue("in FrenzyDetectionTask processDetectedObject");
        switch (detectionKind) {
            case EVERYTHING:
                objTlm.setValue("in processDetectedObject EVERYTHING");
                processEverything(objects);
                break;
            case BOTTOMCAP_DETECTED:
                objTlm.setValue("in processDetectedObject bottom");
                processObject3(objects);
                break;
            case MIDDLECAP_DETECTED:
                objTlm.setValue("in processDetectedObject middle");
                processObject2(objects);
                break;
            case TOPCAP_DETECTED:
                objTlm.setValue("in processDetectedObject top");
                processObject1(objects);
                break;
            default:
                objTlm.setValue("in processDetectedObject else");
        }
    }

    @Override
    public boolean timeslice()
    {
        objTlm.setValue("in timeslice 1");
        //timeslice set to 0 do when it gets called
        if (rateLimitMs != 0) {
            if (timer.time() < rateLimitMs) {
                return false;
            }
        }
        objTlm.setValue("in timeslice 2");
        //shows location of object
        processDetectedObjects(tfod.getUpdatedRecognitions());

        if (rateLimitMs != 0) {
            timer.reset();
        }
        return false;
    }
}

