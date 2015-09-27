

/*
 * Will send an i2c transaction to a device on the sensor
 * mux and will block and alert the user if the device doesn't
 * ack.  Designed to be used during competition to avoid a
 * situation where the drive team fails to turn on the mux.
 *
 * Use with the IRSeeker
 */
void validateMuxAndBlock(const tMUXSensor ir)
{
    bPlaySounds = true;
    nVolume = 4;

    while (HTIRS2readACDir(ir) < 0) {
        PlaySoundFile("warnmux.rso");
        while (bSoundActive) { }
    }
}
