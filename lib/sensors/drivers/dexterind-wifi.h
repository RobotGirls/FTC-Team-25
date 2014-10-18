
////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                 Setup for Wifi Sensor
//                 Sets up flow control
// Script for connecting to internet
//
// ate0     // Turn off the echo.
// at&k1    // Enable software flow control.
//
// at+wauth=1 // Authentication mode: open
// at+wwep1=0fb3ba79eb  // Set WEP key
// at+wa=NPX97    // Connect to the SSID
// at+ndhcp=1   // Enable DHCP <Dynamic Host Configuration Protocol>
//
// at+dnslookup=www.dexterindustries.com  // Lookup the DNS configuration.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////

#pragma systemFile

#define DEBUG_WIFI 1

#ifdef DEBUG_WIFI
#define writeRawHS(X, Y) debugnxtWriteRawHS(X, Y)
#else
#define writeRawHS(X, Y) nxtWriteRawHS(X, Y)
#endif

typedef ubyte buff_t[128];
buff_t buffer;

long nRcvChars = 0;
ubyte BytesRead[8];
ubyte ssid[] = {'x', 'a', 'm', 'm', 'y', '2'};
ubyte newline[] = {0x0D};
ubyte linefeed[] = {0x0A};
ubyte endmarker[] = {27, 'E'};
ubyte wep[10] = {'0','F','B','3','B','A','7','9','E','B'};        // Space for 10 digit WEP key.
ubyte wpa_psk[] = {'g','w','a','h','d','n','m','5','!',';',']'};
//ubyte wpa_psk[] = {'w','l','a','n','s'};

long baudrates[] = {9600, 19200, 38400, 57600, 115200, 230400,460800, 921600};

int appendToBuff(buff_t &buf, const short index, const ubyte &pData, const short nLength)
{
  if (index == 0) memset(buf, 0, sizeof(buf));

  memcpy(&buf[index], &pData, nLength);
  return index + nLength;
}

void debugnxtWriteRawHS(const ubyte &pData, const short nLength)
{
  string tmpString;
  ubyte buff[30];
  memset(&buff[0], 0, 30);
  memcpy(&buff[0], &pData, nLength);
  StringFromChars(tmpString, buff);
  writeDebugStream("%s", tmpString);
  nxtWriteRawHS(&pData, nLength);
}


////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Setup High Speed on Port 4.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////


int checkFailure() {
  ubyte currByte[] = {0};
  ubyte prevByte[] = {0};

  while (nxtGetAvailHSBytes() == 0) wait1Msec(5);

  while (nxtGetAvailHSBytes() > 0) {
    nxtReadRawHS(&currByte[0], 1);
    if ((prevByte[0] == 27) && (currByte[0] == 'F'))
      return 1;
    else if ((currByte[0] > '0') && (currByte[0] < '7'))
      return 1;
    else if (currByte[0] == '9')
      return 2;
  }
  return 0;
}

void setupHighSpeedLink(const bool bMaster)
{
  // Initialize port S$ to "high speed" mode.
  nxtEnableHSPort();
  nxtSetHSBaudRate(9600);
  nxtHS_Mode = hsRawMode;
  return;
}

void Receive(bool wait=false)
{
  if (wait)
    while (nxtGetAvailHSBytes() == 0) wait1Msec(5);

  while (nxtGetAvailHSBytes() > 0) {
    nxtReadRawHS(&BytesRead[0], 1);
    writeDebugStream("%c", BytesRead[0]);
    wait1Msec(2);
  }
}

