
#include "../../lib/sensors/drivers/hitechnic-protoboard.h"

ubyte switch_pin_mask;
tSensors board;

bool limit_switch_init(tSensors link, ubyte pin)
{
    int err;
    int val;

	board = link;

    if (pin > 5) {
        nxtDisplayTextLine(4, "Pin %d is invalid", pin);
		return false;
    }

    switch_pin_mask = (0x1 << pin);

    val = HTPBgetIOCfg(board);
    if (val == -1) {
        nxtDisplayTextLine(4, "Error reading up digital config, %d", err);
		return false;
	}

    err = HTPBsetupIO(board, ~(switch_pin_mask) & (0xff & val));
    if (!err) {
		nxtDisplayTextLine(4, "Error setting up digital outputs, %d", err);
		return false;
	}

	return true;
}

bool is_limit_switch_closed()
{
    ubyte val;

    val = HTPBreadIO(board, switch_pin_mask);
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

bool is_limit_switch_open()
{
    return (!is_limit_switch_closed());
}
