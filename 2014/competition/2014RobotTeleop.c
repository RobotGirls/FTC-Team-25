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
#include "../library/sensors/drivers/hitechnic-protoboard.h"
#include "../library/limitSwitch.h"
#include "../library/light_strip.h"

#define LEFT_HOPPER_UP LEFT_HOPPER_DOWN + ROTATION
#define RIGHT_HOPPER_UP RIGHT_HOPPER_DOWN - ROTATION

#define SERVO_FORWARD 0
#define SERVO_REVERSE 256
#define SERVO_STOPPED 127

#define MIDDLE_ELEV_UP   SERVO_FORWARD
#define MIDDLE_ELEV_DOWN SERVO_REVERSE
#define MIDDLE_ELEV_STOP SERVO_STOPPED

#define ELEVATOR_SPEED   90
#define OFF              0

#define BLOCK_SERVO_RETRACTED 6


typedef enum {
    NORTH,
    SOUTH,
} drive_state_t;

typedef enum {
    UP,
    DOWN,
    RIGHT_UP,
    RIGHT_DOWN,
    LEFT_UP,
    LEFT_DOWN,
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
    BLOCK_SCOOP_UP,
    BLOCK_SCOOP_DOWN,
    BLOCK_SCOOP_STOPPED,
} block_scoop_state_t;

typedef enum {
    FLAG_RAISE_ON,
    FLAG_RAISE_OFF,
} flag_raise_state_t;

typedef enum {
    SPOD_FORWARD,
    SPOD_REVERSE,
    SPOD_OFF,
} spod_state_t;

linear_state_t elevator_state;
conveyor_state_t conveyor_state;
block_scoop_state_t block_scoop_state;
drive_state_t drive_state;
flag_raise_state_t flag_raise_state;
spod_state_t spod_state;

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

void drive_enter_state(drive_state_t state);
void elev_enter_state(linear_state_t state);
void flag_raise_enter_state(flag_raise_state_t state);
void block_scoop_enter_state(block_scoop_state_t state);
void spod_enter_state(spod_state_t state);

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

task endGameTimer()
{
    int cnt;

    for (int i = 0; i < 90; i++) {
        wait1Msec(1000);
    }
    displayEndgame();

    for (int i = 0; i < 20; i++) {
        wait1Msec(1000);
    }
    displayCaution();

    for (int i = 0; i < 8; i++) {
        wait1Msec(1000);
    }
    displayRestingPulse();
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
    while (isLimitSwitchOpen())
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

    nMotorEncoder[rightElevator] = 0;
    elev_enter_state(STOPPED);
}

void all_stop()
{
    motor[driveFrontLeft] = 0;
    motor[driveRearLeft] = 0;
    motor[driveFrontRight] = 0;
    motor[driveRearRight] = 0;
    motor[conveyor] = 0;
}

void initializeRobot()
{
    elevator_state = STOPPED;
    conveyor_state = CONVEYOR_STOPPED;
    block_scoop_enter_state(BLOCK_SCOOP_STOPPED);
    spod_enter_state(SPOD_OFF);

    limitSwitchInit(5);
    lightStripInit(0x1F);

    drive_enter_state(NORTH);

    displayRestingPulse();

    debounce = false;

    nMotorEncoder[rightElevator] = 0;
	servo[blockDump] = BLOCK_SERVO_RETRACTED;

    all_stop();

    return;
}

