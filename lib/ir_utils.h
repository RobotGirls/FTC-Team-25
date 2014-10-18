
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

/*
 * Simplifies getting the strength for any one segment
 *
 * Returns -1 on error.
 */
int get_ir_strength(ir_segment_strength_t seg)
{
	int strength1, strength2, strength3, strength4, strength5;

    if (!HTIRS2readAllACStrength(IRSeeker, strength1, strength2, strength3, strength4, strength5)) {
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
