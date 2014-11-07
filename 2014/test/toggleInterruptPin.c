#pragma config(Sensor, S1,     HTPB,                sensorI2CCustom9V)


#include "../library/sensors/drivers/hitechnic-protoboard.h"

#define RESTING_PULSE 0b1111
#define FORWARD       0b1110
#define BACKWARD      0b1101
#define END_GAME      0b1100
#define CAUTION       0b1011
#define ALL_OFF       0b1000

void writeToProtoboard(int data)
{
    int err;

    err = HTPBwriteIO(HTPB, data);
    if (!err) {
        nxtDisplayBigTextLine(4, "Error writing to protoboard, %d", err);
    }

    /*
     * Toggle the interrupt Pin
     */
    err = HTPBwriteIO(HTPB, (0b0111 & data));
    if (!err) {
        nxtDisplayBigTextLine(4, "Error toggling interrupt pin, %d", err);
    }

    wait1Msec(50);

    err = HTPBwriteIO(HTPB, (0b1111 & data));
    if (!err) {
        nxtDisplayBigTextLine(4, "Error toggling interrupt pin, %d", err);
    }

}

void displayRestingPulse()
{
    writeToProtoboard(RESTING_PULSE);
}

void displayForward()
{
    writeToProtoboard(FORWARD);
}

void displayBackward()
{
    writeToProtoboard(BACKWARD);
}

void displayCaution()
{
    writeToProtoboard(CAUTION);
}

void displayEndgame()
{
    writeToProtoboard(END_GAME);
}

void allOff()
{
    writeToProtoboard(ALL_OFF);
}

void pause()
{
    for (int i=0; i < 5; i++) {
        wait1Msec(1000);
        nxtDisplayCenteredTextLine(4, "Countdown %d", i);
    }
}

#define GAME_MINUTES 1
#define GAME_SECONDS 60 * GAME_MINUTES
#define GAME_SEGMENT_LENGTH 10
#define GAME_SEGMENTS GAME_SECONDS / GAME_SEGMENT_LENGTH

bool done = false;

#define TOGGLE_OFF (0b0000)
#define TOGGLE_ON  (0b1111)

task main()
{
    int err;

	// Setup all the digital IO ports as outputs (0xFF)
    err = HTPBsetupIO(HTPB, 0x0F);
    if (!err) {
		nxtDisplayTextLine(4, "Error setting up digital outputs, %d", err);
		StopAllTasks();
	}

    for (int i = 0; i < 10; i++) {
	    err = HTPBwriteIO(HTPB, TOGGLE_ON);
	    if (!err) {
	        nxtDisplayBigTextLine(4, "Error toggling interrupt pin, %d", err);
	    }

	    wait1Msec(50);

	    err = HTPBwriteIO(HTPB, TOGGLE_OFF);
	    if (!err) {
	        nxtDisplayBigTextLine(4, "Error toggling interrupt pin, %d", err);
	    }

	    wait1Msec(50);
    }
}
