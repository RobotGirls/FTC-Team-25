
void move_to_position(int position)
{
	init_path();

	switch (position) {
	case 1:
        add_segment(-40, -90, 50);
        add_segment(-24, 90, 50);
        add_segment(0, 90, 45);
        break;
    case 2:
        add_segment(-40, 90, 40);
        add_segment(0, -145, 40);
        break;
    case 3:
        break;
    }
    stop_path();
  	dead_reckon();
}

task main()
{
    int i;
    int center_position;

    servo[leftEye] = LSERVO_CENTER;
    servo[rightEye] = RSERVO_CENTER;

    ir_direction_t dir;

    center_position = ultrasound(carrot, -24, US_DIST_POS_1, US_DIST_POS_3);

    for (i = 0; i < center_position; i++) {
        playImmediateTone(251, 50);
        wait1Msec(1000);
    }

    wait1Msec(1000);

    move_to_position(center_position);
    // move_to_beacon_mux(irr_left, irr_right, 20, true);
    // move_to_object(carrot, 5, 16);

    nxtDisplayTextLine(3, "Sensor sees %d", SensorValue[carrot]);
    while(true) {
    }
}
