package com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects;

import lombok.Data;

@Data
public class Itinerary {
    private StationInfo boardPoint;
    private StationInfo offPoint;
    private TransportInfo transportInfo;
    private TransportTime departureDateTimeUTC;
    private TransportTime arrivalDateTimeUTC;
    private int legNumber;
    private boolean partIndicator;
    private boolean oalIndicator;
    private QuantityInfo quantity;
    private String allotmentId;
    private CodeValue movementStatus;
    private CodeValue spaceStatus;
    private String aircraftCategory;

    @Override
    public String toString() {
        return "Itinerary{" +
                "boardPoint=" + boardPoint.getCode() +
                ", offPoint=" + offPoint.getCode() +
                ", carrier=" + transportInfo.getCarrier() +
                ", flightNumber=" + transportInfo.getNumber() +
                ", flightExtension=" + transportInfo.getExtensionNumber() +
                ", pieces=" + quantity.getPiece() +
                ", weight=" + quantity.getWeight().getValue() +
                ", weightUnit=" + quantity.getWeight().getUnit().getCode() +
                ", volume=" + quantity.getVolume().getValue() +
                ", volumeUnit=" + quantity.getVolume().getUnit().getCode() +
                '}';
    }
}
