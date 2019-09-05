package org.snowflake.plus.core;

import lombok.extern.slf4j.Slf4j;
import org.snowflake.plus.core.exception.InitException;

@Slf4j
public class SnowflakeService {

    private SnowflakeIDGen idGen;

    public SnowflakeService(SnowflakeResource resource, SnowflakeNodeHolder holder) throws InitException {
        if (resource.getServerType().equals(WorkIdServerType.local)) {
            idGen = new SnowflakeIDGenImpl(resource.getWorkerId());
        } else {
            idGen = new SnowflakeIDGenImpl(resource, holder);
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
