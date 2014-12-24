
typedef enum direction_ {
    DIR_NONE,
    DIR_RIGHT,
    DIR_LEFT,
    DIR_CENTER,
} ir_direction_t;

typedef enum ir_segment_ {
    IR_SEGMENT_1 = 1,
    IR_SEGMENT_2 = 2,
    IR_SEGMENT_3 = 3,
    IR_SEGMENT_4 = 4,
    IR_SEGMENT_5 = 5,
    IR_SEGMENT_6 = 6,
    IR_SEGMENT_7 = 7,
    IR_SEGMENT_8 = 8,
    IR_SEGMENT_9 = 9,
} ir_segment_t;

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

ir_direction_t get_dir_to_beacon(tSensors link)
{
    int segment;
    ir_direction_t dir;

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

bool is_beacon_in_segment(tSensors link, ir_segment_t target_segment)
{
    int segment;

    segment = HTIRS2readACDir(link);

    nxtDisplayTextLine(5, "Segment: %d, %d", segment, target_segment);

    if (segment == target_segment) {
        return true;
    } else {
        return false;
    }
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

void initialize_receiver(tSensors link, tSensors link2)
{
	// the default DSP mode is 1200 Hz.
	tHTIRS2DSPMode m = DSP_1200;

    HTIRS2setDSPMode(link, m);
    HTIRS2setDSPMode(link2, m);
}

void find_center(tSensors link)
{
    direction_t dir;

    dir = get_dir_to_beacon(link);
    switch (dir) {
    case DIR_RIGHT:
        while (get_dir_to_beacon(link) != DIR_CENTER) {
            rotateClockwise(10);
        }
        allMotorsOff();
        break;
    case DIR_LEFT:
        while (get_dir_to_beacon(link) != DIR_CENTER) {
            rotateCounterClockwise(10);
        }
        allMotorsOff();
        break;
    case DIR_CENTER:
    case DIR_NONE:
        break;
    }

}

void find_absolute_center(tSensors left, tSensors right)
{
    /*
     * Our IR beacons are not calibrated equally.  One gives
     * a much higher reading than the other when far away
     * from the goal.
     */
    //int error_offset = 20;

	int ls1, ls2, ls3, ls4, ls5 = 0;
	int rs1, rs2, rs3, rs4, rs5 = 0;

    find_center(left);
    find_center(right);

    HTIRS2readAllACStrength(left, ls1, ls2, ls3, ls4, ls5);
    HTIRS2readAllACStrength(right, rs1, rs2, rs3, rs4, rs5);

    while (abs(ls3 - rs3) > 5) {
        if (ls3 > rs3) {
            rotateCounterClockwise(5);
        } else {
            rotateClockwise(5);
        }
	    HTIRS2readAllACStrength(left, ls1, ls2, ls3, ls4, ls5);
	    HTIRS2readAllACStrength(right, rs1, rs2, rs3, rs4, rs5);
    }
}

void move_to_beacon(tSensors left, tSensors right, int power, bool log_data)
{
	int s1, s2, s3, s4, s5 = 0;
	int s21, s22, s23, s24, s25 = 0;
    int error;
    int master_power, slave_power;
    float kp;
    bool left_done, right_done;

    // find_absolute_center(left, right);

    left_done = false;
    right_done = false;

    if (log_data == true) {
        dl_init("ir_log.txt", true);
    }

    master_power = power;
    slave_power = power;

    kp = 0.3;
    // error = s3 - s23;
    // slave_power += error * kp;

    while (true) {

	    HTIRS2readAllACStrength(left, s1, s2, s3, s4, s5);
	    HTIRS2readAllACStrength(right, s21, s22, s23, s24, s25);

        /*
         * Assumptions master is left, slave is right
         * s3 is left receiver, s23 is right receiver.
         */
        error = s3 - s23;

        /*
         * If the error is within a "tolerance zone" then
         * we are pointed at the center of the beacon, drive straight.
         */
        if (abs(error) < 8) {
            slave_power = master_power;
        } else {
            slave_power = slave_power + (error * kp);
        }

        // displayString(2, "E: %d", error);
        // displayString(3, "slave: %d", slave_power);
        // displayString(4, "S13: %d, S23: %d", s3, s23);

        /*
         * We want a floor on the slave of stopped
         */
        if (slave_power < 0) {
            slave_power = 0;
        }

        /*
         * To keep the from overshooting implement a ceiling of 1.5 of the master
         */
        if (slave_power > (master_power * 1.5)) {
            slave_power = (master_power * 1.5);
        }

        if (log_data == true) {
            dl_insert_int(master_power);
            dl_append_int(slave_power);
            dl_append_int(s3);
            dl_append_int(s23);
            dl_append_int(error);
            dl_append_int(error * kp);
        }

        /*
         * Compensating for inaccurate receivers.  If we see a zero from the
         * left one then rotate it straight, and read again.
         */
        if (s3 == 0) {
            servo[leftEye] = LSERVO_CENTER;
            HTIRS2readAllACStrength(left, s1, s2, s3, s4, s5);
        }

        /*
         * If we saw a zero from either receiver, then force abort
         * as we are either on top of the beacon or we are off track.
         */
        if ((s3 == 0) || (s23 == 0)) {
            s3 = 180;
            s23 = 180;
        }

        if ((!left_done) && (s3 >= 175)) {
            left_done = true;
            motor[driveRearLeft] = 0;
        } else {
            motor[driveRearLeft] = master_power;
        }

        if ((!right_done) && (s23 >= 175)) {
            right_done = true;
            motor[driveRearRight] = 0;
        } else {
            motor[driveRearRight] = slave_power;
        }

        if (left_done && right_done) {
            break;
        }
        // wait1Msec(50);
    }

    allMotorsOff();

    dl_close();
}
