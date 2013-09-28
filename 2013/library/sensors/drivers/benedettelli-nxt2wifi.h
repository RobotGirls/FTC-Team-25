/*!@addtogroup other
 * @{
 * @defgroup NXT2WIFI NXT2WIFI Wifi Sensor
 * Dani's WiFi Sensor
 * @{
 */

/*
 * $Id: benedettelli-nxt2wifi.h 114 2012-10-08 18:11:44Z xander $
 */

#ifndef __N2W_H__
#define __N2W_H__
/** \file benedettelli-nxt2wifi.h
 * \brief Dani's WiFi Sensor
 *
 * benedettelli-nxt2wifi.h provides an API for Dani's WiFi Sensor.\n
 *
 * Changelog:
 * - 0.1: Initial release
 *
 * Credits:
 * - Big thanks to Dani for providing me with the hardware necessary to write and test this.
 *
 * License: You may use this code as you wish, provided you give credit where its due.
 *
 * THIS CODE WILL ONLY WORK WITH ROBOTC VERSION 3.51 AND HIGHER.
 * \author Xander Soldaat (xander_at_botbench.com)
 * \date 12 June 2012
 * \version 0.1
 * \example benedettelli-nxt2wifi-test1.c
 * \example benedettelli-nxt2wifi-test2.c
 * \example benedettelli-nxt2wifi-test3.c
 * \example benedettelli-nxt2wifi-test4.c
 */

#pragma systemFile

#ifndef __COMMON_H__
#include "common.h"
#endif

#ifndef __TMR_H__
#include "timer.h"
#endif


#define WF_SEC_OPEN 					      0   /*!< Open Security (none) */
#define WF_SEC_WEP_40 				      1   /*!< 40 bit WEP  */
#define WF_SEC_WEP_104				      2   /*!< 104 bit WEP */
#define WF_SEC_WPA_KEY				      3   /*!< WPA using key */
#define WF_SEC_WPA_PASSPHRASE 	    4   /*!< WPA using passphrase */
#define WF_SEC_WPA2_KEY				      5   /*!< WPA2 using key */
#define WF_SEC_WPA2_PASSPHRASE 	    6   /*!< WPA2 using passphrase */
#define WF_SEC_WPA_AUTO_KEY 		    7   /*!< Automatically determine WPA type and use key */
#define WF_SEC_WPA_AUTO_PASSPHRASE  8   /*!< Automatically determine WPA type and use passphrase */

#define AD_HOC 1
#define INFRASTRUCTURE 0

#define N2WchillOut()  wait1Msec(50)                   /*!< Wait 50ms between messages, this allows transmission to be done */
#define N2WsetIPAddress(X)    _N2WsetPar("IPAD", X) /*!< Macro for setting the IP address */
#define N2WsetMask(X)         _N2WsetPar("MASK", X) /*!< Macro for setting the netmask */
#define N2WsetGateway(X)      _N2WsetPar("GWAY", X) /*!< Macro for setting the gateway IP address */
#define N2WsetDNS1(X)         _N2WsetPar("DNS1", X) /*!< Macro for setting the first DNS server IP address */
#define N2WsetDNS2(X)         _N2WsetPar("DNS2", X) /*!< Macro for setting the second DNS server IP address */
#define N2WsetSSID(X)         _N2WsetPar("SSID", X) /*!< Macro for setting the SSID to connect to */
#define N2WsetNetbiosName(X)  _N2WsetPar("NAME", X) /*!< Macro for setting the Netbios Name */

int rxTimer;            /*!< timer for receiving timeouts */
tMassiveArray N2Wrxbuffer; /*!< 128 bit array for receiving */
tMassiveArray N2Wtxbuffer; /*!< 128 bit array for transmission */
string N2WscratchString;   /*!< string for tmp formatting, scratch data */



/**
 * Parse the buffer and return the number in the NXT2WIFI response
 * @param buf the buffer to pull the number from
 * @return the number or -1 if no number found.
 */
int N2WgetNumericResponse(const ubyte &buf)
{
  int pos = 0;
  StringFromChars(N2WscratchString, &buf);
  pos = StringFind(N2WscratchString, "=");
  if (pos != 0)
  {
    StringDelete(N2WscratchString, 0, pos + 1);
    strTrim(N2WscratchString);
    return atof(N2WscratchString);
  }
  return -1;
}


