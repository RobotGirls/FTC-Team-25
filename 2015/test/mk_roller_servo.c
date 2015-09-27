#pragma config(Hubs,  S1, HTMotor,  HTMotor,  HTMotor,  HTServo)
#pragma config(Sensor, S1,     ,               sensorI2CMuxController)
#pragma config(Motor,  motorA,           ,             tmotorNXT, openLoop)
#pragma config(Motor,  motorB,           ,             tmotorNXT, openLoop)
#pragma config(Motor,  motorC,           ,             tmotorNXT, openLoop)
#pragma config(Motor,  mtr_S1_C1_1,     driveFrontLeft, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C1_2,     driveRearLeft, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C2_1,     elbow,         tmotorTetrix, openLoop)
#pragma config(Motor,  mtr_S1_C2_2,     conveyor,      tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C3_1,     driveFrontRight, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Motor,  mtr_S1_C3_2,     driveRearRight, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Servo,  srvo_S1_C4_1,    leftEye,              tServoStandard)
#pragma config(Servo,  srvo_S1_C4_3,    rightEye,             tServoStandard)
#pragma config(Servo,  srvo_S1_C4_5,    roller,               tServoStandard)
#pragma config(Servo,  srvo_S1_C4_6,    servo6,               tServoNone)

#define SERVO_ROLLER_UP             48
#define SERVO_ROLLER_DOWN           137

task main()
{
    servo[roller] = SERVO_ROLLER_UP;

    wait1Msec(5000);
    playImmediateTone(60, 200);

    servo[roller] = SERVO_ROLLER_DOWN;

    while (true) {}
}
