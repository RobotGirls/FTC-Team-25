/*
Copyright 2012 - Daniele Benedettelli

This file is part of NXT2WIFI NXC Suite.

NXT2WIFI NXC Suite is free software: you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your option)
any later version.

NXT2WIFI NXC Suite is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License
along with NXT2WIFI NXC Suite. If not, see http://www.gnu.org/licenses/.

Creation date   : 02/10/2012
Author          : Daniele Benedettelli
Version       : 1.0
*/

// you need to include this file to use the NXT2WIFI function
#include "drivers/benedettelli-nxt2wifi.h"

bool MY_ADHOC = false;
string MY_SSID = "legolair";
//const string  MY_WPA2_KEY = "d4d3a089b20d91ef62bd6045467556a9294355bf63e936e0bb0e952f31071f55";
string  MY_WPA2_PASS = "legolego";
bool MY_DHCP = true;

// customize your Wi-Fi network settings
void CreateCustomWIFI() {
  N2WsetAdHoc(MY_ADHOC);  // set to Infrastructure
  N2WchillOut();
  N2WsetSSID(MY_SSID);    // set the network name
  wait1Msec(500);
  // Set the WPA key
  // WIFI_SecurityWPA2Key(MY_WPA2_KEY);

  // alternatively, you can set the passphrase (takes 30s the first time you connect!)
  N2WSecurityWPA2Passphrase("legolair");
	nxtDisplayTextLine(3, "Calculating WPA...");
	wait1Msec(40000);
  N2WchillOut();
  N2WsetDHCP(MY_DHCP); // enable DHCP
  N2WchillOut();
  N2WSave(); // store custom settings to retentive memory
  N2WchillOut();
  N2WLoad();
}

//
task main() {
  string mac;
  string IP;

  eraseDisplay();
  nxtDisplayTextLine(0, "NXT2WIFI SETUP" );
  nxtDisplayTextLine(7, "IP   SETUP   MAC" );

  nNxtButtonTask = -2;

  // initialise the port, etc
  N2WInitLib();

  memset(N2Wrxbuffer, 0, sizeof(N2Wrxbuffer));
  memset(N2Wtxbuffer, 0, sizeof(N2Wtxbuffer));

  // Disconnect if already connected


  N2WsetDebug(true); // enable debug stream on computer terminal


  while(true)
  {
		// PRESS RIGHT BUTTON TO RETRIEVE MAC ADDRESS
		if (nNxtButtonPressed == kRightButton)
		{
			N2WgetMAC(mac);
			nxtDisplayTextLine(4, mac);
			// Debounce button
			while (nNxtButtonPressed != kNoButton) EndTimeSlice();
		}
		// PRESS RIGHT BUTTON TO TEST NETWORK CREATION AND CONNECTIVITY
		if (nNxtButtonPressed == kEnterButton)
		{
			N2WDisconnect();
			N2WchillOut();
			N2WDelete();
			CreateCustomWIFI();
			N2WchillOut();

			N2WConnect(true);  // connect to custom profile
			wait1Msec(1000);

			nxtDisplayTextLine(3, "Connecting...");
			while (!N2WConnected())
			  wait1Msec(1000);

			nxtDisplayTextLine(3, "Connected!");
			PlayTone(523, 90);  // C5
			wait1Msec(100);
			PlayTone(659,90);   // E5
			wait1Msec(100);
			PlayTone(784,90);
			wait1Msec(100);
			N2WgetIP(IP);
			nxtDisplayTextLine(4, IP);
			while (nNxtButtonPressed != kNoButton) EndTimeSlice();
		}
	  // PRESS LEFT BUTTON TO RETRIEVE IP ADDRESS
		if (nNxtButtonPressed == kLeftButton)
		{
			N2WgetIP(IP);
			nxtDisplayTextLine(4, IP);
			while (nNxtButtonPressed != kNoButton) EndTimeSlice();
		}
	}
}
