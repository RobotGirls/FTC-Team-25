void find_absolute_center
{
	int ls1, ls2, ls3, ls4, ls5 = 0;
	int rs1, rs2, rs3, rs4, rs5 = 0;
	int ldir, rdir;
	int count;

	count = 0;

	eraseDisplay();

	HTIRS2readAllACStrength(left, ls1, ls2, ls3, ls4, ls5);
	HTIRS2readAllACStrength(right, rs1, rs2, rs3, rs4, rs5);

	if ((abs(ls3 - rs3)) >= 20) {
		init_path();
		add_segment(3, 0, 10);
		stop_path();
		dead_reckon();
	}

	HTIRS2readAllACStrength(left, ls1, ls2, ls3, ls4, ls5);
	HTIRS2readAllACStrength(right, rs1, rs2, rs3, rs4, rs5);

	if ((abs(ls3 - rs3)) >= 20) {
        		ls3 = 0;
        		rs3 = 0;
	}

	while (abs(ls3 - rs3) > 3) {
		do_center_rotation(ls3, rs3, reversed);

		HTIRS2readAllACStrength(left, ls1, ls2, ls3, ls4, ls5);
		HTIRS2readAllACStrength(right, rs1, rs2, rs3, rs4, rs5);

		ldir = HTIRS2readACDir(left);
		rdir = HTIRS2readACDir(right);
		nxtDisplayCenteredBigTextLine(2, "L: %d: %d", ls3, ldir);
		nxtDisplayCenteredBigTextLine(4, "R: %d: %d", rs3, rdir);

		if ((ls3 == 0) || (rs3 == 0)) {
			count++;
			reversed = !reversed;
			do_center_rotation(ls3, rs3, reversed);
			if (count >= 5) {
				// ABORT !!
				break;
			}
		}
	}
}
