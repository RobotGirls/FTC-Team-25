bool done;

task main()
{
    long val;

    done = false;

    nMotorEncoder[rightElevator] = 0;

    motor[rightElevator] = -20;
    motor[leftElevator] = 20;

    while (!done) {
        val = nMotorEncoder[rightElevator];
        nxtDisplayCenteredTextLine(4, "Val: %d", val);
        if (nNxtButtonPressed == 3) {
            done = true;
        }
    }

    motor[rightElevator] = 0;
    motor[leftElevator] = 0;

    while (1) {}
}
