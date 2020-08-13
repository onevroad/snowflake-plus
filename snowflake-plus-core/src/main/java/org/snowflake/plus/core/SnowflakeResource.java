package org.snowflake.plus.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SnowflakeResource {

    private WorkType serverType;

    private long startTime;

    private long workerIdBits;

    private long sequenceBits;

    private long workerId;

    private String name;

    private String listenPort;

    private String address;
}
