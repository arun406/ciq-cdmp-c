package com.aktimetrix.service.planner.service;


import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.service.AbstractProcessInstanceProcessor;
import com.aktimetrix.core.stereotypes.ProcessHandler;
import com.aktimetrix.service.planner.transferobjects.Itinerary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class is responsible for handing the process updated events from processor microservice.
 *
 * @author Arun.Kandakatla
 */
@Slf4j
@Component
@ProcessHandler(processCode = Constants.PROCESS_INSTANCE_UPDATE, processType = Constants.PROCESS_INSTANCE_TYPE,
        version = Constants.DEFAULT_VERSION)
public class CiQProcessUpdateProcessor extends AbstractProcessInstanceProcessor {

    @Autowired
    private RouteMapService routeMapService;

    @Value("${tenant.airlineCode:}")
    private String airlineCode;

    /**
     * do the process execution
     *
     * @param context
     */
    @Override
    protected void doProcess(Context context) {
        // latest process instance
        ProcessInstance processInstance = context.getProcessInstance();
        processInstance = processInstanceService.saveProcessInstance(processInstance); // local save
        log.debug("process instance is saved successfully in planner service.{}", processInstance.getId());
        // validate airline code
        if (!isValidItinerariesExists(processInstance)) {
            log.debug("ignoring the process instance as all itineraries do not belongs to tenant airline code");
            return;
        }
        if (processInstance.isComplete()) {
            routeMapService.createCompletePlan(context, processInstance);
        } else {
            routeMapService.upsertIncompletePlan(context, processInstance);
        }
    }

    private boolean isValidItinerariesExists(ProcessInstance processInstance) {
        List<Itinerary> itineraries;
        if (!(processInstance.getMetadata().get("itineraries") instanceof LinkedHashMap)) {
            return false;
        }

        LinkedHashMap<String, Object> itinerariesLL = (LinkedHashMap) processInstance.getMetadata().get("itineraries");
        try {
            String itinerariesStr = objectMapper.writeValueAsString(itinerariesLL);
            itineraries = objectMapper.readValue(itinerariesStr, new TypeReference<>() {
            });
            log.debug("ciq airline code: {}", airlineCode);
            if (itineraries != null && !itineraries.isEmpty()) {
                int reservationPieces = (int) processInstance.getMetadata().get("reservationPieces");
                return !(itineraries.stream().anyMatch(itinerary -> !itinerary.getTransportInfo().getCarrier().equalsIgnoreCase(airlineCode))
                        || itineraries.stream().anyMatch(itinerary -> itinerary.getQuantity().getPiece() > reservationPieces));
            }
        } catch (JsonProcessingException e) {
            log.debug(e.getMessage(), e);
        }
        return true;
    }
}
