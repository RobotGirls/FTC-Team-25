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

	nMotorEncoder[m_shoulder] = 0;
	motor[m_shoulder] = speed_half_up;

    while (nMotorEncoder[m_shoulder] < half_up) {
    }

	while (is_limit_switch_open(0x05)) {
		if (nMotorEncoder[m_shoulder] > half_up) {
			motor[m_shoulder] = speed_all_up;
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

    servo[brush] = 10;
    wait1Msec(1500);
    servo[brush] = 127;
}

void raise_the_monster()
{
	raise_shoulder(shoulder, 35, 15, 2500);

    /*
     * Mark the shoulder motor's encoder position before raising the
     * arm.
     */
    nMotorEncoder[shoulder] = 0;

    if (is_limit_switch_closed(0x05)) {
        motor[shoulder] = 25;
        while (abs(nMotorEncoder[shoulder]) < 100) {
        }
        motor[shoulder] = 0;
    }

    raise_arm(arm_motor);

    servo[leftEye] = LSERVO_CENTER + CROSSEYED;
    servo[rightEye] = RSERVO_CENTER - CROSSEYED;

    /*
     * Did the shoulder move backward?  If so, fix it.
     * Start a timer as a failsafe.  e.g. We should be at
     * the top of rotation, if it takes more than 3 seconds
     * something is wrong anyway.  Abort to prevent damage
     * to the robot.
     */
    /*
    if (nMotorEncoder[shoulder] < 0) {
        clearTimer(T1);
        motor[shoulder] = 10;
        while (nMotorEncoder[shoulder] < 0) {
            if (time1[T1] >= 3000) {
                break;
            }
		}
		motor[shoulder] = 0;
	}
    */

    /*
     * Ensure the shoulder is all the way up.
	 * We shouldn't really need this given the software
	 * that looks for the shoulder rotating backward, but
	 * it can't hurt cause it's a noop if the switch is closed.
     */
    //raise_shoulder(shoulder, 10, 10, 500);
}