/**
 * Parse the buffer and return the string in the NXT2WIFI response
 * @param buf the buffer to pull the string from
 * @param response the string to hold the response from the sensor
 */
void N2WgetStringResponse(const ubyte &buf, string &response)
{
  static tBigByteArray tmpArray;
  memcpy(&tmpArray, &buf, sizeof(tmpArray));

  for (int i = 0; tmpArray[i] != 0; i++)
  {
    writeDebugStream("%c", tmpArray[i]);
    if (tmpArray[i] == '=')
    {
      StringFromChars(response, &tmpArray[i+1]);
      break;
    }
  }
}


/**
 * Write a message to the NXT2WIFI sensor
 * @param buf the buffer to be transmitted
 * @param len the length of the data to be transmitted
 * @return true if no error occured, false if it did
 */
bool N2WRS485write(tMassiveArray &buf, ubyte len)
{
  TFileIOResult res;

  if (nxtGetAvailHSBytes() > 0)
  {
    nxtReadRawHS(&N2Wrxbuffer, nxtGetAvailHSBytes());
    memset(N2Wrxbuffer, 0, sizeof(N2Wrxbuffer));
  }

  // Make sure we're not sending anymore
  while (nxtHS_Status != HS_RECEIVING) EndTimeSlice();


  res = nxtWriteRawHS(buf, len);
  if (res != ioRsltSuccess)
    return false;

  while (nxtHS_Status != HS_RECEIVING) EndTimeSlice();
  return true;
}


/**
 * Read a message from the NXT2WIFI. An optional timeout can be specified.
 * @param buf the buffer in which to store the received data
 * @param len the amount of data received
 * @param timeout optional parameter to specify the timeout, defaults to 100ms
 * @return true if no error occured, false if it did
 */
bool N2WRS485read(tMassiveArray &buf, ubyte &len, int timeout = 100) {
  TFileIOResult res;
	int bytesAvailable = 0;

	memset(N2Wrxbuffer, 0, sizeof(N2Wrxbuffer));

	TMRreset(rxTimer);
  TMRsetup(rxTimer, timeout);

	while(bytesAvailable == 0 && !TMRisExpired(rxTimer)) {
		bytesAvailable = nxtGetAvailHSBytes();
		wait1Msec(10);
	}

	nxtReadRawHS(&buf, bytesAvailable);

  return true;
}


/**
 * Read a very large response from the NXT2WIFI sensor.  This breaks up the
 * reads into many smaller chunks to prevent weirdness on the bus.
 * @param buf the buffer in which to store the received data
 * @param len the amount of data received
 * @param timeout optional parameter to specify the timeout, defaults to 100ms
 * @return true if no error occured, false if it did
 */
bool RS485readLargeResponse(tMassiveArray &buf, int &len, int timeout = 100)
{
  const ubyte chunkSize = 10;
  int bytesAvailable;
	tByteArray tmpBuff;
	int bytesleft = len;
	int bytesToRead = 0;
	int index = 0;
	memset(buf, 0, sizeof(buf));

	TMRreset(rxTimer);
  TMRsetup(rxTimer, timeout);

	while ((bytesleft > 0) && !TMRisExpired(rxTimer))
	{
		memset(tmpBuff, 0, sizeof(tmpBuff));
		bytesAvailable = nxtGetAvailHSBytes();
		bytesToRead = (bytesAvailable > chunkSize) ? chunkSize: bytesAvailable;
		nxtReadRawHS(&tmpBuff, bytesToRead);
		// writeDebugStreamLine("avail: %d", bytesAvailable);
	  // writeDebugStreamLine("index: %d", index);
	  // writeDebugStreamLine("bytesleft: %d, ", bytesleft);
		memcpy(&buf[index], tmpBuff, bytesToRead);
		bytesleft -= bytesToRead;
		index += bytesToRead;
		wait1Msec(5);
	}

	// for (int i = 0; i < len; i++)
  // {
    // writeDebugStream("%c", buf[i]);
    // wait1Msec(1);
  // }
  // writeDebugStreamLine("");
	return (nxtGetAvailHSBytes() == 0);
}


/**
 * Append an array of bytes to a tMassiveArray buffer, starts at index in the buffer
 * and copies nLength bytes.
 * @param buf the buffer to copy to
 * @param index the position in buffer where to start appending to
 * @param pData the data to be appended to buf
 * @param nLength the length of the data to be appended
 * @return the new 'tail' position of buf at which to append.
 */
