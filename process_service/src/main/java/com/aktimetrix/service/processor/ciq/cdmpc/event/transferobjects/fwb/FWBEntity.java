package com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.fwb;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@ToString
@Data
public class FWBEntity implements Serializable {
    private String airWaybillPrefix;
    private String airWaybillSerialNumber;
    private String origin;
    private String destination;
    private Quantity quantity;
    private FlightIdentification flightIdentification;
    private Agent agent;
    private List<String> SpecialHandlingCodes;
}
