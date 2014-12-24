
int count;
int beep;

void move_to_position(int position)
{
	init_path();

	switch (position) {
	case 1:
        add_segment(-53.5, 30, 40);
        add_segment(0, -120, 40);
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

    servo[leftEye] = LSERVO_CENTER;
    servo[rightEye] = RSERVO_CENTER;

    ir_direction_t dir;

    init_path();

    add_segment(-24, 0, 50);
    stop_path();
    dead_reckon();

    if (SensorValue[carrot] < 60) {
        beep = 3;
    } else if (SensorValue[carrot] < 80) {
        beep = 1;
        move_to_position(1);
    } else {
        beep = 2;
        move_to_position(2);
    }

    move_to_beacon(irr_left, irr_right, 20, true);
    move_to_object(carrot, 5, 16);

    for (i = 0; i < beep; i++) {
        playImmediateTone(251, 50);
        wait1Msec(1000);
    }

    nxtDisplayTextLine(3, "Sensor sees %d", SensorValue[carrot]);
    while(true) {
    }
}