void setupBAUDSpeed(long baudrate) {
  int index = 0;
  ubyte baudBuff[6];
  ubyte baud_cmd[] = {'A','T','B','='};

  index = appendToBuff(buffer, index, baud_cmd, sizeof(baud_cmd));

  snprintf(baudBuff, 6, "%d", baudrate);
  index = appendToBuff(buffer, index, baudBuff, sizeof(baudBuff));
  index = appendToBuff(buffer, index, newline, sizeof(newline));
  writeRawHS(buffer, index);
  wait1Msec(100);
  nxtDisableHSPort();
  wait1Msec(10);
  nxtEnableHSPort();
  nxtSetHSBaudRate(baudrate);
  nxtHS_Mode = hsRawMode;
  wait1Msec(10);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//      Clear Read Buffer
//      Run this to clear out the reading buffer.
//      Simply sends a carriage return, then clears the buffer out.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////

void clear_read_buffer()
{
  ubyte nData[] = {13};
  writeRawHS(nData[0], 1);   // Send the carriage return
  wait1Msec(100);
  while(BytesRead[0] < 0){
    nxtReadRawHS(&BytesRead[0], 1);    // Read the response.  Probably an error.
  }
  wait1Msec(100);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//      Echo All Input Off - Turns off the echo effect on the wifi.
//      Sending the serial command "ate0" which turns off the echo effect.
//      Sends one single byte at a time, pauses.
//      Drains receiver with a read each time.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////

void echo_all_input_off()
{
  writeDebugStreamLine("echo_all_input_off");
  ubyte nData[] = {'A','T','E','0',0x0D};

  //writeRawHS(nData[0], sizeof(nData));

  for(int i = 0; i < 5; i++){
    writeRawHS(nData[i], 1);            // Send the command, byte by byte.
    nxtReadRawHS(&BytesRead[0], 8);         // Clear out the echo.
    wait10Msec(10);

  }

  // wait10Msec(100);
}

void set_verbose(bool on)
{
  ubyte nData[] = {'A','T','V','1',0x0D};
  if (!on) nData[3] = '0';

  writeDebugStreamLine("set_verbose");

  writeRawHS(nData[0], sizeof(nData));            // Send the command, byte by byte.
  // wait10Msec(100);
}

void echo_all_input_on()
{
  writeDebugStreamLine("echo_all_input_on");
  ubyte nData[] = {'A','T','e','1',0x0D};
  writeRawHS(nData[0], sizeof(nData));            // Send the command, byte by byte.
  // wait10Msec(100);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//      Software Flow Control On
//      Send string "at&k1" and carriage return.
//      Shouldn't need the wait or read now that we've got the echo off.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////

void software_flow_control()
{
	writeDebugStreamLine("software_flow_control");
	ubyte nData[] = {'A','T','&','k','1',0x0D};
	writeRawHS(nData[0], 6);            // Send the command, byte by byte.
	// wait10Msec(100);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//      Scan for networks
//      Send string "AT+WS" and carriage return.
//      Don't really need to do this; gets back a tonne of data.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////

void scan_for_networks()
{
  writeDebugStreamLine("scan_for_networks");
  ubyte nData[] = {'A','T','+','w','s',0x0D};
  writeRawHS(nData[0], sizeof(nData));            // Send the command, byte by byte.
  // wait10Msec(100);

}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//      WIFI:  Set Authentication Mode
//             Send string "at+wauth=<n>" and carriage return.
//              n = 0 --> Off
//              n = 1 --> On
//
////////////////////////////////////////////////////////////////////////////////////////////////////////

void wifi_auth_mode(int state)
{
  writeDebugStreamLine("wifi_auth_mode");
  char state_n = state+48;
  ubyte nData[] = {'A','T','+','w','a','u','t','h','=',state_n,0x0D};

  writeRawHS(nData[0], sizeof(nData));            // Send the command, byte by byte.
  // wait10Msec(100);
}


////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//      WIFI:  Set WEP key.
//             Send string "at+WWEPn=<m>" and carriage return.
//
//                  m = WEP Key.
//                  n = type of WEP key.
//                        n= 0
//                        n= 1
//                        n= 2
//
////////////////////////////////////////////////////////////////////////////////////////////////////////

void set_ssid() {
  ubyte ssid_cmd[] = {'A','T','+','W','A','='};
  int index = 0;

  index = appendToBuff(buffer, index, ssid_cmd, sizeof(ssid_cmd));
  index = appendToBuff(buffer, index, ssid, sizeof(ssid));
  index = appendToBuff(buffer, index, newline, sizeof(newline));
  writeRawHS(buffer, index);
}

void set_wep_key(int n)
{
  writeDebugStreamLine("set_wep_key");
  int i_n = n+48;

  // Initalize wep_key
  ubyte wep_key[] = {'A','T','+','W','W','E','P',i_n,'=',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',0x0D};
  //wep_key[7] = i_n;       // Insert WEP key type.
  for(int i = 0; i < 10; i++){   // Load in the WEP Key.
    wep_key[i+9] = wep[i];
  }

  writeRawHS(wep_key[0], sizeof(wep_key));            // Send the command, byte by byte.
  // wait10Msec(100);
}


void set_wpa_psk()
{
  ubyte wpa_psk_cmd[] = {'A','T','+','W','P','A','P','S','K','='};
  unsigned byte dummy[] = {','};
  int index = 0;

  index = appendToBuff(buffer, index, wpa_psk_cmd, sizeof(wpa_psk_cmd));
  index = appendToBuff(buffer, index, ssid, sizeof(ssid));
  index = appendToBuff(buffer, index, dummy, sizeof(dummy));
  index = appendToBuff(buffer, index, wpa_psk, sizeof(wpa_psk));
  index = appendToBuff(buffer, index, newline, sizeof(newline));

  writeRawHS(buffer[0], index);
}

void setDHCP(int n)
{
  writeDebugStreamLine("setDHCP");
  ubyte dhcp_cmd[] = {'A','T','+','N','D','H','C','P', '=', n+48, 0x0D};
  writeRawHS(dhcp_cmd[0], sizeof(dhcp_cmd));       // Send the command, byte by byte.
  wait10Msec(100);
}

void getFW()
{
  writeDebugStreamLine("getFW");
  ubyte getfw_cmd[] = {'A','T','+','V','E','R','=','?',0x0D};
  writeRawHS(getfw_cmd[0], sizeof(getfw_cmd));       // Send the command, byte by byte.
}


void getInfo() {
  writeDebugStreamLine("getInfo");
  ubyte status_cmd[] = {'A','T','+','N','S','T','A','T', '=', '?', 0x0D};
  writeRawHS(status_cmd[0], sizeof(status_cmd));       // Send the command, byte by byte.
}

void getInfoWLAN() {
  writeDebugStreamLine("getInfo");
  ubyte status_cmd[] = {'A','T','+','W','S','T','A','T','U','S', 0x0D};
  writeRawHS(status_cmd[0], sizeof(status_cmd));       // Send the command, byte by byte.
}

void closeAllConns() {
  writeDebugStreamLine("closeAllCons");
  ubyte close_cmd[] = {'A','T','+','N','C','L','O','S','E','A','L','L',0x0D};
  writeRawHS(close_cmd[0], sizeof(close_cmd));       // Send the command, byte by byte.
  Receive(true);
}

void closeConn(int cid) {
  writeDebugStreamLine("closeConn");
  cid += 48;
  ubyte close_cmd[] = {'A','T','+','N','C','L','O','S','E','=',cid,0x0D};
  writeRawHS(close_cmd[0], sizeof(close_cmd));       // Send the command, byte by byte.
  Receive(true);
}

void SaveConfig() {
  writeDebugStreamLine("save config");
  ubyte save_profile_cmd[] = {'A','T','&','W','0', 13};
  ubyte set_def_profile_cmd[] = {'A','T','&','Y','0', 13};
  writeRawHS(save_profile_cmd[0], sizeof(save_profile_cmd));
  Receive(true);
  wait1Msec(10);
  writeRawHS(set_def_profile_cmd[0], sizeof(set_def_profile_cmd));
  Receive(true);
}


void startListen(long port) {
  int index = 0;
  ubyte listen_cmd[] = {'A','T','+','N','S','T','C','P','=','8','1'};
  /*
  ubyte strport[6];

  int div = 0;
  int remainder = 0;
  memset(port, 0, 6);

  if (port > 65536) return;
  if ((div = port / 10000) > 0) {
    port[index++] = div+48;


  if ((div = port / 10000) > 0)
    port[index++] = div+48;
  */
  index = appendToBuff(buffer, index, listen_cmd, sizeof(listen_cmd));
  index = appendToBuff(buffer, index, newline, sizeof(newline));
  writeRawHS(buffer[0], index);
  Receive(true);
}

long scanBaudRate() {
  ubyte tmpbuff[8];
  string tmpString;
  ubyte attention[] = {'+','+','+',13};
  for (int i = 0; i < 8; i++) {
    memset(tmpbuff, 0, sizeof(tmpbuff));
    nxtDisableHSPort();
	  wait1Msec(10);
	  nxtEnableHSPort();
	  nxtSetHSBaudRate(baudrates[i]);
	  nxtHS_Mode = hsRawMode;
	  clear_read_buffer();
	  wait1Msec(1000);
	  nxtWriteRawHS(attention, sizeof(attention));
	  nxtReadRawHS(tmpbuff, 7);  // make sure last ubyte is always NULL
    StringFromChars(tmpString, tmpbuff);
    if ((StringFind(tmpString, "ERR") > -1) ||
        (StringFind(tmpString, "OK") > -1) ||
        (StringFind(tmpString, "0") > -1) ||
        (StringFind(tmpString, "2") > -1)) {
      clear_read_buffer();
      return baudrates[i];
    }
	}
	clear_read_buffer();
	return 0;
}

void configureWiFi()
{
  clear_read_buffer();      // Clear out the buffer and test TX/RX.
  wait10Msec(100);          // Must be run first!
  echo_all_input_off();     // Must be run first!
  wait10Msec(100);          // Must be run first!
  Receive();
  eraseDisplay();
  software_flow_control();  // Must be run first!
  Receive();
  set_verbose(false);
  wait1Msec(100);
  Receive();
  //setupBAUDSpeed(230400);
  setupBAUDSpeed(460800);
  wait1Msec(100);
  Receive();
  set_wpa_psk();
  Receive(true);  // gets "calculating"
  Receive(true);  // gets "ok"
  set_ssid();
  Receive(true);
  setDHCP(1);
  Receive(true);
  SaveConfig();
  Receive();
}

////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                        Receive Bytes
//
////////////////////////////////////////////////////////////////////////////////////////////////////////



////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                                        Main Task
//
////////////////////////////////////////////////////////////////////////////////////////////////////////
