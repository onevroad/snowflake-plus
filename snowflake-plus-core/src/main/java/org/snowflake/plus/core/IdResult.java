package org.snowflake.plus.core;

import lombok.Data;

@Data
public class IdResult {

    private long id;

    private Status status;

    public IdResult() {

    }

    public IdResult(long id, Status status) {
        this.id = id;
        this.status = status;
    }
}
