#pragma config(Sensor, S1,     HTPB,                sensorI2CCustom9V)

#include "../../lib/sensors/drivers/hitechnic-protoboard.h"
#include "../../lib/limit_switch.h"

task main()
{
	if (!limit_switch_init(HTPB, 0x05)) {
		goto _err;
	}

	while (true) {
		if (is_limit_switch_closed()) {
			nxtDisplayCenteredBigTextLine(4, "Closed");
		} else {
			nxtDisplayCenteredBigTextLine(4, "Open");
		}
	}

_err:
	while (true) {}
}
