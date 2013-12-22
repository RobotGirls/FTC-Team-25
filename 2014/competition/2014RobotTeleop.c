/////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                           Tele-Operation Mode Code Template
//
// This file contains a template for simplified creation of an tele-op program for an FTC
// competition.
//
// You need to customize two functions with code unique to your specific robot.
//
/////////////////////////////////////////////////////////////////////////////////////////////////////

#include "JoystickDriver.c"  //Include file to "handle" the Bluetooth messages.

#define LEFT_HOPPER_UP LEFT_HOPPER_DOWN + ROTATION
#define RIGHT_HOPPER_UP RIGHT_HOPPER_DOWN - ROTATION

#define CONVEYOR_SPEED_1 10
#define CONVEYOR_SPEED_2 40
#define CONVEYOR_SPEED_3 60
#define CONVEYOR_SPEED_4 80
#define CONVEYOR_SPEED_5 100
#define ELEVATOR_SPEED   70
#define OFF              0

#define SERVO_FORWARD 0
#define SERVO_REVERSE 256
#define SERVO_STOPPED 127

typedef enum {
    NORTH,
    SOUTH,
} drive_state_t;

typedef enum {
    UP,
    DOWN,
    STOPPED,
} linear_state_t;

typedef enum {
    UP_SPEED_1,
    UP_SPEED_2,
    UP_SPEED_3,
    UP_SPEED_4,
    UP_SPEED_5,
    CONVEYOR_DOWN,
    CONVEYOR_STOPPED,
} conveyor_state_t;

typedef enum {
    WHEEL_ON,
    WHEEL_OFF,
} wheel_state_t;

typedef enum {
    HOPPER_DOWN,
    HOPPER_UP,
} hopper_state_t;

linear_state_t elevator_state;
conveyor_state_t conveyor_state;
wheel_state_t wheel_state;
hopper_state_t hopper_state;
drive_state_t drive_state;

int drive_multiplier;

typedef enum {
    RIGHT_TRIGGER_UP = 6,
    RIGHT_TRIGGER_DOWN = 8,
    LEFT_TRIGGER_UP = 5,
    LEFT_TRIGGER_DOWN = 7,
    BUTTON_ONE = 1,
    BUTTON_FOUR = 4,
} joystick_event_t;

bool debounce;

void wheel_enter_state(wheel_state_t state);
void drive_enter_state(drive_state_t state);
void elev_enter_state(linear_state_t state);
void conv_enter_state(conveyor_state_t state);

/////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    initializeRobot
//
// Prior to the start of tele-op mode, you may want to perform some initialization on your robot
// and the variables within your program.
//
// In most cases, you may not have to add any code to this function and it will remain "empty".
//
/////////////////////////////////////////////////////////////////////////////////////////////////////

task debounceTask()
{
    debounce = true;
    wait1Msec(500);
    debounce = false;
}

task moveWaterWheel()
{
    motor[waterWheel] = 20;
    wait1Msec(400);
    motor[waterWheel] = 0;
    wheel_enter_state(WHEEL_OFF);
}

task elevatorSoftwareStop()
{
    long val;

    val = nMotorEncoder[rightElevator];
    while (val > ELEV_ENCODER_UP_VAL) {
        val = nMotorEncoder[rightElevator];
    }

    elev_enter_state(STOPPED);
}

/*
 * Automatically stops the elevator when the touch
 * sensor is pressed.
 */
task waitForElevatorDown()
{
    while (!SensorValue[elevTouch])
    {
        /*
         * If the operator manually stops prior to engaging
         * the touch sensor, then break out of the loop so
         * that we stop the task.
         */
        if (elevator_state == STOPPED) {
            break;
        }
        wait1Msec(5);
    }

    if (SensorValue[elevTouch]) {
        nMotorEncoder[rightElevator] = 0;
    }

    elev_enter_state(STOPPED);
}

