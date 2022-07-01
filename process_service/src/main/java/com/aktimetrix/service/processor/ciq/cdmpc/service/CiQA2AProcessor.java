package com.aktimetrix.service.processor.ciq.cdmpc.service;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.MetadataProvider;
import com.aktimetrix.core.exception.DefinitionNotFoundException;
import com.aktimetrix.core.impl.AbstractProcessor;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.referencedata.model.StepDefinition;
import com.aktimetrix.core.stereotypes.ProcessHandler;
import com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.*;
import com.aktimetrix.service.processor.ciq.cdmpc.impl.CiQStepDefinitionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ComparisonChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author arun kumar kandakatla
 */
@Component
@ProcessHandler(processType = "CiQ", processCode = "A2ATRANSPORT", version = "14")
@Slf4j
public class CiQA2AProcessor extends AbstractProcessor {

    public static final String DEP_T = "DEP-T";
    public static final String FUNCTIONAL_CTX_EXPORT = "E";
    public static final String FUNTIONAL_CTX_IMPORT = "I";
    public static final String FUNTIONAL_CTX_TRANSIT = "T";

    public CiQA2AProcessor() {
        super();
    }

    @Autowired
    private ItineraryService itineraryService;

    @Autowired
    private CargoService cargoService;

    @Autowired
    @Qualifier("stepMetadataProvider")
    private MetadataProvider<Itinerary> stepMetadataProvider;

    @Autowired
    @Qualifier("processMetadataProvider")
    private MetadataProvider<Cargo> processMetadataProvider;

    /**
     * provides CiQ Domain specific metadata
     *
     * @param context Process Context
     * @return metadata
     */
    @Override
    protected Map<String, Object> getStepMetadata(Context context) {
        final Itinerary itinerary = itineraryService.getItineraryFromContext(context);
        return this.stepMetadataProvider.getMetadata(itinerary);
    }


    /**
     * provides CiQ Domain specific metadata
     *
     * @param context Process Context
     * @return metadata
     */
    @Override
    protected Map<String, Object> getProcessMetadata(Context context) {

        ProcessInstance processInstance = context.getProcessInstance();
        if (processInstance != null && !processInstance.isComplete()) { // already incomplete booking received
            // get metadata
            Map<String, Object> metadata = processInstance.getMetadata();
            // perform the validations here -- shipment information should be same for both booking events. TODO
            List<Itinerary> eventDetailsItineraries = itineraryService.getItinerariesFromContext(context);
            if (!eventDetailsItineraries.isEmpty()) {
                List<Itinerary> itineraries = (List<Itinerary>) metadata.get("itineraries");
                itineraries.addAll(eventDetailsItineraries);
                metadata.put("itineraries", itineraries);
            }
            return metadata;
        } else {
            Cargo cargo = cargoService.getCargoFromContext(context);
            Map<String, Object> metadata = this.processMetadataProvider.getMetadata(cargo);
            metadata.put("approvedIndicator", "A");
            // add itineraries to process metadata
            List<Itinerary> itineraries = itineraryService.getItinerariesFromContext(context);
            if (!itineraries.isEmpty()) {
                metadata.put("itineraries", itineraries);
            }
            log.debug("process metadata: {}", metadata.getOrDefault("itineraries", new ArrayList<Itinerary>()));
            return metadata;
        }
    }


    /**
     * CiQ Specific definitions
     *
     * @param context process context
     * @return step definitions
     * @throws DefinitionNotFoundException
     */
    @Override
    public List<StepDefinition> getStepDefinitions(Context context) throws DefinitionNotFoundException {
        final List<StepDefinition> stepDefinitions = super.getStepDefinitions(context);
        Cargo cargo = cargoService.getCargoFromContext(context);
        Objects.requireNonNull(cargo);
        final Itinerary itinerary = itineraryService.getItineraryFromContext(context);
        return new CiQStepDefinitionProvider(stepDefinitions, cargo, itinerary).getDefinitions();
    }

    @Override
    public List<StepInstance> saveStepInstances(String tenant, List<StepDefinition> stepDefinitions, String processInstanceId, Map<String, Object> metadata) {
        List<StepInstance> stepInstances = super.saveStepInstances(tenant, stepDefinitions, processInstanceId, metadata);

        log.debug("updating the location code for each step");
        if (stepInstances == null || stepInstances.isEmpty()) {
            return stepInstances;
        }
        for (StepInstance stepInstance : stepInstances) {
            String locationCode = getLocationCode(stepInstance);
            log.debug("location code for the step {} is :{}", stepInstance.getStepCode(), locationCode);
            stepInstance.setLocationCode(locationCode);
        }

        return stepInstances;
    }

    /**
     * @param stepInstance
     * @return
     */
    private String getLocationCode(StepInstance stepInstance) {
        Map<String, Object> metadata = stepInstance.getMetadata();
        if (StringUtils.equalsIgnoreCase(FUNCTIONAL_CTX_EXPORT, stepInstance.getFunctionalCtx())) {
            return (String) metadata.get("boardPoint");
        } else if (StringUtils.equalsIgnoreCase(FUNTIONAL_CTX_IMPORT, stepInstance.getFunctionalCtx())) {
            return ((String) metadata.get("offPoint"));
        } else if (StringUtils.equalsIgnoreCase(FUNTIONAL_CTX_TRANSIT, stepInstance.getFunctionalCtx())) {
            if (DEP_T.equals(stepInstance.getStepCode())) {
                return (String) metadata.get("boardPoint");
            } else {
                return ((String) metadata.get("offPoint"));
            }
        }
        return null;
    }

    @Override
    protected ProcessInstance getProcessInstance(Context context) {
        ProcessInstance processInstance = super.getProcessInstance(context);
        if (processInstance.getId() == null) {
            return processInstance;
        }
        // old process instance
        if (processInstance.isComplete()) {
            log.debug("finding the matching segment in the old process instance itinerary list.");
            List<Itinerary> from = (List<Itinerary>) processInstance.getMetadata().get("itineraries");
            Itinerary to = itineraryService.getItineraryFromContext(context);
            List<Itinerary> matchingSegments = itineraryService.findMatchingSegments(from, to);
            if (matchingSegments != null && !matchingSegments.isEmpty()) {
                List<Itinerary> itineraries = itineraryService.replaceMatchingSegments(from, to);
                processInstance.getMetadata().put("itineraries", itineraries);
                return processInstance;
            }
        } else {
            // compare quantity in both process instances
            log.debug("compare the quantity of old and new process instances.");
        }
        return processInstance;
    }
}
