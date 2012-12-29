#define ENCPERINCH 200

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
}

/*
 * moveForward
 *
 * Move the robot forward a given number of inches
 * at the given speed.
 */
void moveForward (int inches, int speed)
{
        //motor[leftFront] = inches, speed;
 		//motor[rightFront] = inches, speed;
 		//motor[leftRear] = inches, speed;
 	    //motor[rightRear] = inches, speed;
}

/*
 * moveForwardHalf
 *
 * Move the robot forward a given number of half inches
 * at the given speed.
 */
void moveForwardHalf(int halfInches, int speed)
{
        //motor[leftFront] = (halfInches/2), speed;
 		//motor[rightFront] = (halfInches/2), speed;
 		//motor[leftRear] = (halfInches/2), speed;
 		//motor[rightRear] = (halfInches/2), speed;
}

/*
 * moveBackward
 *
 * Move the robot backward a given number of inches
 * at the given speed.
 */
void moveBackward (int inches, int speed)
{
        //motor[leftFront] = inches, speed;
 		//motor[rightFront] = inches, speed;
 		//motor[leftRear] = inches, speed;
 		//motor[rightRear] = inches,speed;
}

/*
 * moveBackwardHalf
 *
 * Move the robot backward a given number of half inches
 * at the given speed.
 */
void moveBackwardHalf(int inches, int speed)
{
        //motor[leftFront] = -(halfInches/2), speed;
 		//motor[rightFront] = -(halfInches/2), speed;
 		//motor[leftRear] = -(halfInches/2), speed;
 		//motor[rightRear] = -(halfInches/2), speed;
}

/*
 * moveSideways
 *
 * Move the robot sideways a given number of inches.
 * FIXME: This only moves one way. z Fix such that you can
 *        move either right or left.
 */
void moveSideways (int inches, int speed)//fixed to go left right now. To go right put -speed.
{
    int encoderCounts = inches * ENCPERINCH;

	nMotorEncoder[leftFront] = 0;
    nMotorEncoder[rightFront] = 0;
    nMotorEncoder[leftRear] = 0;
    nMotorEncoder[rightRear] = 0;

	motor[leftFront] = -speed;
    motor[rightFront] = -speed;
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
