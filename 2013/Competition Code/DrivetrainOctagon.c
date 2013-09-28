#define ENCPERINCH 720


/**********************************************************************************
 * Movement functions for the octagon drive train.
 **********************************************************************************/

/*
 * rotateClockwise
 *
 * Rotates clockwise
 */
void rotateClockwise(int speed)
{
        motor[leftFront] = speed;
 		motor[rightFront] = speed;
 		motor[leftRear] = speed;
 		motor[rightRear] = speed;
}

/*
 * rotateCounterClockwise
 *
 * Rotates counter clockwise
 */

void rotateCounterClockwise(int speed)
{
        motor[leftFront] = -speed;
 		motor[rightFront] = -speed;
 		motor[leftRear] = -speed;
 		motor[rightRear] = -speed;
}

/*
 * moveForwardOn
 *
 * Turns the motors on, never turns them off.
 */
void moveForwardOn(int speed)
{
	motor[leftFront] = speed;
	motor[rightFront] = -speed;
	motor[leftRear] = speed;
	motor[rightRear] = -speed;
}

void moveBackwardOn(int speed)
{
    motor[leftFront] = -speed;
 	motor[rightFront] = speed;
 	motor[leftRear] = -speed;
    motor[rightRear] = speed;
}

void moveSideRightOn(int speed)
{
     motor[leftFront] = speed;
 	 motor[rightFront] = speed;
 	 motor[leftRear] = -speed;
     motor[rightRear] = -speed;
}

void moveSideLeftOn(int speed)
{
    motor[leftFront] = -speed;
    motor[rightFront] = -speed;
    motor[leftRear] = speed;
    motor[rightRear] = speed;
}

/*
 * moveForwardOff
 *
 * Turns the motors off.
 */
void moveForwardOff()
{
        motor[leftFront] = 0;
 		motor[rightFront] = 0;
 		motor[leftRear] = 0;
 		motor[rightRear] = 0;
}

/*
 * allMotorsOff
 *
 * Turns off all motors on the chassis.
 */
void allMotorsOff()
{
        motor[leftFront] = 0;
 		motor[rightFront] = 0;
 		motor[leftRear] = 0;
 		motor[rightRear] = 0;
}

/*
 * moveForward
 *
 * Move the robot forward a given number of inches
 * at the given speed.
 */
void moveForward (int inches, int speed)
{
    int encoderCounts = inches * ENCPERINCH;

	nMotorEncoder[leftFront] = 0;
    nMotorEncoder[rightFront] = 0;
    nMotorEncoder[leftRear] = 0;
    nMotorEncoder[rightRear] = 0;

	motor[leftFront] = speed;
    motor[rightFront] = -speed;
    motor[leftRear] = speed;
    motor[rightRear] = -speed;

	while(abs(nMotorEncoder[leftFront]) < encoderCounts)
	{
	}

	motor[leftFront] = 0;
    motor[rightFront] = 0;
    motor[leftRear] = 0;
    motor[rightRear] = 0;
}

/*
 * moveForwardHalf
 *
 * Move the robot forward a given number of half inches
 * at the given speed.
 */
//void moveForwardHalf(int halfInches, int speed)
//{
//    int encoderCounts = inches * ENCPERINCH/2;

//	nMotorEncoder[leftFront] = 0;
//    nMotorEncoder[rightFront] = 0;
//    nMotorEncoder[leftRear] = 0;
//    nMotorEncoder[rightRear] = 0;

//	motor[leftFront] = speed;
//    motor[rightFront] = -speed;
//    motor[leftRear] = speed;
//    motor[rightRear] = -speed;

//	while(abs(nMotorEncoder[leftFront]) < encoderCounts)
//	{
//	}

//	motor[leftFront] = 0;
//    motor[rightFront] = 0;
//    motor[leftRear] = 0;
//    motor[rightRear] = 0;
//}

/*
 * moveBackward
 *
 * Move the robot backward a given number of inches
 * at the given speed.
 */
