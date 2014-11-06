#pragma config(Hubs,  S1, HTMotor,  none,     none,     none)
#pragma config(Sensor, S1,     ,               sensorI2CMuxController)
#pragma config(Motor,  mtr_S1_C1_1,     driveRearRight, tmotorTetrix, openLoop, encoder)
#pragma config(Motor,  mtr_S1_C1_2,     driveRearLeft, tmotorTetrix, openLoop, reversed, encoder)
//*!!Code automatically generated by 'ROBOTC' configuration wizard               !!*//

#include "C:\FIRST\FTC-Team-25\lib\drivetrain_andymark_defs.h"
#include "C:\FIRST\FTC-Team-25\lib\drivetrain_square.h"
#include "C:\FIRST\FTC-Team-25\lib\dead_reckon.h"

task main()
{
	init_path();
 	add_segment(70, 0, 50);        // Move robot off ramp.
	add_segment(0, -90, 70);       // Turn towards goal.
	add_segment(-3.5, 0, 50);      // Align.
	add_segment(36, 0, 50);        // Move towards goal.
	stop_path();
	dead_reckon();

	playImmediateTone(251, 50);   // Play tone of frequency 251 for 0.5 seconds.
 	wait1Msec(1250);              // Wait for 2 seconds.

	init_path();
	add_segment(-2, 0, 50);         // Move backwards.
	add_segment(0, -51.875, 25);    // Turn 55 degrees left.
	add_segment(24, 0, 50);         // Move to position 2.
	stop_path();
	dead_reckon();

	playImmediateTone(251, 50);   // Play tone of frequency 251 for 0.5 seconds.
 	wait1Msec(1250);              // Wait for 2 seconds.

	init_path();
	add_segment(15, 0, 50);       // Move to position 3.
	add_segment(0, 55, 25);       // Turn towards position 3.
	stop_path();
	dead_reckon();

	playImmediateTone(251, 50);   // Play tone of frequency 251 for 0.5 seconds.
 	wait1Msec(1250);              // Wait for 2 seconds.
}
