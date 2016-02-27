package team25core;

/*
 * FTC Team 25: cmacfarl, February 25, 2016
 */


import com.qualcomm.hardware.matrix.MatrixI2cTransaction;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbLegacyModule;
import com.qualcomm.robotcore.hardware.I2cController;
import com.qualcomm.robotcore.hardware.LegacyModulePortDeviceImpl;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.TypeConversion;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;

/**
 * Ultrasonic Sensor
 */
public class Team25UltrasonicSensor extends LegacyModulePortDeviceImpl implements UltrasonicSensor, I2cController.I2cPortReadyCallback {

    private static final boolean debug = false;

    public static final int I2C_ADDRESS = 0x02;
    public static final byte I2C_DATA_OFFSET = 4;

    public static final byte COMMAND_SINGLE_SHOT = 0x01;
    public static final byte COMMAND_CONTINUOUS = 0x02;

    public static final byte OFFSET_COMMAND = 0x41;
    public static final byte OFFSET_DISTANCE = 0x42;

    public static final int BUFFER_LENGTH = 8;

    protected enum I2cTransactionState {
        QUEUED,
        PENDING_I2C_READ,
        PENDING_I2C_WRITE,
        PENDING_READ_DONE,
        DONE
    }

    public class UltrasonicI2cTransaction {

        I2cTransactionState state;

        byte[] buffer;
        byte offset;
        byte len;
        boolean write;

        /*
         * Generic read transaction.
         */
        public UltrasonicI2cTransaction()
        {
            offset = OFFSET_DISTANCE;
            len = BUFFER_LENGTH;
            write = false;
        }

        /*
         * Write the command byte.
         */
        public UltrasonicI2cTransaction(byte data)
        {
            offset = OFFSET_COMMAND;
            buffer = new byte[1];
            buffer[0] = data;
            len = (byte)buffer.length;
            write = true;
        }

        public boolean isEqual(UltrasonicI2cTransaction transaction)
        {
            if (this.offset != transaction.offset) {
                return false;
            } else {
                switch (this.offset) {
                case OFFSET_COMMAND:
                    if (Arrays.equals(this.buffer, transaction.buffer)) {
                        return true;
                    }
                    break;
                default:
                    return false;
                }
            }
            return false;
        }
    };

    public static final int MAX_PORT = 5;
    public static final int MIN_PORT = 4;

    private Lock   readLock;
    private byte[] readBuffer;
    private Lock writeLock;
    private byte[] writeBuffer;
    private ModernRoboticsUsbLegacyModule legacyModule;
    private ConcurrentLinkedQueue<UltrasonicI2cTransaction> transactionQueue;
    private volatile boolean waitingForGodot = false;
    private UltrasonicMemoryMap memoryMap;

    public class UltrasonicMemoryMap {
        byte Measurement_0;
        byte Measurement_1;
        byte Measurement_2;
        byte Measurement_3;
        byte Measurement_4;
        byte Measurement_5;
        byte Measurement_6;
        byte Measurement_7;
    };

    public Team25UltrasonicSensor(ModernRoboticsUsbLegacyModule legacyModule, int physicalPort)
    {
        super(legacyModule, physicalPort);
        this.legacyModule = legacyModule;
        transactionQueue = new ConcurrentLinkedQueue<UltrasonicI2cTransaction>();
        memoryMap = new UltrasonicMemoryMap();
        throwIfPortIsInvalid(physicalPort);
        finishConstruction();
    }

    @Override
    protected void moduleNowArmedOrPretending()
    {
        this.readBuffer  = module.getI2cReadCache(physicalPort);
        this.readLock    = module.getI2cReadCacheLock(physicalPort);
        this.writeBuffer = module.getI2cWriteCache(physicalPort);
        this.writeLock   = module.getI2cWriteCacheLock(physicalPort);

        byte[] buf = {COMMAND_SINGLE_SHOT};
        legacyModule.setWriteMode(physicalPort, I2C_ADDRESS, OFFSET_COMMAND);
        legacyModule.setData(physicalPort, buf, buf.length);
        legacyModule.enable9v(physicalPort, true);
        legacyModule.setI2cPortActionFlag(physicalPort);
        legacyModule.readI2cCacheFromController(physicalPort);

        legacyModule.registerForI2cPortReadyCallback(this, physicalPort);
    }

    @Override
    public double getUltrasonicLevel()
    {
        queueTransaction(new UltrasonicI2cTransaction());
        waitOnRead();

        return TypeConversion.unsignedByteToDouble(memoryMap.Measurement_0);
    }

    public UltrasonicMemoryMap getMemoryMap()
    {
        return memoryMap;
    }

    /*
    * Callback method, will be called by the Legacy Module when the port is ready, assuming we
    * registered that call
    */

