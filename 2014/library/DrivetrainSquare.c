
#define ENC_TICKS_PER_DEGREE 50

typedef enum {
	NO_DIR,
	LEFT,
	RIGHT,
    FORWARD,
    BACKWARD
} direction_t;

void initializeMotors(void)
{
	nMotorPIDSpeedCtrl[driveLeft] = mtrSpeedReg;
	nMotorPIDSpeedCtrl[driveRight] = mtrSpeedReg;
}

/**********************************************************************************
 * Movement functions
 **********************************************************************************/

void showTarget(int val)
{
	nxtDisplayTextLine(3, "Target:    %4d", val);
}

void showHeading(void)
{
	nxtDisplayTextLine(4, "Abs:   %4d", HTMCreadHeading(HTMC));
}

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

void moveBackwardOn(int speed)
{
	motor[driveRight] = -speed;
	motor[driveLeft] = -speed;
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
 * allMotorsOff
 *
 * Turns off all motors on the chassis.
 */
void allMotorsOff()
{
    motor[driveLeft] = 0;
    motor[driveRight] = 0;
}

/*
 * moveForward
 *
 * Move the robot forward a given number of inches.
 */
void moveForward (int inches, int speed = 100)
{
	int encoderCounts = inches * ENCPERINCH;

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
 * turn
 *
 * Turn the robot the specified number of degrees
 *
 * A positive value turns right, a negative value
 * turns left.
 */
void turn(int deg, int speed)
{
	int dest,heading;
    bool done = false;

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

	while (!done) {
        heading = HTMCreadHeading(HTMC);
        if ((heading >= dest-1) && (heading <= dest+1)) {
            done = true;
        }
		showHeading();
	}

  	moveForwardOff();
}
