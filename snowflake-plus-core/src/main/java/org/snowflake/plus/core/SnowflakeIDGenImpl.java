package org.snowflake.plus.core;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class SnowflakeIDGenImpl implements SnowflakeIDGen {

    private long startTime;

    private long workerIdBits;
    /**
     * the max number of workerId, 8bit is 255
     */
    private long maxWorkerId;

    private long sequenceBits;

    private long workerIdShift;

    private long timestampLeftShift;

    private long sequenceMask;

    private long workerId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    private static final Random RANDOM = new Random();

    public SnowflakeIDGenImpl(SnowflakeResource resource) {
        init(resource);
        log.info("START SUCCESS USE " + resource.getServerType().name() + " WORKERID-{}", this.workerId);
        checkWorkId();
    }

    public SnowflakeIDGenImpl(SnowflakeResource resource, SnowflakeNodeHolder holder) {
        SnowflakeLocalConfigService localConfigService = new SnowflakeLocalConfigService(resource);
        boolean initFlag = holder.init();
        if (initFlag) {
            resource.setWorkerId(holder.getWorkerId());
            init(resource);
            log.info("START SUCCESS USE {} WORKERID-{}", resource.getServerType(), this.workerId);
        } else {
            boolean loadFileFlag = false;
            try {
                resource.setWorkerId(localConfigService.loadLocalWorkId());
                loadFileFlag = true;
            } catch (Exception e) {
                log.error("Read file error ", e);
            }
            init(resource);
            if (loadFileFlag) {
                log.info("START SUCCESS USE LOCAL FILE WORKERID-{}", this.workerId);
            } else {
                log.info("START SUCCESS USE CONFIG WORKERID-{}", this.workerId);
            }
        }
        checkWorkId();
    }

    private void init(SnowflakeResource resource) {
        this.startTime = resource.getStartTime();
        this.workerIdBits = resource.getWorkerIdBits();
        this.maxWorkerId = ~(-1L << this.workerIdBits);
        this.sequenceBits = resource.getSequenceBits();
        this.workerIdShift = this.sequenceBits;
        this.timestampLeftShift = this.sequenceBits + this.workerIdBits;
        this.sequenceMask = ~(-1L << this.sequenceBits);
        this.workerId = resource.getWorkerId();
    }

    private void checkWorkId() {
        Preconditions.checkArgument(this.workerId >= 0 && this.workerId <= maxWorkerId, "workerID must gte 0 and lte 1023");
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
                sequence = RANDOM.nextInt(10);
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = RANDOM.nextInt(10);
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - startTime) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
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