int N2WappendToBuff(tMassiveArray &buf, const short index, const ubyte &pData, const short nLength)
{
  if (index == 0) memset(buf, 0, sizeof(buf));

  memcpy(&buf[index], &pData, nLength);
  return index + nLength;
}


/**
 * Append a string a tMassiveArray buffer, starts at index in the buffer
 * @param buf the buffer to copy to
 * @param index the position in buffer where to start appending to
 * @param pData the string to be appended to buf
 * @return the new 'tail' position of buf at which to append.
 */
int N2WappendToBuff(tMassiveArray &buf, const short index, string pData)
{
  if (index == 0) memset(buf, 0, sizeof(buf));

  memcpy(&buf[index], pData, strlen(pData));
  return index + strlen(pData);
}


/**
 * Append a string a tBigByteArray buffer, starts at index in the buffer
 * @param buf the buffer to copy to
 * @param index the position in buffer where to start appending to
 * @param pData the string to be appended to buf
 * @param nLength the length of the data to be appended
 * @return the new 'tail' position of buf at which to append.
 */
int N2WappendToBuff(tBigByteArray &buf, const short index, const ubyte &pData, const short nLength)
{
  if (index == 0) memset(buf, 0, sizeof(buf));

  memcpy(&buf[index], &pData, nLength);
  return index + nLength;
}


/**
 * Append a string a tBigByteArray buffer, starts at index in the buffer
 * @param buf the buffer to copy to
 * @param index the position in buffer where to start appending to
 * @param pData the string to be appended to buf
 * @return the new 'tail' position of buf at which to append.
 */
int N2WappendToBuff(tBigByteArray &buf, const short index, const string pData)
{
  if (index == 0) memset(buf, 0, sizeof(buf));

  memcpy(&buf[index], &pData, strlen(pData));
  return index + strlen(pData);
}


/**
 * Enable debugging
 * @param en whether or not to enable debugging
 * @return true if no error occured, false if it did
 */
