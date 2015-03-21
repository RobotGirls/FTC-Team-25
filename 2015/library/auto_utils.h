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

/*
void raise_shoulder(int ticks)
{
	nMotorEncoder[shoulder] = 0;

    motor[shoulder] = 60;
    while (nMotorEncoder[shoulder] < ticks) {           // While encoder counts is less than TICKS, move shoulder at 60
    }                                                   // power. When encoder counts surpasses TICKS, stop shoulder.
    motor[shoulder] = 0;
}
*/


void raise_arm(tMotor m_arm)
{
	move_to(m_arm, -ARM_MOTOR_SPEED, 251000);
}

void raise_shoulder(tMotor m_shoulder, int speed_half_up, int speed_all_up, int half_up)
{
	eraseDisplay();

	if (is_limit_switch_open(0x05)) {
		nMotorEncoder[m_shoulder] = 0;
		motor[m_shoulder] = speed_half_up;
		while (is_limit_switch_open(0x05)) {
			if (nMotorEncoder[m_shoulder] > half_up) {
				motor[m_shoulder] = speed_all_up;
			}
		}
    }
	motor[m_shoulder] = 0;
}

void down_shoulder(tMotor m_shoulder, int speed_half_down, int speed_all_down, int half_down)
{
    if (is_limit_switch_open(0x04)) {
        nMotorEncoder[m_shoulder] = 0;
        motor[m_shoulder] = -speed_half_down;
        while (is_limit_switch_open(0x04)) {
            if (abs(nMotorEncoder[m_shoulder]) > half_down) {
                motor[m_shoulder] = -speed_all_down;
            }
        }
    }
    motor[m_shoulder] = 0;
}

void score_center_goal(int dump_dist)                   // Function that moves the robot to the correct distance, raises
{                                                       // the arm, and spins the balls into the tube.
    move_to_object(carrot, -10, dump_dist);             // Move robot to position where it can dump balls in tube.

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
