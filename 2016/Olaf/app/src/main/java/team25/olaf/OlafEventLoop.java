package team25.olaf;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.FtcEventLoopHandler;
import com.qualcomm.ftccommon.UpdateUI;
import com.qualcomm.ftcrobotcontroller.FtcEventLoop;
import com.qualcomm.ftcrobotcontroller.opmodes.FtcOpModeRegister;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Created by cmacfarl on 6/13/2015.
 */
public class OlafEventLoop extends FtcEventLoop {

    public OlafEventLoop(HardwareFactory hardwareFactory, UpdateUI.Callback callback) {
        super(hardwareFactory, callback);
    }

    /**
     * Init method
     * <p>
     * This code will run when the robot first starts up. Place any initialization code in this
     * method.
     * <p>
     * It is important to save a copy of the event loop manager from this method, as that is how
     * you'll get access to the gamepad.
     * <p>
     * If an Exception is thrown then the event loop manager will not start the robot.
     * <p>
     * @see com.qualcomm.robotcore.eventloop.EventLoop#init(com.qualcomm.robotcore.eventloop.EventLoopManager)
     */
    @Override
    public void init(EventLoopManager eventLoopManager) throws RobotCoreException, InterruptedException {
        super.init(eventLoopManager);

        getOpModeManager().registerOpModes(new OlafOpModeRegister());
    }
}
