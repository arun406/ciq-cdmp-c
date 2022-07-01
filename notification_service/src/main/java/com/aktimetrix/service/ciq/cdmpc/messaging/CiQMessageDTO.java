package com.aktimetrix.service.ciq.cdmpc.messaging;

import com.aktimetrix.service.notification.messaging.MessageDTO;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * This is the base class for the CiQ message objects
 *
 * @author Arun.Kandakatla
 */
@Data
public class CiQMessageDTO extends MessageDTO {
    private String carrierCode;
    private String forwarderIdentifier;
    private String messageType;
    private int messageVersion;
    private String cdmpIdentifier;
    private String messageReference;
    private String messageTimestamp;
    private String phaseNumber;
    private String messageCounter;
}
