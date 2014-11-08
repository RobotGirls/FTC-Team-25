#pragma config(StandardModel, "teddy")
//*!!Code automatically generated by 'ROBOTC' configuration wizard               !!*//

/////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                           Autonomous Mode Code Template
//
// This file contains a template for simplified creation of an autonomous program for an TETRIX robot
// competition.
//
// You need to customize two functions with code unique to your specific robot.
//
/////////////////////////////////////////////////////////////////////////////////////////////////////


#include "JoystickDriver.c"  //Include file to "handle" the Bluetooth messages.
#include "../library/auto_defs.h"
#include "../library/sensors/drivers/hitechnic-sensormux.h"
#include "../library/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../library/sensors/drivers/hitechnic-compass.h"
#include "../library/sensors/drivers/hitechnic-protoboard.h"
#include "../library/DrivetrainSquare.c"
#include "../library/dead_reckon.h"

const tMUXSensor IRSeeker = msensor_S2_4;

#include "../library/light_strip.h"
#include "../library/rnrr_start.h"

task main()
{

    init_path();

    add_segment(24, 0, 100);
    add_segment(25 , 45, 100);

    dead_reckon();
}
