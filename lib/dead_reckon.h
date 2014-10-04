
#define MAX_SEGMENTS 16

/*
 * A segment in a dead reckoning path
 *
 * turn:   rotation to perform prior to moving
 * inches: distance to move
 */
typedef struct segment_ {
    int turn;
    int inches;
    int speed;
} segment_t;

segment_t path[MAX_SEGMENTS];

static int segment_idx = 0;

void init_path()
{
    segment_idx = 0;
}

void add_segment(int inches, int turn, int speed)
{
    if (segment_idx >= MAX_SEGMENTS) {
        nxtDisplayTextLine(3, "Too many segments");
        playImmediateTone(60, 100);
        wait1Msec(1000);
        return;
    }

    path[segment_idx].inches = inches;
    path[segment_idx].turn = turn;
    path[segment_idx].speed = speed;

    segment_idx++;
}

void stop_path()
{
    add_segment(0, 0, 0);
}

/*
 * dead reckon a specific path.  seg_array is an array
 * of segments terminated by a segment of zero length and
 * no turn.
 */
void dead_reckon()
{
    int idx = 0;

    while (true) {
        if ((path[idx].turn == 0) && (path[idx].inches == 0)) {
            //move(0, DIR_FORWARD, 0);
            return;
        }

        if (path[idx].turn != 0) {
            turnEncoder(path[idx].turn, path[idx].speed);
        }
        wait1Msec(300);
        if (path[idx].inches != 0) {
            if (path[idx].inches > 0) {
                move(path[idx].inches, DIR_FORWARD, path[idx].speed);
            } else {
                move(abs(path[idx].inches), DIR_BACKWARD, abs(path[idx].speed));
            }
        }
        idx++;
        wait1Msec(300);
    }
}
