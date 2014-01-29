
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
	nxtDisplayTextLine(4, "Heading:   %4d", HTMCreadHeading(HTMC));
}

void rotateClockwise(int speed)
{
    if (speed > 100) {
        speed = 100;
    }

  	motor[driveRight] = -speed;
	motor[driveLeft] = speed;
}

void rotateCounterClockwise(int speed)
{
    if (speed > 100) {
        speed = 100;
    }

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

void moveForwardCentimeters(int cm, int speed = 100)
{
	int encoderCounts = cm * (ENCPERINCH / 2.54);

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
void moveBackward (int inches, int speed = 100)
{
	int encoderCounts = inches * ENCPERINCH;

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
void turn(int deg)
{
	int dest, delta;
    bool done = false;

	dest = HTMCreadHeading(HTMC);

    nxtDisplayTextLine(2, "Start:   %4d", dest);

	dest = dest + deg;
	if (dest < 0) {
		dest = 360 - abs(dest);
	} else if (dest > 360) {
		dest = dest - 360;
	}

	//showTarget(dest);

    HTMCsetTarget(HTMC, dest);

	while (!done) {
        delta = HTMCreadRelativeHeading(HTMC);
        if (delta == 0) {
            done = true;
        } else if (delta < 0) {
            rotateClockwise(max2(abs(delta), 10));
        } else {
            rotateCounterClockwise(max2(delta, 10));
        }
	}
    //showHeading();
  	moveForwardOff();
}
