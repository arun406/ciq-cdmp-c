package com.aktimetrix.service.processor.ciq.cdmpc.service;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.MetadataProvider;
import com.aktimetrix.core.api.PostProcessor;
import com.aktimetrix.core.exception.DefinitionNotFoundException;
import com.aktimetrix.core.exception.MultiplePostProcessFoundException;
import com.aktimetrix.core.exception.PostProcessorNotFoundException;
import com.aktimetrix.core.impl.AbstractProcessor;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.referencedata.model.ProcessDefinition;
import com.aktimetrix.core.referencedata.model.StepDefinition;
import com.aktimetrix.core.stereotypes.Processor;
import com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.Cargo;
import com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.Itinerary;
import com.aktimetrix.service.processor.ciq.cdmpc.impl.CiQStepDefinitionProvider;
import com.aktimetrix.service.processor.ciq.cdmpc.service.util.CargoUtilService;
import com.aktimetrix.service.processor.ciq.cdmpc.service.util.ItineraryUtilService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author arun kumar kandakatla
 */
@Component
@Processor(processType = "CiQ", processCode = "A2ATRANSPORT", version = "14")
@Slf4j
public class CiQA2AProcessor extends AbstractProcessor {

    public static final String DEP_T = "DEP-T";
    public static final String FUNCTIONAL_CTX_EXPORT = "E";
    public static final String FUNCTIONAL_CTX_IMPORT = "I";
    public static final String FUNCTIONAL_CTX_TRANSIT = "T";

    public CiQA2AProcessor() {
        super();
    }

    @Autowired
    private ItineraryUtilService itineraryService;

    @Autowired
    private CargoUtilService cargoService;

    @Autowired
    @Qualifier("stepMetadataProvider")
    private MetadataProvider<Itinerary> stepMetadataProvider;

    @Autowired
    @Qualifier("processMetadataProvider")
    private MetadataProvider<Cargo> processMetadataProvider;

