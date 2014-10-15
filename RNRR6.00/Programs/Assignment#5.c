typedef enum dir_ {
		dir_right,
		dir_left,
		dir_center,
	} direction;

	direction dir = get_dir_to_beacon();

task main() {
	while(true) {
		switch (dir) {

			case 1:
			case 2:
			case 3:
			case 4:
			nxtDisplayTextLine(3, "Target is to the left.");
			break;

			case 5:
			nxtDisplayTextLine(3, "Target is forward.");
			break;

			case 6:
			case 7:
			case 8:
			case 9:
			nxtDisplayTextLine(3, "Target is to the right.");
			break;

			default:
			nxtDisplayTextLine(3, "Where is the target?");
		}
	}
}
