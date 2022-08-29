package com.aktimetrix.service.planner.service;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.model.*;
import com.aktimetrix.core.service.MeasurementInstanceService;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.aktimetrix.service.planner.Constants;
import com.aktimetrix.service.planner.api.Planner;
import com.aktimetrix.service.planner.exception.ProcessPlanNotExistsException;
import com.aktimetrix.service.planner.transferobjects.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class RouteMapService {
    public static final String RFS_AIRCRAFT_CATEGORY = "RFS";
    public static final List<String> METADATA_PROPERTIES = Arrays.asList("origin", "destination", "forwarderCode",
            "reservationPieces", "reservationWeight", "reservationVolume", "reservationWeightUnit",
            "reservationVolumeUnit");

    final private RestTemplate restTemplate;
    final private Planner planner;
    final private MeasurementInstanceService measurementInstanceService;
    final protected ProcessInstanceService processInstanceService;
    final private ObjectMapper objectMapper;

    @Value("${ciq.cdmpc.encore.reference-data.base-url:http://localhost:6060}")
    private String referenceDataServiceBaseUrl;

    @Value("${ciq.cdmpc.encore.reference-data.routes:/reference-data/encore/routes}")
    private String routesEndpoint;
    public static final String YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * @param processInstance
     * @param oldProcessInstance
     * @return
     */
    public boolean isAnyOtherDifferencesExists(ProcessInstance processInstance, ProcessInstance oldProcessInstance) {

        // remove origin, destination, forwarder, pieces, weight and volume from both process instances and them compare
        Map<String, Object> newMetadata = processInstance.getMetadata();
        Map<String, Object> oldMetadata = oldProcessInstance.getMetadata();

        newMetadata.entrySet()
                .removeIf(entry -> METADATA_PROPERTIES.contains(entry.getKey()));

        oldMetadata.entrySet()
                .removeIf(entry -> METADATA_PROPERTIES.contains(entry.getKey()));

        if (newMetadata.size() != oldMetadata.size()) {
            return true;
        }

        // compare itineraries
        List<Itinerary> newItineraries = (List<Itinerary>) newMetadata.get("itineraries");
        List<Itinerary> oldItineraries = (List<Itinerary>) oldMetadata.get("itineraries");
        if (newItineraries.size() != oldItineraries.size()) {
            return true;
        }

//        newItineraries.stream().anyMatch(itinerary -> oldItineraries.stream().filter());

        boolean allMatch = !newMetadata.entrySet().stream()
                .allMatch(e -> e.getValue().equals(oldMetadata.get(e.getKey())));

        return allMatch;
    }

    /**
     * @param tenant
     * @param processInstance
     * @return
     */
    public boolean isDepStepPlanTimeExpired(String tenant, ProcessInstance processInstance) {
        String origin = (String) processInstance.getMetadata().get("origin");
        List<StepInstance> depStepsAtOrigin = processInstance.getSteps().stream()
                .filter(step -> "DEP".equals(step.getStepCode()) && step.getLocationCode().equals(origin))
                .collect(Collectors.toList());
        for (StepInstance stepInstance : depStepsAtOrigin) {
            List<MeasurementInstance> depPlannedMeasurements = this.measurementInstanceService.getStepMeasurements(tenant
                    , processInstance.getId(), stepInstance.getId(), "P");
            if (depPlannedMeasurements != null && !depPlannedMeasurements.isEmpty()) {
                for (MeasurementInstance depPlannedMeasurement : depPlannedMeasurements) {
                    if ("TIME".equals(depPlannedMeasurement.getCode())) {
                        log.debug("departure planned time is :{}", depPlannedMeasurement.getValue());
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        LocalDateTime planTime = LocalDateTime.parse(depPlannedMeasurement.getValue(), formatter);
                        if (planTime.isBefore(LocalDateTime.now())) {
                            log.debug("departure at {} plan time expired.", origin);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isQuantitySame(ProcessInstance processInstance, ProcessInstance oldProcessInstance) {
        int newPieces = (int) processInstance.getMetadata().get("reservationPieces");
        Double newWeight = (Double) processInstance.getMetadata().get("reservationWeight");
        Double newVolume = (Double) processInstance.getMetadata().get("reservationVolume");


        int oldPieces = (int) oldProcessInstance.getMetadata().get("reservationPieces");
        Double oldWeight = (Double) oldProcessInstance.getMetadata().get("reservationWeight");
        Double oldVolume = (Double) oldProcessInstance.getMetadata().get("reservationVolume");

        if (newPieces == oldPieces && Double.compare(newWeight, oldWeight) == 0 && Double.compare(newVolume, oldVolume) == 0) {
            return true;
        }
        return false;
    }

    public boolean isDepStepCompleted(String tenant, ProcessInstance processInstance) {
        String origin = (String) processInstance.getMetadata().get("origin");
        log.debug("checking flight is departed at origin station : {}", origin);
        List<StepInstance> depSteps = processInstance.getSteps().stream()
                .filter(step -> step.getStepCode().equals("DEP") && step.getLocationCode().equals(origin))
                .collect(Collectors.toList());
        for (StepInstance stepInstance : depSteps) {
            log.debug("step instance id : {}", stepInstance.getId());
            // get Actual measurements for step id from local copy
            List<MeasurementInstance> depActualMeasurements = this.measurementInstanceService.getStepMeasurements(tenant,
                    processInstance.getId(), stepInstance.getId(), "A");
            if (depActualMeasurements != null && !depActualMeasurements.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public String getShipmentIndicator(ProcessInstance processInstance) {
        if (isCargoIQProcess(processInstance)) {
            return Constants.CIQ_SHIPMENT;
        } else {
            return Constants.NETWORK_SHIPMENT;
        }
    }

    public String getApprovedIndicator(ProcessInstance processInstance) {
        if (isFWBAndOrRCSExists(processInstance)) {
            return Constants.N_APPROVED_INDICATOR;
        } else {
            return Constants.A_APPROVED_INDICATOR;
        }
    }

    /**
     * checks and confirm FWB and Or RCS record exists for entity id
     *
     * @param processInstance
     * @return
     */
    private boolean isFWBAndOrRCSExists(ProcessInstance processInstance) {
        String entityId = processInstance.getEntityId();
        String entityType = processInstance.getEntityType();
        boolean fwb = this.measurementInstanceService
                .isActualMeasurementsAvailableForStep(processInstance.getTenant(), entityId, entityType, "FWB");

        boolean rcs = this.measurementInstanceService
                .isActualMeasurementsAvailableForStep(processInstance.getTenant(), entityId, entityType, "RCS");

        return fwb || rcs;
    }

    private boolean isCargoIQProcess(ProcessInstance processInstance) {
        // call the lines rest endpoint
        String url = referenceDataServiceBaseUrl + routesEndpoint + "?tenant={tenant}&airline={airline}&forwarder={forwarder}&origin={origin}&destination={destination}";
        log.debug("routes api url : {}", url);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity requestEntity = new HttpEntity<>(headers);
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("tenant", processInstance.getTenant());
        uriVariables.put("airline", processInstance.getTenant());
        uriVariables.put("forwarder", (String) processInstance.getMetadata().get("forwarderCode"));
        uriVariables.put("origin", (String) processInstance.getMetadata().get("origin"));
        uriVariables.put("destination", (String) processInstance.getMetadata().get("destination"));
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class, uriVariables);
        log.debug("response status: {}", response.getStatusCode());
        if (HttpStatus.OK == response.getStatusCode() && !StringUtils.equalsIgnoreCase(response.getBody(), "[]")) {
            return true;
        }
        return false;
    }

    public void updateProcessInstance(ProcessInstance processInstance) {
        processInstance.getMetadata().put("phaseNumber", 1);
        processInstance.getMetadata().put("planNumber", 1);
        processInstance.getMetadata().put("flightSpecific", "F");
    }

    /**
     * check all measurements are captured or not
     *
     * @param tenantKey
     * @param processInstance
     * @param planMeasurementType
     * @return
     */
    public boolean isAllMeasurementsCaptured(String tenantKey, ProcessInstance processInstance, String planMeasurementType) {
        return this.measurementInstanceService.isAllMeasurementsCaptured(tenantKey, processInstance, planMeasurementType);
    }

    /**
     * returns the Direct Trucking Indicator
     *
     * @param processInstance
     * @return
     */
    public String getDirectTruckingIndicator(ProcessInstance processInstance) {
        String directTruckingIndicator = "N";
        List<Itinerary> itineraries = new ArrayList<>();
        if (processInstance.getMetadata().get("itineraries") instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> itinerariesLL = (LinkedHashMap) processInstance.getMetadata().get("itineraries");
            try {
                String itinerariesStr = objectMapper.writeValueAsString(itinerariesLL);
                itineraries = objectMapper.readValue(itinerariesStr, new TypeReference<List<Itinerary>>() {
                });
            } catch (JsonProcessingException e) {
                log.debug(e.getMessage(), e);
            }
        }
        if (!itineraries.isEmpty()) {
            List<String> airports = itineraries.stream().flatMap(i -> Stream.of(i.getBoardPoint().getCode(), i.getOffPoint().getCode()))
                    .collect(Collectors.toList());
            log.debug("airports in the route :{}", airports);
            // call airport reference data service to see airport is offline or online. // it should be cached at client side
            // get offline stations
            List<String> offlineAirports = new ArrayList<>();
            if (!offlineAirports.isEmpty()) {
                // get aircraft category of the segment
                for (Itinerary itinerary : itineraries) {
                    if (offlineAirports.contains(itinerary.getBoardPoint().getCode())
                            || offlineAirports.contains(itinerary.getOffPoint().getCode())) {
                        if (!RFS_AIRCRAFT_CATEGORY.equals(itinerary.getAircraftCategory())) {
                            directTruckingIndicator = "Y";
                        }
                    }
                }
            }
        }
        return (directTruckingIndicator);
    }

    /**
     * Create the process plan when process instance is completed.
     *
     * @param context
     * @param processInstance
     */
    public void createCompletePlan(Context context, ProcessInstance processInstance) {
        boolean allMeasurementsCaptured =
                this.isAllMeasurementsCaptured(context.getTenant(), processInstance, Constants.PLAN_MEASUREMENT_TYPE);
        log.debug("all measurements captured : {}", allMeasurementsCaptured);
        if (allMeasurementsCaptured) createProcessPlanInstance(context, processInstance);
    }

    /**
     * Creates a new Plan Object or returns and existing incomplete plan object
     *
     * @param context
     * @param processInstance
     * @return
     */
    private ProcessPlanInstance getIncompletePlan(Context context, ProcessInstance processInstance) {
        return this.planner.getIncompletePlan(context.getTenant(), processInstance.getId());
    }

    private ProcessPlanInstance getPreviousPlan(String tenant, String entityType, String entityId) {
        List<ProcessPlanInstance> activeProcessPlanInstances = this.planner.getActivePlans(tenant, entityId, entityType);
        if (activeProcessPlanInstances == null || activeProcessPlanInstances.isEmpty()) {
            return null;
        }
        return activeProcessPlanInstances.get(0);
    }

    /**
     * Create or update the plan in incomplete status
     *
     * @param context
     * @param processInstance
     */
    public void upsertIncompletePlan(Context context, ProcessInstance processInstance) {
        // create or update existing incomplete plan
        ProcessPlanInstance plan = getIncompletePlan(context, processInstance);
        if (plan == null) {
            plan = createProcessPlanInstance(processInstance, context, 1);
            log.debug("plan is created as :{}", plan);
        }
        String shipmentIndicator = this.getShipmentIndicator(processInstance);
        log.debug("shipment indicator :{}", shipmentIndicator);
        plan = createPlan(context.getTenant(), plan, shipmentIndicator, context.getProcessInstance());
        context.setProcessPlanInstance(plan);
        log.debug("plan id {}'s status is {} and version is {}", plan.getId(), plan.getStatus(), plan.getVersion());
    }


    /**
     * @param context
     * @param processInstance
     */
    private void createProcessPlanInstance(Context context, ProcessInstance processInstance) {
        int version = 1;
        String status = com.aktimetrix.service.planner.Constants.STATUS_ORIGINAL;
        log.debug("check any incomplete incompletePlan exists for the process instance id: {}", processInstance.getId());
        ProcessPlanInstance incompletePlan = getIncompletePlan(context, processInstance);
        if (incompletePlan != null)
            log.debug("incompletePlan id {}'s status is {} and version is {}", incompletePlan.getId(), incompletePlan.getStatus(), incompletePlan.getVersion());
        String shipmentIndicator = getShipmentIndicator(processInstance);
        //check any previous incompletePlan exists for entity id and entity type which is in original status
        log.debug("checking any previous incompletePlan exists for the entity ");
        ProcessPlanInstance previousPlan = getPreviousPlan(context.getTenant(), processInstance.getEntityId(), processInstance.getEntityType());
        log.debug("previous route map : {}", previousPlan);
        if (previousPlan != null) {
            final boolean completePlan = updateCompletePlan(context, processInstance, shipmentIndicator, previousPlan);
            if (completePlan) {
                // previous rm exists
                version = previousPlan.getVersion() + 1;
                if (incompletePlan != null) {
                    status = previousPlan.getStatus();
                }
            }
        }
        log.debug("creating a new complete incompletePlan with version and incompletePlan number as 1");
        if (incompletePlan == null) {
            incompletePlan = createProcessPlanInstance(processInstance, context, version);
        } else {
            incompletePlan.setVersion(version);
            incompletePlan.setPlanNumber(version);
            incompletePlan.setStatus(status);
            incompletePlan.setModifiedOn(LocalDateTime.now(ZoneOffset.UTC));
        }
        ProcessPlanInstance plan = createPlan(context.getTenant(), incompletePlan, shipmentIndicator, context.getProcessInstance());
        context.setProcessPlanInstance(plan);
    }


    /**
     * Update the existing plan or cancels the existing plan or recreate re-plan
     *
     * @param context
     * @param currentProcessInstance
     * @param shipmentIndicator
     * @param previousPlan
     * @return
     */
    private boolean updateCompletePlan(Context context, ProcessInstance currentProcessInstance,
                                       String shipmentIndicator, ProcessPlanInstance previousPlan) {

        ProcessInstance previousProcessInstance = processInstanceService.getProcessInstance(context.getTenant(), previousPlan.getProcessInstanceId());
        log.debug("previous route map(incompletePlan) process instance");
        log.debug("previous RM process instance id {}", previousPlan.getProcessInstanceId());
        if ("C".equalsIgnoreCase(previousPlan.getShipmentIndicator())) {
            //BKDC_A012_C060 (to establish if previous RM was for a Cargo iQ Partner shipment)
            // rmp is sent or not is depends on shipment indicator. if shipment is a cargo ciq partner shipment RMP will be sent to forwarder.
            if (!currentProcessInstance.getMetadata().get("origin").equals(previousProcessInstance.getMetadata().get("origin")) ||
                    !currentProcessInstance.getMetadata().get("destination").equals(previousProcessInstance.getMetadata().get("destination")) ||
                    !currentProcessInstance.getMetadata().get("forwarderCode").equals(previousProcessInstance.getMetadata().get("forwarderCode"))) {
                log.debug("origin or destination or forwarder code of current rm is different from previous rm.");

                if (com.aktimetrix.service.planner.Constants.STATUS_BASELINE.equals(previousPlan.getStatus())) {
                    if (!currentProcessInstance.getMetadata().get("origin").equals(previousProcessInstance.getMetadata().get("origin"))) {
                        log.debug("previous rm status is baseline and origin is different from the current rm");
                        // check no DEP for previous origin
                        log.debug("checking 'DEP' received at origin station");
                        if (this.isDepStepCompleted(context.getTenant(), previousProcessInstance)) {
                            log.debug("Departure event is received for the origin stations of baseline rm. ignoring the current rm.");
                            // ignore process instance
                            return false;
                        }
                        log.debug("'DEP' is not received at origin :{}", previousProcessInstance.getMetadata().get("origin"));
                    }
                }
                // cancel the previousPlan
                String status = previousPlan.getStatus();
                log.debug("status of previous rm is :{}.", status);
                log.debug("cancelling the previous plan.");
                ProcessPlanInstance cancelledPlan = cancelPlan(context.getTenant(), previousPlan);
                context.setProperty("cancelled-plan", cancelledPlan); // can be done much better TODO
                context.setProperty("cancelled-process-instance", previousProcessInstance);
                // create new process plan
                return true;
            }
        }
        boolean quantitySame = this.isQuantitySame(currentProcessInstance, previousProcessInstance);
        boolean anyOtherDifferencesExists = this.isAnyOtherDifferencesExists(currentProcessInstance, previousProcessInstance);

        if (com.aktimetrix.core.api.Constants.STATUS_BASELINE.equals(previousPlan.getStatus())) {
            if (!"C".equals(shipmentIndicator)) {
                //non Cargo iQ shipment handling
                log.debug("non cargo iq shipment handling.");
                return false;
            }
            if (!quantitySame) {
                if (!this.isDepStepCompleted(context.getTenant(), previousProcessInstance)) {
                    if (!this.isDepStepPlanTimeExpired(context.getTenant(), previousProcessInstance)) {
                        log.debug("update RM and create RMP with U (summary section only)");
                        ProcessPlanInstance processPlanInstance = updatePlan(shipmentIndicator, context.getTenant(),
                                previousPlan, currentProcessInstance);
                        context.setProcessPlanInstance(processPlanInstance);
                    }
                }
            }
            if (!anyOtherDifferencesExists) {
                // process stops;
                log.debug("no other information is different. process stops");
                return false;
            }
            ProcessPlanInstance replan = replan(context.getTenant(), previousPlan, currentProcessInstance, shipmentIndicator);
            context.setProcessPlanInstance(replan);
            return false;
        }

        previousPlan.setPlanNumber(previousPlan.getPlanNumber() + 1);
        previousPlan.setVersion(previousPlan.getVersion() + 1);
        ProcessPlanInstance processPlanInstance = updatePlan(shipmentIndicator, context.getTenant(), previousPlan, currentProcessInstance
        );
        context.setProcessPlanInstance(processPlanInstance);
        return false;
    }

    /**
     * @param processInstance
     * @param context
     * @param version
     * @return
     */
    private ProcessPlanInstance createProcessPlanInstance(ProcessInstance processInstance, Context context, int version) {
        String status = processInstance.isComplete() ? com.aktimetrix.service.planner.Constants.STATUS_ORIGINAL :
                com.aktimetrix.service.planner.Constants.STATUS_CREATED;
        String completeInd = processInstance.isComplete() ? "Y" : "N";
        return new ProcessPlanInstance(context.getTenant(), processInstance.getId(), processInstance.getProcessCode(), processInstance.getProcessType(),
                LocalDateTime.now(ZoneOffset.UTC), "Y", status,
                completeInd, processInstance.getEntityId(), processInstance.getEntityType(), version, version);
    }

    /**
     * Create the processPlanInstance
     *
     * @param tenant
     * @param processPlanInstance
     * @param shipmentIndicator
     * @param processInstance
     * @return
     */
    private ProcessPlanInstance createPlan(String tenant, ProcessPlanInstance processPlanInstance,
                                           String shipmentIndicator, ProcessInstance processInstance) {
        if (!"C".equals(shipmentIndicator)) {
            //non Cargo iQ shipment handling
            log.debug("non cargo iq shipment handling.");
        }
        if (isMilestoneSequenceValid(processPlanInstance)) {
            processPlanInstance.setValid(true);
        }
        String approvedIndicator = this.getApprovedIndicator(processInstance);
        processPlanInstance.setApprovedIndicator(approvedIndicator);
        String directTruckingIndicator = this.getDirectTruckingIndicator(processInstance);
        processPlanInstance.setDirectTruckingIndicator(directTruckingIndicator);
        processPlanInstance.setShipmentIndicator(shipmentIndicator);
        processPlanInstance.setFlightSpecificIndicator("F");
        processPlanInstance.setPhaseNumber("1");
        String completeInd = processInstance.isComplete() ? "Y" : "N";
        processPlanInstance.setCompleteInd(completeInd);
        boolean rmpSent = sendRMP(processPlanInstance);
        processPlanInstance.setRmpSent(rmpSent);
        log.debug("saving the processPlanInstance :{}", processPlanInstance);

        // get Planned Measurements for each step
        ArrayListMultimap<String, StepPlanInstance> stepsWithPlannedMeasurements = getStepPlanInstances(tenant, processInstance);
        printMeasurements(stepsWithPlannedMeasurements);
        processPlanInstance.setStepPlanInstances(stepsWithPlannedMeasurements);
        return this.planner.createPlan(tenant, processPlanInstance);
    }

    /**
     * @param tenant
     * @param previousPlan
     * @return
     */
    private ProcessPlanInstance cancelPlan(String tenant, ProcessPlanInstance previousPlan) {
        previousPlan.setStatus("Cancelled");
        previousPlan.setModifiedOn(LocalDateTime.now(ZoneOffset.UTC));
        ProcessPlanInstance processPlanInstance = planner.updatePlan(tenant, previousPlan);
        // send CAN
        sendCAN(previousPlan);
        log.debug("sending the CAN message");
        return processPlanInstance;
    }

    /**
     * Update the existing plan
     *
     * @param shipmentIndicator
     * @param tenant
     * @param plan
     * @param processInstance
     * @return
     */
    private ProcessPlanInstance updatePlan(String shipmentIndicator, String tenant, ProcessPlanInstance plan,
                                           ProcessInstance processInstance) {
        if (!"C".equals(shipmentIndicator)) {
            //non Cargo iQ shipment handling
            log.debug("non cargo iq shipment handling.");
        }
        log.debug("update the existing plan");
        if (isMilestoneSequenceValid(plan)) {
            log.debug("valid sequence.");
            plan.setValid(true);
        }
        String approvedIndicator = this.getApprovedIndicator(processInstance);
        plan.setApprovedIndicator(approvedIndicator);
        plan.setFlightSpecificIndicator("F");
        plan.setPhaseNumber("1");
       /* if (updatePlanNumber) {
            plan.setPlanNumber(plan.getPlanNumber() + 1);
            plan.setVersion(plan.getVersion() + 1);
        }*/
        log.debug("Update the existing plan by pointing it to new process instance.");
        plan.setProcessInstanceId(processInstance.getId()); // only pieces/weight/volume is updated
        // get Planned Measurements for each step for the latest process instance
        ArrayListMultimap<String, StepPlanInstance> oldStepWithPlannedMeasurements = plan.getStepPlanInstances();
        printMeasurements(oldStepWithPlannedMeasurements);
        ArrayListMultimap<String, StepPlanInstance> newStepsWithPlannedMeasurements = getStepPlanInstances(tenant, processInstance);
        printMeasurements(newStepsWithPlannedMeasurements);

        // update step status
        updatedStepStatus(tenant, newStepsWithPlannedMeasurements, oldStepWithPlannedMeasurements);
        plan.setStepPlanInstances(newStepsWithPlannedMeasurements);
        boolean rmpSent = sendRMP(plan);
        plan.setRmpSent(rmpSent);
        String updateFlag = "U";
        if (newStepsWithPlannedMeasurements != null && newStepsWithPlannedMeasurements.isEmpty()) {
            boolean match = newStepsWithPlannedMeasurements.entries().stream().anyMatch(entry -> !entry.getValue().getStatus().equals("S"));
            if (match) {
                updateFlag = "N";
            }
        }

        plan.setUpdateFlag(updateFlag);
        return this.planner.updatePlan(tenant, plan);
    }

    /**
     * Update the Plan - Status is set to REPLAN
     *
     * @param tenant
     * @param plan
     * @param processInstance
     * @param shipmentIndicator
     * @return
     */
    private ProcessPlanInstance replan(String tenant, ProcessPlanInstance plan, ProcessInstance processInstance, String shipmentIndicator) {
//        a) Update baseline RM.
//        b) Update re-plan RM in case re-plan RM exists.
//        c) Send RMP “summary section only” with “U” (indicator in field 19 (flag update)) to CDMP-F.
        if (isMilestoneSequenceValid(plan)) {
            log.debug("sequence is valid");
            plan.setValid(true);
        }
        plan.setTenant(tenant);
        // create re-plan RM for baseline plan
        // send rmp -- update
        String approvedIndicator = this.getApprovedIndicator(processInstance);
        plan.setApprovedIndicator(approvedIndicator);
        plan.setFlightSpecificIndicator("F");
        plan.setShipmentIndicator(shipmentIndicator);
        plan.setPhaseNumber("1");
        plan.setVersion(plan.getVersion() + 1);
        plan.setPlanNumber(plan.getPlanNumber() + 1);
        plan.setStatus(com.aktimetrix.service.planner.Constants.STATUS_REPLAN);
        plan.setProcessInstanceId(processInstance.getId());
        // get Planned Measurements for each step for the latest process instance
        ArrayListMultimap<String, StepPlanInstance> oldStepWithPlannedMeasurements = plan.getStepPlanInstances();
        printMeasurements(oldStepWithPlannedMeasurements);
        ArrayListMultimap<String, StepPlanInstance> newStepsWithPlannedMeasurements = getStepPlanInstances(tenant, processInstance);
        printMeasurements(newStepsWithPlannedMeasurements);

        // update step status
        updatedStepStatus(tenant, newStepsWithPlannedMeasurements, oldStepWithPlannedMeasurements);
        plan.setStepPlanInstances(newStepsWithPlannedMeasurements);
        boolean rmpSent = sendRMP(plan);
        plan.setRmpSent(rmpSent);
        String updateFlag = "U";
        if (newStepsWithPlannedMeasurements != null && newStepsWithPlannedMeasurements.isEmpty()) {
            boolean match = newStepsWithPlannedMeasurements.entries().stream().anyMatch(entry -> !entry.getValue().getStatus().equals("S"));
            if (match) {
                updateFlag = "N";
            }
        }

        plan.setUpdateFlag(updateFlag);
        return this.planner.updatePlan(tenant, plan);
    }

    private boolean isMilestoneSequenceValid(ProcessPlanInstance previousPlan) {
        return true;
    }

    /**
     * @param tenant
     * @param processInstance
     * @return
     */
    private ArrayListMultimap<String, StepPlanInstance> getStepPlanInstances(String tenant, ProcessInstance processInstance) {
        String processInstanceId = processInstance.getId();
        List<MeasurementInstance> plannedMeasurements = this.measurementInstanceService.getPlannedMeasurements(tenant, processInstanceId);
        ArrayListMultimap<String, StepPlanInstance> multimap = ArrayListMultimap.create();

        Map<String, Map<String, List<MeasurementInstance>>> collect = plannedMeasurements.stream()
                .collect(Collectors.groupingBy(MeasurementInstance::getStepCode, Collectors.groupingBy(MeasurementInstance::getStepInstanceId)));

        Map<String, List<StepPlanInstance>> stepPlanInstances = collect.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().entrySet().stream()
                        .map(e1 -> new StepPlanInstance(e.getKey(), e1.getKey(), processInstanceId, e1.getValue()))
                        .collect(Collectors.toList())));

        stepPlanInstances = stepPlanInstances.entrySet().stream()/*.filter(e -> Arrays.asList("FWB", "LAT", "RCS", "NFD", "AWD", "DLV").contains(e.getKey()))*/
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    if (!Arrays.asList("FWB", "LAT", "RCS", "NFD", "AWD", "DLV").contains(e.getKey())) {
                        return e.getValue();
                    }
                    Optional<MeasurementInstance> first = e.getValue().stream()
                            .flatMap(spi -> spi.getPlannedMeasurements().stream().filter(mi -> "TIME".equals(mi.getCode())))
                            .sorted((o1, o2) -> {
                                LocalDateTime t1 = LocalDateTime.parse(o1.getValue(), DateTimeFormatter.ofPattern(YYYY_MM_DD_T_HH_MM_SS));
                                LocalDateTime t2 = LocalDateTime.parse(o2.getValue(), DateTimeFormatter.ofPattern(YYYY_MM_DD_T_HH_MM_SS));
                                if (Arrays.asList("FWB", "LAT", "RCS").contains(o1.getStepCode())) {
                                    if (t2.isBefore(t1)) {
                                        return 1;
                                    }
                                } else if (Arrays.asList("NFD", "AWD", "DLV").contains(o1.getStepCode())) {
                                    if (t2.isAfter(t1)) {
                                        return -1;
                                    }
                                }
                                return 0;
                            })
                            .findFirst();
                    if (first.isPresent()) {
                        MeasurementInstance measurementInstance = first.get();
                        return e.getValue().stream().filter(spi -> spi.getPlannedMeasurements().stream()
                                        .anyMatch(mi -> mi.getId().equals(measurementInstance.getId())))
                                .collect(Collectors.toList());
                    }
                    return new ArrayList<>();
                }));
        stepPlanInstances.forEach((s, spiList) -> spiList.stream().forEach(spi -> multimap.put(s, spi)));
        return multimap;
    }

    /**
     * in case of Plan Update updating the step plan status.
     *
     * @param tenant
     * @param newStepsWithPlannedMeasurements
     * @param oldStepsWithPlannedMeasurements
     */
    private void updatedStepStatus(String tenant, ArrayListMultimap<String, StepPlanInstance> newStepsWithPlannedMeasurements,
                                   ArrayListMultimap<String, StepPlanInstance> oldStepsWithPlannedMeasurements) {
        for (Map.Entry<String, StepPlanInstance> entry : newStepsWithPlannedMeasurements.entries()) {
            String stepCode = entry.getKey();
            StepPlanInstance stepPlanInstance = entry.getValue();
            LocalDateTime newPlanTime = getPlanTime(stepPlanInstance);
            String newMeasuredAt = getMeasuredAt(stepPlanInstance);
            if (newPlanTime != null && newMeasuredAt != null
                    && isRecalculated(oldStepsWithPlannedMeasurements, stepCode, newPlanTime, newMeasuredAt)) {
                stepPlanInstance.setStatus("N");
                continue;
            }
            if (isCompleted(tenant, oldStepsWithPlannedMeasurements, stepCode)) {
                stepPlanInstance.setStatus("P");
                continue;
            }
            log.debug("checking the plan time of the step in the new plan is before the BKD event received time.");
            LocalDateTime eventTimestamp = LocalDateTime.now(ZoneOffset.UTC);
            if (newPlanTime.isBefore(eventTimestamp)) {
                log.debug("plan time of the step in the new plan is before the message received time. setting the step status to 'S'");
                stepPlanInstance.setStatus("P");
                continue;
            }
            stepPlanInstance.setStatus("S");
        }
    }

    private void printMeasurements(ArrayListMultimap<String, StepPlanInstance> newStepsWithPlannedMeasurements) {
        if (newStepsWithPlannedMeasurements != null) {
            log.debug("steps with planned measurements size : {}", newStepsWithPlannedMeasurements.size());
            newStepsWithPlannedMeasurements.entries().stream().forEach(entry -> {
                log.debug("entry: {}, and the value : {} ", entry.getKey(), entry.getValue());
            });
        }
    }

    /**
     * @param plan
     * @return
     */
    private boolean sendRMP(ProcessPlanInstance plan) {
        log.debug("sending RMP");
        if ("C".equals(plan.getShipmentIndicator())) {
            return true;
        } else {
            return false;
        }
    }

    private void sendCAN(ProcessPlanInstance previousPlan) {
        log.debug("CAN message is sent for {}", previousPlan.getId());
    }

    private String getMeasuredAt(StepPlanInstance stepPlanInstance) {
        return stepPlanInstance.getPlannedMeasurements().stream()
                .filter(mi -> mi.getCode().equals("TIME"))
                .map(MeasurementInstance::getMeasuredAt)
                .findFirst().orElse(null);
    }

    private LocalDateTime getPlanTime(StepPlanInstance stepPlanInstance) {
        return stepPlanInstance.getPlannedMeasurements().stream()
                .filter(mi -> mi.getCode().equals("TIME"))
                .map(mi -> LocalDateTime.parse(mi.getValue(), DateTimeFormatter.ofPattern(YYYY_MM_DD_T_HH_MM_SS)))
                .findFirst().orElse(null);
    }

    private boolean isCompleted(String tenant, ArrayListMultimap<String, StepPlanInstance> oldStepsWithPlannedMeasurements, String stepCode) {
        List<StepPlanInstance> stepPlanInstances = oldStepsWithPlannedMeasurements.get(stepCode);
        for (StepPlanInstance oldPlanInstance : stepPlanInstances) {
            log.debug("iterating for step :{}", stepCode);
            // check actual received for the step code
            String oldStepId = oldPlanInstance.getStepInstanceId();
            String oldProcessId = oldPlanInstance.getProcessInstanceId();
            log.debug("new process instance id: {}, new step instance id: {}", oldProcessId, oldStepId);
            boolean completed = this.planner.isStepCompleted(tenant, oldProcessId, oldStepId);
            log.debug("is step completed :{}", completed);
            if (completed) {
                log.debug("step in the old plan is already completed. so marking the corresponding step in the new plan to 'P'");
                // preceding steps status should be set to "P".
                return true;
            }
        }
        return false;
    }

    private boolean isRecalculated(ArrayListMultimap<String, StepPlanInstance> stepPlanInstancesMap,
                                   String stepCode, LocalDateTime newPlanTime, String newMeasuredAt) {
        if (!stepPlanInstancesMap.containsKey(stepCode)) {
            // step is new - set the status to "N"
            log.debug("step is new - set the status to 'N'");
            return true;
        }
        // check the plantime measurement.
        List<StepPlanInstance> stepPlanInstances = stepPlanInstancesMap.get(stepCode);
        for (StepPlanInstance oldPlanInstance : stepPlanInstances) {
            log.debug("iterating for step :{}", stepCode);
            // status "N"
            //- milestones with change in planned time (OR)
            //- milestones with change in station at which it is planned.
            log.debug("step exists in the old plan. comparing the location code and plan time");
            List<MeasurementInstance> plannedMeasurements = oldPlanInstance.getPlannedMeasurements();
            String measuredAt = getMeasuredAt(oldPlanInstance);
            LocalDateTime planTime = getPlanTime(oldPlanInstance);
            if (measuredAt != null && planTime != null && !planTime.isEqual(newPlanTime) || !measuredAt.equals(newMeasuredAt)) {
                log.debug("either plan time is different or location code is different. setting the status to 'N' in new plan");
                return true;
            }
        }
        return false;
    }

    /**
     * returns the active plan status
     *
     * @param tenant
     * @param entityId
     * @param entityType
     * @return
     */
    public String getRouteMapStatus(String tenant, String entityId, String entityType) throws ProcessPlanNotExistsException {
        List<ProcessPlanInstance> activeProcessPlanInstances = this.planner.getActivePlans(tenant, entityId, entityType);
        if (activeProcessPlanInstances != null && !activeProcessPlanInstances.isEmpty()) {
            return activeProcessPlanInstances.get(0).getStatus();
        }
        throw new ProcessPlanNotExistsException("");
    }

    /**
     * Itinerary predicates
     *
     * @author Arun.Kandakatla
     */
    public static class ItineraryPredicates {

        public static Predicate<Itinerary> isFlightSame(TransportInfo ti1) {
            return i -> {
                TransportInfo ti2 = i.getTransportInfo();
                return ComparisonChain.start().compare(ti1.getCarrier(), ti2.carrier)
                        .compare(ti1.getNumber(), ti2.getNumber())
                        .compare(ti1.getExtensionNumber(), ti2.getExtensionNumber())
                        .result() > 0;
            };
        }

        public static Predicate<Itinerary> isFlightDateSame(TransportTime departureDateTimeUTC1) {
            return i -> {
                TransportTime departureDateTimeUTC2 = i.getDepartureDateTimeUTC();
                log.debug("std 1: {}", departureDateTimeUTC1.getSchedule());
                log.debug("std 2: {}", departureDateTimeUTC2.getSchedule());
                return departureDateTimeUTC2.getSchedule().isEqual(departureDateTimeUTC1.getSchedule());
            };
        }

        public static Predicate<Itinerary> isBoardPointSame(StationInfo boardPoint1) {
            return i -> {
                StationInfo boardPoint2 = i.getBoardPoint();
                return ComparisonChain.start()
                        .compare(boardPoint1.getCode(), boardPoint2.getCode()).result() > 0;
            };
        }

        public static Predicate<Itinerary> isOffPointSame(StationInfo offPoint1) {
            return i -> {
                StationInfo offPoint2 = i.getOffPoint();
                return ComparisonChain.start()
                        .compare(offPoint1.getCode(), offPoint2.getCode()).result() > 0;
            };
        }

        public static Predicate<Itinerary> isQuantitySame(QuantityInfo quantityInfo1) {

            return i -> {
                QuantityInfo quantityInfo2 = i.getQuantity();
                return ComparisonChain.start().compare(quantityInfo1.getPiece(), quantityInfo2.getPiece())
                        .compare(quantityInfo1.getWeight().getValue(), quantityInfo2.getWeight().getValue())
                        .compare(quantityInfo1.getVolume().getValue(), quantityInfo2.getVolume().getValue())
                        .result() > 0;
            };

        }
    }
}
