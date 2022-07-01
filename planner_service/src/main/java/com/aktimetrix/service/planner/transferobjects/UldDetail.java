package com.aktimetrix.service.planner.transferobjects;

import lombok.Data;

@Data
public class UldDetail {
    public String uldType;
    public String contourCode;
    public String rateType;
    public String loadingCode;
    public UnitValue tareWeight;
    public UnitValue maximumWeight;
    public UnitValue actualWeight;
    public UnitValue maximumVolume;
    public UnitValue actualVolume;
    public String carrierCode;
    public int slac;
}
