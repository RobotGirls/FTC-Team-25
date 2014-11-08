

const tMUXSensor HTMC = msensor_S2_1;

typedef enum {
    DIR_FORWARD,
    DIR_BACKWARD
} direction_t;

void initializeMotors(void)
{
#ifdef FOUR_WHEEL_DRIVE
	nMotorPIDSpeedCtrl[driveFrontLeft] = mtrSpeedReg;
	nMotorPIDSpeedCtrl[driveFrontRight] = mtrSpeedReg;
#endif
	nMotorPIDSpeedCtrl[driveRearLeft] = mtrSpeedReg;
	nMotorPIDSpeedCtrl[driveRearRight] = mtrSpeedReg;
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
  	motor[driveFrontRight] = -speed;
  	motor[driveRearRight] = -speed;
	motor[driveFrontLeft] = speed;
	motor[driveRearLeft] = speed;
}

void rotateCounterClockwise(int speed)
{
    if (speed > 100) {
        speed = 100;
    }
	motor[driveFrontRight] = speed;
	motor[driveRearRight] = speed;
	motor[driveFrontLeft] = -speed;
	motor[driveRearLeft] = -speed;
}

/*
 * allMotorsOff
 *
 * Turns off all motors on the chassis.
 */
void allMotorsOff()
{
#ifdef FOUR_WHEEL_DRIVE
    motor[driveFrontLeft] = 0;
    motor[driveFrontRight] = 0;
#endif
    motor[driveRearLeft] = 0;
    motor[driveRearRight] = 0;
}

void allMotorsOn(int speed)
{
#ifdef FOUR_WHEEL_DRIVE
    motor[driveFrontLeft] = speed;
    motor[driveFrontRight] = speed;
#endif
    motor[driveRearLeft] = speed;
    motor[driveRearRight] = speed;
}

void setAllMotorsEncoderTarget(int t)
{
#ifdef FOUR_WHEEL_DRIVE
    nMotorEncoderTarget[driveFrontRight] = t;
    nMotorEncoderTarget[driveFrontLeft] = t;
#endif
    nMotorEncoderTarget[driveRearRight] = t;
    nMotorEncoderTarget[driveRearLeft] = t;

            nxtDisplayTextLine(2, "FrontRight %d", nMotorEncoderTarget[driveFrontRight]);
            nxtDisplayTextLine(3, "FrontLeft  %d", nMotorEncoderTarget[driveFrontLeft]);
            nxtDisplayTextLine(4, "RearRight  %d", nMotorEncoderTarget[driveRearRight]);
            nxtDisplayTextLine(5, "RearLeft   %d", nMotorEncoderTarget[driveRearLeft]);
}

void resetAllMotorsEncoder(void)
{
#ifdef FOUR_WHEEL_DRIVE
    nMotorEncoder[driveFrontRight] = 0;
    nMotorEncoder[driveFrontLeft] = 0;
#endif
    nMotorEncoder[driveRearRight] = 0;
    nMotorEncoder[driveRearLeft] = 0;
}


void waitForIdle(int t)
{
    while ((abs(nMotorEncoder[driveFrontRight]) < t) &&
           (abs(nMotorEncoder[driveFrontLeft]) < t) &&
           (abs(nMotorEncoder[driveRearLeft]) < t) &&
           (abs(nMotorEncoder[driveRearRight]) < t))
	{
    }
    motor[driveRearLeft] = 0;
    motor[driveRearRight] = 0;
    motor[driveFrontLeft] = 0;
    motor[driveFrontRight] = 0;
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

	resetAllMotorsEncoder();

    switch (dir) {
    case DIR_FORWARD:
	    direction_multiplier = 1;
        break;
    case DIR_BACKWARD:
	    direction_multiplier = -1;
        break;
    }

    if ((inches == 0) && (speed == 0)) {
        allMotorsOff();
    } else if ((inches == 0) && (speed != 0)) {
	    allMotorsOn(direction_multiplier * speed);
    } else {
	    setAllMotorsEncoderTarget(encoderCounts);
        allMotorsOn(direction_multiplier * speed);
        waitForIdle(encoderCounts);
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
    int dest, delta, curr, initial, idx;

    if (deg == 0) {
        return;
    }

    idx = 0;

    dest = calcTarget(deg);

    HTMCsetTarget(HTMC, dest);

    eraseDisplay();
    initial = HTMCreadHeading(HTMC);
    nxtDisplayTextLine(3, "Target: %d", dest);
    nxtDisplayTextLine(4, "Dest: %d", curr);

    resetAllMotorsEncoder();

    if (deg > 0) {
	    rotateClockwise(speed);
    } else {
	    rotateCounterClockwise(speed);
    }

    waitForIdle(abs(encoderCounts));

    /*
     * How close did we get to the target?
     */
    /*
    delta = HTMCreadRelativeHeading(HTMC);
    while ((delta > 2) || (delta < -2)) {
        idx++;
	    resetAllMotorsEncoder();
        encoderCounts = ENC_TICKS_PER_DEGREE;
	    setAllMotorsEncoderTarget(encoderCounts);
        if (delta > 0) {
#ifdef FOUR_WHEEL_DRIVE
		    nMotorEncoderTarget[driveFrontRight] = encoderCounts;
		    nMotorEncoderTarget[driveFrontLeft] = encoderCounts;
#endif
		    nMotorEncoderTarget[driveRearRight] = -encoderCounts;
		    nMotorEncoderTarget[driveRearLeft] = -encoderCounts;
            rotateCounterClockwise(speed/2);
        } else {
#ifdef FOUR_WHEEL_DRIVE
		    nMotorEncoderTarget[driveFrontRight] = -encoderCounts;
		    nMotorEncoderTarget[driveFrontLeft] = -encoderCounts;
#endif
		    nMotorEncoderTarget[driveRearRight] = encoderCounts;
		    nMotorEncoderTarget[driveRearLeft] = encoderCounts;
            rotateClockwise(speed/2);
        }
        delta = HTMCreadRelativeHeading(HTMC);
		eraseDisplay();
		curr = HTMCreadHeading(HTMC);
		nxtDisplayTextLine(1, "Delta: %d", delta);
		nxtDisplayTextLine(2, "Initial: %d", initial);
		nxtDisplayTextLine(3, "Target: %d", dest);
		nxtDisplayTextLine(4, "Current: %d", curr);
		nxtDisplayTextLine(5, "Adjust: %d", idx);
        if (nNxtButtonPressed == 3) {
            break;
        }
    } */

    eraseDisplay();
    curr = HTMCreadHeading(HTMC);
 	nxtDisplayTextLine(1, "Delta: %d", delta);
    nxtDisplayTextLine(2, "Initial: %d", initial);
    nxtDisplayTextLine(3, "Target: %d", dest);
    nxtDisplayTextLine(4, "Current: %d", curr);
    nxtDisplayTextLine(5, "Adjust: %d", idx);
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
  	move(0, DIR_FORWARD, 0);
}
