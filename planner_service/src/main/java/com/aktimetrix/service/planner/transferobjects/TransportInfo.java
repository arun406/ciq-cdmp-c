package com.aktimetrix.service.planner.transferobjects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransportInfo {
    public String carrier;
    public String number;
    public String extensionNumber;

    public TransportInfo(String carrier, String number) {
        this.carrier = carrier;
        this.number = number;
    }
}
