#pragma config(UserModel, "../pragmas/baemax.h")

int encodercounts;

task main()
{
    nMotorEncoder[arm_motor] = 0;

    encodercounts = 0;

    if (nNxtButtonPressed == 1) {
        motor[arm_motor] = 25;

        encodercounts = encodercounts + 50;

        while (nMotorEncoder[arm_motor] < encodercounts) {
        }
        motor[arm_motor] = 0;
    } else if (nNxtButtonPressed == 2) {
        motor[arm_motor] = -25;

        encodercounts = encodercounts - 50;

        while (nMotorEncoder[arm_motor] > encodercounts) {
        }
        motor[arm_motor] = 0;
    }
    nxtDisplayCenteredBigTextLine(3, "%d", encodercounts);
}