void wheel_enter_state(wheel_state_t state)
{
    wheel_state = state;

    switch (wheel_state) {
    case WHEEL_ON:
        StartTask(moveWaterWheel);
        break;
    case WHEEL_OFF:
        break;
    }
}

void all_stop()
{
    motor[driveLeft] = 0;
    motor[driveRight] = 0;
    motor[conveyor] = 0;
}

void initializeRobot()
{
    elevator_state = STOPPED;
    conveyor_state = CONVEYOR_STOPPED;
    wheel_state = WHEEL_OFF;
    hopper_state = HOPPER_DOWN;

    drive_enter_state(NORTH);

    debounce = false;

    servo[left] = SERVO_STOPPED;
    servo[right] = SERVO_STOPPED;

    servo[leftHopper] = LEFT_HOPPER_DOWN;
    servo[rightHopper] = RIGHT_HOPPER_DOWN;

    motor[waterWheel] = 0;

    nMotorEncoder[rightElevator] = 0;

    all_stop();

    return;
}

void hopper_enter_state(hopper_state_t state)
{
    hopper_state = state;

    switch (hopper_state) {
    case HOPPER_UP:
        servo[leftHopper] = LEFT_HOPPER_UP;
        servo[rightHopper] = RIGHT_HOPPER_UP;
        break;
    case HOPPER_DOWN:
        servo[leftHopper] = LEFT_HOPPER_DOWN;
        servo[rightHopper] = RIGHT_HOPPER_DOWN;
        break;
    }
}

void elev_enter_state(linear_state_t state)
{
    elevator_state = state;

    switch (elevator_state) {
    case UP:
	    motor[leftElevator] = ELEVATOR_SPEED;
	    motor[rightElevator] = -ELEVATOR_SPEED;
        conv_enter_state(CONVEYOR_STOPPED);
        StartTask(elevatorSoftwareStop);
        break;
    case DOWN:
	    motor[leftElevator] = -ELEVATOR_SPEED;
	    motor[rightElevator] = ELEVATOR_SPEED;
        StartTask(waitForElevatorDown);
        break;
/*
    case RIGHT_UP:
	    motor[rightElevator] = -ELEVATOR_SPEED;
        break;
    case RIGHT_DOWN:
	    motor[rightElevator] = ELEVATOR_SPEED;
        break;
    case LEFT_UP:
	    motor[leftElevator] = ELEVATOR_SPEED;
        break;
    case LEFT_DOWN:
	    motor[leftElevator] = -ELEVATOR_SPEED;
        break;
*/
    case STOPPED:
	    motor[leftElevator] = OFF;
	    motor[rightElevator] = OFF;
        break;
    }
}

void conv_enter_state(conveyor_state_t state)
{
    conveyor_state = state;

    switch (conveyor_state) {
    case UP_SPEED_1:
	    motor[conveyor] = CONVEYOR_SPEED_1;
        servo[left] = SERVO_REVERSE;
        servo[right] = SERVO_FORWARD;
        break;
    case UP_SPEED_2:
	    motor[conveyor] = CONVEYOR_SPEED_2;
        servo[left] = SERVO_REVERSE;
        servo[right] = SERVO_FORWARD;
        break;
    case UP_SPEED_3:
	    motor[conveyor] = CONVEYOR_SPEED_3;
        servo[left] = SERVO_REVERSE;
        servo[right] = SERVO_FORWARD;
        break;
    case UP_SPEED_4:
	    motor[conveyor] = CONVEYOR_SPEED_4;
        servo[left] = SERVO_REVERSE;
        servo[right] = SERVO_FORWARD;
        break;
    case UP_SPEED_5:
	    motor[conveyor] = CONVEYOR_SPEED_5;
        servo[left] = SERVO_REVERSE;
        servo[right] = SERVO_FORWARD;
        break;
    case CONVEYOR_DOWN:
	    motor[conveyor] = -CONVEYOR_SPEED_1;
        servo[left] = SERVO_STOPPED;
        servo[right] = SERVO_STOPPED;
        break;
    case CONVEYOR_STOPPED:
	    motor[conveyor] = OFF;
        servo[left] = SERVO_STOPPED;
        servo[right] = SERVO_STOPPED;
        break;
    }
}

