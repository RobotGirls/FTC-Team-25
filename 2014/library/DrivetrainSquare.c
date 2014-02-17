
#define ENC_TICKS_PER_DEGREE 25

typedef enum {
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
 * Move the robot the given direction a given number of
 * inches.  If inches is 0 and speed is non-zero, turn on
 * motors and do not turn off.  0,0 turns off motors.
 */
void move(float inches, direction_t dir, int speed = 100)
{
	int encoderCounts = inches * ENCPERINCH;
    int direction_multiplier;

	nMotorEncoder[driveRight] = 0;
	nMotorEncoder[driveLeft] = 0;

    switch (dir) {
    case FORWARD:
	    direction_multiplier = 1;
        break;
    case BACKWARD:
	    direction_multiplier = -1;
        break;
    }

    if ((inches == 0) && (speed == 0)) {
        motor[driveRight] = 0;
        motor[driveLeft] = 0;
    } else if ((inches == 0) && (speed != 0)) {
	    motor[driveRight] = direction_multiplier * speed;
		motor[driveLeft] = direction_multiplier * speed;
    } else {
	    nMotorEncoderTarget[driveRight] = direction_multiplier * encoderCounts;
	    nMotorEncoderTarget[driveLeft] = direction_multiplier * encoderCounts;
	    motor[driveRight] = direction_multiplier * speed;
		motor[driveLeft] = direction_multiplier * speed;

	    while ((nMotorRunState[driveRight] != runStateIdle) && (nMotorRunState[driveLeft] != runStateIdle)) {
	    }
    }
}

int calcTarget(int deg)
{
    int target;

	target = HTMCreadHeading(HTMC);

    nxtDisplayTextLine(2, "Start:   %4d", target);

	target = target + deg;
	if (target < 0) {
		target = 360 - abs(target);
	} else if (target > 360) {
		target = target - 360;
	}

    return target;
}

void turnEncoder(int deg, int speed)
{
    int encoderCounts = deg * ENC_TICKS_PER_DEGREE;
    int dest, delta;

    if (deg == 0) {
        return;
    }

    dest = calcTarget(deg);

    HTMCsetTarget(HTMC, dest);

    nMotorEncoder[driveLeft] = 0;
    nMotorEncoder[driveRight] = 0;

    nMotorEncoderTarget[driveLeft] = encoderCounts;
    nMotorEncoderTarget[driveRight] = encoderCounts;

    if (deg > 0) {
	    rotateClockwise(speed);
    } else {
	    rotateCounterClockwise(speed);
    }

    while ((nMotorRunState[driveLeft] != runStateIdle) && (nMotorRunState[driveRight] != runStateIdle)) {
    }

    /*
     * How close did we get to the target?
     */
    delta = HTMCreadRelativeHeading(HTMC);
    if ((delta <= 3) && (delta >= -3)) {
        return;
    } else {
        turnEncoder(delta, speed);
    }
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

	dest = calcTarget(deg);

	//showTarget(dest);

    HTMCsetTarget(HTMC, dest);

	while (!done) {
        delta = HTMCreadRelativeHeading(HTMC);
        if ((delta <= 3) && (delta >= -3)) {
            done = true;
        } else if (delta < 0) {
            rotateClockwise(max2(abs(delta), 10));
        } else {
            rotateCounterClockwise(max2(delta, 10));
        }
	}
    //showHeading();
  	move(0, FORWARD, 0);
}
