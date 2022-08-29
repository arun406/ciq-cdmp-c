package com.aktimetrix.service.notification.notification;

import com.aktimetrix.core.service.S3StorageService;
import com.aktimetrix.service.ciq.cdmpc.messaging.CiQMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Slf4j
@Component
public class S3Notifier implements Notifier {

    @Autowired
    private S3StorageService s3StorageService;

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
        try {
            // push the message to FF folder.
            s3StorageService.upload(new FileInputStream(ciQMessage.getPayload()), "ciq.cdmpc.xxx",
                    (String) ciQMessage.getHeaders().get("file_name"));
        } catch (FileNotFoundException e) {
            log.error("unable to upload the file to s3:", e);
        }
    }
}
