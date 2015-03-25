#pragma config(UserModel, "../pragmas/baemax.h")

//*!!Code automatically generated by 'ROBOTC' configuration wizard               !!*//

#define FOUR_WHEEL_DRIVE
#define DEAD_RECKON_GYRO

#include "../../lib/sensors/drivers/hitechnic-sensormux.h"

#include "JoystickDriver.c"  //Include file to "handle" the Bluetooth messages.
#include "../../lib/sensors/drivers/hitechnic-sensormux.h"
#include "../../lib/sensors/drivers/hitechnic-protoboard.h"
#include "../../lib/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../../lib/sensors/drivers/hitechnic-gyro.h"

const tMUXSensor HTGYRO  = msensor_S4_3;
bool beacon_done;
int distance_monitor_distance;

#include "../library/baemax_defs.h"
#include "../../lib/baemax_drivetrain_defs.h"
#include "../../lib/drivetrain_square.h"
#include "../../lib/limit_switch.h"
#include "../../lib/dead_reckon.h"
#include "../../lib/data_log.h"
#include "../../lib/ir_utils.h"
#include "../../lib/us_utils.h"
#include "../../lib/button_utils.h"
#include "../../lib/us_cascade_utils.c"
#include "../library/auto_utils.h"

const tMUXSensor irr_left = msensor_S4_1;
const tMUXSensor irr_right = msensor_S4_2;
const tMUXSensor HTPB = msensor_S4_4;

static int drive_multiplier = 1;

typedef enum door_state_ {
    DOOR_OPEN,
    DOOR_CLOSED,
    DOOR_CENTERGOAL_RAMP,
} door_state_t;

typedef enum brush_state_ {
    BRUSH_OFF,
    BRUSH_FORWARD,
    BRUSH_BACKWARD,
} brush_state_t;

typedef enum center_dispenser_state_ {
    CENTER_DISPENSER_STOWED,
    CENTER_DISPENSER_DEPLOYED,
} center_dispenser_state_t;

typedef enum shoulder_state_ {
    SHOULDER_UP,
    SHOULDER_DOWN,
    SHOULDER_STOP,
} shoulder_state_t;

typedef enum arm_state_ {
    ARM_PICKUP,
    ARM_EXTENDED,
    ARM_RETRACTED,
    ARM_GOAL,
} arm_state_t;

typedef enum dock_state_ {
    DOCK_FINGER_UP,
    DOCK_FINGER_DOWN,
} dock_state_t;

typedef enum joystick_event_ {
    RIGHT_TRIGGER_UP = 6,
    RIGHT_TRIGGER_DOWN = 8,
    LEFT_TRIGGER_UP = 5,
    LEFT_TRIGGER_DOWN = 7,
    BUTTON_ONE = 1,
    BUTTON_TWO = 2,
    BUTTON_THREE = 3,
    BUTTON_FOUR = 4,
} joystick_event_t;

brush_state_t brush_state;
arm_state_t arm_state;
shoulder_state_t shoulder_state;
dock_state_t dock_state;
center_dispenser_state_t center_dispenser_state;
door_state_t door_state;

bool debounce;
bool deadman_ltd_running;
bool deadman_rtd_running;
bool deadman_rtu_running;

void shoulder_enter_state(shoulder_state_t state);
void brush_enter_state(brush_state_t state);

task debounceTask()
{
    debounce = true;
    wait1Msec(500);
    debounce = false;
}

task deadman_ltd()
{
    if (!deadman_ltd_running) {
	    deadman_ltd_running = true;
	    shoulder_enter_state(SHOULDER_DOWN);

	    while (joy2Btn(Btn7)) {
	    }

	    shoulder_enter_state(SHOULDER_STOP);
	    deadman_ltd_running = false;
    }
}

task deadman_rtu()
{
    if (!deadman_rtu_running) {
        deadman_rtu_running = true;
        brush_enter_state(BRUSH_BACKWARD);

        while (joy2Btn(Btn6)) {
            // Assigns brush button.
        }

        brush_enter_state(BRUSH_OFF);
        deadman_rtu_running = false;
    }
}

task deadman_rtd()
{
    if (!deadman_rtd_running) {
        deadman_rtd_running = true;
        brush_enter_state(BRUSH_FORWARD);

        while (joy2Btn(Btn8)) {
            // Assigns brush button.
        }

        brush_enter_state(BRUSH_OFF);
        deadman_rtd_running = false;
    }
}

bool limit_shoulder_running;