bool N2WsetDebug(bool en)
{
  N2WscratchString = (en) ? "$DBG1\n" : "$DBG0\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}


/**
 * Connect to the currently configured WiFi network
 * @param custom use the default or custom profile
 * @return true if no error occured, false if it did
 */
bool N2WConnect(bool custom)
{
  N2WscratchString = (custom) ? "$WFC1\n" : "$WFC0\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer, strlen(N2WscratchString));
}


/**
 * Disconnect from the current WiFi network
 * @return true if no error occured, false if it did
 */
bool N2WDisconnect() {
  N2WscratchString = "$WFX\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer, strlen(N2WscratchString));
}


/**
 * Stop reconnecting when disconnected
 * @return true if no error occured, false if it did
 */
void N2WStopConnecting()
{
  N2WscratchString = "$WFQ\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  N2WRS485write(N2Wtxbuffer, strlen(N2WscratchString));
}


/**
 * Delete the currently configured custom profile
 * @return true if no error occured, false if it did
 */
bool N2WDelete()
{
  N2WscratchString = "$WFKD\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer, strlen(N2WscratchString));
}


/**
 * Save the currently configured custom profile
 * @return true if no error occured, false if it did
 */
bool N2WSave() {
  N2WscratchString = "$WFKS\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer, strlen(N2WscratchString));
}


/**
 * Load the currently configured custom profile
 * @return true if no error occured, false if it did
 */
bool N2WLoad() {
  N2WscratchString = "$WFKL\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer, strlen(N2WscratchString));
}


/**
 * Confgure the security settings for the custom profile\n
 * Note: this is an internal function and shouldn't be used directly
 * @param mode the security mode to use
 * @param keypass the keypass to use
 * @param keylen the length of the key
 * @param keyind used for WEP, usually set to 0
 * @return true if no error occured, false if it did
 */
bool N2WSecurity(int mode, const ubyte &keypass, int keylen, int keyind) {
  int index = 0;
  index = N2WappendToBuff((tBigByteArray)N2Wtxbuffer, index, "$WFS?");
  sprintf(N2WscratchString, "%d:", mode);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  index = N2WappendToBuff(N2Wtxbuffer, index, keypass, keylen);
  sprintf(N2WscratchString, ":%d\n", keyind);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  //for (int i = 0; i < index;i++)
  //  {
  //    writeDebugStream("%c", N2Wtxbuffer[i]);
  //    wait1Msec(1);
  //  }
  return N2WRS485write(N2Wtxbuffer, index);
}


/**
 * Set the WPA2 key
 * @param key the WPA2 key to use
 * @param len the length of the WPA2 key
 * @return true if no error occured, false if it did
 */
bool N2WSecurityWPA2Key(tBigByteArray &key, int len) {
	return N2WSecurity(WF_SEC_WPA2_KEY, key, len, 0);
}


/**
 * Set the WPA2 passphrase
 * @param passphrase the WPA2 passphrase to use
 * @return true if no error occured, false if it did
 */
bool N2WSecurityWPA2Passphrase(const string passphrase) {
  writeDebugStreamLine("hallo daar");
  tByteArray tmpArray;
	memcpy(tmpArray, passphrase, strlen(passphrase));
	return N2WSecurity(WF_SEC_WPA2_PASSPHRASE, tmpArray, strlen(passphrase), 0);
}


/**
 * Set the WPA key
 * @param key the WPA key to use
 * @param len the length of the WPA key
 * @return true if no error occured, false if it did
 */
bool N2WSecurityWPAKey(tBigByteArray &key, int len) {
	return N2WSecurity(WF_SEC_WPA_KEY, key, len, 0);
}


/**
 * Set the WPA passphrase
 * @param passphrase the WPA passphrase to use
 * @return true if no error occured, false if it did
 */
bool N2WSecurityWPAPassphrase(string passphrase) {
  tByteArray tmpArray;
	memcpy(tmpArray, passphrase, strlen(passphrase));
	return N2WSecurity(WF_SEC_WPA_PASSPHRASE, tmpArray, strlen(passphrase), 0);
}


/**
 * Set the WEP passphrase.  Please don't use this, it's very insecure.
 * @param passphrase the WEP passphrase to use
 * @return true if no error occured, false if it did
 */
bool N2WSecurityWEP104(string passphrase) {
  tByteArray tmpArray;
	memcpy(tmpArray, passphrase, strlen(passphrase));
	return N2WSecurity(WF_SEC_WEP_104, tmpArray, strlen(passphrase), 0);
}


/**
 * Use no security at all.  Just as effective as WEP but less annoying.
 * @return true if no error occured, false if it did
 */
bool N2WSecurityOpen() {
  tByteArray tmpArray;
	return N2WSecurity(WF_SEC_OPEN, tmpArray , 0, 0);
}


/**
 * Use ad-hoc network or infrastructure
 * @param adhoc if true, use adhoc, otherwise use infrastructure mode
 * @return true if no error occured, false if it did
 */
bool N2WsetAdHoc(bool adhoc) {
  if (adhoc)
    N2WscratchString = "$WFE?TYPE=1\n";
  else
    N2WscratchString = "$WFE?TYPE=0\n";
  //N2WscratchString = (adhoc) ? "$WFE?TYPE=1\n" : "$WFE?TYPE=0\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}


/**
 * Set a specific parameter.\n
 * Note: this is an internal function and should not be used directly.
 * @param type the parameter type
 * @param param the value to pass to the parameter
 * @return true if no error occured, false if it did
 */
bool _N2WsetPar(const string type, const string param) {
  int index = 0;
	N2WscratchString = "$WFE?";
	index = N2WappendToBuff((tBigByteArray)N2Wtxbuffer, index, N2WscratchString);
	index = N2WappendToBuff((tBigByteArray)N2Wtxbuffer, index, type);
	index = N2WappendToBuff((tBigByteArray)N2Wtxbuffer, index, "=");
	index = N2WappendToBuff((tBigByteArray)N2Wtxbuffer, index, param);
	index = N2WappendToBuff((tBigByteArray)N2Wtxbuffer, index, "\n");
  return N2WRS485write(N2Wtxbuffer[0], index);
}


/**
 * Configure to use DHCP.
 * @param yes if set to true, use DHCP
 * @return true if no error occured, false if it did
 */
bool N2WsetDHCP(bool yes) {
  N2WscratchString = (yes) ? "$WFE?DHCP=1\n" : "$WFE?DHCP=0\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}


/**
 * Set the default profileto connect to after initial startup
 * @param profile The profile to connect to, 0 = none, 1 = custom profile, 2 = default
 * @return true if no error occured, false if it did
 */
bool N2WsetDefaultProfile(ubyte profile)
{
  ubyte len;
  sprintf(N2WscratchString, "$COS%d\n", profile);
  memset(N2Wtxbuffer, 0, sizeof(N2Wtxbuffer));
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
  wait1Msec(10);
	N2WRS485read(N2Wrxbuffer, len, 100);
	return (N2WgetNumericResponse(N2Wrxbuffer) == 1);
}


/**
 * Check if a the custom profile exists.
 * @return true if the profile exists, false if it does not or an error occured.
 */
bool N2WCustomExist()
{
  ubyte len;
  N2WscratchString = "$WFKE\n";
  memset(N2Wtxbuffer, 0, sizeof(N2Wtxbuffer));
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
  wait1Msec(10);
	N2WRS485read(N2Wrxbuffer, len, 100);
	return (N2WgetNumericResponse(N2Wrxbuffer) == 1);
}


/**
 * Enter or exit hibernation mode
 * @param hibernate enter hibernation mode if true, exit if false
 * @return true if no error occured, false if it did
 */
bool N2WsetHibernate(bool hibernate)
{
  N2WscratchString = (hibernate) ? "$WFH\n" : "WFO\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}



/**
 * Enable or disable power saving
 * @param powersave enable powersaving if true, disable if false
 * @return true if no error occured, false if it did
 */
bool N2WsetPowerSave(bool powersave)
{
  N2WscratchString = (powersave) ? "$WFP1\n" : "WFP0\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}


/**
 * Get the current connection status.
 * @return true if no error occured, false if it did
 */
int N2WStatus() {
  ubyte len;
  N2WscratchString = "$WFGS\n";
  memset(N2Wtxbuffer, 0, sizeof(N2Wtxbuffer));
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
  N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);
	return N2WgetNumericResponse(N2Wrxbuffer);
}


/**
 * Are we connected to the WiFi network?
 * @return true if connected, false if not connected an error occured
 */
bool N2WConnected() {
	return (N2WStatus() == 2);
}


/**
 * Get the current IP address.  If the address could not be determined
 * 0.0.0.0 is returned.
 * @param IP the string to put the address into
 * @return true if no error occured, false if it did
 */
bool N2WgetIP(string &IP) {
  ubyte len;
  N2WscratchString = "$WFIP.\n";
  memset(N2Wtxbuffer, 0, sizeof(N2Wtxbuffer));
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);
	if (len < 1)
	{
	  IP = "0.0.0.0";
	  return false;
	}
	else
	{
	  N2WgetStringResponse(N2Wrxbuffer, IP);
	  return true;
	}
}


/**
 * Get the NXT2WIFI's MAC address.
 * @param mac string to hold MAC address
 */
void N2WgetMAC(string &mac)
{
  ubyte len;
  sprintf(N2WscratchString, "$MAC\n");
  memset(N2Wtxbuffer, 0, sizeof(N2Wtxbuffer));
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);

	if (len < 1)
	{
	  mac = "00-00-00-00-00-00";
	}
	else
  {
    N2WgetStringResponse(N2Wrxbuffer, mac);
  }
}


/**
 * Open a UDP datastream to a remote host on a port
 * @param id the connection ID to use, can be 1 to 4
 * @param ip the IP address of the remote host
 * @param port the port of the service on the remote host
 * @return true if no error occured, false if it did
 */
bool N2WUDPOpenClient(int id, string ip, int port) {
  int index = 0;
  int respOK = 0;
  ubyte len;
  sprintf(N2WscratchString, "$UDPOC%d?", id);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  index = N2WappendToBuff(N2Wtxbuffer, index, ip);
  sprintf(N2WscratchString, ",%d\n", port);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);
	respOK = N2WgetNumericResponse(N2Wrxbuffer);
	return (respOK == 1) ? true : false;
}


/**
 * Open a listening UDP socket on the specified port
 * @param id the connection ID to use, can be 1 to 4
 * @param port the port on which to start listening
 * @return true if no error occured, false if it did
 */
bool N2WUDPOpenServer(int id, int port) {
  int index = 0;
  int respOK = 0;
  ubyte len;
  sprintf(N2WscratchString, "$UDPOS%d?%d\n", id, port);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);
	respOK = N2WgetNumericResponse(N2Wrxbuffer);
	return (respOK == 1) ? true : false;
}


