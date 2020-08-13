package org.snowflake.plus.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "snowflake.plus")
public class SnowflakePlusProperties {

    private WorkType workType = WorkType.local;

    /**
     * 初始时间：2019-08-01 00:00:00 (UTC)
     */
    private long startTime = 1564617600000L;

    private long workerIdBits = 8L;

    private long sequenceBits = 12L;

    private long workerId = 0L;

    private String name;

    private int listenPort;

    private String address;
}
