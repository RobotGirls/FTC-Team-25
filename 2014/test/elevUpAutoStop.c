#pragma config(StandardModel, "tommy")

bool done;

task main()
{
    long val;

    done = false;

    nMotorEncoder[rightElevator] = 0;

    motor[rightElevator] = -20;
    motor[leftElevator] = 20;

    val = nMotorEncoder[rightElevator];
    while (val > -16000) {
        val = nMotorEncoder[rightElevator];
    }

    motor[rightElevator] = 0;
    motor[leftElevator] = 0;

    while (1) {}
}
