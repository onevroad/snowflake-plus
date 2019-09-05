package org.snowflake.plus.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public class SnowflakeLocalConfigService {

    private String listenPort;
    private String propPath;

    public SnowflakeLocalConfigService(SnowflakeResource resource) {
        this.listenPort = resource.getListenPort();
        this.propPath = System.getProperty("java.io.tmpdir") + File.separator + resource.getName() + "/snowflake-conf/{port}/workerId.properties";
    }

    /**
     * 在节点文件系统上缓存一个workID值,zk失效,机器重启时保证能够正常启动
     *
     * @param workerId
     */
    public void updateLocalWorkerId(int workerId) {
        File snowflakeConfFile = new File(propPath.replace("{port}", listenPort));
        boolean exists = snowflakeConfFile.exists();
        log.info("file exists status is {}", exists);
        if (exists) {
            try {
                FileUtils.writeStringToFile(snowflakeConfFile, "workerId=" + workerId, false);
                log.info("update file cache workerId is {}", workerId);
            } catch (IOException e) {
                log.error("update file cache error ", e);
            }
        } else {
            //不存在文件,父目录页肯定不存在
            try {
                boolean mkdirs = snowflakeConfFile.getParentFile().mkdirs();
                log.info("init local file cache create parent dis status is {}, worker id is {}", mkdirs, workerId);
                if (mkdirs) {
                    if (snowflakeConfFile.createNewFile()) {
                        FileUtils.writeStringToFile(snowflakeConfFile, "workerId=" + workerId, false);
                        log.info("local file cache workerId is {}", workerId);
                    }
                } else {
                    log.warn("create parent dir error===");
                }
            } catch (IOException e) {
                log.warn("create workerId conf file error", e);
            }
        }
    }
    
    public int loadLocalWorkId() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(propPath.replace("{port}", listenPort))));
        int workerId = Integer.parseInt(properties.getProperty("workerId"));
        log.warn("START FAILED ,use local node file properties workerId-{}", workerId);
        return workerId;
    }
}