void block_scoop_enter_state(block_scoop_state_t state)
{
    block_scoop_state = state;

    switch (block_scoop_state) {
    case BLOCK_SCOOP_UP:
        servo[middleElev] = MIDDLE_ELEV_UP;
        break;
    case BLOCK_SCOOP_STOPPED:
        servo[middleElev] = MIDDLE_ELEV_STOP;
        break;
    case BLOCK_SCOOP_DOWN:
        servo[middleElev] = MIDDLE_ELEV_DOWN;
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
        StartTask(elevatorSoftwareStop);
        break;
    case DOWN:
	    motor[leftElevator] = -ELEVATOR_SPEED;
	    motor[rightElevator] = ELEVATOR_SPEED;
        StartTask(waitForElevatorDown);
        break;
    case STOPPED:
	    motor[leftElevator] = OFF;
	    motor[rightElevator] = OFF;
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
    switch (block_scoop_state) {
    case BLOCK_SCOOP_UP:
        block_scoop_enter_state(BLOCK_SCOOP_STOPPED);
        break;
    case BLOCK_SCOOP_DOWN:
        block_scoop_enter_state(BLOCK_SCOOP_UP);
        break;
    case BLOCK_SCOOP_STOPPED:
        block_scoop_enter_state(BLOCK_SCOOP_UP);
        break;
    }
}

void handle_event_ltd()
{
    switch (block_scoop_state) {
    case BLOCK_SCOOP_UP:
        block_scoop_enter_state(BLOCK_SCOOP_DOWN);
        break;
    case BLOCK_SCOOP_DOWN:
        block_scoop_enter_state(BLOCK_SCOOP_STOPPED);
        break;
    case BLOCK_SCOOP_STOPPED:
        block_scoop_enter_state(BLOCK_SCOOP_DOWN);
        break;
    }
}

void spod_enter_state(spod_state_t state)
{
    spod_state = state;

    switch (spod_state) {
    case SPOD_FORWARD:
	    motor[leftspod] = -100;
	    motor[rightspod] = 100;
        break;
    case SPOD_REVERSE:
	    motor[leftspod] = 100;
	    motor[rightspod] = -100;
        break;
    case SPOD_OFF:
        motor[leftspod] = 0;
        motor[rightspod] = 0;
        break;
    }
}

void handle_event_btn1()
{
    switch (spod_state) {
    case SPOD_FORWARD:
        spod_enter_state(SPOD_REVERSE);
        break;
    case SPOD_REVERSE:
        spod_enter_state(SPOD_OFF);
        break;
    case SPOD_OFF:
        spod_enter_state(SPOD_REVERSE);
        break;
    }
}

void handle_event_btn4()
{
    switch (spod_state) {
    case SPOD_FORWARD:
        spod_enter_state(SPOD_OFF);
        break;
    case SPOD_REVERSE:
        spod_enter_state(SPOD_FORWARD);
        break;
    case SPOD_OFF:
        spod_enter_state(SPOD_FORWARD);
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
        displayForward();
        break;
    case SOUTH:
        drive_multiplier = -1;
        /*
         * TODO: Set south leds.
         */
        displayBackward();
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

void flag_raise_enter_state(flag_raise_state_t state)
{
    flag_raise_state = state;

    switch (flag_raise_state) {
    case FLAG_RAISE_ON:
        motor[flag] = 100;
        break;
    case FLAG_RAISE_OFF:
        motor[flag] = 0;
        break;
    }
}

void handle_joy1_btn2()
{
    switch (flag_raise_state) {
    case FLAG_RAISE_ON:
        flag_raise_enter_state(FLAG_RAISE_OFF);
        break;
    case FLAG_RAISE_OFF:
        flag_raise_enter_state(FLAG_RAISE_ON);
        break;
    }
}


void handle_joy1_event(joystick_event_t event)
{
    switch (event) {
    case BUTTON_ONE:
        handle_joy1_btn1();
        break;
    case BUTTON_FOUR:
        handle_joy1_btn2();
        break;
    }

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

    StartTask(endGameTimer);

    displayForward();

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
            } else if (joy1Btn(BUTTON_FOUR)) {
                handle_joy1_event(BUTTON_FOUR);
            }
        }

        //if (drive_multiplier) {
            //right_y = joystick.joy1_y2;
            //left_y = joystick.joy1_y1;
        //} else {
            right_y = joystick.joy1_y1;
            left_y = joystick.joy1_y2;
        //}

        if (abs(right_y) > 20) {
	    	motor[driveFrontRight] = drive_multiplier * right_y;
	    	motor[driveRearRight] = drive_multiplier * right_y;
		}
		else {
		    motor[driveFrontRight] = 0;
		    motor[driveRearRight] = 0;
		}

        if (abs(left_y) > 20) {
		    motor[driveFrontLeft] = drive_multiplier * left_y;
		    motor[driveRearLeft] = drive_multiplier * left_y;
		}
		else
		{
		    motor[driveFrontLeft] = 0;
		    motor[driveRearLeft] = 0;
		}
    }
}
