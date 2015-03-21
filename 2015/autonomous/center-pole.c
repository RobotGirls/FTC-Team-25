#pragma config(UserModel, "../pragmas/baemax.h")
//*!!Code automatically generated by 'ROBOTC' configuration wizard               !!*//

#define FOUR_WHEEL_DRIVE
#define DEAD_RECKON_GYRO

#include "JoystickDriver.c"
#include "../../lib/sensors/drivers/hitechnic-sensormux.h"
#include "../../lib/sensors/drivers/hitechnic-protoboard.h"
#include "../../lib/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../../lib/sensors/drivers/hitechnic-gyro.h"

const tMUXSensor HTGYRO  = msensor_S4_3;
bool beacon_done;
int distance_monitor_distance;

#include "../../lib/rnrr_start.h"
#include "../library/baemax_defs.h"
#include "../../lib/baemax_drivetrain_defs.h"
#include "../../lib/drivetrain_square.h"
#include "../../lib/dead_reckon.h"
#include "../../lib/data_log.h"
#include "../../lib/ir_utils.h"
#include "../../lib/us_utils.h"
#include "../../lib/us_cascade_utils.c"
#include "../../lib/limit_switch.h"
#include "../library/auto_utils.h"

const tMUXSensor irr_left = msensor_S4_1;
const tMUXSensor irr_right = msensor_S4_2;
const tMUXSensor HTPB = msensor_S4_4;

ir_direction_t dir;
bool in_front_of_center;
bool done_scoring;

task drop_shoulder() {
    down_shoulder(shoulder, 35, 10, 2051);
}

task auto_timer()
{
    int i;

    for (i = 0; i < 27; i++) {
        wait1Msec(1000);
        if (done_scoring == true) {
            break;
        }
    }

	if (in_front_of_center) {
		move_to(shoulder, -20, 400);
		move_to(arm_motor, 45, 25100);

        if (i < 20) {
            //startTask(drop_shoulder);
            //wait1Msec(2000);
			down_shoulder(shoulder, 75, 20, 4000);

            init_path();
			add_segment(-18, 90, 65);
			add_segment(0, -90, 100);
            stop_path();
            dead_reckon();

            servo[fist] = 127;

            init_path();
            add_segment(-23, 0, 100);
            add_segment(0, 25, 100);
			stop_path();
			dead_reckon();
        }
	}
}

void move_to_pole(int count)                 // Function that moves the robot to the pole
{                                            // based on the position of the center goal.

    switch (count) {
    case 1:
        init_path();
        add_segment(-18, -45, 50);
        add_segment(-10, 90, 100);
        add_segment(0, 45, 100);
        stop_path();
        dead_reckon();

        /*
        add_segment(0, 45, 100);
        add_segment(-10, -45, 100);
        add_segment(0, 45, 100);
        add_segment(-10, -45, 100);
        add_segment(0, 45, 100);
        */
        break;
    case 2:
        init_path();
        add_segment(-13, 0, 50);
        add_segment(0, 25, 100);
        stop_path();
        dead_reckon();

        servo[fist] = 127;
        wait1Msec(500);

        init_path();
        add_segment(-25, 0, 100);
        add_segment(0, 25, 100);
        stop_path();
        dead_reckon();
        break;
    case 3:
        init_path();
        add_segment(-10, 90, 50);
        add_segment(-25, -90, 100);
        stop_path();
        dead_reckon();
        break;
    }
}

task distance_monitor()
{
    if (SensorValue[carrot] < distance_monitor_distance) {
	    while (SensorValue[carrot] <= distance_monitor_distance) {
	    }
    } else {
	    while (SensorValue[carrot] >= distance_monitor_distance) {
	        /* Noop, or a wait for that condition to be true */
	    }
    }
    beacon_done = true;
}

task main()
{
    int i;
    int center_position;
    int offset;
    int bias;

    //disableDiagnosticsDisplay();

    bias = HTGYROstartCal(HTGYRO);
    offset = initialize_receiver(irr_left, irr_right);

	if (!limit_switch_init(HTPB, 0x05)) {
		nxtDisplayCenteredBigTextLine(3, "ERROR");
		nxtDisplayTextLine(6, "CF251, LP: IZZIE");
	}

	/*
	 * Ensure arm is at the origin and the motor is off
	 */
	move_to(arm_motor, ARM_MOTOR_SPEED, 25100);

	in_front_of_center = false;
    done_scoring = false;
    servo[leftEye] = LSERVO_CENTER;
    servo[rightEye] = RSERVO_CENTER;
    servo[door] = SERVO_DOOR_CLOSED;
    servo[brush] = 127;
    servo[fist] = 0;

    RNRR_waitForStart();

	startTask(auto_timer);

    center_position = ultrasound(carrot, -24, US_DIST_POS_1, US_DIST_POS_3);    // Sets the value integer center_position to 1, 2, or 3
                                                                                // based on ultrasound sensor readings.
    //for (i = 0; i < center_position; i++) {
    //    playImmediateTone(251, 25);                                             // Beep the center structure position for 0.25 second.
    //    wait1Msec(750);
    //}

    eraseDisplay();

    nxtDisplayCenteredBigTextLine(3, "%d", center_position);

    if (center_position == 3) {
        servo[leftEye] = LSERVO_CENTER + CROSSEYED;
        servo[rightEye] = RSERVO_CENTER - CROSSEYED;

		raise_shoulder(shoulder, 35, 10, 2500);
        raise_arm(arm_motor);
		in_front_of_center = true;

        //servo[leftEye] = LSERVO_CENTER + CROSSEYED;
        //servo[rightEye] = RSERVO_CENTER - CROSSEYED;

        //find_absolute_center(irr_left, irr_right, false);

        /*
         * Ensure the shoulder is all the way up
         */
        raise_shoulder(shoulder, 10, 10, 500);

        score_center_goal(CENTER_GOAL_DUMP_DISTANCE);
        done_scoring = true;
    } else if (center_position == 2) {
        init_path();
        add_segment(30, 90, 50);
        add_segment(0, -45, 40);
        stop_path();
        dead_reckon();

		raise_shoulder(shoulder, 35, 10, 2500);
        raise_arm(arm_motor);
		in_front_of_center = true;

        servo[leftEye] = LSERVO_CENTER + CROSSEYED;
        servo[rightEye] = RSERVO_CENTER - CROSSEYED;

        //find_absolute_center(irr_left, irr_right, false);

        /*
         * Ensure the shoulder is all the way up
         */
        raise_shoulder(shoulder, 10, 10, 500);

        score_center_goal(CENTER_GOAL_DUMP_DISTANCE);
        done_scoring = true;
    } else {
        move_to_pole(center_position);
    }

    while(true) {
        nxtDisplayTextLine(1, "Sensor value: %d", SensorValue[carrot]);
    }
}
