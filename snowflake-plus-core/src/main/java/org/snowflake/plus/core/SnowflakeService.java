package org.snowflake.plus.core;

import lombok.extern.slf4j.Slf4j;
import org.snowflake.plus.core.exception.InitException;

@Slf4j
public class SnowflakeService {

    private SnowflakeIDGen idGen;

    public SnowflakeService(String name, String zkAddress, Integer port, Integer workerId) throws InitException {
        idGen = new SnowflakeIDGenImpl(name, zkAddress, port, workerId);
        if (idGen.init()) {
            log.info("Snowflake Service Init Successfully");
        } else {
            throw new InitException("Snowflake Service Init Fail");
        }
    }

    public Long getId() {
        IdResult idResult = idGen.get();
        if (idResult.getStatus().equals(Status.EXCEPTION)) {
            log.error("Generate id error: {}", idResult.toString());
        }
        return idResult.getId();
    }
}
