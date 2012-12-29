
/*
 * Utility functions for the 2012 - 2013 season.
 *
 * Depends upon the pragmas for the competition bot.
 */

const tMUXSensor HTMC = msensor_S3_1;
const tMUXSensor IRSeeker = msensor_S3_2;
//const tMUXSensor touchSensor = msensor_S3_2;

#define SHELFUP 5
#define SHELFDOWN 240
#define SHELFPLACE 86
#define SHELF_AUTO_PLACE 105
#define SHELF_AUTO_PUSH_STOP 161
#define SHELFREMOVE 75
#define SHELFDISCHARGE 144
#define IRUP 130
#define IRDOWN 234
#define IR_DEPLOY_RING 104
#define IRRING 110
#define BEACON_TARGET_STRENGTH 120
#define RAMP_START 166
#define RAMP_DEPLOY 48
#define SHELFINCREMENT 1
#define BEACON_CENTER 5

int alignedHeading;

/**********************************************************************************
 * Display and debugging functions
 **********************************************************************************/

void showTarget(int val)
{
	nxtDisplayTextLine(3, "Target:    %4d", val);
}

void showHeading(void)
{
	nxtDisplayTextLine(4, "Abs:   %4d", HTMCreadHeading(HTMC));
}

void pauseDebug(char *str, int seconds)
{
    nxtDisplayTextLine(3, str);
    wait1Msec(seconds * 1000);
}

int getStrength(void)
{
    int strength1, strength2, strength3, strength4, strength5;

    HTIRS2readAllACStrength(IRSeeker, strength1, strength2, strength3, strength4, strength5);
    return (strength3);
}

/*
 * moveToBeacon
 *
 * Move toward the beacon until you see the given target strength
 */
void moveToBeacon(int targetStrength)
{
	int strength1;
	int strength2;
	int strength3;
	int strength4;
	int strength5;

	HTIRS2readAllACStrength(IRSeeker, strength1, strength2, strength3, strength4, strength5);

    moveForwardOn(25);

	motor[driveRight] = 25;
	motor[driveLeft] = 25;

	while (strength3 < targetStrength) {
		HTIRS2readAllACStrength(IRSeeker, strength1, strength2, strength3, strength4, strength5);
	}
	moveForwardOff();
}

/*
 * turn
 *
 * Turn the robot the specified number of degrees
 *
 * A positive value turns right, a negative value
 * turns left.
 */
void turn(int deg, int speed)
{
	int dest;

	dest = HTMCreadHeading(HTMC);

	dest = dest + deg;
	if (dest < 0) {
		dest = 360 - abs(dest);
	} else if (dest > 360) {
		dest = dest - 360;
	}

    eraseDisplay();

	showTarget(dest);

	if (deg < 0) {
		rotateCounterClockwise(speed);
	} else {
		rotateClockwise(speed);
	}

	while (HTMCreadHeading(HTMC) != dest) {
		showHeading();
	}

  	moveForwardOff();
}

void markHeading(void)
{
    alignedHeading = HTMCreadHeading(HTMC);
}

int getMarkedHeading(void)
{
    return alignedHeading;
}

void turnEncoder(int deg, int speed)
{
    int encoderCounts;

    encoderCounts = deg * ENC_TICKS_PER_DEGREE;

	if (deg < 0) {
		rotateCounterClockwise(speed);
	} else {
		rotateClockwise(speed);
	}

	nMotorEncoder[driveRight] = 0;

    while (abs(nMotorEncoder[driveRight]) < encoderCounts) { }

    moveForwardOff();
}


 /**********************************************************************************************
 * Functions that manipulate mechanisms
 **********************************************************************************************/

void raiseShelfToPlacePosition(void)
{
	servo[gravityShelf] = SHELFPLACE;
}

void raiseShelfToAutoPlacePosition(direction_t dir)
{
    switch (dir) {
        case NO_DIR:
	        servo[gravityShelf] = SHELF_AUTO_PLACE - 20;
            break;
        case RIGHT:
        case LEFT:
            servo[gravityShelf] = SHELF_AUTO_PLACE - 20;
            break;
        default:
            break;
    }
}

void raiseShelfToAutoPushStopPosition(void)
{
	servo[gravityShelf] = SHELF_AUTO_PUSH_STOP;
}

void lowerShelfToDischargePosition(void)
{
	servo[gravityShelf] = SHELFDISCHARGE;
}

void deployPusher(void)
{
	servo[IRServo] = IRRING;
}
