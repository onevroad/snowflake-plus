package org.snowflake.plus.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "snowflake.plus")
public class SnowflakePlusProperties {

    private ServerType serverType = ServerType.local;

    private int workerId = 0;

    private String name;

    private int port;

    private String address;

    enum ServerType {
        local,
        zookeeper,
        other
    }
}
