package org.snowflake.plus.samples;

import org.snowflake.plus.core.IdResult;
import org.snowflake.plus.core.SnowflakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SnowflakeIdController {

    @Autowired
    private SnowflakeService snowflakeService;

    @GetMapping("/snowflake/id/get")
    public IdResult getId() {
        return snowflakeService.getId();
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}