task limit_shoulder()
{
    if (is_limit_switch_open(0x05)) {
        nMotorEncoder[shoulder] = 0;
        shoulder_enter_state(SHOULDER_UP);

        wait1Msec(500);

		while (is_limit_switch_open(0x05) && !any_trigger_pressed()) {
			if (nMotorEncoder[shoulder] > 2500) {
				motor[shoulder] = 10;
            }
        }
    }
    shoulder_enter_state(SHOULDER_STOP);
    wait1Msec(500);
    limit_shoulder_running = false;
}

void all_stop()
{
    motor[driveFrontLeft] = 0;
    motor[driveRearLeft] = 0;
    motor[driveFrontRight] = 0;
    motor[driveRearRight] = 0;
}

void dock_enter_state(dock_state_t state)
{
    dock_state = state;

    switch (state) {
    case DOCK_FINGER_UP:
        servo[finger] = SERVO_FINGER_UP;
        break;
    case DOCK_FINGER_DOWN:
        servo[finger] = SERVO_FINGER_DOWN;
        break;
    }
}

void door_enter_state(door_state_t state)
{
    door_state = state;

    switch (door_state) {
        case DOOR_OPEN:
            servo[door] = SERVO_DOOR_OPEN;
            break;
        case DOOR_CLOSED:
            servo[door] = SERVO_DOOR_CLOSED;
            break;
        case DOOR_CENTERGOAL_RAMP:
            servo[door] = SERVO_DOOR_CENTERGOAL_RAMP;
            break;
    }
}

void handle_tophat_up()
{
    switch (door_state) {
    case DOOR_CLOSED:
        door_enter_state(DOOR_CENTERGOAL_RAMP);
        break;
    case DOOR_OPEN:
        break;
    case DOOR_CENTERGOAL_RAMP:
        door_enter_state(DOOR_OPEN);
        break;
    }
    startTask(debounceTask);
}

void handle_tophat_down()
{
    switch (door_state) {
    case DOOR_CLOSED:
        break;
    case DOOR_CENTERGOAL_RAMP:
        door_enter_state(DOOR_CLOSED);
        break;
    case DOOR_OPEN:
        door_enter_state(DOOR_CENTERGOAL_RAMP);
        break;
    }
    startTask(debounceTask);
}

void brush_enter_state(brush_state_t state)
{
    brush_state = state;

    switch (state) {
    case BRUSH_OFF:
        servo[brush] = 127;
        break;
    case BRUSH_FORWARD:
        servo[brush] = 5;
        break;
    case BRUSH_BACKWARD:
        servo[brush] = 250;
        break;
    }
}

void arm_enter_state(arm_state_t state)
{
	nMotorEncoder[arm_motor] = 0;

    switch (state) {
    case ARM_PICKUP:
        nxtDisplayCenteredBigTextLine(3, "PICKUP");
        if (arm_state == ARM_GOAL) {
            move_to(arm_motor, ARM_MOTOR_SPEED, ENC_ARM_Y);
        } else if (arm_state == ARM_RETRACTED) {
            move_to(arm_motor, -ARM_MOTOR_SPEED, ENC_ARM_X);
        } else {
			motor[arm_motor] = 0;
    	}
        break;
    case ARM_EXTENDED:
        nxtDisplayCenteredBigTextLine(3, "EXTENDED");
        if (arm_state == ARM_GOAL) {
        	move_to(arm_motor, -ARM_MOTOR_SPEED, ENC_ARM_Z);
        } else {
        	motor[arm_motor] = 0;
    	}
        break;
    case ARM_GOAL:
        nxtDisplayCenteredBigTextLine(3, "GOAL");
        if (arm_state == ARM_EXTENDED) {
	        move_to(arm_motor, ARM_MOTOR_SPEED, ENC_ARM_Z);
	    } else if (arm_state == ARM_PICKUP) {
			move_to(arm_motor, -ARM_MOTOR_SPEED, ENC_ARM_Y);
	    } else {
			motor[arm_motor] = 0;
	    }
        break;
    case ARM_RETRACTED:
        if (arm_state == ARM_PICKUP) {
			nxtDisplayCenteredBigTextLine(3, "RET Pick");
        	move_to(arm_motor, ARM_MOTOR_SPEED, ENC_ARM_X);
        } else if (arm_state == ARM_RETRACTED) {
			nxtDisplayCenteredBigTextLine(3, "RET RET");
        	move_to(arm_motor, ARM_MOTOR_SPEED, 25100);
    	} else {
    		motor[arm_motor] = 0;
        }
        break;
    }

    arm_state = state;
}

void shoulder_enter_state(shoulder_state_t state)
{
    shoulder_state = state;

    switch (state) {
    case SHOULDER_DOWN:
        motor[shoulder] = -SHOULDER_POWER;
        break;
    case SHOULDER_UP:
        motor[shoulder] = SHOULDER_POWER;
        break;
    case SHOULDER_STOP:
        motor[shoulder] = 0;
        break;
    }
}

