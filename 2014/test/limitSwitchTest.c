#pragma config(Sensor, S1,     HTPB,                sensorI2CCustom9V)


#include "../library/sensors/drivers/hitechnic-protoboard.h"
#include "../library/limitSwitch.h"

task main()
{
    limitSwitchInit();

    while (1) {
        /*
         * Using a pullup as input, so when the switch
         * closes the pin sees ground.  So 1 is open 0 is closed
         */
        if (isLimitSwitchClosed()) {
            nxtDisplayTextLine(4, "Closed");
        } else {
            nxtDisplayTextLine(4, "Open");
        }

        wait1Msec(200);
    }
}