/**
 * Check if there are bytes available for reading on the specified connection.
 * @param id the connection ID to use, can be 1 to 4
 * @return the number of bytes available for reading
 */
int N2WUDPAvail(int id) {
  int index = 0;
  ubyte len;
  sprintf(N2WscratchString, "$UDPL%d\n", id);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);
	return N2WgetNumericResponse(N2Wrxbuffer);
}


/**
 * Read the specified number of bytes from the connection ID.
 * Bytes are read into the N2Wrxbuffer variable.
 * @param id the connection ID to use, can be 1 to 4
 * @param datalen the number of bytes to read
 * @return true if no error occured, false if it did
 */
int N2WUDPRead(int id, int datalen) {
  int index = 0;
  memset(N2WscratchString, 0, 20);
  ubyte offset;

  sprintf(N2WscratchString, "$UDPR%d?%d\n", id, datalen);
  offset = (datalen > 9) ? 8 : 7;
  datalen += offset;
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
 	N2WchillOut();
 	//wait1Msec(100);
 	RS485readLargeResponse(N2Wrxbuffer, datalen, 100);


 	// This part seperates the data from the pre-amble
	for (int i = 0; i < sizeof(N2Wrxbuffer); i++)
	{
	  if (N2Wrxbuffer[i] != ',')
	  {
	    continue;
	  }
	  else
	  {
	    N2WscratchString = "";
	    memmove(&N2Wrxbuffer[0], &N2Wrxbuffer[offset+1], datalen);
	    // writeDebugStream("size: ");
	    // writeDebugStreamLine(N2WscratchString);
	    return atoi(N2WscratchString);
	  }
	}
	return 0;
}


