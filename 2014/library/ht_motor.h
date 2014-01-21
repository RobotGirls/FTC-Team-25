
#include "ht_motor_private.h"

/*
 * HT_MotorSetPIDControl
 *
 * Sets PID control on the given controller/port pair.
 */
void HT_MotorSetPIDControl(unsigned int controller, unsigned int port, bool ctrl)
{
    motor_def_t *m;

    m = _get_motor(controller, port);
    if (m == NULL) {
        nxtDisplayTextLine(4, "Invalid controller or port");
        return;
    }

    _lock_semaphore();
    if (ctrl) {
        m->mode = PID_CONTROL;
    } else {
        m->mode = NO_PID_CONTROL;
    }
    _unlock_semaphore();
}

/*
 * HT_MotorSetSpeed
 *
 * Sets the speed of the given motor.  Note that this
 * does not turn on the motor.
 */
void HT_MotorSetSpeed(unsigned int controller, unsigned int port, int speed)
{
    motor_def_t *m;

    m = _get_motor(controller, port);
    if (m == NULL) {
        nxtDisplayTextLine(4, "Invalid controller or port");
        return;
    }

    _lock_semaphore();
    m->speed = speed;
    _unlock_semaphore();
}

/*
 * HT_MotorOn
 *
 * Turn the motor on at the current set speed.  Note that the set speed
 * will persist across toggles of the motor's run state.
 */
void HT_MotorOn(int controller, int port)
{
    motor_def_t *m;

    m = _get_motor(controller, port);
    if (m == NULL) {
        nxtDisplayTextLine(4, "Invalid controller or port");
        return;
    }

    _lock_semaphore();
    m->running = true;
    _unlock_semaphore();
}

/*
 * HT_MotorOff
 *
 * Turn the motor off.  The motor may be turned back on without
 * resetting the speed and it will run at the last set speed.
 */
void HT_MotorOff(int controller, int port)
{
    motor_def_t *m;
    int cache_speed;

    m = _get_motor(controller, port);
    if (m == NULL) {
        nxtDisplayTextLine(4, "Invalid controller or port");
        return;
    }

    cache_speed = m->speed;

    _lock_semaphore();
    m->running = false;
    m->speed = 0;
    _unlock_semaphore();

    _motor_heartbeat(m);

    m->speed = cache_speed;
}

/*
 * HT_MotorInit
 *
 * Initialize the software.  Should be called once at startup.
 */
void HT_MotorInit(tSensors l)
{
    link = l;

    _init_semaphore();

	/*
	 * This would be statically initialized but RobotC
	 * has a bug with initialization of 2 dimensional arrays
	 * of structs
	 */
    _init_elem(motorArray[0][0], 1, 1, 0x2, 0, 0, false);
    _init_elem(motorArray[0][1], 1, 2, 0x2, 0, 0, false);
	_init_elem(motorArray[1][0], 2, 1, 0x4, 0, 0, false);
	_init_elem(motorArray[1][1], 2, 2, 0x4, 0, 0, false);
	_init_elem(motorArray[2][0], 3, 1, 0x6, 0, 0, false);
	_init_elem(motorArray[2][1], 3, 2, 0x6, 0, 0, false);
	_init_elem(motorArray[3][0], 4, 1, 0x8, 0, 0, false);
	_init_elem(motorArray[3][1], 4, 2, 0x8, 0, 0, false);

    StartTask(_run_motor);
}
