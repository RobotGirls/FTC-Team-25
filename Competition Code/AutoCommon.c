
void initializeRobot()
{
  	// Place code here to sinitialize servos to starting positions.
  	// Sensors are automatically configured and setup by ROBOTC. They may need a brief time to stabilize.
  	servo[gravityShelf] = SHELFDOWN;
  	servo[IRServo] = IRUP;
    servo[Ramp] = RAMP_START;

	/*
	 * Assume lined up perpendicular to the pegs.
	 */
	HTMCsetTarget(HTMC);

	nMotorPIDSpeedCtrl[driveLeft] = mtrSpeedReg;
	nMotorPIDSpeedCtrl[driveRight] = mtrSpeedReg;
	nMotorPIDSpeedCtrl[driveSide] = mtrSpeedReg;

    /*
     * Do not let the motors coast
     */
    bFloatDuringInactiveMotorPWM = false;

	// the default DSP mode is 1200 Hz.
	tHTIRS2DSPMode mode = DSP_1200;

	// set the DSP to the new mode
	// HTIRS2setDSPMode(IRSeeker, mode);

  	return;
}

/*
 * lookForIRBeacon
 *
 * Drive sideways until we see the beacon in segment 5
 * of the IR receiver.
 */
direction_t lookForIRBeacon(void)
{
	int segment;
    direction_t moved_dir;
	int strength1, strength2, strength3, strength4, strength5;

	segment = HTIRS2readACDir(IRSeeker);

	if (segment != BEACON_CENTER) {
        if (segment > BEACON_CENTER) {
		    motor[driveSide] = -40;
            moved_dir = RIGHT;
        } else {
            motor[driveSide] = 40;
            moved_dir = LEFT;
        }
	} else {
		return NO_DIR;
	}

    HTIRS2readAllACStrength(IRSeeker, strength1, strength2, strength3, strength4, strength5);

	while (segment != BEACON_CENTER) {
    	segment = HTIRS2readACDir(IRSeeker);
	}

    /*
     * We are in the correct segment, so move until the strength
     * is balanced.
     */
    HTIRS2readAllACStrength(IRSeeker, strength1, strength2, strength3, strength4, strength5);

    while (strength3 > strength2) {
        HTIRS2readAllACStrength(IRSeeker, strength1, strength2, strength3, strength4, strength5);
    }

	motor[driveSide] = 0;

    pauseDebug("ir found", 1);

    // moveSideways(7);

    return (moved_dir);
}

void moveForwardToIRBeacon(int strength)
{
    int val;
    char tmp[50];

    motor[driveLeft] = 70;
    motor[driveRight] = 70;

    val = getStrength();
    while (val <= strength) {
        val = getStrength();
        sprintf(tmp, "strength %d", val);
        nxtDisplayCenteredTextLine(2, tmp);
   }

    motor[driveLeft] = 0;
    motor[driveRight] = 0;
}

/*
 * lookForWhiteLine
 *
 * Look for an edge of the white line by
 * moving sideways until we see the transistion.
 */
void lookForWhiteLine(direction_t dir)
{
	int val;

	LSsetActive(lightSensor);

	switch (dir) {
		case LEFT:
			motor[driveSide] = 15;
	        // Look for the first right edge.
		   	val = LSvalNorm(lightSensor);
			while (val <= 22) {
				val = LSvalNorm(lightSensor);
			}
			break;
		case RIGHT:
	        // Look for first the left edge and then the right
			motor[driveSide] = -15;
		   	val = LSvalNorm(lightSensor);
			while (val <= 22) {
				val = LSvalNorm(lightSensor);
			}

		    while (val >=20) {
		        val = LSvalNorm(lightSensor);
		    }
			break;
        case FORWARD:
            motor[driveLeft] = 10;
            motor[driveRight] = 10;
		   	val = LSvalNorm(lightSensor);
			while (val <= 28) {
				val = LSvalNorm(lightSensor);
			}
            break;
		case NO_DIR:
		default:
			return;
	}

	LSsetInactive(lightSensor);

	motor[driveSide] = 0;
    motor[driveLeft] = 0;
    motor[driveRight] = 0;

    pauseDebug("on white line", 1);
}

/*
 * Move forward until we have detected a white line.
 * dist is the distance to move full speed.  We then
 * slow down so that we don't overshoot the line.  e.g.
 * You know you won't find the line befor dist inches.
 */
void moveForwardToWhiteLine(int dist)
{
    moveForward(dist);
    lookForWhiteLine(FORWARD);
}

/*
 * alignToPeg
 *
 * We may have moved off perpendicular to the
 * peg during our travels.  If so, rotate back.
 */
direction_t alignToPeg(void)
{
	int bearing;
    char str[48];

	// Are we aligned?  If so we do nothing.
	bearing = HTMCreadRelativeHeading(HTMC);

    sprintf(str, "Compass off %d", bearing);
    pauseDebug(str, 5);

	if (bearing == 0) {
		return NO_DIR;
	} else {
		turn(bearing, 10);
		if (bearing < 0) {
			return LEFT;
		} else {
			return RIGHT;
		}
	}
}

void moveForwardToPushStop()
{
    int val;

    //servoChangeRate[IRServo] = 1;
    servo[IRServo] = IR_DEPLOY_RING;
    //wait1Msec(3000);
    //servoChangeRate[IRServo] = 10;

    wait1Msec(1000);

    // We are aligned.  Move forward until the
    // the touch sensor is depressed.
    moveForwardOn(10);
    val = SensorValue[touchSensor];

	while (val != 1) {
        val = SensorValue[touchSensor];
        nxtDisplayCenteredBigTextLine(3, "Move: %d", val);
    }
    moveForwardOff();
}

/*
 * placeRing
 *
 * Completely automated place functionality for putting
 * a ring on a peg.
 *
 * Assumes we are on the platform and in front of the
 * peg we want to place the ring on.
 */
void placeRing(void)
{
	raiseShelfToAutoPlacePosition();

    pauseDebug("shelf raised, servo deployed", 1);

    moveForwardToPushStop();

    pauseDebug("are we in place?", 1);

	// We are aligned, and on the white line so
	// move forward until we hit the proper strength
	// value from the beacon.
	//moveToBeacon(BEACON_TARGET_STRENGTH);

    servo[gravityShelf] = 250;
    moveSideways(20);

	moveBackward(3);
}
