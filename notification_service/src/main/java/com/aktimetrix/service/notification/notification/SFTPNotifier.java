package com.aktimetrix.service.notification.notification;

import com.aktimetrix.service.ciq.cdmpc.messaging.CiQMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Arun.Kandakatla
 */
@Component("SFTPNotifier")
public class SFTPNotifier implements Notifier {

    @Autowired
    private SFTPConfig.SFTPGateway sftpGateway;

    /**
     * sends the notification
     *
     * @param notification
     * @return
     */
    @Override
    public void sendNotification(Notification notification) {
        if (notification == null)
            return;
        CiQMessage ciQMessage = (CiQMessage) notification.getData();
        sftpGateway.sendToSftp(ciQMessage.getPayload(), ciQMessage.getHeaders());
    }
}
