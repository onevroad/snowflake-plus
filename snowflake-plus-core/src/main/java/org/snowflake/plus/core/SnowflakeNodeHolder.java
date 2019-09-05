package org.snowflake.plus.core;

public abstract class SnowflakeNodeHolder {

    protected SnowflakeResource resource;

    public SnowflakeNodeHolder(SnowflakeResource resource) {
        this.resource = resource;
    }

    abstract boolean init();

    public long getWorkerId() {
        return resource.getWorkerId();
    }

}
