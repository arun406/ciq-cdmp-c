package com.aktimetrix.service.notification.messaging;

/**
 * Core class for generating outward messages
 *
 * @author Arun.Kandakatla
 */
public interface MessageGenerator {
    Message generate(Object object);
}