/**
 * Write the specified number of bytes to the connection ID.
 * @param id the connection ID to use, can be 1 to 4
 * @param data the tHugeByteArray containing the data to be transmitted
 * @param datalen the number of bytes to read
 * @return true if no error occured, false if it did
 */
bool N2WUDPWrite(int id, tHugeByteArray &data, int datalen) {
  // writeDebugStreamLine("N2WUDPWrite");
  int index = 0;
  int respOK;
  memset(N2WscratchString, 0, 20);

  sprintf(N2WscratchString, "$UDPW%d?%d,", id, datalen);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  index = N2WappendToBuff(N2Wtxbuffer, index, data[0], datalen);
  N2WscratchString = "\n";
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
  N2WchillOut();
	respOK = N2WgetNumericResponse(N2Wrxbuffer);
	return (respOK == 1) ? true : false;
}


/**
 * Close the specified connection.  Use 0 to close all connections
 * @param id the connection ID to use, can be 1 to 4 or 0 for all
 * @return true if no error occured, false if it did
 */
bool N2WUDPClose(int id) {
	StringFormat(N2WscratchString, "$UDPX%d\n", id);
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}


/**
 * Flush the buffers of the specified connection
 * @param id the connection ID to use, can be 1 to 4 or 0 for all
 * @return true if no error occured, false if it did
 */
bool N2WUDPFlush(int id) {
	StringFormat(N2WscratchString, "$UDPF%d\n", id);
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}


/**
 * Open a TCP connection to a remote host on a port
 * @param id the connection ID to use, can be 1 to 4
 * @param ip the IP address of the remote host
 * @param port the port of the service on the remote host
 * @return true if no error occured, false if it did
 */
bool N2WTCPOpenClient(int id, string ip, int port) {
  int index = 0;
  int respOK = 0;
  ubyte len;
  sprintf(N2WscratchString, "$TCPOC%d?", id);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  index = N2WappendToBuff(N2Wtxbuffer, index, ip);
  sprintf(N2WscratchString, ",%d\n", port);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);
	respOK = N2WgetNumericResponse(N2Wrxbuffer);
	return (respOK == 1) ? true : false;
}


/**
 * Open a listening UDP socket on the specified port
 * @param id the connection ID to use, can be 1 to 4
 * @param port the port on which to start listening
 * @return true if no error occured, false if it did
 */
