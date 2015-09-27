#include "../library/sensors/drivers/hitechnic-protoboard.h"

#define RESTING_PULSE 0b10000
#define FORWARD       0b10001
#define BACKWARD      0b10010
#define END_GAME      0b10011
#define CAUTION       0b10100
#define PULSE_RED     0b10110
#define WIPE_TEAL     0b10111
#define WIPE_RED      0b11000
#define FLASH_GREEN   0b11001
#define IR_SEG_4      0b11010
#define IR_SEG_6      0b11011
#define IR_SEG_5      0b11100
#define HAPPY         0b10101

void lightStripInit(ubyte mask)
{
    int err;
    int val;

    val = HTPBgetIOCfg(HTPB);
    if (val == -1) {
        nxtDisplayTextLine(4, "Error reading up digital config, %d", err);
		StopAllTasks();
	}

    /*
     * Set the output ports for the drive pins
     */

    err = HTPBsetupIO(HTPB, (val | mask));
    if (!err) {
		nxtDisplayTextLine(4, "Error setting up digital outputs, %d", err);
		StopAllTasks();
	}
}

void writeToProtoboard(int data)
{
    int err;

    err = HTPBwriteIO(HTPB, data);
    if (!err) {
        nxtDisplayBigTextLine(4, "Error writing to protoboard, %d", err);
    }

    wait1Msec(50);

    /*
     * Toggle the interrupt Pin
     */
    err = HTPBwriteIO(HTPB, (0b01111 & data));
    if (!err) {
        nxtDisplayBigTextLine(4, "Error toggling interrupt pin, %d", err);
    }

    wait1Msec(50);

    err = HTPBwriteIO(HTPB, (0b11111 & data));
    if (!err) {
        nxtDisplayBigTextLine(4, "Error toggling interrupt pin, %d", err);
    }

}

void displayRestingPulse()
{
    nxtDisplayCenteredTextLine(5, "Resting");
    writeToProtoboard(RESTING_PULSE);
}

void displayForward()
{
    nxtDisplayCenteredTextLine(5, "Forward");
    writeToProtoboard(FORWARD);
}

void displayBackward()
{
    nxtDisplayCenteredTextLine(5, "Backward");
    writeToProtoboard(BACKWARD);
}

void displayCaution()
{
    nxtDisplayCenteredTextLine(5, "Caution");
    writeToProtoboard(CAUTION);
}

void displayEndgame()
{
    nxtDisplayCenteredTextLine(5, "EndGame");
    writeToProtoboard(END_GAME);
}

void pulseRed()
{
    writeToProtoboard(PULSE_RED);
}

void wipeTeal()
{
    writeToProtoboard(WIPE_TEAL);
}

void wipeRed()
{
    writeToProtoboard(WIPE_RED);
}

void flashGreen()
{
    writeToProtoboard(FLASH_GREEN);
}

void displayHappy()
{
    nxtDisplayCenteredTextLine(5, "Happy");
    writeToProtoboard(HAPPY);
}
