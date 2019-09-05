package org.snowflake.plus.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.snowflake.plus.core.exception.CheckLastTimeException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SnowflakeZookeeperHolder extends SnowflakeNodeHolder {
    //保存自身的key  ip:port-000000001
    private String zkAddressNode = null;
    //保存自身的key ip:port
    private String listenAddress;
    private String prefixZkPath;
    //保存所有数据持久的节点
    private String pathForever;
    private String ip;
    private long lastUpdateTime;

    public SnowflakeZookeeperHolder(String name, String port, String address) {
        super(name, port, address);
        this.ip = IpUtils.getIp();
        this.listenAddress = ip + ":" + port;
        this.prefixZkPath = "/snowflake/" + name;
        this.pathForever = prefixZkPath + "/forever";
    }

    @Override
    public boolean init() {
        try {
            CuratorFramework curator = createWithOptions(address, new RetryUntilElapsed(1000, 4), 10000, 6000);
            curator.start();
            Stat stat = curator.checkExists().forPath(pathForever);
            if (stat == null) {
                //不存在根节点,机器第一次启动,创建/snowflake/ip:port-000000000,并上传数据
                zkAddressNode = createNode(curator);
                //worker id 默认是0
                //定时上报本机时间给forever节点
                scheduledUploadData(curator, zkAddressNode);
                return true;
            } else {
                //ip:port->00001
                Map<String, Integer> nodeMap = Maps.newHashMap();
                //ip:port->(ipport-000001)
                Map<String, String> realNode = Maps.newHashMap();
                //存在根节点,先检查是否有属于自己的根节点
                List<String> keys = curator.getChildren().forPath(pathForever);
                for (String key : keys) {
                    String[] nodeKey = key.split("-");
                    realNode.put(nodeKey[0], key);
                    nodeMap.put(nodeKey[0], Integer.parseInt(nodeKey[1]));
                }
                Integer workerIdOnNode = nodeMap.get(listenAddress);
                if (workerIdOnNode != null) {
                    //有自己的节点,zkAddressNode=ip:port
                    zkAddressNode = pathForever + "/" + realNode.get(listenAddress);
                    //启动worker时使用会使用
                    workerId = workerIdOnNode;
                    if (!checkInitTimeStamp(curator, zkAddressNode)) {
                        throw new CheckLastTimeException("init timestamp check error,forever node timestamp gt this node time");
                    }
                    //准备创建临时节点
                    doService(curator);
                    log.info("[Old NODE]find forever node have this endpoint ip-{} port-{} workID-{} childNode and start SUCCESS", ip, port, workerId);
                } else {
                    //表示新启动的节点,创建持久节点 ,不用check时间
                    String newNode = createNode(curator);
                    zkAddressNode = newNode;
                    String[] nodeKey = newNode.split("-");
                    workerId = Integer.parseInt(nodeKey[1]);
                    doService(curator);
                    log.info("[New NODE]can not find node on forever node that endpoint ip-{} port-{} workID-{},create own node on forever node and start SUCCESS ", ip, port, workerId);
                }
            }
        } catch (Exception e) {
            log.error("Start node ERROR {}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    private void doService(CuratorFramework curator) {
        // /snowflake_forever/ip:port-000000001
        scheduledUploadData(curator, zkAddressNode);
    }

    private void scheduledUploadData(final CuratorFramework curator, final String zkAddressNode) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("schedule-upload-time").build());
        //每3s上报数据
        executor.scheduleWithFixedDelay(() -> updateNewData(curator, zkAddressNode), 1L, 3L, TimeUnit.SECONDS);
    }

    private boolean checkInitTimeStamp(CuratorFramework curator, String zkAddressNode) throws Exception {
        byte[] bytes = curator.getData().forPath(zkAddressNode);
        Endpoint endPoint = deBuildData(new String(bytes));
        //该节点的时间不能小于最后一次上报的时间
        return endPoint.getTimestamp() <= System.currentTimeMillis();
    }

    /**
     * 创建持久顺序节点 ,并把节点数据放入 value
     *
     * @param curator
     * @return
     * @throws Exception
     */
    private String createNode(CuratorFramework curator) throws Exception {
        try {
            return curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(pathForever + "/" + listenAddress + "-", buildData().getBytes());
        } catch (Exception e) {
            log.error("create node error msg {} ", e.getMessage());
            throw e;
        }
    }

    private void updateNewData(CuratorFramework curator, String path) {
        try {
            if (System.currentTimeMillis() < lastUpdateTime) {
                return;
            }
            curator.setData().forPath(path, buildData().getBytes());
            lastUpdateTime = System.currentTimeMillis();
        } catch (Exception e) {
            log.info("update init data error path is {} error is {}", path, e);
        }
    }

    /**
     * 构建需要上传的数据
     *
     * @return
     */
    private String buildData() throws JsonProcessingException {
        Endpoint endpoint = new Endpoint(ip, port, System.currentTimeMillis());
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(endpoint);
        return json;
    }

    private Endpoint deBuildData(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Endpoint endpoint = mapper.readValue(json, Endpoint.class);
        return endpoint;
    }

    private CuratorFramework createWithOptions(String connectionString, RetryPolicy retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs) {
        return CuratorFrameworkFactory.builder().connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }

    /**
     * 上报数据结构
     */
    static class Endpoint {
        private String ip;
        private String port;
        private long timestamp;

        public Endpoint() {
        }

        public Endpoint(String ip, String port, long timestamp) {
            this.ip = ip;
            this.port = port;
            this.timestamp = timestamp;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

}
