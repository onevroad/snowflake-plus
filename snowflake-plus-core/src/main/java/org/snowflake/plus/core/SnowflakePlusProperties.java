package org.snowflake.plus.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "snowflake.plus")
public class SnowflakePlusProperties {

    private String name;

    private Integer port;

    private String zkAddress;
}