void handle_joy2_ltu()
{
	nxtDisplayCenteredBigTextLine(4, "Joy1 LTU");

    if (!limit_shoulder_running) {
        limit_shoulder_running = true;
        /*
         * If the driver is moving when LTU is pressed
         * then the motors get locked 'on'.  Ensure that
         * they are off here.
         */
        motor[driveFrontLeft]  = 0;
        motor[driveRearLeft]   = 0;
        motor[driveFrontRight] = 0;
        motor[driveRearRight]  = 0;
	    startTask(limit_shoulder);
    }
}

void handle_joy2_ltd()
{
    //center_dispenser_enter_state(CENTER_DISPENSER_STOWED);

    if (!deadman_ltd_running) {
        startTask(deadman_ltd);
    }
}

void handle_joy2_rtd()
{
    if (!deadman_rtd_running) {
        startTask(deadman_rtd);
    }
}

void handle_joy2_rtu()
{
    if (!deadman_rtu_running) {
        startTask(deadman_rtu);
    }
}

void handle_joy2_btn2()
{
    switch (arm_state) {
    case ARM_PICKUP:
        arm_enter_state(ARM_RETRACTED);
        break;
    case ARM_EXTENDED:
        arm_enter_state(ARM_GOAL);
        break;
    case ARM_RETRACTED:
		arm_enter_state(ARM_RETRACTED);
        break;
    case ARM_GOAL:
        arm_enter_state(ARM_PICKUP);
        break;
    }
}

void handle_joy2_btn4()
{
    switch (arm_state) {
    case ARM_PICKUP:
        arm_enter_state(ARM_GOAL);
        break;
    case ARM_EXTENDED:
        break;
    case ARM_RETRACTED:
        arm_enter_state(ARM_PICKUP);
        break;
    case ARM_GOAL:
        arm_enter_state(ARM_EXTENDED);
        break;
    }
}

void handle_joy2_btn1()
{
    switch (dock_state) {
    case DOCK_FINGER_UP:
        break;
    case DOCK_FINGER_DOWN:
        dock_enter_state(DOCK_FINGER_UP);
        break;
    }
}

void handle_joy2_btn3()
{
    switch (dock_state) {
    case DOCK_FINGER_UP:
        dock_enter_state(DOCK_FINGER_DOWN);
        break;
    case DOCK_FINGER_DOWN:
        break;
    }
}

bool center_goal_task_running = false;

task center_goal()
{
	center_goal_task_running = true;
    disableDiagnosticsDisplay();
    eraseDisplay();
    playImmediateTone(60, 100);
    servo[leftEye] = LSERVO_CENTER + CROSSEYED;
    servo[rightEye] = RSERVO_CENTER - CROSSEYED;
    raise_the_monster();
    score_center_goal(CENTER_GOAL_DUMP_DISTANCE);
    center_goal_task_running = false;
}

void handle_joy1_event(joystick_event_t event)
{
    switch (event) {
    case BUTTON_ONE:
		// useable
        break;
    case BUTTON_TWO:
        stopTask(center_goal);
        allMotorsOff();
        center_goal_task_running = false;
        break;
    case BUTTON_THREE:
   		stopTask(limit_shoulder);
		shoulder_enter_state(SHOULDER_STOP);
		limit_shoulder_running = false;
        break;
    case BUTTON_FOUR:
        startTask(center_goal);
        break;
    case RIGHT_TRIGGER_UP:
    	move_to(arm_motor, ARM_MOTOR_SPEED, 251000);
    default:
    }

    startTask(debounceTask);
}

void handle_joy2_event(joystick_event_t event)
{
    switch (event) {
    case BUTTON_ONE:
        handle_joy2_btn1();
        break;
    case BUTTON_THREE:
        handle_joy2_btn3();
        break;
    case BUTTON_TWO:
        handle_joy2_btn2();
        break;
    case BUTTON_FOUR:
        handle_joy2_btn4();
        break;
    case LEFT_TRIGGER_UP:
        handle_joy2_ltu();
        break;
    case LEFT_TRIGGER_DOWN:
        handle_joy2_ltd();
        break;
    case RIGHT_TRIGGER_UP:
        handle_joy2_rtu();
        break;
    case RIGHT_TRIGGER_DOWN:
        handle_joy2_rtd();
        break;
    }

    startTask(debounceTask);
}

