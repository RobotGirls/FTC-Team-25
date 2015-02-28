#pragma config(Hubs,  S1, HTMotor,  HTMotor,  HTMotor,  HTServo)
#pragma config(Hubs,  S2, HTServo,  none,     none,     none)
#pragma config(Sensor, S1,     ,               sensorI2CMuxController)
#pragma config(Sensor, S2,     ,               sensorI2CMuxController)
#pragma config(Sensor, S3,     HTSMUX,         sensorI2CCustom)
#pragma config(Motor,  motorA,          rampRight,     tmotorNXT, PIDControl, reversed)
#pragma config(Motor,  motorB,          rampLeft,      tmotorNXT, PIDControl, encoder)
#pragma config(Motor,  motorC,           ,             tmotorNXT, openLoop)
#pragma config(Motor,  mtr_S1_C1_1,     driveFrontLeft, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Motor,  mtr_S1_C1_2,     driveRearLeft, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C2_1,     elbow,         tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C2_2,     conveyor,      tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C3_1,     driveFrontRight, tmotorTetrix, PIDControl, encoder)
#pragma config(Motor,  mtr_S1_C3_2,     driveRearRight, tmotorTetrix, PIDControl, reversed, encoder)
#pragma config(Servo,  srvo_S1_C4_1,    leftEye,              tServoStandard)
#pragma config(Servo,  srvo_S1_C4_2,    servo2,               tServoNone)
#pragma config(Servo,  srvo_S1_C4_3,    rightEye,             tServoStandard)
#pragma config(Servo,  srvo_S1_C4_4,    servo4,               tServoNone)
#pragma config(Servo,  srvo_S1_C4_5,    roller,               tServoStandard)
#pragma config(Servo,  srvo_S1_C4_6,    servo6,               tServoNone)
#pragma config(Servo,  srvo_S2_C1_1,    leftFang,             tServoStandard)
#pragma config(Servo,  srvo_S2_C1_2,    leftGoalGuide,               tServoNone)
#pragma config(Servo,  srvo_S2_C1_3,    autoThumb,            tServoStandard)
#pragma config(Servo,  srvo_S2_C1_4,    autoElbow,              tServoNone)
#pragma config(Servo,  srvo_S2_C1_5,    rightGoalGuide,              tServoNone)
#pragma config(Servo,  srvo_S2_C1_6,    rightFang,            tServoStandard)
