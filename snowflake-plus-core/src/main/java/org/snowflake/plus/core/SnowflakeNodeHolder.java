package org.snowflake.plus.core;

public abstract class SnowflakeNodeHolder {

    protected String name;
    protected String port;
    protected String address;
    protected int workerId;

    public SnowflakeNodeHolder(String name, String port, String address) {
        this.name = name;
        this.port = port;
        this.address = address;
    }

    abstract boolean init();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }
}
