#pragma config(Hubs,  S1, HTMotor,  HTMotor,  HTMotor,  HTServo)
#pragma config(Hubs,  S2, HTServo,  none,     none,     none)
#pragma config(Sensor, S1,     ,               sensorI2CMuxController)
#pragma config(Sensor, S2,     ,               sensorI2CMuxController)
#pragma config(Sensor, S3,     HTSMUX,         sensorI2CCustom)
#pragma config(Motor,  motorA,          rampRight,     tmotorNXT, PIDControl, reversed)
#pragma config(Motor,  motorB,          rampLeft,      tmotorNXT, PIDControl, encoder)
#pragma config(Motor,  motorC,           ,             tmotorNXT, openLoop)
#pragma config(Motor,  mtr_S1_C1_1,     driveFrontLeft, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C1_2,     driveRearLeft, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C2_1,     elbow,         tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C2_2,     conveyor,      tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C3_1,     driveFrontRight, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Motor,  mtr_S1_C3_2,     driveRearRight, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Servo,  srvo_S1_C4_1,    leftEye,              tServoStandard)
#pragma config(Servo,  srvo_S1_C4_2,    servo2,               tServoNone)
#pragma config(Servo,  srvo_S1_C4_3,    rightEye,             tServoStandard)
#pragma config(Servo,  srvo_S1_C4_4,    servo4,               tServoNone)
#pragma config(Servo,  srvo_S1_C4_5,    roller,               tServoStandard)
#pragma config(Servo,  srvo_S1_C4_6,    servo6,               tServoNone)
#pragma config(Servo,  srvo_S2_C1_1,    autoElbow,            tServoStandard)
#pragma config(Servo,  srvo_S2_C1_2,    servo8,               tServoNone)
#pragma config(Servo,  srvo_S2_C1_3,    autoThumb,            tServoStandard)
#pragma config(Servo,  srvo_S2_C1_4,    servo10,              tServoNone)
#pragma config(Servo,  srvo_S2_C1_5,    servo11,              tServoNone)
#pragma config(Servo,  srvo_S2_C1_6,    servo12,              tServoNone)

#include "./../lib/sensors/drivers/hitechnic-sensormux.h"
#include "./../lib/sensors/drivers/lego-ultrasound.h"

// The sensor is connected to the first port
// of the SMUX which is connected to the NXT port S1.
// To access that sensor, we must use msensor_S1_1.  If the sensor
// were connected to 3rd port of the SMUX connected to the NXT port S4,
// we would use msensor_S4_3

const tMUXSensor LEGOUS = msensor_S3_2;

#define SERVO_ROLLER_UP             25
#define SERVO_ROLLER_OSC_DOWN       80

task validate_conveyor()
{
    while (true) {
        motor[conveyor] = 0;
        wait1Msec(100);
        motor[conveyor] = 80;
        wait1Msec(10000);
    }
}

task servo_pos()
{
    int pos = SERVO_ROLLER_UP;

    while (true) {
        if (nNxtButtonPressed == 1) {
            pos = pos + 5;
            servo[roller] = pos;
        } else if (nNxtButtonPressed == 2) {
            pos = pos - 5;
            servo[roller] = pos;
        }
        nxtDisplayCenteredBigTextLine(5, "%d", pos);
        wait1Msec(500);
    }
}

task ball_watch()
{
    int dist;
    int i;
    int ball_count;

    ball_count = 0;
    nxtDisplayCenteredBigTextLine(2, "%d", ball_count);

    while (true) {
        dist = USreadDist(LEGOUS);
        if (dist <= 7) {
            // eraseDisplay();
            nxtDisplayCenteredBigTextLine(2, "%d", ++ball_count);

            wait1Msec(2000);

            playImmediateTone(60, 100);

	        for (i = SERVO_ROLLER_UP; i <= SERVO_ROLLER_OSC_DOWN; i++) {
	            servo[roller] = i;
	            wait1Msec(10);
	        }
            servo[roller] = SERVO_ROLLER_UP;

            wait1Msec(1000);
        }
    }
}

task main()
{
    eraseDisplay();

    servo[roller] = SERVO_ROLLER_UP;

    startTask(ball_watch);
    startTask(servo_pos);
    startTask(validate_conveyor);

    while (true) {}
}
