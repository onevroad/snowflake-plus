package org.snowflake.plus.core;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowflake.plus.core.exception.InitException;

public class SnowflakeService {
    private Logger logger = LoggerFactory.getLogger(SnowflakeService.class);
    SnowflakeIDGen idGen;
    public SnowflakeService() throws InitException {
        Properties properties = new Properties();
        boolean flag = Boolean.parseBoolean(properties.getProperty(Constants.LEAF_SNOWFLAKE_ENABLE, "true"));
        if (flag) {
            String zkAddress = properties.getProperty(Constants.LEAF_SNOWFLAKE_ZK_ADDRESS);
            int port = Integer.parseInt(properties.getProperty(Constants.LEAF_SNOWFLAKE_PORT));
            idGen = new SnowflakeIDGenImpl(zkAddress, port);
            if(idGen.init()) {
                logger.info("Snowflake Service Init Successfully");
            } else {
                throw new InitException("Snowflake Service Init Fail");
            }
        }
    }
    public Result getId(String key) {
        return idGen.get(key);
    }
}