bool N2WTCPOpenServer(int id, int port) {
  int index = 0;
  int respOK = 0;
  ubyte len;
  sprintf(N2WscratchString, "$TCPOS%d?%d\n", id, port);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);
	respOK = N2WgetNumericResponse(N2Wrxbuffer);
	return (respOK == 1) ? true : false;
}


/**
 * Closes a connection to a remote client
 * @param id the connection ID to use, can be 1 to 4
 * @return true if no error occured, false if it did
 */
bool N2WTCPDetachClient(int id) {
  int index = 0;
  int respOK = 0;
  ubyte len;
  sprintf(N2WscratchString, "$TCPD%d\n", id);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);
	respOK = N2WgetNumericResponse(N2Wrxbuffer);
	return (respOK == 1) ? true : false;
}


/**
 * Close the specified connection
 * @param id the connection ID to use, can be 1 to 4 or 0 for all
 * @return true if no error occured, false if it did
 */
bool N2WTCPClose(int id) {
	StringFormat(N2WscratchString, "$TCPX%d\n", id);
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}


/**
 * Flush the buffers of the specified connection
 * @param id the connection ID to use, can be 1 to 4 or 0 for all
 * @return true if no error occured, false if it did
 */
bool N2WTCPFlush(int id) {
	StringFormat(N2WscratchString, "$TCPF%d\n", id);
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}


/**
 * Check if there are bytes available for reading on the specified connection.
 * @param id the connection ID to use, can be 1 to 4
 * @return the number of bytes available for reading
 */
int N2WTCPAvail(int id) {
  int index = 0;
  ubyte len;
  sprintf(N2WscratchString, "$TCPL%d\n", id);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);
	return N2WgetNumericResponse(N2Wrxbuffer);
}


/**
 * Read the specified number of bytes from the connection ID.
 * Bytes are read into the N2Wrxbuffer variable.
 * @param id the connection ID to use, can be 1 to 4
 * @param datalen the number of bytes to read
 * @return true if no error occured, false if it did
 */
int N2WTCPRead(int id, int datalen) {
  int index = 0;
  memset(N2WscratchString, 0, 20);
  ubyte offset;

  sprintf(N2WscratchString, "$TCPR%d?%d\n", id, datalen);
  offset = (datalen > 9) ? 8 : 7;
  datalen += offset;
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
 	N2WchillOut();
 	//wait1Msec(100);
 	RS485readLargeResponse(N2Wrxbuffer, datalen, 100);


	for (int i = 0; i < sizeof(N2Wrxbuffer); i++)
	{
	  if (N2Wrxbuffer[i] != ',')
	  {
	    continue;
	  }
	  else
	  {
	    N2WscratchString = "";
	    memmove(&N2Wrxbuffer[0], &N2Wrxbuffer[offset+1], datalen);
	    // writeDebugStream("size: ");
	    // writeDebugStreamLine(N2WscratchString);
	    return atoi(N2WscratchString);
	  }
	}
	return 0;
}


/**
 * Write the specified number of bytes to the connection ID.
 * @param id the connection ID to use, can be 1 to 4
 * @param data the tHugeByteArray containing the data to be transmitted
 * @param datalen the number of bytes to read
 * @return true if no error occured, false if it did
 */
int N2WTCPWrite(int id, tHugeByteArray &data, int datalen)
{
  writeDebugStream("N2WTCPWrite: ");
  writeDebugStreamLine("datalen: %d", datalen);
  int index = 0;
  int respOK;
  memset(N2WscratchString, 0, 20);

  sprintf(N2WscratchString, "$TCPW%d?%d,", id, datalen);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  index = N2WappendToBuff(N2Wtxbuffer, index, data[0], datalen);
  N2WscratchString = "\n";
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
  N2WchillOut();
	respOK = N2WgetNumericResponse(N2Wrxbuffer);
	return (respOK == 1) ? true : false;
}


/**
 * Get the remote client's IP address
 * @param id the connection ID to use, can be 1 to 4
 * @param ip string to hold IP address
 * @return true if no error occured, false if it did
 */
void N2WTCPClientIP(int id, string &ip)
{
  ubyte len;
  sprintf(N2WscratchString, "$TCPSI%d\n", id);
  memset(N2Wtxbuffer, 0, sizeof(N2Wtxbuffer));
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);

	if (len < 1)
	{
	  ip = "0.0.0.0";
	}
	else
  {
    N2WgetStringResponse(N2Wrxbuffer, ip);
  }
}


