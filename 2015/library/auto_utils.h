typedef enum motor_state_ {
	MOTOR_OK,
	MOTOR_STALL,
} motor_state_t;

#define STALL_TIMEOUT 500

motor_state_t move_to(tMotor m, int speed, int count)
{
	motor_state_t rtn;
	int last;

	rtn = MOTOR_OK;
	nMotorEncoder[m] = 0;
	last = nMotorEncoder[m];
	clearTimer(T1);

	motor[m] = speed;

	while (abs(nMotorEncoder[m]) < count) {
		if (nMotorEncoder[m] != last) {
			clearTimer(T1);
			last = nMotorEncoder[m];
		}
		if (time1[T1] >= STALL_TIMEOUT) {
			rtn = MOTOR_STALL;
			break;
		}
	}

	motor[m] = 0;

	return rtn;
}


void raise_arm(int ticks)
{
    motor[shoulder] = 30;
    while (nMotorEncoder[shoulder] < ticks) {        // While encoder counts is less than UPCOUNTS, move shoulder at 15
    }                                                   // power. When encoder counts surpasses UPCOUNTs, stop shoulder.
    motor[shoulder] = 0;

    servoChangeRate[arm] = 0;
    servo[arm] = SERVO_ARM_EXTENDED;                    // Move the arm to the extended position.

    wait1Msec(12000);
}

void score_center_goal(int dump_dist)          // Function that moves the robot to the correct distance, raises
{                                                       // the arm, and spins the balls into the tube.
    move_to_object(carrot, -5, dump_dist);              // Move robot to position where it can dump balls in tube.

    servo[door] = SERVO_DOOR_CENTERGOAL_RAMP;           // Open the servo door (releases balls).

    //servo[brush] = 255;                                 // Rotate brush (spins balls into center goal).
    //wait1Msec(2000);
    //servo[brush] = 127;
    //while (true) {
	    servo[brush] = 255;
	    wait1Msec(1000);
        servo[brush] = 127;
	    //servo[brush] = 255;
        //wait1Msec(200);
    //}
}
