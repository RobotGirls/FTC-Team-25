
#define MAIN_BUTTON   3
#define LEFT_ARROW    2
#define RIGHT_ARROW   1
#define CANCEL_BUTTON 0

static bool debounce = false;

task debounceTask()
{
    debounce = true;
    wait1Msec(500);
    debounce = false;
}

bool isMainButtonPressed()
{
    if ((!debounce) && (nNxtButtonPressed == MAIN_BUTTON)) {
        StartTask(debounceTask);
        return true;
    } else {
        return false;
    }
}

bool isLeftArrowButtonPressed()
{
    if ((!debounce) &&(nNxtButtonPressed == LEFT_ARROW)) {
        StartTask(debounceTask);
        return true;
    } else {
        return false;
    }
}

bool isRightArrowButtonPressed()
{
    if ((!debounce) &&(nNxtButtonPressed == RIGHT_ARROW)) {
        StartTask(debounceTask);
        return true;
    } else {
        return false;
    }
}

bool isCancelButtonPressed()
{
    if ((!debounce) &&(nNxtButtonPressed == CANCEL_BUTTON)) {
        StartTask(debounceTask);
        return true;
    } else {
        return false;
    }
}