void moveBackward (int inches, int speed)
{
    int encoderCounts = inches * ENCPERINCH;

	nMotorEncoder[leftFront] = 0;
    nMotorEncoder[rightFront] = 0;
    nMotorEncoder[leftRear] = 0;
    nMotorEncoder[rightRear] = 0;

	motor[leftFront] = -speed;
    motor[rightFront] = speed;
    motor[leftRear] = -speed;
    motor[rightRear] = speed;

	while(abs(nMotorEncoder[leftFront]) < encoderCounts)
	{
	}

	motor[leftFront] = 0;
    motor[rightFront] = 0;
    motor[leftRear] = 0;
    motor[rightRear] = 0;
}

/*
 * moveBackwardHalf
 *
 * Move the robot backward a given number of half inches
 * at the given speed.
 */
void moveBackwardHalf(int inches, int speed)
{
    int encoderCounts = inches * ENCPERINCH/2;

	nMotorEncoder[leftFront] = 0;
    nMotorEncoder[rightFront] = 0;
    nMotorEncoder[leftRear] = 0;
    nMotorEncoder[rightRear] = 0;

	motor[leftFront] = -speed;
    motor[rightFront] = speed;
    motor[leftRear] = -speed;
    motor[rightRear] = speed;

	while(abs(nMotorEncoder[leftFront]) < encoderCounts)
	{
	}

	motor[leftFront] = 0;
    motor[rightFront] = 0;
    motor[leftRear] = 0;
    motor[rightRear] = 0;
}

/*
 * moveSideways
 *
 * Move the robot sideways a given number of inches.
 * FIXME: This only moves one way. z Fix such that you can
 *        move either right or left.
 */
void moveSidewaysRight (int inches, int speed)
{
    int encoderCounts = inches * ENCPERINCH;

	nMotorEncoder[leftFront] = 0;
    nMotorEncoder[rightFront] = 0;
    nMotorEncoder[leftRear] = 0;
    nMotorEncoder[rightRear] = 0;

	motor[leftFront] = speed;
    motor[rightFront] = speed;
    motor[leftRear] = -speed;
    motor[rightRear] = -speed;

	while(abs(nMotorEncoder[leftFront]) < encoderCounts)
	{
	}

	motor[leftFront] = 0;
    motor[rightFront] = 0;
    motor[leftRear] = 0;
    motor[rightRear] = 0;
}

void moveSidewaysLeft (int inches, int speed)
{
    int encoderCounts = inches * ENCPERINCH;

	nMotorEncoder[leftFront] = 0;
    nMotorEncoder[rightFront] = 0;
    nMotorEncoder[leftRear] = 0;
    nMotorEncoder[rightRear] = 0;

	motor[leftFront] = -speed;
    motor[rightFront] = -speed;
    motor[leftRear] = speed;
    motor[rightRear] = speed;

	while(abs(nMotorEncoder[leftFront]) < encoderCounts)
	{
	}

	motor[leftFront] = 0;
    motor[rightFront] = 0;
    motor[leftRear] = 0;
    motor[rightRear] = 0;
}

//initializes all motors on the robot
void initializeMotors(void)
{
	nMotorPIDSpeedCtrl[leftFront] = mtrSpeedReg;
	nMotorPIDSpeedCtrl[rightFront] = mtrSpeedReg;
	nMotorPIDSpeedCtrl[leftRear] = mtrSpeedReg;
    nMotorPIDSpeedCtrl[rightRear] = mtrSpeedReg;
}
//sideways motions
void sidewaysMovement(int speed)
{
    moveSideLeftOn(speed);
    moveSideRightOn(speed);
}
//void linearEncoder (int inches, int speed)
//{
//    int encoderCounts = inches * ENCPERINCH;

//	//nMotorEncoder[linearSlide] = 0;

//	//motor[linearSlide] = speed;

//	while(abs(nMotorEncoder[linearSlide]) < encoderCounts)
//	{
//	}

//	motor[linearSlide] = 0;
//}
