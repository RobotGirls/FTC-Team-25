
/*
 * Tells the drivetrain layer that we are using
 * four motors to drive the wheels instead either
 * just two motors, or two motors and chain.
 */
// #define FOUR_WHEEL_DRIVE

/*
 * If defined, put definitions for devices on the
 * mux in mux_defs.h.  If not defined and the same
 * definitions are in the pragmas, then software will
 * build.
 */
// #define USING_SENSOR_MUX

#define ENCPERINCH              118.75
#define ENC_TICKS_PER_DEGREE    18.5

#define SERVO_DOOR_OPEN             103
#define SERVO_DOOR_CENTERGOAL_RAMP  21
#define SERVO_DOOR_CLOSED           45
