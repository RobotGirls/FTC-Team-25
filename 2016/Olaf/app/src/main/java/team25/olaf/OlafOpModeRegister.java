package team25.olaf;

import com.qualcomm.ftcrobotcontroller.opmodes.K9AutoTime;
import com.qualcomm.ftcrobotcontroller.opmodes.K9IrSeeker;
import com.qualcomm.ftcrobotcontroller.opmodes.K9Line;
import com.qualcomm.ftcrobotcontroller.opmodes.K9TankDrive;
import com.qualcomm.ftcrobotcontroller.opmodes.K9TeleOp;
import com.qualcomm.ftcrobotcontroller.opmodes.NullOp;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;

/**
 * Created by cmacfarl on 6/14/2015.
 */
public class OlafOpModeRegister implements OpModeRegister {

    public void register(OpModeManager manager) {

    /*
     * register your op modes here.
     * The first parameter is the name of the op mode
     * The second parameter is the op mode class property
     *
     * If two or more op modes are registered with the same name, the app will display an error.
     */

        manager.register("NullOp", NullOp.class);
        manager.register("K9TeleOp", K9TeleOp.class);
        manager.register("K9TankDrive", K9TankDrive.class);
        manager.register("K9Line", K9Line.class);
        manager.register("K9IrSeeker", K9IrSeeker.class);
        manager.register("K9AutoTime", K9AutoTime.class);
    /*
    manager.register("IrSeekerOp", IrSeekerOp.class);
    manager.register("CompassCalibration", CompassCalibration.class);
    manager.register("NxtTeleOp", NxtTeleOp.class);
    manager.register("NxtEncoderOp", NxtEncoderOp.class);
    */
    }
}
