
#include "..\library\sensors\drivers\common.h"

#define MAX_CONTROLLERS  4
#define MAX_PORTS        2

#define NO_PID_CONTROL 0
#define PID_CONTROL    1

typedef struct motor_def_ {
    unsigned int controller;
    unsigned int port;
    unsigned int i2c_addr;
    int          speed;
    unsigned int mode;
    bool         running;
} motor_def_t;

/*
 * This would be statically initialized but RobotC
 * has a bug with initialization of 2 dimensional arrays
 * of structs
 */
motor_def_t motorArray[MAX_CONTROLLERS][MAX_PORTS];

TSemaphore  motor_semaphore;
tSensors link;

void _init_elem(motor_def_t *m, unsigned int controller, unsigned int port,
                    unsigned int i2c_addr, int speed, unsigned int mode,
                    bool running)
{
    m->controller = controller;
    m->port = port;
    m->i2c_addr = i2c_addr;
    m->speed = speed;
    m->mode = mode;
    m->running = running;
}

void _init_semaphore()
{
    SemaphoreInitialize(motor_semaphore);
}

void _lock_semaphore()
{
    SemaphoreLock(motor_semaphore);
}

void _unlock_semaphore()
{
    SemaphoreUnlock(motor_semaphore);
}

void _motor_heartbeat(motor_def_t *m)
{
    tByteArray I2Crequest;

    _lock_semaphore();

    I2Crequest[0] = 4;
    I2Crequest[1] = m->i2c_addr;
    if (m->port == 1) {
	    I2Crequest[2] = 0x44;
	    I2Crequest[3] = m->mode;
        I2Crequest[4] = m->speed & 0xFF;
    } else {
	    I2Crequest[2] = 0x46;
        I2Crequest[3] = m->speed & 0xFF;
	    I2Crequest[4] = m->mode;
    }

    writeI2C(link, I2Crequest);

    _unlock_semaphore();
}

motor_def_t *_get_motor(unsigned int controller, unsigned int port)
{
    if ((controller >= MAX_CONTROLLERS) || (port >= MAX_PORTS)) {
        return NULL;
    } else {
        return &motorArray[controller][port];
    }
}

task _run_motor()
{
    while (true) {
        for (int i = 0; i < MAX_CONTROLLERS; i++) {
            for (int j = 0; j < MAX_PORTS; j++ ) {
	            if (motorArray[i][j].running == false) {
	                continue;
	            } else {
		            _motor_heartbeat(motorArray[i][j]);
                }
            }
        }
        wait1Msec(30);
    }
}
