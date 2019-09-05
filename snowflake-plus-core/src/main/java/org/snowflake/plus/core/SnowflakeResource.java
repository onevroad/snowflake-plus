package org.snowflake.plus.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SnowflakeResource {

    private WorkIdServerType serverType;

    private long workerId;

    private String name;

    private String listenPort;

    private String address;
}
