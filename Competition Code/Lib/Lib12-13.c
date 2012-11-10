
#define ENCPERINCH 110  //was 87 for Johnny
#define ENCPERDEG 25 //was 24 for Johnny

/*
 * moveForward
 *
 * Move the robot forward a given number of inches.
 */
void moveForward (int inches)
{
  int encoderCounts=inches*ENCPERINCH;
  int foo;

  nMotorEncoder[driveRight]=0;
  nMotorEncoder[driveLeft]=0;

  while(abs(nMotorEncoder[driveLeft]) < encoderCounts && abs(nMotorEncoder[driveRight]) < encoderCounts)
  {
    foo = nMotorEncoder[driveLeft] && nMotorEncoder[driveRight];


    motor[driveRight]=50;
    motor[driveLeft]=50;
  }
  motor[driveLeft]=0;
  motor[driveRight]=0;
}

/*
 * moveBackward
 *
 * Move the robot backward a given number of inches
 */
void moveBackward (int inches)
{
  int encoderCounts=inches*ENCPERINCH;

  nMotorEncoder[driveRight]=0;
  nMotorEncoder[driveLeft]=0;
  while (abs(nMotorEncoder[driveLeft])<encoderCounts && abs(nMotorEncoder[driveRight])<encoderCounts)
  {
    motor[driveRight]=-50;
    motor[driveLeft]=-50;
  }
  motor[driveLeft]=0;
  motor[driveRight]=0;
}

/*
 * turn
 *
 * Turn the robot the specified number of degrees
 *
 * A positive value turns right, a negative value
 * turns left.
 */
void turn (int degree)
{
  int encoderCounts=abs(degree)*ENCPERDEG;
  nMotorEncoder[driveRight]=0;
  nMotorEncoder[driveLeft]=0;

  if(degree<0)                                                //left
  {
    while(abs(nMotorEncoder[driveLeft])<encoderCounts )
    {
      motor[driveRight]=-50;
      motor[driveLeft]=50;
		}

		motor[driveLeft]=0;
		motor[driveRight]=0;
  }
  else                                                 //turn right
	{
	  while(abs(nMotorEncoder[driveRight])<encoderCounts )
	  {
	    motor[driveRight]=50;
	    motor[driveLeft]=-50;
	  }

	   motor[driveLeft]=0;
	   motor[driveRight]=0;
	 }
}

/*
 * moveSideways
 *
 * Move the robot sideways a given number of inches.
 */
void moveSideways (int inches)
{
  int encoderCounts=inches*ENCPERINCH;
  int foo;

  nMotorEncoder[driveSide]=0;

  while(abs(nMotorEncoder[driveSide]) < encoderCounts)
  {
    foo = nMotorEncoder[driveSide];


    motor[driveSide]=50;
  }

  motor[driveLeft]=0;
  motor[driveRight]=0;
}
