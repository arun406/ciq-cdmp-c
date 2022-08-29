package com.aktimetrix.service.processor.ciq.cdmpc.service.util;


import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.api.Context;
import com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.BKDEventDetails;
import com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.Cargo;
import com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.Itinerary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItineraryUtilService {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Validates the Itinerary
     *
     * @param itinerary
     * @return
     */
    public boolean isValidItinerary(Itinerary itinerary) {
        // check every element. All element should be valid.
        // check airline code.  Airline should be tenant airline code
        // check itinerary quantity with shipment quantity. Itinerary quantity should be less than or equal to shipment quantity.
        return true;
    }

    /**
     * Validates the Itinerary
     *
     * @param itinerary
     * @return
     */
    public boolean isValidItinerary(Itinerary itinerary, Cargo cargo) {
        this.isValidItinerary(itinerary);
        // check itinerary quantity with shipment quantity. Itinerary quantity should be less than or equal to shipment quantity.
        return true;
    }

    /**
     * find the 'to' itinerary is already present in 'from' list
     *
     * @param from
     * @param to
     * @return
     */
    public Itinerary findMatchingItinerary(List<Itinerary> from, Itinerary to) {
        // find itinerary matching board point, off point, quantity, flight and date information
        return null;
    }


    /**
     * find the matching segments from 'from' list
     * Matching segment means, board point , off point and quantity is same.
     *
     * @param from
     * @param to
     * @return
     */
    public List<Itinerary> findMatchingSegments(List<Itinerary> from, Itinerary to) {
        if (from == null || from.isEmpty()) {
            return new ArrayList();
        }
        List<Itinerary> collect = from.stream().filter(i -> i.getBoardPoint().getCode().equals(to.getBoardPoint().getCode())
                        && i.getOffPoint().getCode().equals(to.getOffPoint().getCode())
                        && i.getQuantity().getPiece() == to.getQuantity().getPiece()
                        && Double.compare(i.getQuantity().getWeight().getValue(), to.getQuantity().getWeight().getValue()) == 0
                        && i.getQuantity().getWeight().getUnit().getCode().equals(to.getQuantity().getWeight().getUnit().getCode()))
                .collect(Collectors.toList());
        if (collect.isEmpty()) {
            return new ArrayList<>();
        }
        return collect;
    }

    /**
     * find and replace matching segments from 'from' list
     * Matching segment means, board point , off point and quantity is same.
     *
     * @param from
     * @param to
     * @return
     */
    public List<Itinerary> replaceMatchingSegments(List<Itinerary> from, Itinerary to) {
        if (from == null || from.isEmpty()) {
            return new ArrayList();
        }
        from.forEach(i -> {
            String boardPoint = i.getBoardPoint().getCode();
            String offPoint = i.getOffPoint().getCode();
            if (boardPoint.equals(to.getBoardPoint().getCode())
                    && offPoint.equals(to.getOffPoint().getCode())
                    && i.getQuantity().getPiece() == to.getQuantity().getPiece()
                    && i.getQuantity().getWeight().getValue() == to.getQuantity().getWeight().getValue()
                    && i.getQuantity().getWeight().getUnit().getCode().equals(to.getQuantity().getWeight().getUnit().getCode())) {
                log.debug("found match segment replacing transport information and date information");
                // update the transport and flight date information

                i.setTransportInfo(to.getTransportInfo());
                i.setDepartureDateTimeUTC(to.getDepartureDateTimeUTC());
                i.setArrivalDateTimeUTC(to.getArrivalDateTimeUTC());
            }
        });
        return from;
    }

    /**
     * Check both 'from' and 'to' itinerary have same flight information.
     *
     * @param from
     * @param to
     * @return
     */
    public boolean isItineraryFlightOrDateSame(Itinerary from, Itinerary to) {
        if (from == null || to == null)
            return false;

        LocalDateTime fromSchedule = from.getDepartureDateTimeUTC().getSchedule();
        LocalDateTime toSchedule = to.getDepartureDateTimeUTC().getSchedule();

        if (!from.getTransportInfo().getCarrier().equals(to.getTransportInfo().getCarrier())
                || !from.getTransportInfo().getNumber().equals(to.getTransportInfo().getNumber())
                || !from.getTransportInfo().getExtensionNumber().equals(to.getTransportInfo().getExtensionNumber())
                || !fromSchedule.isEqual(toSchedule)) {
            return false;
        }
        return true;
    }


    /**
     * Convert the Event_Data to List of Itinerary objects
     *
     * @param context
     * @return
     */
    public List<Itinerary> extractItinerariesFromContext(Context context) {
        try {
            String eventDetailsJson = this.objectMapper.writeValueAsString(context.getProperty(Constants.EVENT_DATA));
            BKDEventDetails eventDetails = this.objectMapper.readValue(eventDetailsJson, new TypeReference<>() {
            });
            return eventDetails.getItineraries();
        } catch (JsonProcessingException e) {
            log.error("error converting event details to Itineraries.");
        }
        return new ArrayList<>();
    }

    /**
     * Convert the Event_Data to Itinerary
     *
     * @param context
     * @return
     */
    public Itinerary getItineraryFromContext(Context context) {
        List<Itinerary> itinerariesFromContext = extractItinerariesFromContext(context);
        if (itinerariesFromContext != null && itinerariesFromContext.size() == 1) {
            return itinerariesFromContext.get(0);
        }
        return null;
    }
}
