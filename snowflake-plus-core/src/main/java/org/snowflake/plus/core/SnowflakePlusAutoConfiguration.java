package org.snowflake.plus.core;

import org.snowflake.plus.core.exception.InitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SnowflakePlusProperties.class)
public class SnowflakePlusAutoConfiguration {

    @Autowired
    private SnowflakePlusProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeResource snowflakeResource() {
        return new SnowflakeResource(properties.getWorkType(), properties.getStartTime(), properties.getWorkerIdBits(), properties.getSequenceBits(),
                properties.getWorkerId(), properties.getName(), String.valueOf(properties.getListenPort()), properties.getAddress());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "snowflake.plus", value = "server-type", havingValue = "zookeeper")
    public SnowflakeNodeHolder snowflakeNodeHolder(SnowflakeResource resource) {
        return new SnowflakeZookeeperHolder(resource);
    }

    @Bean
    public SnowflakeService snowflakeService(SnowflakeResource resource, @Autowired(required = false) SnowflakeNodeHolder holder) throws InitException {
        return new SnowflakeService(resource, holder);
    }
}
