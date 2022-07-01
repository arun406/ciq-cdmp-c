package com.aktimetrix.service.notification.notification;


/**
 * Core interface for defining notification senders
 *
 * @author Arun.Kandakatla
 */
public interface Notifier {

    /**
     * sends the notification
     *
     * @return
     */
    void sendNotification(Notification notification);
}
