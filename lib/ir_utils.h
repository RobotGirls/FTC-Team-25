
typedef enum direction_ {
    DIR_NONE,
    DIR_RIGHT,
    DIR_LEFT,
    DIR_CENTER,
} direction_t;

/*
 * The IR Receiver can return the strength
 * from 5 different segments (unlike position
 * which has 9 segments.
 */

typedef enum ir_segment_strength_ {
    IR_STRENGTH_1,
    IR_STRENGTH_2,
    IR_STRENGTH_3,
    IR_STRENGTH_4,
    IR_STRENGTH_5,
} ir_segment_strength_t;

direction_t get_dir_to_beacon(tSensors link)
{
    int segment;
    direction_t dir;

    segment = HTIRS2readACDir(link);

    switch (segment) {
    case 0:
        dir = DIR_NONE;
        break;
    case 1:
    case 2:
    case 3:
    case 4:
        dir = DIR_LEFT;
        break;
    case 5:
        dir = DIR_CENTER;
        break;
    case 6:
    case 7:
    case 8:
    case 9:
        dir = DIR_RIGHT;
        break;
    }

    return dir;
}

/*
 * Simplifies getting the strength for any one segment
 *
 * Returns -1 on error.
 */
int get_ir_strength(tSensors link, ir_segment_strength_t seg)
{
	int strength1, strength2, strength3, strength4, strength5;

    if (!HTIRS2readAllACStrength(link, strength1, strength2, strength3, strength4, strength5)) {
        return -1;
    }

    switch (seg) {
    case IR_STRENGTH_1:
        return strength1;
        break;
    case IR_STRENGTH_2:
        return strength2;
        break;
    case IR_STRENGTH_3:
        return strength3;
        break;
    case IR_STRENGTH_4:
        return strength4;
        break;
    case IR_STRENGTH_5:
        return strength5;
        break;
    default:
        return -1;
    }
}


/*
 * Beacon finding.
 *
 * IR receiver must be predefined and called 'irr'
 *
 * Can we use the PID principle to drive motors toward a beacon?
 *
 * Using the narrow band segment, 4, take the error of relative
 * strengths to drive a slave motor into a turn to either direction.
 *
 * The strength will oscillate between strength readings 2 and 3.
 * If 2 > 3 then right (slave) must speed up, if the reverse then
 * right (slave) must slow down to induce a right hand turn.
 */

void initialize_receiver(tSensors link)
{
	// the default DSP mode is 1200 Hz.
	tHTIRS2DSPMode m = DSP_1200;

    HTIRS2setDSPMode(link, m);
}

void move_to_beacon(tSensors link, int power)
{
	int _dirAC = 0;
	int s1, s2, s3, s4, s5 = 0;
    int error;
    int master_power, slave_power;
    float kp;
    bool done;

    master_power = power;
    slave_power = power;

	_dirAC = HTIRS2readACDir(link);

	// Read the individual signal strengths of the internal sensors
	// Do this for both unmodulated (DC) and modulated signals (AC)
	HTIRS2readAllACStrength(link, s1, s2, s3, s4, s5);

    kp = 1.0;
    error = s2 - s3;
    slave_power += error * kp;
    done = false;

    while (!done) {
        motor[driveRearLeft] = master_power;
        motor[driveRearRight] = slave_power;
	    if (!HTIRS2readAllACStrength(link, s1, s2, s3, s4, s5)) {
            wait1Msec(10);
		    continue;
        }
        error = s2 - s3;
        slave_power += error * kp;

        displayString(2, "E: %d", error);
        displayString(3, "slave: %d", slave_power);

        if ((s2 >= 200) || (s3 >= 200)) {
            done = true;
        }
    }
}
