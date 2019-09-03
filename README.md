# snowflake-plus[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

参照美团的snowflake方案设计的，结合了spring-boot，开箱即用, 无需配置workId

所有workId都通过zookeeper管理，且第一次获取workId后，会在本地备份一份，以防zookeeper挂掉

## 快速开始
- 导入依赖
```xml
<dependency>
    <groupId>org.onevroad</groupId>
    <artifactId>snowflake-plus-core</artifactId>
    <version>0.1.1</version>
</dependency>
```
- 添加zookeeper依赖
```xml
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>4.2.0</version>
</dependency>
```

- 添加配置
```yaml
snowflake:
  plus:
    #应用注册名
    name: snowflake-plus-sample
    #应用注册端口
    port: 8001
    #zookeeper地址
    zk-address: localhost:2181
```

- 获取ID
```java
@Component
public class IdProducer {
    @Autowired
    private SnowflakeService snowflakeService;

    
    public Long getId() {
        return snowflakeService.getId().getId();
    }
}
```

## Future
- 支持其他的注册中心（Eureka，Consul等）
