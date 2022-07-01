package com.aktimetrix.service.planner.transferobjects;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QuantityInfo {
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

    public QuantityInfo(int piece, double weight, double volume) {
        this.piece = piece;
        this.weight = new UnitValue(weight, "K");
        this.volume = new UnitValue(volume, "CM");
    }
}
