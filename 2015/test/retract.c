#pragma config(UserModel, "../pragmas/baemax.h")

#define FOUR_WHEEL_DRIVE
#define DEAD_RECKON_GYRO

#include "../../lib/sensors/drivers/hitechnic-sensormux.h"
#include "../../lib/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../../lib/sensors/drivers/hitechnic-gyro.h"

const tMUXSensor HTGYRO  = msensor_S4_3;
bool beacon_done;
int distance_monitor_distance;

#include "../library/baemax_defs.h"
#include "../../lib/baemax_drivetrain_defs.h"
#include "../../lib/drivetrain_square.h"
#include "../../lib/dead_reckon.h"
#include "../../lib/data_log.h"
#include "../../lib/ir_utils.h"
#include "../../lib/us_utils.h"
#include "../../lib/us_cascade_utils.c"
#include "../library/auto_utils.h"

task main()
{
	motor_state_t state;

	state = move_to(arm_motor, 60, 10000);

	eraseDisplay();

	if (state == MOTOR_OK) {
		nxtDisplayCenteredBigTextLine(4, "OK");
	} else {
		nxtDisplayCenteredBigTextLine(4, "STALLED");
	}

	while (true) {};
}
