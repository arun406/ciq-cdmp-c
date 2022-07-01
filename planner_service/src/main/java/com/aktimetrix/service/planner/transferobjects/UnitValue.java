package com.aktimetrix.service.planner.transferobjects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UnitValue {
    private double value;
    private Unit unit;

    public UnitValue(double value, String unit) {
        this.value = value;
        this.unit = new Unit(unit);
    }
}
