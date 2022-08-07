package com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.fwb;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@ToString
@Data
public class Quantity implements Serializable {
    private String shipmentDescriptionCode;
    private int pieces;
    private String weightCode;
    private BigDecimal weight;
    private String volumeCode;
    private BigDecimal volume;
}
