package org.snowflake.plus.core;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class SnowflakeIDGenImpl implements SnowflakeIDGen {

    /**
     * 初始时间：2019-07-18 08:00:00 (UTC+8)
     */
    private final long twepoch = 1563408000000L;
    /**
     * workerId的bit位数
     */
    private final long workerIdBits = 10L;
    /**
     * 最大能够分配的workerId =1023
     */
    private final long maxWorkerId = ~(-1L << workerIdBits);
    /**
     * sequence的bit位数
     */
    private final long sequenceBits = 12L;

    private final long workerIdShift = sequenceBits;

    private final long timestampLeftShift = sequenceBits + workerIdBits;

    private final long sequenceMask = ~(-1L << sequenceBits);

    private long workerId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    private boolean initFlag = false;

    private static final Random RANDOM = new Random();

    public SnowflakeIDGenImpl(long workerId) {
        this.workerId = workerId;
        checkWorkId();
    }

    public SnowflakeIDGenImpl(SnowflakeResource resource, SnowflakeNodeHolder holder) {
        SnowflakeLocalConfigService localConfigService = new SnowflakeLocalConfigService(resource);
        boolean initFlag = holder.init();
        if (initFlag) {
            this.initFlag = true;
            this.workerId = holder.getWorkerId();
            resource.setWorkerId(this.workerId);
            log.info("START SUCCESS USE ZK WORKERID-{}", this.workerId);
        } else {
            try {
                this.workerId = localConfigService.loadLocalWorkId();
                resource.setWorkerId(this.workerId);
            } catch (Exception e) {
                log.error("Read file error ", e);
                this.workerId = resource.getWorkerId();
            }
            if (this.workerId == 0) {
                log.info("START SUCCESS USE DEFAULT WORKERID-{}", this.workerId);
            } else {
                log.info("START SUCCESS USE CONFIG WORKERID-{}", this.workerId);
            }
        }
        checkWorkId();
    }

    private void checkWorkId() {
        Preconditions.checkArgument(this.workerId >= 0 && this.workerId <= maxWorkerId, "workerID must gte 0 and lte 1023");
    }

    @Override
    public boolean init() {
        return initFlag;
    }

    @Override
    public synchronized IdResult get() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        return new IdResult(-1, Status.EXCEPTION);
                    }
                } catch (InterruptedException e) {
                    log.error("wait interrupted");
                    return new IdResult(-2, Status.EXCEPTION);
                }
            } else {
                return new IdResult(-3, Status.EXCEPTION);
            }
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //seq 为0的时候表示是下一毫秒时间开始对seq做随机
                sequence = RANDOM.nextInt(100);
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = RANDOM.nextInt(100);
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
        return new IdResult(id, Status.SUCCESS);

    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

}
