
#include "../library/sensors/drivers/hitechnic-protoboard.h"

#define SWITCH_PIN 5
#define SWITCH_PIN_MASK (0x1 << SWITCH_PIN)

void limitSwitchInit()
{
    int err;

    err = HTPBsetupIO(HTPB, 0x00);
    if (!err) {
		nxtDisplayTextLine(4, "Error setting up digital outputs, %d", err);
		StopAllTasks();
	}
}

bool isLimitSwitchClosed()
{
    ubyte val;

    val = HTPBreadIO(HTPB, SWITCH_PIN_MASK);
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
