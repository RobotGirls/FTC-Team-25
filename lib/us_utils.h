
void move_to_object(tSensors link, int cm)
{
    while(SensorValue[link] > cm) {
        motor[driveRearRight] = 35;
        motor[driveRearLeft] = 35;
    }
}