void handle_event_rtu()
{
    switch (elevator_state) {
    case STOPPED:
        elev_enter_state(UP);
        break;
    case UP:
        elev_enter_state(STOPPED);
        break;
    case DOWN:
        elev_enter_state(UP);
        break;
    }
}

void handle_event_rtd()
{
    switch (elevator_state) {
    case STOPPED:
        elev_enter_state(DOWN);
        break;
    case UP:
        elev_enter_state(DOWN);
        break;
    case DOWN:
        elev_enter_state(STOPPED);
        break;
    }
}

void handle_event_ltu()
{
    switch (conveyor_state) {
    case CONVEYOR_STOPPED:
        conv_enter_state(UP_SPEED_1);
        break;
    case UP_SPEED_1:
        conv_enter_state(UP_SPEED_2);
        break;
    case UP_SPEED_2:
        conv_enter_state(UP_SPEED_3);
        break;
    case UP_SPEED_4:
        conv_enter_state(UP_SPEED_5);
        break;
    case UP_SPEED_5:
        conv_enter_state(UP_SPEED_1);
        break;
    case CONVEYOR_DOWN:
        conv_enter_state(CONVEYOR_STOPPED);
        break;
    }
}

void handle_event_ltd()
{
    switch (conveyor_state) {
    case CONVEYOR_STOPPED:
        conv_enter_state(CONVEYOR_DOWN);
        break;
    case UP_SPEED_1:
    case UP_SPEED_2:
    case UP_SPEED_3:
    case UP_SPEED_4:
    case UP_SPEED_5:
        conv_enter_state(CONVEYOR_STOPPED);
        break;
    case CONVEYOR_DOWN:
        conv_enter_state(CONVEYOR_STOPPED);
        break;
    }
}

void handle_event_btn1()
{
    wheel_enter_state(WHEEL_ON);
}

void handle_event_joy1_ltd()
{
    motor[rightElevator] = 0;
    motor[leftElevator] = -ELEVATOR_SPEED;
}

void handle_event_joy1_rtd()
{
    motor[leftElevator] = 0;
    motor[rightElevator] = ELEVATOR_SPEED;
}

void handle_event_joy1_ltu()
{
    motor[rightElevator] = 0;
    motor[leftElevator] = ELEVATOR_SPEED;
}

void handle_event_joy1_rtu()
{
    motor[leftElevator] = 0;
    motor[rightElevator] = -ELEVATOR_SPEED;
}

void handle_event_btn4()
{
    switch (hopper_state) {
    case HOPPER_DOWN:
        hopper_enter_state(HOPPER_UP);
        break;
    case HOPPER_UP:
        hopper_enter_state(HOPPER_DOWN);
        break;
    }
}

void handle_event(joystick_event_t event)
{
    switch (event) {
    case RIGHT_TRIGGER_UP:
        handle_event_rtu();
        break;
    case RIGHT_TRIGGER_DOWN:
        handle_event_rtd();
        break;
    case LEFT_TRIGGER_UP:
        handle_event_ltu();
        break;
    case LEFT_TRIGGER_DOWN:
        handle_event_ltd();
        break;
    case BUTTON_ONE:
        handle_event_btn1();
        break;
    case BUTTON_FOUR:
        handle_event_btn4();
        break;
    }

    StartTask(debounceTask);
}

void drive_enter_state(drive_state_t state)
{
    drive_state = state;

    switch (drive_state) {
    case NORTH:
        drive_multiplier = 1;
        /*
         * TODO: Set north leds.
         */
        break;
    case SOUTH:
        drive_multiplier = -1;
        /*
         * TODO: Set south leds.
         */
        break;
    }
}

void handle_joy1_btn1()
{
    switch (drive_state) {
    case NORTH:
        drive_enter_state(SOUTH);
        break;
    case SOUTH:
        drive_enter_state(NORTH);
        break;
    }
}

