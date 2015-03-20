

#ifdef __HTSMUX_H__
tMUXSensor board;
#else
tSensors board;
#endif

#ifdef __HTSMUX_H__
bool limit_switch_init(tMUXSensor link, ubyte pin)
{
    int err;
    int val;

	board = link;

	return true;
}
#else
bool limit_switch_init(tSensors link, ubyte pin_mask)
{
    int err;
    int val;

	board = link;

    val = HTPBgetIOCfg(board);
    if (val == -1) {
        nxtDisplayTextLine(4, "Error reading up digital config, %d", err);
		return false;
	}

    err = HTPBsetupIO(board, ~(pin_mask) & (0xff & val));
    if (!err) {
		nxtDisplayTextLine(4, "Error setting up digital outputs, %d", err);
		return false;
	}

	return true;
}
#endif

bool is_limit_switch_closed(ubyte pin)
{
    ubyte val;
	ubyte switch_pin_mask;

	switch_pin_mask = (0x1 << pin);

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

bool is_limit_switch_open(ubyte pin)
{
    return (!is_limit_switch_closed(pin));
}
