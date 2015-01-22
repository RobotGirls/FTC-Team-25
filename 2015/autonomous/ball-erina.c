#pragma config(Hubs,  S1, HTServo,  HTMotor,  HTMotor,  HTMotor)
#pragma config(Hubs,  S2, HTServo,  none,     none,     none)
<<<<<<< HEAD
=======
#pragma config(Sensor, S2,     carrot,         sensorNone)
>>>>>>> upstream/master
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

#include "JoystickDriver.c"

<<<<<<< HEAD
#define FOUR_WHEEL_DRIVE
#define UPCOUNTS      4000
#define LSERVO_CENTER 134
#define RSERVO_CENTER 113
#define RSERVO_PERP   235

#include "../../lib/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../../lib/drivetrain_andymark_defs.h"
#include "../../lib/drivetrain_square.h"
#include "../../lib/dead_reckon.h"
#include "../../lib/data_log.h"
#include "../../lib/ir_utils.h"
=======
#include "JoystickDriver.c"  //Include file to "handle" the Bluetooth messages.

/////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                    initializeRobot
//
// Prior to the start of autonomous mode, you may want to perform some initialization on your robot.
// Things that might be performed during initialization include:
//   1. Move motors and servos to a preset position.
//   2. Some sensor types take a short while to reach stable values during which time it is best that
//      robot is not moving. For example, gyro sensor needs a few seconds to obtain the background
//      "bias" value.
//
// In many cases, you may not have to add any code to this function and it will remain "empty".
//
/////////////////////////////////////////////////////////////////////////////////////////////////////


/////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                         Main Task
//
// The following is the main code for the autonomous robot operation. Customize as appropriate for
// your specific robot.
//
// The types of things you might do during the autonomous phase (for the 2008-9 FTC competition)
// are:
//
//   1. Have the robot follow a line on the game field until it reaches one of the puck storage
//      areas.
//   2. Load pucks into the robot from the storage bin.
//   3. Stop the robot and wait for autonomous phase to end.
//
// This simple template does nothing except play a periodic tone every few seconds.
//
// At the end of the autonomous period, the FMS will autonmatically abort (stop) execution of the program.
//
/////////////////////////////////////////////////////////////////////////////////////////////////////

#define FOUR_WHEEL_DRIVE
>>>>>>> upstream/master

#define SERVO_ARM_EXTENDED          90
#define SERVO_ARM_RETRACTED         150
#define SERVO_ARM_EXTENDED_HALF     120
#define SERVO_ARM_PICKUP            140

#define SERVO_DOCK_ARM_FORWARD      80
#define SERVO_DOCK_ARM_BACKWARD     0
#define SERVO_DOCK_ARM_STOPPED      127
#define SERVO_FINGER_UP             160
#define SERVO_FINGER_DOWN           146

<<<<<<< HEAD
=======
#define UPCOUNTS      4000
#define LSERVO_CENTER 134
#define RSERVO_CENTER 113
#define RSERVO_PERP   235

#include "../../lib/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../../lib/baemax_drivetrain_defs.h"
#include "../../lib/drivetrain_square.h"
#include "../../lib/dead_reckon.h"
#include "../../lib/data_log.h"
#include "../../lib/ir_utils.h"

>>>>>>> upstream/master
task ext_dock_arm()
{
    servo[dockarm] = SERVO_DOCK_ARM_FORWARD;
    wait1Msec(2000);
    servo[finger] = SERVO_FINGER_UP;
    wait1Msec(1500);
    servo[dockarm] = SERVO_DOCK_ARM_STOPPED;
}

void initializeRobot()
{
    servo[arm] = SERVO_ARM_RETRACTED;
    servo[finger] = SERVO_FINGER_DOWN;
    nMotorEncoder(shoulder) = 0;
    servo[brush] = 127;

}

task main()
{
  initializeRobot();

  waitForStart(); // Wait for the beginning of autonomous phase.

    init_path();
    add_segment(-83, 0, 30);  //gets off the ramp to medium goal
    stop_path();
    dead_reckon();

    motor[shoulder] = 20;
    while (nMotorEncoder(shoulder) < UPCOUNTS) {
    }
    motor[shoulder] = 0;

    servo[brush] = 255;
    wait1Msec(10000);
    servo[brush] = 127;

<<<<<<< HEAD
    while (true) {
=======
    while (true){
>>>>>>> upstream/master
    }
}
