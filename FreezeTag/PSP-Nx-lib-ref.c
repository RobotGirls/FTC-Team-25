/************************************************************************/
/*                                                                      */
/* Program Name: PSP-Nx-lib.c                                       */
/* ===========================                                          */
/*                                                                      */
/* Copyright (c) 2008 by mindsensors.com                                */
/* Email: info (<at>) mindsensors (<dot>) com                           */
/*                                                                      */
/* This program is free software. You can redistribute it and/or modify */
/* it under the terms of the GNU General Public License as published by */
/* the Free Software Foundation; version 3 of the License.              */
/* Read the license at: http://www.gnu.org/licenses/gpl.txt             */
/*                                                                      */
/************************************************************************/

/*
 * History
 * ------------------------------------------------
 * Author     Date      Comments
 * Deepak     04/08/09  Initial Authoring.
 */


/*--------------------------------------
  Controller button layout:
----------------------------------------

      L1                R1
      L2                R2

      d                 triang
   a     c         square     circle
      b                  cross

     l_j_b              r_j_b
     l_j_x              r_j_x
     l_j_y              r_j_y

-------------------------------------- */
/*
  bits as follows:
   b1:   a b c d x r_j_b l_j_b x
   b2:   square cross circle triang R1 L1 R2 L2
*/
typedef struct {
  char   b1;  //raw byte read from PSP-Nx
  char   b2;  //raw byte read from PSP-Nx

  // computed button states
  char   l1;
  char   l2;
  char   r1;
  char   r2;
  char   a;
  char   b;
  char   c;
  char   d;
  char   triang;
  char   square;
  char   circle;
  char   cross;
  char   l_j_b;  // joystick button state
  char   r_j_b;  // joystick button state

  int   l_j_x;   // analog value of joystick scaled from 0 to 100
  int   l_j_y;   // analog value of joystick scaled from 0 to 100
  int   r_j_x;   // analog value of joystick scaled from 0 to 100
  int   r_j_y;   // analog value of joystick scaled from 0 to 100
} psp;

void PSP_SendCommand(tSensors port, byte i2cAddr, byte  command)
{
	byte msg[5];

	// Build the I2C message
	msg[0] = 3;
	msg[1] = i2cAddr;
	msg[2] = 0x41;
	msg[3] = command;

	// Wait for I2C bus to be ready
	while (nI2CStatus[port] == STAT_COMM_PENDING){}
	// when the I2C bus is ready, send the message you built
	sendI2CMsg(port, &msg[0], 0);
	while (nI2CStatus[port] == STAT_COMM_PENDING){}
}

void PSP_ReadButtonState(tSensors port, byte i2cAddr, psp & currState)
{
  byte msg[5];
  unsigned byte replyMsg[7];
  byte b0, b1;

  msg[0] = 2;
  msg[1] = i2cAddr;
  msg[2] = 0x42;

  currState.b1 = 0;
  currState.b2 = 0;
  currState.l1 = 0;
  currState.l2 = 0;
  currState.r1 = 0;
  currState.r2 = 0;
  currState.a = 0;
  currState.b = 0;
  currState.c = 0;
  currState.d = 0;
  currState.triang = 0;
  currState.square = 0;
  currState.circle = 0;
  currState.cross = 0;
  currState.l_j_b = 0;
  currState.r_j_b = 0;
  currState.l_j_x = 0;
  currState.l_j_y = 0;
  currState.r_j_x = 0;
  currState.r_j_y = 0;

  while (nI2CStatus[port] == STAT_COMM_PENDING)
    {
      // Wait for I2C bus to be ready
    }
  // when the I2C bus is ready, send the message you built
  sendI2CMsg (port, &msg[0], 6);
  while (nI2CStatus[port] == STAT_COMM_PENDING)
    {
      // Wait for I2C bus to be ready
    }
  // read back the response from I2C
  readI2CReply (port, &replyMsg[0], 6);

	b0 = replyMsg[0]&0xff;
	b1 = replyMsg[1]&0xff;

	currState.b1        = b0;
	currState.b2        = b1;

	currState.l_j_b     = (b0 >> 1) & 0x01;
	currState.r_j_b     = (b0 >> 2) & 0x01;

	currState.d         = (b0 >> 4) & 0x01;
	currState.c         = (b0 >> 5) & 0x01;
	currState.b         = (b0 >> 6) & 0x01;
	currState.a         = (b0 >> 7) & 0x01;

	currState.l2        = (b1     ) & 0x01;
	currState.r2        = (b1 >> 1) & 0x01;
	currState.l1        = (b1 >> 2) & 0x01;
	currState.r1        = (b1 >> 3) & 0x01;
	currState.triang    = (b1 >> 4) & 0x01;
	currState.circle    = (b1 >> 5) & 0x01;
	currState.cross     = (b1 >> 6) & 0x01;
	currState.square    = (b1 >> 7) & 0x01;

	currState.l_j_x = (((replyMsg[2]&0xff) - 128) * 100)/128;
	currState.l_j_y = (((replyMsg[3]&0xff) - 128) * 100)/128;
	currState.r_j_x = (((replyMsg[4]&0xff) - 128) * 100)/128;
	currState.r_j_y = (((replyMsg[5]&0xff) - 128) * 100)/128;

}

/**
 * Read a byte
 */
byte PSPV4_ReadByte (tSensors port, byte i2cAddr, byte reg_to_read)
{
  byte message[5];
  unsigned byte buf[20];

  message[0]=2;
  message[1]=i2cAddr;
  message[2]=reg_to_read;

  while (nI2CStatus[port] ==  STAT_COMM_PENDING){}
  //When the I2C bus is ready, send the message
 	sendI2CMsg(port, &message[0], 1);
 	while (nI2CStatus[port] ==  STAT_COMM_PENDING){}
  //when the I2C bus is ready, read the response
	readI2CReply (port, &buf[0], 1);
	return buf;
}

byte PSPV4_ReadRefereeSignal(tSensors port, byte i2cAddr)
{
  return PSPV4_ReadByte(port, i2cAddr, 0x56);
}