/**
 * Get the remote client's MAC address.  Only useful for local network client.
 * @param id the connection ID to use, can be 1 to 4
 * @param mac string to hold MAC address
 */
void N2WTCPClientMAC(int id, string &mac)
{
  ubyte len;
  sprintf(N2WscratchString, "$TCPSM%d\n", id);
  memset(N2Wtxbuffer, 0, sizeof(N2Wtxbuffer));
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
	N2WchillOut();
	N2WRS485read(N2Wrxbuffer, len, 100);

	if (len < 1)
	{
	  mac = "00-00-00-00-00-00";
	}
	else
  {
    N2WgetStringResponse(N2Wrxbuffer, mac);
  }
}


/**
 * Enable the built-in webserver.  This cannot be used together with
 * normal TCP/UDP operations.
 * @param enable enables the web server if set to true, disables it if false
 * @return true if no error occured, false if it did
 */
bool N2WenableWS(bool enable) {
  N2WscratchString = (enable) ? "$SRV=1\n" : "$SRV=0\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}

/**
 * Read data from the web server.  This cannot be used together with
 * normal TCP/UDP operations.
 * @param btnID ID of the button that was pressed
 * @param state additional data from the button
 * @return true if no error occured, false if it did
 */
bool N2WreadWS(ubyte &btnID, ubyte &state)
{
  ubyte avail = nxtGetAvailHSBytes();
  if (avail >= 5)
  {
    nxtReadRawHS(&N2Wrxbuffer, avail);
    writeDebugStream("N2WreadWS[%d]: ", avail);
    // for (int i = 0; i < avail; i++)
    // {
      // writeDebugStream("0x%02X ", N2Wrxbuffer[i]);
    // }
    // writeDebugStreamLine("");

    // search for marker of start of message
    // Should be 0x00 0x80 0x14
    for (int i = 0; i < avail - 2; i++)
    {
      if ((N2Wrxbuffer[i] == 0x00) && (N2Wrxbuffer[i+1] == 0x80) && (N2Wrxbuffer[i+2] == 0x14))
      {
        btnID = N2Wrxbuffer[i+3];
        state = N2Wrxbuffer[i+4];
        return true;
      }
    }
  }
  return false;
}


/**
 * Write data to the webserver.
 * @param id the field number in the web page
 * @param wsmessage data to be transmitted
 * @param wsmsglen length of the data
 * @return true if no error occured, false if it did
 */
bool N2WwriteWS(ubyte id, tHugeByteArray &wsmessage, ubyte wsmsglen)
{
	writeDebugStream("N2WwriteWS: ");
	writeDebugStreamLine("datalen: %d", wsmsglen);
	int index = 0;
	int respOK;
	memset(N2WscratchString, 0, 20);

  sprintf(N2WscratchString, "$XD%d?%d,", id, wsmsglen);
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  index = N2WappendToBuff(N2Wtxbuffer, index, wsmessage[0], wsmsglen);
  N2WscratchString = "\n";
  index = N2WappendToBuff(N2Wtxbuffer, index, N2WscratchString);
  N2WRS485write(N2Wtxbuffer[0], index);
  N2WchillOut();
	respOK = N2WgetNumericResponse(N2Wrxbuffer);
	return (respOK == 1) ? true : false;
}


/**
 * Clears all webpage fields.  This cannot be used together with
 * normal TCP/UDP operations.
 * @return true if no error occured, false if it did
 */
bool N2WclearFields() {
  N2WscratchString = "$RXD\n";
  memcpy(N2Wtxbuffer, N2WscratchString, strlen(N2WscratchString));
  return N2WRS485write(N2Wtxbuffer[0], strlen(N2WscratchString));
}


/**
 * Initialise the port, setup buffers
 */
void N2WInitLib() {
  nxtEnableHSPort();
  nxtHS_Mode = hsRawMode;
  nxtSetHSBaudRate(230400);
  rxTimer = TMRnewTimer();
  memset(N2Wrxbuffer, 0, sizeof(N2Wrxbuffer));
  memset(N2Wtxbuffer, 0, sizeof(N2Wtxbuffer));
}

#endif // __N2W_H__

/*
 * $Id: benedettelli-nxt2wifi.h 114 2012-10-08 18:11:44Z xander $
 */
/* @} */
/* @} */
