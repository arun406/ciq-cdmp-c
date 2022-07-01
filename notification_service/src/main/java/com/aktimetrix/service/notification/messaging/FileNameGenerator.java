package com.aktimetrix.service.notification.messaging;

@FunctionalInterface
public interface FileNameGenerator {
    String generateFileName(MessageDTO message);
}
