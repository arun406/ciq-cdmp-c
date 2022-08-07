package com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QuantityInfo {
    private String shipmentDescriptionCode;
    private int piece;
    private Object slac;
    private UnitValue weight;
    private UnitValue volume;
    private List<Dimension> dimension;
    private List<UldDetail> uldDetails;
    private List<SpecialHandling> shcList;

    public QuantityInfo(int piece) {
        this.piece = piece;
    }
}
