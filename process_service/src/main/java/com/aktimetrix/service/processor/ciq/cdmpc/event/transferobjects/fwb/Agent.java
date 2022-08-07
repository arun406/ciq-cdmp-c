package com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.fwb;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Agent {
    private String name;
    private String place;
    private String code; // this is new
}
