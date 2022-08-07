package com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.fwb;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
public class FlightIdentification {
    private String carrierCode;
    private String flightNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate flightDate;
}
