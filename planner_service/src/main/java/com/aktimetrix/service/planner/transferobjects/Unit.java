package com.aktimetrix.service.planner.transferobjects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Unit {
    private String code;
    private String description;

    public Unit(String code) {
        this.code = code;
    }
}
