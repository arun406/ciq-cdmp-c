package com.aktimetrix.service.ciq.cdmpc.messaging;

import lombok.Data;
import lombok.ToString;

/**
 * @author Arun.Kandakatla
 */
@Data
@ToString
public class CANMessageDTO extends CiQMessageDTO {
    private String airWaybillPrefix;
    private String airWaybillNumber;
}
