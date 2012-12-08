
/*
 * Utility functions for the 2012 - 2013 season.
 *
 * Depends upon the pragmas for the competition bot.
 */

#define ENCPERINCH 140
#define SHELFUP 5
#define SHELFDOWN 240
#define SHELFPLACE 86
#define SHELFDISCHARGE 0
#define IRUP 130
#define IRDOWN 234
#define IRRING 83
#define BEACON_TARGET_STRENGTH 120
#define RAMP_START 96
#define SHELFINCREMENT (SHELFDOWN - SHELFUP)/10

typedef enum {
	NO_DIR,
	LEFT,
	RIGHT
} direction_t;

/*
 * Note that there's a problem if both motors do not turn
 * at the same rate.  #askjulie
 */
#define WAIT_UNTIL_MOTOR_OFF \
	while (nMotorRunState[driveRight] != runStateIdle) {} \
	motor[driveLeft] = 0;


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

/**********************************************************************************
 * Movement functions
 **********************************************************************************/

void rotateClockwise(int speed)
{
  	motor[driveRight] = -speed;
	motor[driveLeft] = speed;
}

void rotateCounterClockwise(int speed)
{
	motor[driveRight] = speed;
	motor[driveLeft] = -speed;
}

/*
 * moveForward
 *
 * Move the robot forward a given number of inches.
 */
void moveForward (int inches)
{
	int encoderCounts = inches * ENCPERINCH;

	nMotorEncoder[driveRight] = 0;
	nMotorEncoder[driveLeft] = 0;

	motor[driveRight] = 75;
	motor[driveLeft] = 75;

	while(abs(nMotorEncoder[driveLeft]) < encoderCounts && abs(nMotorEncoder[driveRight]) < encoderCounts)
	{
	}

	motor[driveLeft] = 0;
	motor[driveRight] = 0;
}

/*
 * moveBackward
 *
 * Move the robot backward a given number of inches
 */
void moveBackward (int inches)
{
	int encoderCounts = inches * ENCPERINCH;

	nMotorEncoder[driveRight] = 0;
	nMotorEncoder[driveLeft] = 0;

	motor[driveRight] = -50;
	motor[driveLeft] = -50;

	while (abs(nMotorEncoder[driveLeft]) < encoderCounts && abs(nMotorEncoder[driveRight]) < encoderCounts)
	{
	}

	motor[driveLeft] = 0;
	motor[driveRight] = 0;
}

/*
 * moveSideways
 *
 * Move the robot sideways a given number of inches.
 * FIXME: This only moves one way.  Fix such that you can
 *        move either right or left.
 */
void moveSideways (int inches)
{
	int encoderCounts = inches * ENCPERINCH;

	nMotorEncoder[driveSide] = 0;

	motor[driveSide] = -50;

	while(abs(nMotorEncoder[driveSide]) < encoderCounts)
	{
	}

	motor[driveSide] = 0;
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

	motor[driveRight] = 25;
	motor[driveLeft] = 25;

	while (strength3 < targetStrength) {
		HTIRS2readAllACStrength(IRSeeker, strength1, strength2, strength3, strength4, strength5);
	}
	motor[driveRight] = 0;

	motor[driveLeft] = 0;
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

	showTarget(dest);

	if (deg < 0) {
		rotateCounterClockwise(speed);
	} else {
		rotateClockwise(speed);
	}

	while (HTMCreadHeading(HTMC) != dest) {
		showHeading();
	}

  	motor[driveRight] = 0;
	motor[driveLeft] = 0;
}

 /**********************************************************************************************
 * Functions that manipulate mechanisms
 **********************************************************************************************/

void raiseShelfToPlacePosition(void)
{
	servo[gravityShelf] = SHELFPLACE;
}

void lowerShelfToDischargePosition(void)
{
	servo[gravityShelf] = SHELFDISCHARGE;
}

void deployPusher(void)
{
	servo[IRServo] = IRRING;
}
