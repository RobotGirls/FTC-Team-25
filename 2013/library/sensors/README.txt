/*
 * $Id: README.txt 114 2012-10-08 18:11:44Z xander $
 */

-= Note =-
These drivers are for RobotC 3.51 and higher and are part of the
version 3.0 final of the 3rd Party RobotC Drivers suite.

What's new in 3.0 final?
* Changed my email address in all the files to a botbench.com one
* Added HiTechnic Force Sensor driver + example programs
* Added Mindsensors LigthSensorArray + example programs (one of which is a calibration program)
* Modified Mindsensors IMU driver.  You can now only retrieve all 3 axes at once, this is a limitation of the sensor.  The ability to retrieve the magnetic fields is now also possible.
* Fixed a bug in the SuperPro Experiment 2 program where not all digital IOs were set to output.
* Implemented a new function in the Firgelli Linear Actuator driver, you can now issue a STOP, added 3rd test program to demonstrate how to use it.

Please note that the NXT2WIFI driver is currently a little funky.  There have been some hardware and firmware changes and I'm waiting for the hardware to come in.  My driver is based on a previous generation of pre-production hardware.  Reset assured that when the new hardware comes in, I will update my driver to make it work again.

-= API Documentation =-
The complete API documentation can be found in the html folder.
Just point your browser at the index.html file.

-= Downloads and support =-
These drivers can also be downloaded from:
http://sourceforge.net/projects/rdpartyrobotcdr/

The documentation is hosted here:
http://botbench.com/driversuite/

For support questions, please use the RobotC 3rd party sensors forum:
http://www.robotc.net/forums/viewforum.php?f=41

Thanks,
Xander Soldaat (xander@botbench.com)

/*
 * $Id: README.txt 114 2012-10-08 18:11:44Z xander $
 */
