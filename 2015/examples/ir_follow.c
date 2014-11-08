#pragma config(Hubs,  S1, HTMotor,  none,     none,     none)
#pragma config(Hubs,  S2, HTMotor,  none,     none,     none)
#pragma config(Sensor, S3,     irSensor,       sensorHiTechnicIRSeeker600)
#pragma config(Motor,  mtr_S1_C1_1,     frontLeft,     tmotorTetrix, openLoop, encoder)
#pragma config(Motor,  mtr_S1_C1_2,     frontRight,    tmotorTetrix, openLoop, encoder)
#pragma config(Motor,  mtr_S2_C1_1,     backLeft,      tmotorTetrix, openLoop, encoder)
#pragma config(Motor,  mtr_S2_C1_2,     backRight,     tmotorTetrix, openLoop, encoder)
//*!!Code automatically generated by 'ROBOTC' configuration wizard               !!*//

/*
 * To do: create sensor pragmas.
 */

#include "sensors/drivers/hitechnic-irseeker-v2.h"

typedef enum dir_ {
    right,
    left,
    center,
} direction;

	direction get_dir_to_beacon(void) {
	}

int segment;
segment = HTIRS2readACdir(irSensor);

direction dir;

switch (segment) {
	case 1:
	case 2:
	case 3:
	case 4:
		dir = left;
		break;

	case 5:
		dir = center;
		break;

	case 6:
	case 7:
	case 8:
	case 9:
		dir = right;
		break;
}

return dir;

task main()
{
  	/*
     * To do: variable declarations.
     */

	while (true)
    {

        /*
         * To do: determine where beacon is.
         */

        /*
         * To do: determine distance of beacon(strength).
         */

        /*
         * Todo: Display direction and strength on the display.
         */
	while(true) {
        dir = get_dir_to_beacon;

		switch (dir) {
            case left:
            nxtDisplayTextLine(4, "The target is to the left.");
			break;

            case center:
            nxtDisplayTextLine(4, "The target is in front of me.");
			break;

            case right:
            nxtDisplayTextLine(4, "The target is to the right.");
            break;

            default:
            nxtDisplayTextLine(4, "I don't know where the target is.");
		}
	}
        /*
         * Todo: Turn the robot and then move it forward until
         *       it gets close to you and then stop.
         */
    }
}