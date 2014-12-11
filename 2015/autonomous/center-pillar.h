
#define LSERVO_LEFT_SLANT 110
#define LSERVO_CENTER 145
#define RSERVO_CENTER 113
#define RSERVO_PERP   235

#include "../../lib/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../../lib/drivetrain_andymark_defs.h"
#include "../../lib/drivetrain_square.h"
#include "../../lib/dead_reckon.h"
#include "../../lib/data_log.h"
#include "../../lib/ir_utils.h"

ir_direction_t dir;

int count;

void move_to_position(int position)
{
    int i;

	init_path();

    for (i = 0; i < position; i++) {
        playImmediateTone(60, 50);
        wait1Msec(2000);
    }

	switch (position) {
	case 1:
        // add_segment(52.5, 0, 50);    // move to position 1
        // add_segment(20, -90, 40);    // (starting on ramp)
        break;
    case 2:
        // add_segment(30, -33, 40);       //move to position 2
        add_segment(6, 0, 40);
        add_segment(22, -45, 40);
        add_segment(0, 90, 30);
        break;
    case 3:
        add_segment(6, 0, 40);          //move to position 3
        add_segment(78, -45, 40);
        add_segment(0, 135, 30);
        break;
    }
    stop_path();
  	dead_reckon();
}

/*
 * The idea here is that we will use the left side narrow segment
 * of the left receiver to do a rotational sweep and attempt to
 * identify whether the beacon is in position 1 or position 2.
 *
 * We do this by looking at the servo position when segment 4
 * registers.  If we can determine servo position ranges for
 * the beacon in two different locations then we are good, we
 * don't care about the third because if we don't see it in
 * either of those two ranges, we know it's in the third position
 */
#define SWEEP_START 230
#define SWEEP_END   90
#define POS_1_START 0
#define POS_1_END   0
#define POS_2_START 0
#define POS_2_END   0

int where_is_beacon()
{
    int position;
    int val;

    position = 3;

    servoChangeRate[leftEye] = 0;
    servo[leftEye] = SWEEP_START;
    wait1Msec(1000);

    servoChangeRate[leftEye] = 1;
    servo[leftEye] = SWEEP_END;
    val = ServoValue[leftEye];
    while (val > SWEEP_END) {
        if (is_beacon_in_segment(irr_left, IR_SEGMENT_4)) {
            /*
             * Stop the servo
             */
            servo[leftEye] = val;
            if ((val > POS_1_START) && ( val < POS_1_END)) {
                position = 1;
            } else if ((val > POS_2_START) && (val < POS_2_END)) {
                position = 2;
            } else {
                position = 3;
            }
            break;
        }
	    val = ServoValue[leftEye];
    }

    return position;
}

task main()
{
    int r_val;
    int l_val;

    ir_direction_t r_dir;
    ir_direction_t l_dir;

    initialize_receiver(irr_left, irr_right);

    wait1Msec(500);

    move_to_position(where_is_beacon());

    servo[rightEye] = RSERVO_CENTER;
    servo[leftEye] = LSERVO_CENTER;

    wait1Msec(1000);

    move_to_beacon(irr_left, irr_right, 15, true);

    init_path();
    add_segment(12, 0, 40);
    stop_path();
    dead_reckon();
}
