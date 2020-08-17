# snowflake-plus [![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

This is a id generator which use the snowflake algorithm. It's developed by spring-boot. This component works without configuring work-id. The idea of this component came from Meituan tech who designed the snowflake id with zookeeper.

## Work Type
There is three types for the component.
- local: The work-id will be got from your local application's properties file.
- zookeeper: The work-id will be registered to your zookeeper server.
- ip: The work-id will be got by the last part of your local server's IP. For example: If your IP is 192.168.1.200, the work-id is 200.

## Quick Start
- add maven dependency
```xml
<dependency>
    <groupId>org.onevroad</groupId>
    <artifactId>snowflake-plus-core</artifactId>
    <version>0.3.0</version>
</dependency>
```
- add zookeeper's dependency(if your work-type is zookeeper)
```xml
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>4.2.0</version>
</dependency>
```

- add config
```yaml
snowflake:
  plus:
    #local，zookeeper，ip. Default is local.
    work-type: zookeeper
    #initial start time, default: 2019-08-01 00:00:00 (UTC)
    start-time: 1564617600000
    #the number of bit for worker id, default: 8bit
    worker-id-bits: 8
    #the number of bit for sequence, default: 12bit
    sequence-bits: 12
    #If your work-type is local, you need configure the work-id
    worker-id: 1
    #If your work-type is zookeeper, you need configure the following parameters
    #application's registered name
    name: snowflake-plus-sample
    #application's registered port
    port: 8001
    #the zookeeper address
    zk-address: localhost:2181
```

- get ID
```java
@RestController
public class SnowflakeIdController {

    @Autowired
    private SnowflakeService snowflakeService;

    @GetMapping("/snowflake/id/get")
    public long getId() {
        return snowflakeService.getId();
    }
}
```

## Feature
- support other work-type
