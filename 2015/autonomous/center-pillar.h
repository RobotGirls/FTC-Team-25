
#include "../../lib/sensors/drivers/hitechnic-irseeker-v2.h"
#include "../../lib/drivetrain_andymark_defs.h"
#include "../../lib/drivetrain_square.h"
#include "../../lib/dead_reckon.h"
#include "../../lib/data_log.h"
#include "../../lib/ir_utils.h"

#define LSERVO_CENTER 134
#define RSERVO_CENTER 113
#define RSERVO_PERP   235

ir_direction_t dir;

int count;

void move_to_position(int position)
{
	init_path();

	switch (position) {
	case 1:
        // add_segment(52.5, 0, 50);    // move to position 1
        // add_segment(20, -90, 40);    // (starting on ramp)
        break;
    case 2:
        // add_segment(30, -33, 40);       //move to position 2
        servo[rightEye] = RSERVO_PERP;
        add_segment(12, 0, 40);
        add_segment(22, 45, 40);
        break;
    case 3:
        add_segment(22, 0, 40);          //move to position 3
        add_segment(9, 45, 40);
        break;
    }
    stop_path();
  	dead_reckon();
}

task main()
{
    int r_val;
    int l_val;

    ir_direction_t r_dir;
    ir_direction_t l_dir;

    r_val = get_ir_strength(irr_left, IR_STRENGTH_3);
    l_val = get_ir_strength(irr_right, IR_STRENGTH_3);

    r_dir = get_dir_to_beacon(irr_right);
    l_dir = get_dir_to_beacon(irr_right);

    if (r_dir == 5 && l_dir == 5) {
        move_to_position(1);
    }

    if (r_dir == 5 && l_dir == 0) {
        move_to_position(2);
    }
    if (r_dir == 0 && l_dir == 5) {
        move_to_position(3);
    }
}
