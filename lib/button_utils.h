
bool any_trigger_pressed() {
	if (joy2Btn(Btn5) || joy2Btn(Btn7)) {
		return true;
	} else {
		return false;
	}
}
