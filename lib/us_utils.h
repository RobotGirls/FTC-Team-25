
void move_to_object(tSensors link, int speed, int cm)
{
    motor[driveRearRight] = speed;
    motor[driveRearLeft] = speed;

    while (SensorValue[link] > cm) {
        /* Noop, or a wait for that condition to be true */
    }

    motor[driveRearLeft] = 0;
    motor[driveRearRight] = 0;

}

void move_to_object_mux(tMUXSensor link, int speed, int cm)
{
    motor[driveRearRight] = speed;
    motor[driveRearLeft] = speed;

    while (SensorValue[link] > cm) {
        /* Noop, or a wait for that condition to be true */
    }

    motor[driveRearLeft] = 0;
    motor[driveRearRight] = 0;

}
