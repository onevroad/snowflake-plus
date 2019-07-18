package org.snowflake.plus.core;

public interface SnowflakeIDGen {
    Result get(String key);
    boolean init();
}