    @Autowired
    private ObjectMapper objectMapper;

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
            List<Itinerary> eventDetailsItineraries = itineraryService.extractItinerariesFromContext(context);
            List<Itinerary> itineraries = (List<Itinerary>) metadata.get("itineraries");
            itineraries.addAll(eventDetailsItineraries);
            metadata.put("itineraries", itineraries);
            return metadata;
        }
        Cargo cargo = this.cargoService.extractCargoFromContext(context);
        Map<String, Object> metadata = this.processMetadataProvider.getMetadata(cargo);
        metadata.put("approvedIndicator", "A");
        // add itineraries to process metadata
        List<Itinerary> itineraries = itineraryService.extractItinerariesFromContext(context);
        metadata.put("itineraries", itineraries);
        log.debug("process metadata: {}", metadata.getOrDefault("itineraries", new ArrayList<Itinerary>()));
        return metadata;

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
        // get the step definitions from the core.
        final List<StepDefinition> stepDefinitions = super.getStepDefinitions(context);

        Cargo cargo = this.cargoService.extractCargoFromContext(context);
        Objects.requireNonNull(cargo);
        final Itinerary itinerary = this.itineraryService.getItineraryFromContext(context);
        Objects.requireNonNull(itinerary);
        // update the steps based on CiQ CDPM-C specification
        CiQStepDefinitionProvider definitionProvider = new CiQStepDefinitionProvider(stepDefinitions, cargo, itinerary);
        return definitionProvider.getDefinitions();
    }


    /**
     * creating the step instances
     *
     * @param context
     * @return
     * @throws DefinitionNotFoundException
     */
    @Override
    protected List<StepInstance> createStepInstances(Context context) throws DefinitionNotFoundException {
        List<StepInstance> stepInstances = super.createStepInstances(context);
        // update the existing step instances process instance id
        if (stepInstances == null) {
            stepInstances = new ArrayList<>();
        }
        log.debug("step instances {}", stepInstances);

        final ProcessInstance processInstance = context.getProcessInstance();
        stepInstances.addAll(processInstance.getSteps().stream()
                .map(stepInstance -> {
                    stepInstance.setProcessInstanceId(processInstance.getId());
                    return stepInstance;
                })
                .collect(Collectors.toList()));

        log.debug("step instances size {}", stepInstances.size());
        return stepInstances;
    }

    /**
     * @param tenant          tenant
     * @param stepDefinitions step definitions
     * @param metadata        metadata
     * @return
     */
    @Override
    public List<StepInstance> createStepInstances(String tenant, List<StepDefinition> stepDefinitions, Map<String, Object> metadata) {
        List<StepInstance> stepInstances = super.createStepInstances(tenant, stepDefinitions, metadata);

        log.debug("updating the location code for each step");
        if (stepInstances == null || stepInstances.isEmpty()) {
            return new ArrayList<>();
        }
        stepInstances.forEach(stepInstance -> {
            String locationCode = getLocationCode(stepInstance);
            log.debug("location code for the step {} is :{}", stepInstance.getStepCode(), locationCode);
            stepInstance.setLocationCode(locationCode);
        });

        return stepInstances;
    }

    /**
     * get the location code for step
     *
     * @param stepInstance
     * @return
     */
    private String getLocationCode(StepInstance stepInstance) {
        Map<String, Object> metadata = stepInstance.getMetadata();
        if (StringUtils.equalsIgnoreCase(FUNCTIONAL_CTX_EXPORT, stepInstance.getFunctionalCtx())) {
            return (String) metadata.get("boardPoint");
        } else if (StringUtils.equalsIgnoreCase(FUNCTIONAL_CTX_IMPORT, stepInstance.getFunctionalCtx())) {
            return ((String) metadata.get("offPoint"));
        } else if (StringUtils.equalsIgnoreCase(FUNCTIONAL_CTX_TRANSIT, stepInstance.getFunctionalCtx())) {
            if (DEP_T.equals(stepInstance.getStepCode())) {
                return (String) metadata.get("boardPoint");
            } else {
                return ((String) metadata.get("offPoint"));
            }
        }
        return null;
    }

    @Override
    protected ProcessInstance createProcessInstance(Context context) {

        log.debug("preparing the process metadata from the current context");
        final Map<String, Object> processMetadata = this.getProcessMetadata(context);
        ProcessDefinition definition = (ProcessDefinition) context.getProperty(Constants.PROCESS_DEFINITION);
        log.debug("definition code: {}", definition.getProcessCode());
        String entityId = (String) context.getProperty(Constants.ENTITY_ID);
        String entityType = (String) context.getProperty(Constants.ENTITY_TYPE);
        log.debug("entity id: {}, entity type :{}", entityId, entityType);

        log.debug("looking for process instance with same entity id and entity type.");
        // check process instance already exists for this entity type, entity id, process code combination
        String tenant = context.getTenant();
        List<ProcessInstance> processInstances = super.processInstanceService.getProcessInstances(tenant,
                definition.getProcessType(), definition.getProcessCode(), entityType, entityId);

        // when its new process instance
        if (processInstances == null || processInstances.isEmpty()) {
            return super.createProcessInstance(context, definition, entityType, entityId);
        }
        ProcessInstance processInstance = createProcessInstance(processMetadata, processInstances);

        // if the existing consignment does not match create new process instance.
        if (processInstance == null) {
            return super.createProcessInstance(context, definition, entityType, entityId);
        }

        if (!processInstance.isComplete()) {
            log.debug("retrieving existing incomplete process instances.");
            updateProcessInstanceMetadataAndSteps(context, tenant, processInstance);
        } else {
            log.debug("finding the matching segment in the old process instance itinerary list.");
            updateProcessInstanceMetadata(context, processInstance);
        }
        return processInstance;
    }

    /**
     * add itineraries from the incomplete process instance
     *
     * @param context
     * @param processInstance
     */
    private void updateProcessInstanceMetadata(Context context, ProcessInstance processInstance) {
        List<Itinerary> from = (List<Itinerary>) processInstance.getMetadata().get("itineraries");
        Itinerary to = itineraryService.getItineraryFromContext(context);
        List<Itinerary> matchingSegments = itineraryService.findMatchingSegments(from, to);
        log.debug("matching segments size : {}", matchingSegments.size());

        if (matchingSegments.size() > 1) {
            throw new RuntimeException("unable identify the identify itinerary to replace");
        }
        if (matchingSegments.size() == 1) {
            String tenant = context.getTenant();
            String id = processInstance.getId();
            Itinerary matchingSegment = matchingSegments.get(0);
            log.debug("matching segment: {}", matchingSegment);
            List<StepInstance> stepInstances = this.stepInstanceService.getStepInstancesByProcessInstanceId(tenant, id);
            log.debug("filtering the steps for the replace itinerary");
            if (stepInstances != null && !stepInstances.isEmpty()) {
                final List<StepInstance> filteredSteps = this.filterStepsByItinerary(stepInstances, matchingSegment);
                ProcessInstance newProcessInstance = cloneProcessInstance(processInstance, filteredSteps);
                List<Itinerary> itineraries = itineraryService.replaceMatchingSegments(from, to);
                newProcessInstance.getMetadata().put("itineraries", itineraries); // update itineraries
            }
        }
    }

    /**
     * @param processInstance
     * @param filteredSteps
     */
    private ProcessInstance cloneProcessInstance(ProcessInstance processInstance, List<StepInstance> filteredSteps) {
        ProcessInstance newProcessInstance = new ProcessInstance();
        newProcessInstance.setPreviousId(processInstance.getId());
        newProcessInstance.setId(null);
        newProcessInstance.setVersion(processInstance.getVersion() + 1);
        newProcessInstance.setSteps(filteredSteps);
        newProcessInstance.setProcessCode(processInstance.getProcessCode());
        newProcessInstance.setProcessType(processInstance.getProcessType());
        newProcessInstance.setActive(true);
        newProcessInstance.setComplete(processInstance.isComplete());
        newProcessInstance.setValid(processInstance.isValid());
        newProcessInstance.setStatus(Constants.PROCESS_CREATED);
        newProcessInstance.setTenant(processInstance.getTenant());
        newProcessInstance.setCategoryCode(processInstance.getCategoryCode());
        newProcessInstance.setSubCategoryCode(processInstance.getSubCategoryCode());
        newProcessInstance.setShipmentIdentifier(processInstance.getShipmentIdentifier());
        newProcessInstance.setModifiedOn(LocalDateTime.now(ZoneOffset.UTC));
        newProcessInstance.setEntityId(processInstance.getEntityId());
        newProcessInstance.setEntityType(processInstance.getEntityType());
        newProcessInstance.setApprovedIndicator(processInstance.getApprovedIndicator());
        newProcessInstance.setCreatedOn(processInstance.getCreatedOn());
        newProcessInstance.setCreatedBy(processInstance.getCreatedBy());
        newProcessInstance.setMetadata(processInstance.getMetadata());
        return newProcessInstance;
    }

    /**
     * Filter the step instances by the itinerary details
     *
     * @param stepInstances
     * @param itinerary
     * @return
     */
    public List<StepInstance> filterStepsByItinerary(List<StepInstance> stepInstances, Itinerary itinerary) {

        String flightNumber = itinerary.getTransportInfo().getCarrier() +
                itinerary.getTransportInfo().getNumber();
        if (itinerary.getTransportInfo().getExtensionNumber() != null) {
            flightNumber = flightNumber + itinerary.getTransportInfo().getExtensionNumber();
        }
        log.debug("flight number :{}", flightNumber);
        String finalFlightNumber = flightNumber;

        log.debug("itinerary: {}", itinerary);
        return stepInstances.stream()
                .filter(si -> {
                            si.getMetadata().forEach((s, o) -> {
                                log.debug("{}:{}", s, o);
                            });
                            if (si.getMetadata().get("boardPoint").equals(itinerary.getBoardPoint().getCode())
                                    && si.getMetadata().get("offPoint").equals(itinerary.getOffPoint().getCode())
                                    && si.getMetadata().get("flightNumber").equals(finalFlightNumber)
                                    && si.getMetadata().get("flightDate").equals(itinerary.getDepartureDateTimeUTC().getEstimated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                    && (int) si.getMetadata().get("pieces") == itinerary.getQuantity().getPiece()
                                    && new BigDecimal((String) si.getMetadata().get("wt")).compareTo(BigDecimal.valueOf(itinerary.getQuantity().getWeight().getValue())) == 0
                                    && new BigDecimal((String) si.getMetadata().get("vol")).compareTo(BigDecimal.valueOf(itinerary.getQuantity().getVolume().getValue())) == 0
                                    && si.getMetadata().get("wtUnit").equals(itinerary.getQuantity().getWeight().getUnit().getCode())
                                    && si.getMetadata().get("volUnit").equals(itinerary.getQuantity().getVolume().getUnit().getCode())) {
                                return false;
                            }
                            return true;
                        }
                )
                .map(stepInstance -> {
                    stepInstance.setId(null);
                    stepInstance.setProcessInstanceId(null);
                    return stepInstance;
                })
                .collect(Collectors.toList());
    }

    /**
     * @param context
     * @param tenant
     * @param processInstance
     */
    private void updateProcessInstanceMetadataAndSteps(Context context, String tenant, ProcessInstance processInstance) {
        String id = processInstance.getId();
        List<StepInstance> stepInstances = this.stepInstanceService.getStepInstancesByProcessInstanceId(tenant, id);
        // update steps
        processInstance.setSteps(stepInstances);

        List<Itinerary> eventDetailsItineraries = this.itineraryService.extractItinerariesFromContext(context);
        Map<String, Object> metadata = processInstance.getMetadata();
        List<Itinerary> itineraries = (List<Itinerary>) metadata.get("itineraries");
        itineraries.addAll(eventDetailsItineraries);
        metadata.put("itineraries", itineraries);

        // update metadata
        processInstance.setMetadata(metadata);
    }

    /**
     * filter and get process instance
     *
     * @param processMetadata
     * @param processInstances
     * @return
     */
    private ProcessInstance createProcessInstance(Map<String, Object> processMetadata, List<ProcessInstance> processInstances) {
        return processInstances.stream()
                .sorted(Comparator.comparing(ProcessInstance::getVersion).reversed()) // high version first
                .filter(pi -> pi.getMetadata().get("origin").equals((String) processMetadata.get("origin"))
                        && pi.getMetadata().get("destination").equals((String) processMetadata.get("destination"))
                        && (int) pi.getMetadata().get("reservationPieces") == (int) processMetadata.get("reservationPieces")
                        && pi.getMetadata().get("reservationWeightUnit").equals((String) processMetadata.get("reservationWeightUnit"))
                        && pi.getMetadata().get("reservationVolumeUnit").equals((String) processMetadata.get("reservationVolumeUnit"))
                        && new BigDecimal((String) pi.getMetadata().get("reservationWeight")).compareTo((BigDecimal) processMetadata.get("reservationWeight")) == 0
                        && new BigDecimal((String) pi.getMetadata().get("reservationVolume")).compareTo((BigDecimal) processMetadata.get("reservationVolume")) == 0
                )
                .findFirst()
                .orElse(null);
    }

    @Override
    public void executePostProcessors(Context context) {
        super.executePostProcessors(context);
        // publish the process_event and step_event
        try {
            PostProcessor completenessChecker = registryService.getPostProcessor("COMPLETE_CHECKER");
            log.debug("pi completeness checker found {}", completenessChecker.getClass().getCanonicalName());
            completenessChecker.process(context);
            PostProcessor piPublisher = registryService.getPostProcessor("PI_PUBLISHER");
            log.debug("pi publisher found {}", piPublisher.getClass().getCanonicalName());
            piPublisher.process(context);
            PostProcessor siPublisher = registryService.getPostProcessor("SI_PUBLISHER");
            log.debug("si publisher found {}", siPublisher.getClass().getCanonicalName());
            siPublisher.process(context);
        } catch (PostProcessorNotFoundException | MultiplePostProcessFoundException e) {
            log.debug(e.getMessage(), e);
        }
    }
}