    protected boolean queueTransaction(UltrasonicI2cTransaction transaction, boolean force)
    {
        /*
         * Yes, inefficient, but if the queue is more than a few transactions
         * deep we have other problems.  The force parameter allows a controller
         * to queue a transaction regardless of whether or not a matching
         * transaction is already queued.
         */
        if (!force) {
            Iterator<UltrasonicI2cTransaction> it = transactionQueue.iterator();
            while (it.hasNext()) {
                UltrasonicI2cTransaction t = (UltrasonicI2cTransaction)it.next();
                if (t.isEqual(transaction)) {
                    buginf("NO Queue transaction " + transaction.toString());
                    return false;
                }
            }
            /*
             * One might ask if we have a property match, but a value mismatch, why
             * not replace the new value with the old?  That would result in transaction
             * reordering which might not be desirable.  Something to think on.
             */
        }

        /*
         * Doesn't exist, plop it in.
         */
        buginf("YES Queue transaction " + transaction.toString());
        transactionQueue.add(transaction);
        return true;
    }

    protected boolean queueTransaction(UltrasonicI2cTransaction transaction)
    {
        return queueTransaction(transaction, false);
    }

    public void doPing()
    {
        queueTransaction(new UltrasonicI2cTransaction(COMMAND_SINGLE_SHOT));
    }

    @Override
    public void portIsReady(int port)
    {
        if (transactionQueue.isEmpty()) {
            return;
        }

        UltrasonicI2cTransaction transaction = transactionQueue.peek();

        /*
         * If the transaction is in the PENDING_I2C state then if this is a read
         * go fetch the result (and wait for another round trip to this function.
         *
         * If it's a write, we are done, pull it off the transaction queue.
         *
         * Process the next transaction if the queue is not empty.
         */
        if (transaction.state == I2cTransactionState.PENDING_I2C_READ) {
            /*
             * Go do a usb read, and then come back here.
             */
            legacyModule.readI2cCacheFromModule(physicalPort);
            transaction.state = I2cTransactionState.PENDING_READ_DONE;
            return;
        } else if (transaction.state == I2cTransactionState.PENDING_I2C_WRITE) {
            /*
             * It was a write, dequeue it and see if we have anything else.
             */
            transaction = transactionQueue.poll();
            /*
             * Now are we empty?  If so we are done.
             */
            if (transactionQueue.isEmpty()) {
                return;
            }
            /*
             * Not done, grab the next transaction.
             */
            transaction = transactionQueue.peek();
        } else if (transaction.state == I2cTransactionState.PENDING_READ_DONE) {

            handleReadDone();

            transaction = transactionQueue.poll();
            if (transactionQueue.isEmpty()) {
                return;
            }
            transaction = transactionQueue.peek();
        }

        try {
            if (transaction.write) {
                legacyModule.enableI2cWriteMode(port, I2C_ADDRESS, transaction.offset, transaction.len);
                legacyModule.copyBufferIntoWriteBuffer(port, transaction.buffer);
                transaction.state = I2cTransactionState.PENDING_I2C_WRITE;
            } else {
                legacyModule.enableI2cReadMode(port, I2C_ADDRESS, transaction.offset, transaction.len);
                transaction.state = I2cTransactionState.PENDING_I2C_READ;
            }
            legacyModule.writeI2cCacheToController(port);
        } catch (IllegalArgumentException e) {
            RobotLog.e(e.getMessage());
        }
    }

    private void populateMemoryMap()
    {
        try {
            readLock.lock();
            memoryMap.Measurement_0 = readBuffer[I2C_DATA_OFFSET];
            memoryMap.Measurement_1 = readBuffer[I2C_DATA_OFFSET + 1];
            memoryMap.Measurement_2 = readBuffer[I2C_DATA_OFFSET + 2];
            memoryMap.Measurement_3 = readBuffer[I2C_DATA_OFFSET + 3];
            memoryMap.Measurement_4 = readBuffer[I2C_DATA_OFFSET + 4];
            memoryMap.Measurement_5 = readBuffer[I2C_DATA_OFFSET + 5];
            memoryMap.Measurement_6 = readBuffer[I2C_DATA_OFFSET + 6];
            memoryMap.Measurement_7 = readBuffer[I2C_DATA_OFFSET + 7];
        } finally {
            readLock.unlock();
        }
    }

    private void handleReadDone()
    {
        populateMemoryMap();

        synchronized (this) {
            if (waitingForGodot) {
                waitingForGodot = false;
                this.notify();
            }
        }
    }

    private void waitOnRead()
    {
        synchronized(this) {
            waitingForGodot = true;
            try {
                while (waitingForGodot) {
                    this.wait(0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * A convenience function for turning off/on local debugs.
     */
    protected void buginf(String s)
    {
        if (debug) {
            RobotLog.i(s);
        }
    }

    @Override
    public String status() {
        return String.format("NXT Ultrasonic Sensor, connected via device %s, port %d",
                module.getSerialNumber().toString(), physicalPort);  }

    @Override
    public String getDeviceName() {
        return "NXT Ultrasonic Sensor";
    }

    @Override
    public String getConnectionInfo() {
        return module.getConnectionInfo() + "; port " + physicalPort;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String toString()
    {
        return String.format("Ultrasonic: %6.1f", getUltrasonicLevel());
    }

    @Override
    public void close() {
        // take no action
    }

    private void throwIfPortIsInvalid(int port) {
        if (port < MIN_PORT || port > MAX_PORT) {
            throw new IllegalArgumentException(
                    String.format( "Port %d is invalid for " + getDeviceName()+ "; valid ports are %d or %d", port, MIN_PORT, MAX_PORT));
        }
    }
}
