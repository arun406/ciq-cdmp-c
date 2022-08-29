package com.aktimetrix.service.ciq.cdmpc.messaging;

import com.aktimetrix.service.notification.messaging.GenericMessage;
import org.springframework.messaging.MessageHeaders;

import java.io.File;
import java.util.Map;

/**
 * CiQ Message is a generic Message with File payload
 *
 * @author Arun.Kandakatla
 */
public class CiQMessage extends GenericMessage<File> {

    public CiQMessage(File payload) {
        super(payload);
    }

    public CiQMessage(File payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    public CiQMessage(File payload, MessageHeaders headers) {
        super(payload, headers);
    }
}

