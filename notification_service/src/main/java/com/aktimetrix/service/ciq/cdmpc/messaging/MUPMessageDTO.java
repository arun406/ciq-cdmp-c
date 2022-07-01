package com.aktimetrix.service.ciq.cdmpc.messaging;

import lombok.Data;
import lombok.ToString;

import java.time.OffsetDateTime;

/**
 * @author Arun.Kandakatla
 */
@Data
@ToString
public class MUPMessageDTO extends CiQMessageDTO {

    private String forwarderIdentifier;
    private String airWaybillPrefix;
    private String airWaybillNumber;
    private String milestone;
    private String airportOfEvent;
    private String airportOfArrival;
    private String carrier;
    private String flightNumber;
    private int piecesReported;
    private double weightReported;
    private double volumeReported;
    private WeightCode weightUnit;
    private VolumeCode volumeUnit;
    private CompletionCode completionCode;
    private SequenceErrorCode sequenceErrorCode;
    private OffsetDateTime messageReceiptTimestamp;
    private OffsetDateTime milestoneReportedTimestamp;
    private OffsetDateTime flightScheduleTimestamp;
    private String directTruckingIndicator;
    private String message; // actual event message
}
