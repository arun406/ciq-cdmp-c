package com.aktimetrix.service.planner.processor;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.impl.publisher.ProcessPlanInstancePublisher;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.aktimetrix.service.planner.service.RouteMapService;
import com.aktimetrix.service.planner.transferobjects.Itinerary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class is responsible for handing the process created events from processor microservice.
 *
 * @author Arun.Kandakatla
 */
@Slf4j
@Service
@com.aktimetrix.core.stereotypes.Processor(processType = "core",
        processCode = {"process-instance-created", "process-instance-updated"})
public class CiQProcessCreateProcessor extends AbstractProcessInstanceProcessor {

    private final RouteMapService routeMapService;

    public CiQProcessCreateProcessor(ProcessInstanceService processInstanceService, ObjectMapper objectMapper,
                                     ProcessPlanInstancePublisher processPlanInstancePublisher,
                                     RouteMapService routeMapService) {
        super(processInstanceService, objectMapper, processPlanInstancePublisher);
        this.routeMapService = routeMapService;
    }

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
        processInstance = this.processInstanceService.saveProcessInstance(processInstance); // local save
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

    /**
     * is valid itineraries exists
     *
     * @param processInstance
     * @return
     */
    private boolean isValidItinerariesExists(ProcessInstance processInstance) {
        List<Itinerary> itineraries;
        if (!(processInstance.getMetadata().get("itineraries") instanceof LinkedHashMap)) {
            return false;
        }

        LinkedHashMap<String, Object> itinerariesLL = (LinkedHashMap) processInstance.getMetadata().get("itineraries");
        try {
            String itinerariesStr = objectMapper.writeValueAsString(itinerariesLL);
            itineraries = objectMapper.readValue(itinerariesStr, new TypeReference<List<Itinerary>>() {
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
