package osgi.common.util.modbustcp.slave.db;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osgi.common.util.PropertyUtil;

/**
 * modbus slave 数据池管理系统
 * @author zcc
 * @since 2021年12月13日
 */
public class ModbusPoolDBMS {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusPoolDBMS.class);

    private static Map<String, ModbusPoolDB> instanceHolder = new ConcurrentHashMap<>(4);

    private ModbusPoolDBMS() {
    }

    public static void createInstance(String unitId, int capacity) {
        instanceHolder.putIfAbsent(unitId, new ModbusPoolDB(capacity));
    }

    static Map<String, ModbusPoolDB> getInstanceHolder() {
        return instanceHolder;
    }

    static ModbusPoolDB getInstance(String unitId) {
        return instanceHolder.get(unitId);
    }

    public static int getCapacity(String unitId) {
        ModbusPoolDB db = instanceHolder.get(unitId);
        if (db == null) {
            throw new IllegalArgumentException("unit Id doesn't exist!");
        }
        return db.getCapacity();
    }

    public static DBStatus getStatus(String unitId) {
        return getInstance(unitId).getStatus();
    }

    public static void start(String unitId) {
        if (getStatus(unitId) == DBStatus.RUNNING) {
            return;
        }
        try {
            Serializer.deserialize();
        } catch (IOException e) {
            LOGGER.error("Serializer.deserialize() e,", e);
        }
        setStatus(DBStatus.RUNNING, unitId);
        setRdb(useRDBConfig, rdbPeriodConfig);
    }

    public static void stop(String unitId) {
        if (getStatus(unitId) == DBStatus.STOPPED) {
            return;
        }
        setStatus(DBStatus.STOPPED, unitId);
        setRdb(0, 0);
        try {
            Serializer.serialize();
        } catch (IOException e) {
            LOGGER.error("Serializer.deserialize() e,", e);
        }
    }

    private static int useRDBConfig = PropertyUtil.getAppPropAsInteger("scm.common.modbusslave.useRDB", 0);

    private static long rdbPeriodConfig = PropertyUtil.getAppPropAsLong("scm.common.modbusslave.rdbPeriod",
            10 * 60 * 1000);

    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * use rdb to serialize data at fix rate
     * @param useRDB set 1 to use rdb. else close rdb
     * @param rdbPeriod period to serialize data
     */
    public static void setRdb(int useRDB, long rdbPeriod) {
        if (useRDB == 1) {
            if (scheduledExecutorService.isShutdown() || scheduledExecutorService.isTerminated()) {
                scheduledExecutorService = null;
                scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            }
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                try {
                    Serializer.serialize();
                } catch (IOException e) {
                    LOGGER.error("Serializer.deserialize() e,", e);
                }
            }, 0, rdbPeriod, TimeUnit.MILLISECONDS);
        } else {
            try {
                scheduledExecutorService.awaitTermination(2000, TimeUnit.MILLISECONDS);
                scheduledExecutorService.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private static void setStatus(DBStatus status, String unitId) {
        getInstance(unitId).setStatus(status);
    }

    /**
     * 从当前地址开始(包括)读取n个字.
     *
     * @param add the add
     * @param quantity the quantity
     * @param unitId the unit id
     * @return the short[]
     * @throws TimeoutException 当前地址被写锁定，等待释放超时
     * @throws IndexOutOfBoundsException 输入有误
     */
    public static short[] readHoldingRegister(int add, int quantity, String unitId)
            throws TimeoutException, IndexOutOfBoundsException {
        return getInstance(unitId).readHoldingRegister(add, quantity);
    }

    /**
     * 往当前地址写入一个字.
     *
     * @param add the add
     * @param value the value
     * @param writeTransactionId the write transaction id
     * @param unitId the unit id
     * @throws TransactionUsageException 当前地址被其它事务线程写入锁定
     * @throws IndexOutOfBoundsException 输入有误
     */
    public static void writeSingleRegister(int add, short value, int writeTransactionId, String unitId)
            throws TransactionException, IndexOutOfBoundsException {
        getInstance(unitId).writeSingleRegister(add, value, writeTransactionId);
    }

    /**
     * 往当前地址（包含）写入多个字.
     *
     * @param add the add
     * @param quantity the quantity
     * @param value the value
     * @param writeTransactionId the write transaction id
     * @param unitId the unit id
     * @throws TransactionUsageException 当前地址区间被其它事务线程写入锁定
     * @throws IndexOutOfBoundsException 输入有误
     */
    public static void writeMultiRegister(int add, int quantity, short[] value, int writeTransactionId, String unitId)
            throws TransactionException, IndexOutOfBoundsException {
        getInstance(unitId).writeMultiRegister(add, quantity, value, writeTransactionId);
    }

    public enum DBStatus {
        STARTING(0), RUNNING(1), STOPPED(2);

        int value;

        DBStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        static DBStatus switchValue(int value) {
            switch (value) {
            case 0:
                return STARTING;
            case 1:
                return RUNNING;
            case 2:
                return STOPPED;
            default:
                throw new IllegalArgumentException();
            }
        }
    }

    static class ModbusPoolDB {
        /** The holding registers. */
        final short[] holdingRegisters;

        volatile int[] writeLocks;

        private Object lock = new Object();

        private volatile int status;

        DBStatus getStatus() {
            return DBStatus.switchValue(status);
        }

        void setStatus(DBStatus status) {
            this.status = status.getValue();
        }

        int getCapacity() {
            return holdingRegisters.length;
        }

        ModbusPoolDB(int capacity) {
            this.holdingRegisters = new short[capacity];
            writeLocks = new int[capacity];
        }

        /**
         * @param add
         * @param quantity
         * @param writeTransactionId
         * @return -1 if set lock successful,otherwise return the transctionId by which the address is locked
         */
        private void setWriteLock(int add, int quantity, int writeTransactionId) {
            for (int i = 0; i < quantity; i++) {
                writeLocks[add + i] = writeTransactionId;
            }
        }

        private int checkLock(int add, int quantity, int writeTransactionId) {
            for (int i = 0; i < quantity; i++) {
                int transctionId = writeLocks[add + i];
                if (transctionId != 0 && transctionId != writeTransactionId) {
                    return transctionId;
                }
            }
            return -1;
        }

        short[] readHoldingRegister(int add, int quantity)
                throws TimeoutException, IndexOutOfBoundsException, DatabaseNotInServiceException {
            checkRunning();
            if (add < 0 || quantity <= 0 || add + quantity > holdingRegisters.length) {
                throw new IndexOutOfBoundsException();
            }
            short[] res = new short[quantity];
            int count = 0;
            int cnt = 0;
            for (int i = 0; i < quantity; i++) {
                cnt = 0;
                while (true) {
                    if (cnt > 5 || count > 20) {
                        throw new TimeoutException();
                    }
                    int writeLock = writeLocks[add + i];
                    if (writeLock != 0) {
                        cnt++;
                        count++;
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }
                    res[i] = holdingRegisters[add + i];
                    break;
                }
            }

            return res;
        }

        private void unlockWriteLock(int add, int quantity) {
            for (int i = 0; i < quantity; i++) {
                writeLocks[add + i] = 0;
            }
        }

        void writeSingleRegister(int add, short value, int writeTransactionId)
                throws TransactionException, IndexOutOfBoundsException, DatabaseNotInServiceException {
            checkRunning();
            writeMultiRegister(add, 1, new short[] { value }, writeTransactionId);
        }

        private void checkRunning() {
            if (status != DBStatus.RUNNING.getValue()) {
                throw new DatabaseNotInServiceException();
            }
        }

        void writeMultiRegister(int add, int quantity, short[] value, int writeTransactionId)
                throws TransactionException, IndexOutOfBoundsException, DatabaseNotInServiceException {
            checkRunning();
            if (add < 0 || quantity <= 0 || add + quantity > holdingRegisters.length) {
                throw new IndexOutOfBoundsException();
            }
            int transactionId = checkLock(add, quantity, writeTransactionId);
            if (transactionId != -1) {
                throw new TransactionException("address is locked by transactionId " + transactionId);
            }
            synchronized (lock) {
                setWriteLock(add, quantity, writeTransactionId);
                for (int i = 0; i < quantity; i++) {
                    if (i < value.length) {
                        holdingRegisters[add + i] = value[i];
                    } else {
                        holdingRegisters[add + i] = 0;
                    }
                }
                unlockWriteLock(add, quantity);
            }
        }
    }

}
