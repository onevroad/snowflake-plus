package org.snowflake.plus.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "snowflake.plus")
public class SnowflakePlusProperties {

    private WorkType workType = WorkType.local;

    /**
     * init start time
     * default：2019-08-01 00:00:00 (UTC)
     */
    private long startTime = 1564617600000L;

    /**
     * the bit of worker id
     * default：8
     */
    private long workerIdBits = 8L;

    /**
     * the bit of sequence
     * default：12
     */
    private long sequenceBits = 12L;

    /**
     * worker id
     * default：0
     */
    private long workerId = 0L;

    private String name;

    private int listenPort;

    private String address;
}
