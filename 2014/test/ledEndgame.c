#pragma config(Sensor, S1,     HTPB,                sensorI2CCustom9V)


#include "../library/sensors/drivers/hitechnic-protoboard.h"

#define RESTING_PULSE 0b10000
#define FORWARD       0b10001
#define BACKWARD      0b10010
#define END_GAME      0b10011
#define CAUTION       0b10100
#define IR_SEG_2      0b10110
#define IR_SEG_8      0b10111
#define IR_SEG_3      0b11000
#define IR_SEG_7      0b11001
#define IR_SEG_4      0b11010
#define IR_SEG_6      0b11011
#define IR_SEG_5      0b11100

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

void displayIRSeg2()
{
    nxtDisplayCenteredTextLine(5, "IRSeg2");
    writeToProtoboard(IR_SEG_2);
}

void displayIRSeg3()
{
    nxtDisplayCenteredTextLine(5, "IRSeg3");
    writeToProtoboard(IR_SEG_3);
}

void displayIRSeg4()
{
    nxtDisplayCenteredTextLine(5, "IRSeg4");
    writeToProtoboard(IR_SEG_4);
}

void displayIRSeg5()
{
    nxtDisplayCenteredTextLine(5, "IRSeg5");
    writeToProtoboard(IR_SEG_5);
}

void displayIRSeg6()
{
    nxtDisplayCenteredTextLine(5, "IRSeg6");
    writeToProtoboard(IR_SEG_6);
}

void displayIRSeg7()
{
    nxtDisplayCenteredTextLine(5, "IRSeg7");
    writeToProtoboard(IR_SEG_7);
}

void displayIRSeg8()
{
    nxtDisplayCenteredTextLine(5, "IRSeg8");
    writeToProtoboard(IR_SEG_8);
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

task gameTimer()
{
    for (int i = 0; i < GAME_SEGMENTS; i++) {
        wait1Msec(1000 * GAME_SEGMENT_LENGTH);
        nxtDisplayCenteredTextLine(2, "Game segment %d", i);

        if (i % 2) {
		    displayForward();
        } else {
		    displayBackward();
        }
    }

    done = true;
}

task main()
{
    int err;
    int display;
    bool button_pressed;

	// Setup all the digital IO ports as outputs (0xFF)
    err = HTPBsetupIO(HTPB, 0x3F);
    if (!err) {
		nxtDisplayTextLine(4, "Error setting up digital outputs, %d", err);
		StopAllTasks();
	}

    display = 0;

    while (1) {
       nxtDisplayCenteredTextLine(6, "Display: %d", display);
       switch (display) {
        case 0:
            displayRestingPulse();
            break;
        case 1:
            displayForward();
            break;
        case 2:
            displayBackward();
            break;
        case 3:
            displayEndgame();
            break;
        case 4:
            displayCaution();
            break;
        case 5:
            displayIRSeg2();
            break;
        case 6:
            displayIRSeg3();
            break;
        case 7:
            displayIRSeg4();
            break;
        case 8:
            displayIRSeg5();
            break;
        case 9:
            displayIRSeg6();
            break;
        case 10:
            displayIRSeg7();
            break;
        case 11:
            displayIRSeg8();
            break;
        }

        button_pressed = false;

        while (!button_pressed) {
            if (nNxtButtonPressed == 1) { // Right Arrow
                display++;
                if (display > 11) {
                    display = 0;
                }
                button_pressed = true;
            }
        }

        wait1Msec(1000);
    }
}
