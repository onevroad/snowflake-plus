package org.snowflake.plus.core;

public interface SnowflakeIDGen {
    /**
     * 获取ID
     */
    IdResult get();

    /**
     * 初始化服务
     */
    boolean init();
}
