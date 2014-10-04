

typedef enum  dir_ {
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

void rotateClockwise(int speed)
{
    if (speed > 100) {
        speed = 100;
    }
#ifdef FOUR_WHEEL_DRIVE
  	motor[driveFrontRight] = -speed;
	motor[driveFrontLeft] = speed;
#endif
  	motor[driveRearRight] = -speed;
	motor[driveRearLeft] = speed;
}

void rotateCounterClockwise(int speed)
{
    if (speed > 100) {
        speed = 100;
    }
#ifdef FOUR_WHEEL_DRIVE
	motor[driveFrontRight] = speed;
	motor[driveFrontLeft] = -speed;
#endif
	motor[driveRearRight] = speed;
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

#ifdef FOUR_WHEEL_DRIVE
	nxtDisplayTextLine(2, "FrontRight %d", nMotorEncoderTarget[driveFrontRight]);
	nxtDisplayTextLine(3, "FrontLeft  %d", nMotorEncoderTarget[driveFrontLeft]);
#endif
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
    while (
#ifdef FOUR_WHEEL_DRIVE
           (abs(nMotorEncoder[driveFrontRight]) < t) &&
           (abs(nMotorEncoder[driveFrontLeft]) < t) &&
#endif
           (abs(nMotorEncoder[driveRearLeft]) < t)) //&&
           //(abs(nMotorEncoder[driveRearRight]) < t))
	{ /* Do nothing but wait */ }

#ifdef FOUR_WHEEL_DRIVE
    motor[driveFrontLeft] = 0;
    motor[driveFrontRight] = 0;
#endif
    motor[driveRearLeft] = 0;
    motor[driveRearRight] = 0;
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

void turnEncoder(int deg, int speed)
{
    int encoderCounts = abs(deg * ENC_TICKS_PER_DEGREE);

    if (deg == 0) {
        return;
    }

    resetAllMotorsEncoder();

    if (deg > 0) {
	    rotateClockwise(speed);
    } else {
	    rotateCounterClockwise(speed);
    }
    waitForIdle(encoderCounts);
}
