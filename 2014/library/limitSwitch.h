
#include "../library/sensors/drivers/hitechnic-protoboard.h"

ubyte switch_pin_mask;

void limitSwitchInit(ubyte pin)
{
    int err;
    int val;

    if (pin > 5) {
        nxtDisplayTextLine(4, "Pin %d is invalid", pin);
        StopAllTasks();
    }

    switch_pin_mask = (0x1 << pin);

    val = HTPBgetIOCfg(HTPB);
    if (val == -1) {
        nxtDisplayTextLine(4, "Error reading up digital config, %d", err);
		StopAllTasks();
	}

    err = HTPBsetupIO(HTPB, ~(switch_pin_mask) & (0xff & val));
    if (!err) {
		nxtDisplayTextLine(4, "Error setting up digital outputs, %d", err);
		StopAllTasks();
	}
}

bool isLimitSwitchClosed()
{
    ubyte val;

    val = HTPBreadIO(HTPB, switch_pin_mask);
    /*
     * Using a pullup as input, so when the switch
     * closes the pin sees ground.  So 1 is open 0 is closed
     */
    if (val) {
        return false;
    } else {
        return true;
    }
}

bool isLimitSwitchOpen()
{
    return (!isLimitSwitchClosed());
}
