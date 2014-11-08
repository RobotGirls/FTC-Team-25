

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
           (abs(nMotorEncoder[driveRearLeft]) < t) &&
           (abs(nMotorEncoder[driveRearRight]) < t))
	{
    }

#ifdef FOUR_WHEEL_DRIVE
    motor[driveFrontLeft] = 0;
    motor[driveFrontRight] = 0;
#endif
    motor[driveRearLeft] = 0;
    motor[driveRearRight] = 0;
}

void move_with_software_pid(int t, int power)
{
    /*
     * The idea is to pick one motor as the master and then
     * continually adjust the slave to match the master.
     */
	int master_power = power;
	int slave_power = power;

    /*
     * Resetting the encoder each time, so we need to track total ticks
     * to know when to stop.
     */
    int total_ticks = 0;

	/*
     * The difference between the master encoder and the slave encoder
     * If they aren't the same, then one is moving faster and we must
     * correct for that.
     */
	int error = 0;

	/*
     * The P in PID.  The proportional amount that we want to change the
     * motor's power by.  This seems large, but it appears to work pretty well,
     * with a very small delay in between reading the error factor.
     */
	float kp = 1.0;

	nMotorEncoder[driveRearLeft] = 0;
	nMotorEncoder[driveRearRight] = 0;

    while (abs(total_ticks) < t)
	{
	    motor[driveRearLeft] = master_power;
	    motor[driveRearRight] = slave_power;

	    /*
         * If the left motor (the master) is moving faster than the right (the slave), then this will be
	     * a positive number, meaning the master is moving faster so the slave has to speed up.
         */
	    error = nMotorEncoder[driveRearLeft] - nMotorEncoder[driveRearRight];

	    /*
         * Multiple error by the scaling, or proportional factor, and adjust the slave.
         */
	    slave_power += error * kp;
	    motor[driveRearRight] = slave_power;

        /*
         * Debugging.  The NXT display comes in handy.
         */
        displayString(1, "Ticks %d", total_ticks);
        displayString(2, "Error: %d", error);
        displayString(4, "R: %d, L: %d", slave_power, master_power);

	    /*
         * Need a fresh error factor each iteration.
         */
	    nMotorEncoder[driveRearLeft] = 0;
	    nMotorEncoder[driveRearRight] = 0;

	    /*
         * Pretty fast interval.  I think the Hitechnic controller by default uses a 25ms
         * refresh interval, but that turned out to be too slow.
         */
	    wait1Msec(5);

        /*
         * Update the master tick count so we know when to stop.
         */
        total_ticks += nMotorEncoder[driveRearLeft];  // The left is the master...
    }

    motor[driveRearLeft] = 0;
    motor[driveRearRight] = 0;
}

void turnEncoder(float deg, int speed)
{
    float encoderCounts = abs(deg * ENC_TICKS_PER_DEGREE);

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

#ifdef USE_COMPASS_CORRECTION
    int target_pos, target_rel, actual;
    target_pos = HTMCsetTarget(compass, 0);
#endif

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
        move_with_software_pid(encoderCounts, direction_multiplier * speed);
    }

#ifdef USE_COMPASS_CORRECTION
	    target_rel = HTMCreadRelativeHeading(compass);
	    actual = HTMCreadHeading(compass);
		displayString(1, "Start t: %d", target_pos);
	    while (abs(target_rel) > 0) {
			displayString(2, "Target: %d", target_pos);
			displayString(3, "Actual: %d", actual);
			displayString(4, "Relative: %d", target_rel);
	        if (target > 0) {
	            turnEncoder(.25, 20);
	        } else {
	            turnEncoder(-.25, 20);
	        }
	        target_rel = HTMCreadRelativeHeading(compass);
	        actual = HTMCreadHeading(compass);
	    }
#endif

}
