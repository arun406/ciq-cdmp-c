package com.aktimetrix.service.notification.messaging;

import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Arun.Kandakatla
 */
public class GenericMessage<T> implements Message<T>, Serializable {

    private final T payload;
    private final MessageHeaders headers;

    public GenericMessage(T payload) {
        this(payload, new MessageHeaders(null));
    }

    public GenericMessage(T payload, Map<String, Object> headers) {
        this(payload, new MessageHeaders(headers));
    }

    public GenericMessage(T payload, MessageHeaders headers) {
        Assert.notNull(payload, "Payload must not be null");
        Assert.notNull(headers, "MessageHeaders must not be null");
        this.payload = payload;
        this.headers = headers;
    }

    @Override
    public T getPayload() {
        return this.payload;
    }

    @Override
    public org.springframework.messaging.MessageHeaders getHeaders() {
        return this.headers;
    }
}
