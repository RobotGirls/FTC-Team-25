package team25core;

import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

/**
 * Created by katie on 10/29/2016.
 */

public class MRLightSensor implements LightSensor {

    OpticalDistanceSensor ods;

    public MRLightSensor(OpticalDistanceSensor opticalDistanceSensor)
    {
       ods = opticalDistanceSensor;
    }
    @Override
    public double getLightDetected()
    {
        return ods.getLightDetected();
    }

    @Override
    public double getRawLightDetected()
    {
        return ods.getRawLightDetected();
    }

    @Override
    public double getRawLightDetectedMax()
    {
        return ods.getRawLightDetectedMax();
    }

    @Override
    public void enableLed(boolean enable)
    {
        ods.enableLed(enable);
    }

    @Override
    public String status()
    {
        return ods.status();
    }

    @Override
    public Manufacturer getManufacturer()
    {
        return ods.getManufacturer();
    }

    @Override
    public String getDeviceName()
    {
        return ods.getDeviceName();
    }

    @Override
    public String getConnectionInfo()
    {
        return ods.getConnectionInfo();
    }

    @Override
    public int getVersion()
    {
        return ods.getVersion();
    }

    @Override
    public void resetDeviceConfigurationForOpMode()
    {
        ods.resetDeviceConfigurationForOpMode();
    }

    @Override
    public void close()
    {
        ods.close();
    }
}
