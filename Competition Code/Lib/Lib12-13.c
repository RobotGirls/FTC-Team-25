
/*
 * Utility functions for the 2012 - 2013 season.
 *
 * Depends upon the pragmas for the competition bot.
 */

const tMUXSensor HTMC = msensor_S3_1;
const tMUXSensor IRSeeker = msensor_S3_2;
//const tMUXSensor touchSensor = msensor_S3_2;

#define ENCPERINCH 140
#define ENC_TICKS_PER_DEGREE 25
#define SHELFUP 5
#define SHELFDOWN 240
#define SHELFPLACE 86
#define SHELF_AUTO_PLACE 135
#define SHELF_AUTO_PUSH_STOP 161
#define SHELFREMOVE 75
#define SHELFDISCHARGE 144
#define IRUP 130
#define IRDOWN 234
#define IR_DEPLOY_RING 116
#define IRRING 110
#define BEACON_TARGET_STRENGTH 120
#define RAMP_START 166
#define RAMP_DEPLOY 48
#define SHELFINCREMENT 1
#define BEACON_CENTER 4

typedef enum {
	NO_DIR,
	LEFT,
	RIGHT,
    FORWARD,
    BACKWARD
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
 * moveForwardOn
 *
 * Turns the motors on, never turns them off.
 */
void moveForwardOn(int speed)
{
	motor[driveRight] = speed;
	motor[driveLeft] = speed;
}

int getStrength(void)
{
    int strength1, strength2, strength3, strength4, strength5;

    HTIRS2readAllACStrength(IRSeeker, strength1, strength2, strength3, strength4, strength5);
    return (strength3);
}

/*
 * moveForwardOff
 *
 * Turns the motors off.
 */
void moveForwardOff()
{
	motor[driveRight] = 0;
	motor[driveLeft] = 0;
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

	motor[driveRight] = 100;
	motor[driveLeft] = 100;

	while (abs(nMotorEncoder[driveLeft]) < encoderCounts && abs(nMotorEncoder[driveRight]) < encoderCounts)
	{
	}

	motor[driveLeft] = 0;
	motor[driveRight] = 0;
}

void moveForwardHalf(int inches, int speed)
{
	int encoderCounts = inches * (ENCPERINCH/2);

	nMotorEncoder[driveRight] = 0;
	nMotorEncoder[driveLeft] = 0;

	motor[driveRight] = speed;
	motor[driveLeft] = speed;

	while (abs(nMotorEncoder[driveLeft]) < encoderCounts && abs(nMotorEncoder[driveRight]) < encoderCounts)
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

void moveBackwardHalf(int inches, int speed)
{
	int encoderCounts = inches * (ENCPERINCH/2);

	nMotorEncoder[driveRight] = 0;
	nMotorEncoder[driveLeft] = 0;

	motor[driveRight] = -speed;
	motor[driveLeft] = -speed;

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
void moveSideways (int inches, int speed)
{
	int encoderCounts = inches * ENCPERINCH;

	nMotorEncoder[driveSide] = 0;
	motor[driveSide] = -speed;

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

void raiseShelfToAutoPlacePosition(void)
{
	servo[gravityShelf] = SHELF_AUTO_PLACE;
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
