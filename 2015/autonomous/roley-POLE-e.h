
#define SERVO_ARM_EXTENDED          90
#define SERVO_ARM_RETRACTED         150
#define SERVO_ARM_EXTENDED_HALF     120
#define SERVO_ARM_PICKUP            140

#define SERVO_DOCK_ARM_FORWARD      80
#define SERVO_DOCK_ARM_BACKWARD     0
#define SERVO_DOCK_ARM_STOPPED      127
#define SERVO_FINGER_UP             160
#define SERVO_FINGER_DOWN           146

#define LSERVO_CENTER 180
#define RSERVO_CENTER 90
#define RSERVO_PERP   235

#define FOUR_WHEEL_DRIVE
#include "../../lib/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../../lib/drivetrain_andymark_defs.h"
#include "../../lib/drivetrain_square.h"
#include "../../lib/dead_reckon.h"
#include "../../lib/data_log.h"
#include "../../lib/ir_utils.h"
#include "JoystickDriver.c"

ir_direction_t dir;

int count;

void move_to_position(int position)
{
    int i;

	init_path();

    for (i = 0; i < position; i++) {
        playImmediateTone(60, 50);
        wait1Msec(2000);
    }

	switch (position) {
	case 1:
        add_segment(-7, 0, 40);
        add_segment(-18, 45, 50);      //  go to kickstand at position 1
        add_segment(0, 100, 70);
        break;
    case 2:
        add_segment(6, 0, 40);
        add_segment(-20, 45, 40);    //  go to kickstand at position 2
        add_segment(-20, -45, 40);
        add_segment(0, 110, 70);
        break;
    case 3:
        add_segment(6, 0, 40);
        add_segment(-41, 45, 40);      // go to kickstand at position 3
        add_segment(-25, -92, 40);
        add_segment(0, 100, 70);
        break;
    }
    stop_path();
  	dead_reckon();
}

/*
 * Use dual IR sensors to locate beacon position from a known
 * location where the direction information makes beacon location deterministic.
 */

int find_beacon()
{
    int position;

    ir_direction_t r_dir;
    ir_direction_t l_dir;

    position = 2;

	if(get_dir_to_beacon(irr_left)==DIR_LEFT)
	{
		position = 1;
	}
	else if (get_dir_to_beacon(irr_right)==DIR_RIGHT)
	{
		position = 3;
	}

    return position;
}

void initializeRobot()
{
    servo[arm] = SERVO_ARM_RETRACTED;
    servo[finger] = SERVO_FINGER_DOWN;
    servo[brush]=127;
    servo[rightEye]=127;
    servo[leftEye]=127;
}

task main()
{
    initializeRobot();

    waitForStart(); // Wait for the beginning of autonomous phase.

    initialize_receiver(irr_left, irr_right);

    wait1Msec(500);

	init_path();
    add_segment(-6, 0, 50);
    add_segment(-29, -30, 50);
    add_segment(-6, 75, 60);
	stop_path();
	dead_reckon();

	servo[rightEye] = RSERVO_CENTER;
    servo[leftEye] = LSERVO_CENTER;
    wait1Msec(2000);

	move_to_position(find_beacon());

	while(true){}
}
