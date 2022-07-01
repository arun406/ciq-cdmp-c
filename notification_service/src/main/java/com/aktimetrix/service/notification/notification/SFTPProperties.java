package com.aktimetrix.service.notification.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "notifiers.sftp")
public class SFTPProperties {
    private String host;
    private int port;
    private String user;
    private String password;
    private Resource privateKey;
    private String remoteDirectory;
    private String privateKeyPassphrase;
    private boolean useTemporaryFileName;
    private String temporaryFileSuffix = ".tmp";
    private long sessionWaitTimeout = 1000;
    private int poolSize = 10;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Resource getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(Resource privateKey) {
        this.privateKey = privateKey;
    }

    public String getRemoteDirectory() {
        return remoteDirectory;
    }

    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
    }

    public String getPrivateKeyPassphrase() {
        return privateKeyPassphrase;
    }

    public void setPrivateKeyPassphrase(String privateKeyPassphrase) {
        this.privateKeyPassphrase = privateKeyPassphrase;
    }

    public boolean isUseTemporaryFileName() {
        return useTemporaryFileName;
    }

    public void setUseTemporaryFileName(boolean useTemporaryFileName) {
        this.useTemporaryFileName = useTemporaryFileName;
    }

    public String getTemporaryFileSuffix() {
        return temporaryFileSuffix;
    }

    public void setTemporaryFileSuffix(String temporaryFileSuffix) {
        this.temporaryFileSuffix = temporaryFileSuffix;
    }

    public long getSessionWaitTimeout() {
        return sessionWaitTimeout;
    }

    public void setSessionWaitTimeout(long sessionWaitTimeout) {
        this.sessionWaitTimeout = sessionWaitTimeout;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
