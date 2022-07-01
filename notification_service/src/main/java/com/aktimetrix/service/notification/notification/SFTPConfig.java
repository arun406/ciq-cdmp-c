package com.aktimetrix.service.notification.notification;

import com.jcraft.jsch.ChannelSftp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.file.DefaultFileNameGenerator;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

import java.io.File;
import java.util.Map;

/**
 * @author Arun.Kandakatla
 */
@Configuration
@IntegrationComponentScan
@EnableIntegration
public class SFTPConfig {

    @Autowired
    private SFTPProperties sftpProperties;

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpProperties.getHost());
        factory.setPort(sftpProperties.getPort());
        factory.setUser(sftpProperties.getUser());
        factory.setAllowUnknownKeys(true);
        if (sftpProperties.getPrivateKey() != null) {
            factory.setPrivateKey(sftpProperties.getPrivateKey());
            factory.setPrivateKeyPassphrase(sftpProperties.getPrivateKeyPassphrase());
        } else {
            factory.setPassword(sftpProperties.getPassword());
        }
        CachingSessionFactory<ChannelSftp.LsEntry> lsEntryCachingSessionFactory = new CachingSessionFactory<>(factory);
        lsEntryCachingSessionFactory.setSessionWaitTimeout(sftpProperties.getSessionWaitTimeout());
        lsEntryCachingSessionFactory.setPoolSize(sftpProperties.getPoolSize());

        return lsEntryCachingSessionFactory;
    }

    @Bean
    @ServiceActivator(inputChannel = "toSftpChannel")
    public MessageHandler handler() {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpressionString("headers['remote-target-dir']");
        DefaultFileNameGenerator fileNameGenerator = new DefaultFileNameGenerator();
        handler.setFileNameGenerator(fileNameGenerator);
        handler.setUseTemporaryFileName(sftpProperties.isUseTemporaryFileName());
        handler.setTemporaryFileSuffix(sftpProperties.getTemporaryFileSuffix());
        return handler;
    }

    @MessagingGateway
    public interface SFTPGateway {
        @Gateway(requestChannel = "toSftpChannel")
        void sendToSftp(@Payload File file, @Headers Map<String, Object> headers);
    }
}
