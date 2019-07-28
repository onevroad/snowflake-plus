package org.snowflake.plus.core;

import org.snowflake.plus.core.exception.InitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SnowflakePlusProperties.class)
public class SnowflakePlusAutoConfiguration {

    @Autowired
    private SnowflakePlusProperties properties;

    @Bean
    public SnowflakeService snowflakeService() throws InitException {
        return new SnowflakeService(properties.getName(), properties.getZkAddress(), properties.getPort(), properties.getWorkerId());
    }
}
