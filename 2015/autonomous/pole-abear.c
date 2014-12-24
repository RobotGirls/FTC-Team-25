#pragma config(Hubs,  S1, HTServo,  HTMotor,  HTMotor,  HTMotor)
#pragma config(Hubs,  S2, HTServo,  none,     none,     none)
#pragma config(Sensor, S2,     carrot,         sensorNone)
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

#define FOUR_WHEEL_DRIVE

#define LSERVO_CENTER 134
#define RSERVO_CENTER 113
#define RSERVO_PERP   235

#include "../../lib/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../../lib/baemax_drivetrain_defs.h"
#include "../../lib/drivetrain_square.h"
#include "../../lib/dead_reckon.h"
#include "../../lib/data_log.h"
#include "../../lib/ir_utils.h"

ir_direction_t dir;

int count;
int beep;

void move_to_pole(int count)
{
    init_path();

    switch (count) {
    case 1:
        add_segment(-26, -15, 45);
        add_segment(7.5, -105, 80);
        break;
    case 2:
        add_segment(-22, 0, 45);
        add_segment(7.5, 135, 50);
        break;
    case 3:
        add_segment(-27, -27.5, 45);
        add_segment(13.5, 202, 50);
        break;
    }
    stop_path();
  	dead_reckon();
}

task main()
{
    int i;

    initialize_receiver(irr_left, irr_right);

    servo[leftEye] = LSERVO_CENTER;
    servo[rightEye] = RSERVO_CENTER;

    init_path();
    add_segment(-24, 0, 50);
    stop_path();
    dead_reckon();

    if (SensorValue[carrot] < 60) {
        beep = 3;
        move_to_pole(3);
    }
    else if (SensorValue[carrot] > 200) {
        beep = 2;
        move_to_pole(2);
    }
    else if (SensorValue[carrot] < 80) {
        beep = 1;
        move_to_pole(1);
    }

    for (i = 0; i < beep; i++) {
        playImmediateTone(251, 50);
        wait1Msec(1000);
    }
}
