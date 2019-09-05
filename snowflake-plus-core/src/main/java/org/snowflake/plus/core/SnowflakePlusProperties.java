package org.snowflake.plus.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "snowflake.plus")
public class SnowflakePlusProperties {

    private WorkIdServerType serverType = WorkIdServerType.local;

    private long workerId = 0L;

    private String name;

    private int listenPort;

    private String address;
}
