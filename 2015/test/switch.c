#pragma config(Sensor, S1,     HTPB,                sensorI2CCustom9V)

#include "../../lib/sensors/drivers/hitechnic-protoboard.h"
#include "../../lib/limit_switch.h"

task main()
{
    // pin 5 = behind shoulder

	if (!limit_switch_init(HTPB, 0x05)) {
		goto _err;
	}

	while (true) {
		if (is_limit_switch_closed(0x05)) {
			nxtDisplayCenteredBigTextLine(2, "Closed");
		} else {
			nxtDisplayCenteredBigTextLine(2, "Open");
		}
        if (is_limit_switch_closed(0x04)) {
            nxtDisplayCenteredBigTextLine(5, "Closed");
        } else {
            nxtDisplayCenteredBigTextLine(5, "Open");
        }
	}

_err:
	while (true) {}
}
