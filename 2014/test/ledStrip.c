#pragma config(Sensor, S1,     HTPB,                sensorI2CCustom9V)


#include "../library/sensors/drivers/hitechnic-protoboard.h"

#define RESTING_PULSE 0b111
#define FORWARD       0b110
#define BACKWARD      0b101
#define END_GAME      0b100
#define CAUTION       0b011

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
    err = HTPBwriteIO(HTPB, (0x0111 & data));
    if (!err) {
        nxtDisplayBigTextLine(4, "Error toggling interrupt pin, %d", err);
    }

    wait1Msec(50);

    err = HTPBwriteIO(HTPB, (0x1111 & data));
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

task main()
{
    int err;

	// Setup all the digital IO ports as outputs (0xFF)
	err = HTPBsetupIO(HTPB, 0xFF);
    if (!err) {
		nxtDisplayTextLine(4, "Error setting up digital outputs, %d", err);
		StopAllTasks();
	}

    displayEndgame();
    wait1Msec(3000);
    displayCaution();
    wait1Msec(3000);
    displayForward();
    wait1Msec(3000);
    displayBackward();
    wait1Msec(3000);
    displayRestingPulse();
}
