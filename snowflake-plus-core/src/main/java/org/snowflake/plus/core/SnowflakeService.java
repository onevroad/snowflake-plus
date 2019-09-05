package org.snowflake.plus.core;

import lombok.extern.slf4j.Slf4j;
import org.snowflake.plus.core.exception.InitException;

@Slf4j
public class SnowflakeService {

    private SnowflakeIDGen idGen;

    public SnowflakeService(String name, String address, int port, int workerId, SnowflakePlusProperties.ServerType serverType) throws InitException {
        if (serverType.equals(SnowflakePlusProperties.ServerType.local)) {
            idGen = new SnowflakeIDGenImpl(workerId);
        } else if (serverType.equals(SnowflakePlusProperties.ServerType.zookeeper)) {
            idGen = new SnowflakeIDGenImpl(name, port, workerId, new SnowflakeZookeeperHolder(name, String.valueOf(port), address));
        } else {
            idGen = new SnowflakeIDGenImpl(name, port, workerId, new SnowflakeZookeeperHolder(name, String.valueOf(port), address));
        }
        if (idGen.init()) {
            log.info("Snowflake Service Init Successfully");
        } else {
            throw new InitException("Snowflake Service Init Fail");
        }
    }

    public long getId() {
        IdResult idResult = idGen.get();
        if (idResult.getStatus().equals(Status.EXCEPTION)) {
            log.error("Generate id error: {}", idResult.toString());
        }
        return idResult.getId();
    }
}