void initialize_robot()
{
	if (!limit_switch_init(HTPB, 0x05)) {
		nxtDisplayCenteredBigTextLine(3, "ERROR");
		nxtDisplayTextLine(6, "CF251, LP: IZZIE");
	}

    disableDiagnosticsDisplay();
    eraseDisplay();

    nMotorEncoder[shoulder] = 0;

    limit_shoulder_running = false;

    /*
     * Ensure that the arm will go to the origin from whereever
     * it may be.  Set the state to retracted, and then call
     * arm_enter_state() and it will ensure that the motor retracts
     * to the stall point.
     */
    arm_state = ARM_RETRACTED;
    arm_enter_state(ARM_RETRACTED);
    nxtDisplayCenteredBigTextLine(3, "RETRACTED");

    shoulder_enter_state(SHOULDER_STOP);
    brush_enter_state(BRUSH_OFF);
    dock_enter_state(DOCK_FINGER_UP);
    door_enter_state(DOOR_CLOSED);

    deadman_ltd_running = false;
    deadman_rtd_running = false;
    deadman_rtu_running = false;

    servo[dockarm] = SERVO_DOCK_ARM_STOPPED;
    servo[rightEye] = 128;
    servo[leftEye] = 128;
    servo[fist] = 25;

    all_stop();

    return;
}

task main()
{
    short right_y;
    short left_y;
    short left_dock_y;

    debounce = false;

    initialize_robot();

    waitForStart();   // wait for start of tele-op phase

    // StartTask(endGameTimer);

    while (true) {

        getJoystickSettings(joystick);

        if (!debounce) {
 	        if (joy2Btn(Btn1)) {
	            handle_joy2_event(BUTTON_ONE);
 	        } else if (joy1Btn(Btn1)) {
	            handle_joy1_event(BUTTON_ONE);
	        } else if (joy2Btn(Btn2)) {
	            handle_joy2_event(BUTTON_TWO);
	        } else if (joy1Btn(Btn2)) {
	            handle_joy1_event(BUTTON_TWO);
	        } else if (joy2Btn(Btn3)) {
	            handle_joy2_event(BUTTON_THREE);
	        } else if (joy1Btn(Btn3)) {
	            handle_joy1_event(BUTTON_THREE);
	        } else if (joy2Btn(Btn4)) {
	            handle_joy2_event(BUTTON_FOUR);
	        } else if (joy1Btn(Btn4)) {
	            handle_joy1_event(BUTTON_FOUR);
	        } else if (joy2Btn(Btn5)) {
	            handle_joy2_event(LEFT_TRIGGER_UP);
	        } else if (joy2Btn(Btn6)) {
	            handle_joy2_event(RIGHT_TRIGGER_UP);
	        } else if (joy2Btn(Btn7)) {
	            handle_joy2_event(LEFT_TRIGGER_DOWN);
            } else if (joy2Btn(Btn8)) {
	            handle_joy2_event(RIGHT_TRIGGER_DOWN);
	        } else if (joy1Btn(Btn6)) {
	        	handle_joy1_event(RIGHT_TRIGGER_UP);
	    	} else if (joystick.joy2_TopHat == 0) { // Up d-pad
                door_enter_state(DOOR_CLOSED);
            } else if (joystick.joy2_TopHat == 2) { // Left d-pad
                door_enter_state(DOOR_CENTERGOAL_RAMP);
            } else if (joystick.joy2_TopHat == 4) { // Down d-pad
                door_enter_state(DOOR_OPEN);
            }
        }

        /*
         * Lock out the drivetrain if we are doing autonomous center goal dispense
         */
        if ((!center_goal_task_running) && (!limit_shoulder_running)) {
	        left_dock_y = joystick.joy2_y1;
	        right_y = joystick.joy1_y1;
	        left_y = joystick.joy1_y2;

	        if (abs(left_y) > 20) {
		    	motor[driveFrontRight] = drive_multiplier * left_y;
		    	motor[driveRearRight] = drive_multiplier * left_y;
			}
			else {
			    motor[driveFrontRight] = 0;
			    motor[driveRearRight] = 0;
			}

	        if (abs(right_y) > 20) {
			    motor[driveFrontLeft] = drive_multiplier * right_y;
			    motor[driveRearLeft] = drive_multiplier * right_y;
			}
			else
			{
			    motor[driveFrontLeft] = 0;
			    motor[driveRearLeft] = 0;
			}
        }

        if (abs(left_dock_y) > 20) {
            if (left_dock_y > 0) {
                servo[dockarm] = SERVO_DOCK_ARM_FORWARD;
            } else {
                servo[dockarm] = SERVO_DOCK_ARM_BACKWARD;
            }
        } else {
            servo[dockarm] = SERVO_DOCK_ARM_STOPPED;
        }
    }
}