void handle_joy1_event(joystick_event_t event)
{
    switch (event) {
    case BUTTON_ONE:
        handle_joy1_btn1();
        break;
    }
/*
    case LEFT_TRIGGER_DOWN:
        handle_joy1_ltd();
        break;
    }
    case RIGHT_TRIGGER_DOWN:
        handle_joy1_rtd();
        break;
    }
    case LEFT_TRIGGER_UP:
        handle_joy1_ltu();
        break;
    }
    case RIGHT_TRIGGER_UP:
        handle_joy1_rtu();
        break;
    }
*/
    StartTask(debounceTask);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                         Main Task
//
// The following is the main code for the tele-op robot operation. Customize as appropriate for
// your specific robot.
//
// Game controller / joystick information is sent periodically (about every 50 milliseconds) from
// the FMS (Field Management System) to the robot. Most tele-op programs will follow the following
// logic:
//   1. Loop forever repeating the following actions:
//   2. Get the latest game controller / joystick settings that have been received from the PC.
//   3. Perform appropriate actions based on the joystick + buttons settings. This is usually a
//      simple action:
//      *  Joystick values are usually directly translated into power levels for a motor or
//         position of a servo.
//      *  Buttons are usually used to start/stop a motor or cause a servo to move to a specific
//         position.
//   4. Repeat the loop.
//
// Your program needs to continuously loop because you need to continuously respond to changes in
// the game controller settings.
//
// At the end of the tele-op period, the FMS will autonmatically abort (stop) execution of the program.
//
/////////////////////////////////////////////////////////////////////////////////////////////////////

task main()
{
    short right_y;
    short left_y;

    initializeRobot();

    waitForStart();   // wait for start of tele-op phase

    while (true)
    {
        getJoystickSettings(joystick);

        if (!debounce) {
	        if (joy2Btn(RIGHT_TRIGGER_UP)) {
	            handle_event(RIGHT_TRIGGER_UP);
	        } else if (joy2Btn(RIGHT_TRIGGER_DOWN)) {
	            handle_event(RIGHT_TRIGGER_DOWN);
	        } else if (joy2Btn(LEFT_TRIGGER_UP)) {
	            handle_event(LEFT_TRIGGER_UP);
	        } else if (joy2Btn(LEFT_TRIGGER_DOWN)) {
	            handle_event(LEFT_TRIGGER_DOWN);
	        } else if (joy2Btn(BUTTON_ONE)) {
                handle_event(BUTTON_ONE);
            } else if (joy2Btn(BUTTON_FOUR)) {
                handle_event(BUTTON_FOUR);
            } else if (joy1Btn(BUTTON_ONE)) {
                handle_joy1_event(BUTTON_ONE);
	        } else if (joy1Btn(LEFT_TRIGGER_DOWN)) {
	            handle_joy1_event(LEFT_TRIGGER_DOWN);
	        } else if (joy1Btn(RIGHT_TRIGGER_DOWN)) {
	            handle_joy1_event(RIGHT_TRIGGER_DOWN);
	        } else if (joy1Btn(LEFT_TRIGGER_UP)) {
	            handle_joy1_event(LEFT_TRIGGER_UP);
	        } else if (joy1Btn(RIGHT_TRIGGER_UP)) {
	            handle_joy1_event(RIGHT_TRIGGER_UP);
            }
        }

        if (drive_multiplier) {
            right_y = joystick.joy1_y2;
            left_y = joystick.joy1_y1;
        } else {
            right_y = joystick.joy1_y1;
            left_y = joystick.joy1_y2;
        }

        if (abs(right_y) > 20) {
	    	motor[driveRight] = drive_multiplier * right_y;
		}
		else {
		    motor[driveRight] = 0;
		}

        if (abs(left_y) > 20) {
		    motor[driveLeft] = drive_multiplier * left_y;
		}
		else
		{
		    motor[driveLeft] = 0;
		}
    }
}
