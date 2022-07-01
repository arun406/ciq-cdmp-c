package com.aktimetrix.service.ciq.cdmpc.messaging;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * This class represents the RMP message data elements of CiQ
 *
 * @author Arun.Kandakatla
 */
@Data
@ToString
public class RMPMessageDTO extends CiQMessageDTO {
    private String airWaybillPrefix;
    private String airWaybillNumber;
    private String airportOfReceipt;
    private String airportOfDelivery;
    private int totalPieces;
    private double totalWeight;
    private String weightCode;
    private double totalVolume;
    private String volumeCode;
    private int routePlanNumber;
    private String productCode;
    private String updateFlag;
    private String approvedIndicator;
    private String flightSpecificIndicator;
    private String minimumWeightTolerance;
    private String minimumVolumeTolerance;
    private String directTruckingIndicator;
    private List<RMPMilestoneDTO> milestones;
}
