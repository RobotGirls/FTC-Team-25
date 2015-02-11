
void score_center_goal(int arm_dist, int dump_dist)     // Function that moves the robot to the correct distance, raises
{                                                       // the arm, and spins the balls into the tube.
    move_to_object(carrot, -5, dump_dist);              // Move robot to position where it can dump balls in tube.

    servo[door] = SERVO_DOOR_CENTERGOAL_RAMP;           // Open the servo door (releases balls).

    //servo[brush] = 255;                                 // Rotate brush (spins balls into center goal).
    //wait1Msec(2000);
    //servo[brush] = 127;
    //while (true) {
	    servo[brush] = 0;
	    wait1Msec(137);
        servo[brush] = 127;
	    //servo[brush] = 255;
        //wait1Msec(200);
    //}
}
