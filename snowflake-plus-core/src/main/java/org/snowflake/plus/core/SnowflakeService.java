package org.snowflake.plus.core;

import lombok.extern.slf4j.Slf4j;
import org.snowflake.plus.core.exception.InitException;
import org.springframework.util.StringUtils;

@Slf4j
public class SnowflakeService {

    private SnowflakeIDGen idGen;

    public SnowflakeService(SnowflakeResource resource, SnowflakeNodeHolder holder) throws InitException {
        if (resource.getServerType().equals(WorkIdServerType.local)) {
            idGen = new SnowflakeIDGenImpl(resource.getWorkerId());
        } else if (resource.getServerType().equals(WorkIdServerType.ip)) {
            String ip = IpUtils.getIp();
            if (StringUtils.isEmpty(ip)) {
                throw new InitException("Snowflake Service Init Failed! Get IP Error!");
            }
            String[] ipSegments = ip.split("\\.");
            String workId = ipSegments[ipSegments.length - 1];
            idGen = new SnowflakeIDGenImpl(workId);
        } else if (resource.getServerType().equals(WorkIdServerType.zookeeper)) {
            idGen = new SnowflakeIDGenImpl(resource, holder);
        } else {
            throw new InitException("Snowflake Service Init Failed!");
        }
        log.info("Snowflake Service Init Successfully");
    }

    public long getId() {
        IdResult idResult = idGen.get();
        if (idResult.getStatus().equals(Status.EXCEPTION)) {
            log.error("Generate id error: {}", idResult.toString());
        }
        return idResult.getId();
    }
}
