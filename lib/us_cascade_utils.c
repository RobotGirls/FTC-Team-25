#ifdef __HTSMUX_SUPPORT__
int ultrasound(tMUXSensor us_sensor, int distance, int ultrasound_dist1, int ultrasound_dist3)
#else
int ultrasound(tSensors us_sensor, int distance, int ultrasound_dist1, int ultrasound_dist3)
#endif
{
    int s_val;

    init_path();
    add_segment(distance, 0, 45);
    stop_path();
    dead_reckon();

    wait1Msec(1000);

#ifdef __HTSMUX_SUPPORT__
    s_val = USreadDist(us_sensor);
#else
    s_val = SensorValue[us_sensor];
#endif

    if (s_val < ultrasound_dist3) {
        return 3;
    } else if (s_val < ultrasound_dist1) {
        return 1;
    } else {
        return 2;
    }
}
