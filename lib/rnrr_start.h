
void RNRR_waitForStart()
{
    while (true)
    {
      if (nNxtButtonPressed == 1) {
          disableDiagnosticsDisplay();
          eraseDisplay();
          nxtDisplayCenteredTextLine(1, "WARNING: OVERRIDE");
          for (int i = 0; i < 5; i++) {
              nxtDisplayCenteredBigTextLine(4, "%d", 5 - i);
              PlayImmediateTone(60, 80);
              wait1Msec(1000);
          }
          break;
      }
      getJoystickSettings(joystick);
      if (!joystick.StopPgm)
          break;
    }
    return;
}
