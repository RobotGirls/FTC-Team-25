
task main()
{
    bPlaySounds = true;
    nVolume = 4;

    PlaySoundFile("warnmux.rso");
    while (bSoundActive) { }
}
