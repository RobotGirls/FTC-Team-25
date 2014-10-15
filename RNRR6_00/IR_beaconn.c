task main()
{
typeDef enum dir_{
	DIR_right,
	DIR_left,
	DIR_center,
} Direction;

direction get_DIR_to_beacon(void)
{
	int segment;
	segment=HTIRS2readACDir(irRECIVER);
}
