package com.aktimetrix.service.ciq.cdmpc.messaging;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author Arun.Kandakatla
 */

@Data
@ToString
public class RMPMilestoneDTO {
    private String milestoneCode;
    private MilestoneChangeStatus changeStatus;
    private String flightOriginAirport;
    private String flightDestinationAirport;
    private String carrierCode;
    private String flightNumber;
    private String pieces;
    private String weight;
    private String volume;
    private String planTime;    //UTC
    private String flightTimestamp; //UTC
    private String typeOfTimeIndicator;
    private String aircraftCategory;
    private String aircraftType;
}
