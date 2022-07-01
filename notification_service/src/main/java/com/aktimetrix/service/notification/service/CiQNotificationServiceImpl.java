package com.aktimetrix.service.notification.service;

import com.aktimetrix.core.transferobjects.ProcessPlanDTO;
import com.aktimetrix.service.ciq.cdmpc.messaging.CiQMessage;
import com.aktimetrix.service.ciq.cdmpc.messaging.service.CiQCAN12MessageGenerator;
import com.aktimetrix.service.ciq.cdmpc.messaging.service.CiQRMP12MessageGenerator;
import com.aktimetrix.service.notification.api.CiQNotificationService;
import com.aktimetrix.service.notification.messaging.Message;
import com.aktimetrix.service.notification.notification.Notification;
import com.aktimetrix.service.notification.notification.Notifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This is the core notification service implementation for CiQ. It will be used to send CiQ out going messages using SFTP notifier.
 *
 * @author Arun.Kandakatla
 */
@Service
public class CiQNotificationServiceImpl implements CiQNotificationService {

    private static final String CIQ_NOTIFICATION_TYPE_RMP = "ciq.notification.type.rmp";
    private static final String CIQ_NOTIFICATION_TYPE_MUP = "ciq.notification.type.mup";
    private static final String CIQ_NOTIFICATION_TYPE_CAN = "ciq.notification.type.can";
    private static final String CIQ_NOTIFICATION_TYPE_ERR = "ciq.notification.type.err";
    private static final String CIQ_NOTIFICATION_TYPE_MEX = "ciq.notification.type.mex";

    public static final int CIQ_MESSAGE_VERSION = 12;

    @Autowired
    private CiQRMP12MessageGenerator ciQRMP12MessageGenerator;

    @Autowired
    private CiQCAN12MessageGenerator ciQCAN12MessageGenerator;

    @Qualifier("SFTPNotifier")
    @Autowired
    private Notifier notifier;

    @Override
    public void sendRMP(ProcessPlanDTO plan) {
        Integer sequenceNumber = null;
        Message ciQMessage = ciQRMP12MessageGenerator.generate(plan);
        if (ciQMessage instanceof CiQMessage) {
            sequenceNumber = (Integer) ((CiQMessage) ciQMessage).getHeaders().get("sequence-number");
        }
        Notification rmpNotification = new Notification(CIQ_NOTIFICATION_TYPE_RMP);
        rmpNotification.setData(ciQMessage);
//        this.notifier.sendNotification(rmpNotification);
    }

    @Override
    public void sendMUP() {

    }

    @Override
    public void sendCAN(ProcessPlanDTO plan) {
        Integer sequenceNumber = null;
        Message ciQMessage = ciQCAN12MessageGenerator.generate(plan);
        if (ciQMessage instanceof CiQMessage) {
            sequenceNumber = (Integer) ((CiQMessage) ciQMessage).getHeaders().get("sequence-number");
        }
        Notification rmpNotification = new Notification(CIQ_NOTIFICATION_TYPE_CAN);
        rmpNotification.setData(ciQMessage);
//        this.notifier.sendNotification(rmpNotification);
    }

    @Override
    public void sendERR() {

    }
}
