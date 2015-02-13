
void raise_arm()
{
    motor[shoulder] = 30;
    while (nMotorEncoder[shoulder] < UPCOUNTS) {        // While encoder counts is less than UPCOUNTS, move shoulder at 15
    }                                                   // power. When encoder counts surpasses UPCOUNTs, stop shoulder.
    motor[shoulder] = 0;

    servoChangeRate[arm] = 0;
    servo[arm] = SERVO_ARM_EXTENDED;                    // Move the arm to the extended position.

    wait1Msec(12000);
}

void score_center_goal(int dump_dist)          // Function that moves the robot to the correct distance, raises
{                                                       // the arm, and spins the balls into the tube.
    move_to_object(carrot, -5, dump_dist);              // Move robot to position where it can dump balls in tube.

    servo[door] = SERVO_DOOR_CENTERGOAL_RAMP;           // Open the servo door (releases balls).

    //servo[brush] = 255;                                 // Rotate brush (spins balls into center goal).
    //wait1Msec(2000);
    //servo[brush] = 127;
    //while (true) {
	    servo[brush] = 255;
	    wait1Msec(500);
        servo[brush] = 127;
	    //servo[brush] = 255;
        //wait1Msec(200);
    //}
}
