#pragma config(Hubs,  S1, HTServo,  HTMotor,  HTMotor,  HTMotor)
#pragma config(Hubs,  S2, HTServo,  none,     none,     none)
#pragma config(Sensor, S3,     irr_left,       sensorI2CCustom)
#pragma config(Sensor, S4,     irr_right,      sensorI2CCustom)
#pragma config(Motor,  mtr_S1_C2_1,     driveRearRight, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C2_2,     driveFrontRight, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C3_1,     shoulder,      tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C3_2,     motorG,        tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C4_1,     driveFrontLeft, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Motor,  mtr_S1_C4_2,     driveRearLeft, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Servo,  srvo_S1_C1_1,    finger,               tServoStandard)
#pragma config(Servo,  srvo_S1_C1_2,    brush,                tServoContinuousRotation)
#pragma config(Servo,  srvo_S1_C1_3,    arm,                  tServoStandard)
#pragma config(Servo,  srvo_S1_C1_4,    dockarm,              tServoContinuousRotation)
#pragma config(Servo,  srvo_S1_C1_5,    servo5,               tServoNone)
#pragma config(Servo,  srvo_S1_C1_6,    servo6,               tServoNone)
#pragma config(Servo,  srvo_S2_C1_1,    centerDispenser,      tServoStandard)
#pragma config(Servo,  srvo_S2_C1_2,    leftEye,              tServoStandard)
#pragma config(Servo,  srvo_S2_C1_3,    rightEye,             tServoStandard)
#pragma config(Servo,  srvo_S2_C1_4,    servo10,              tServoNone)
#pragma config(Servo,  srvo_S2_C1_5,    servo11,              tServoNone)
#pragma config(Servo,  srvo_S2_C1_6,    servo12,              tServoNone)
//*!!Code automatically generated by 'ROBOTC' configuration wizard               !!*//

#define LSERVO_CENTER 134
#define RSERVO_CENTER 113
#define RSERVO_PERP   235

#define FOUR_WHEEL_DRIVE

#include "../../lib/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../../lib/drivetrain_andymark_defs.h"
#include "../../lib/drivetrain_square.h"
#include "../../lib/dead_reckon.h"
#include "../../lib/data_log.h"
#include "../../lib/ir_utils.h"

ir_direction_t dir;

int count;

void move_to_position(int position)
{
	init_path();

	switch (position) {
	case 1:
        nxtDisplayTextLine(4, "position 1");
       // add_segment(52.5, 0, 50);  //move to position 1
        add_segment(20, -90, 40);
        break;
    case 2:
        servo[rightEye] = RSERVO_PERP;
        nxtDisplayTextLine(4, "position 2");
        add_segment(30, -33, 40);   //move to position 2
        break;
    case 3:
        nxtDisplayTextLine(4, "position 3");
        add_segment(33, 45, 40);  //move to position 3
        break;
    }
    stop_path();
  	dead_reckon();
}

void move_to_pole(int count)
{
    init_path();

    switch (count) {
    case 1:
        add_segment(60, -45, 50);   //move to pole position 1
      	add_segment(28, 135, 50);
        add_segment(0, -90, 50);    // hit pole
        break;
    case 2:
        add_segment(32, 0, 50);   //move to pole position 2
        add_segment(30, 90, 50);
		add_segment(16, 90, 50);
	    add_segment(0, -90, 50);    // hit pole
        break;
    case 3:
        add_segment(36, 90, 50);    //move to pole position 3
      	//add_segment(15, 45, 50);
        //add_segment(0,90, 50);
        //add_segment(0, -90, 50) // hit pole
        break;
    }
    stop_path();
  	dead_reckon();
}

task main()
{
    int i;

    servo[leftEye] = LSERVO_CENTER;
    servo[rightEye] = RSERVO_CENTER;
    count = 0;

    for (i = 1; i < 4; i++) {
        move_to_position(i);
        dir = get_dir_to_beacon(irr_right);

        if (dir == DIR_CENTER){
            count = i;
            break;
        }
    }

    count = 3;

    for (i = 0; i < count; i++) {
        playImmediateTone(251, 50);
        wait1Msec(1000);
    }

    servo[rightEye] = RSERVO_CENTER;

    move_to_pole(count);
}
