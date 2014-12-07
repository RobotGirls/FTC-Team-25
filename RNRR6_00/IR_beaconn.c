
typedef enum dir_{
	right,
	left,
	center,
} direction;

direction get_dir_to_beacon (void)
{
	int segment;
	segment = HTIRS2readACDir(irRECIVER);

	switch (segment) {

	case 1:
	case 2:
	case 3:
	case 4:
		return left;
		break;

	case 5:
		return center;
		break;

	case 6:
	case 7:
	case 8:
	case 9:
		return right;
	 	break;
	}
}

task main()
{
	while (true) {
 	direction get_dir_to_beacon;

	switch

		case left:
		return nxtDisplayTextLine ( 5 , "turning left" )
		break;

		case right:
		return nxtDisplayTextLine ( 5 , "turning right" )
		break;

		case center:
		return nxtDisplayTextLine ( 5 , "moving foward" )
	}
}
