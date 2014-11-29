
TFileIOResult nIoResult;
TFileHandle hFileHandle;
short nFileSize = 32767;
bool _timestamp = false;

void dl_init(char* sFileName, bool timestamp)
{
	Delete(sFileName, nIoResult);
	hFileHandle = 0;
	OpenWrite(hFileHandle, nIoResult, sFileName, nFileSize);

	_timestamp = timestamp;
}

void dl_insert_int(int data)
{
	char tmp[20];

    if (_timestamp) {
		sprintf(tmp, "\n%d, %d, ", nPgmTime, data);
    } else {
		sprintf(tmp, "\n%d, ", data);
    }
	WriteString(hFileHandle, nIoResult, tmp);
}

void dl_append_int(int data)
{
	char tmp[20];

	sprintf(tmp, "%d, ", data);
	WriteString(hFileHandle, nIoResult, tmp);
}

void dl_insert_float(float data)
{
	char tmp[20];

    if (_timestamp) {
		sprintf(tmp,"\n%d, %f, ", nPgmTime, data);
    } else {
		sprintf(tmp,"\n%f, ", data);
    }
	WriteString(hFileHandle, nIoResult, tmp);
}

void dl_append_float(float data)
{
	char tmp[20];

	sprintf(tmp,"%f, ", data);
	WriteString(hFileHandle, nIoResult, tmp);
}

void dl_close()
{
	Close(hFileHandle, nIoResult);
}
