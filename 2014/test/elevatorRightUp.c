

task main()
{
    int rightVal;

    motor[rightElevator] = -20;

    while(1) {
	    rightVal = nMotorEncoder[rightElevator];
        nxtDisplayCenteredTextLine(2, "right: %d\n", rightVal);
    }
}
