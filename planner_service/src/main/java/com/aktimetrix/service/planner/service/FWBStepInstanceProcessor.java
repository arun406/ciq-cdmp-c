package com.aktimetrix.service.planner.service;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.Processor;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.model.ProcessPlanInstance;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.model.StepPlanInstance;
import com.aktimetrix.core.service.MeasurementInstanceService;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.aktimetrix.service.planner.Constants;
import com.aktimetrix.service.planner.api.Planner;
import com.google.common.collect.ArrayListMultimap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class FWBStepInstanceProcessor implements Processor {

    private ProcessInstanceService processInstanceService;
    private MeasurementInstanceService measurementInstanceService;
    private Planner planner;
    private FWBStepService fwbStepService;

    /**
     * @param processInstanceService     process instance service
     * @param measurementInstanceService measurement instance service
     * @param planner                    planner
     * @param fwbStepService             fwb step utility service
     */
    @Autowired
    public FWBStepInstanceProcessor(ProcessInstanceService processInstanceService,
                                    MeasurementInstanceService measurementInstanceService, Planner planner,
                                    FWBStepService fwbStepService) {
        this.processInstanceService = processInstanceService;
        this.measurementInstanceService = measurementInstanceService;
        this.planner = planner;
        this.fwbStepService = fwbStepService;
    }

    /**
     * process the incoming context
     *
     * @param context process context
     */
    @Override
    public void process(Context context) {

        final List<StepInstance> stepInstances = context.getStepInstances();
        final Optional<StepInstance> stepInstanceOptional = collectionToStream(stepInstances).findFirst();
        if (!stepInstanceOptional.isPresent()) {
            return;
        }
        final StepInstance stepInstance = stepInstanceOptional.get();
        final String processInstanceId = stepInstance.getProcessInstanceId();
        log.info("step id: {}, process instance id : {} , step code: {}",
                stepInstance.getStepCode(), stepInstance.getId(), processInstanceId);

        log.debug("fetching the process instance object.");
        final String tenant = context.getTenant();
        final ProcessInstance processInstance = this.processInstanceService.getProcessInstance(tenant, processInstanceId);
        if (processInstance == null) {
            log.debug("process instance not available for {}", processInstanceId);
            return;
        }
        final boolean allMeasurementsCaptured = this.measurementInstanceService
                .isAllMeasurementsCaptured(tenant, processInstance, stepInstance.getId(), Constants.ACTUAL_MEASUREMENT_TYPE);
        log.info("is all measurements for the step {} are received ? {}", stepInstance.getStepCode(), allMeasurementsCaptured);
        if (!allMeasurementsCaptured) {
            return;
        }

        log.info("process instance : {}, entity type: {}, entity code : {}, process status: {}",
                processInstance.getId(), processInstance.getEntityType(), processInstance.getEntityId(), processInstance.getStatus());

        log.debug("fetching the latest active plan for current process instance id");
        final ProcessPlanInstance activePlan = this.planner.getActivePlan(tenant, processInstance.getId());
        log.info("latest active plan id :{}, status: {}", activePlan.getId(), activePlan.getStatus());

        if (Constants.STATUS_BASELINE.equals(activePlan.getStatus())) {
            log.debug("disregard  FWB and storing the result for reporting purpose.");
            return;
        }
        log.debug("validating the forwarder code");
        final String forwarderCode = (String) stepInstance.getMetadata().get("forwarderCode");
        final Map<String, Object> metadata = processInstance.getMetadata();
        if (!forwarderCode.equals(metadata.get("forwarderCode"))) {
            log.debug("cancellation process begin. \n 1. send CAN for latest RM to CDMP-F. \n 2. Discard all RMs. \n 3. Store FWB and any FOH and RCS ");
            return;
        }

        log.debug("validating origin and destination");
        if (!stepInstance.getMetadata().get("origin").equals(metadata.get("origin"))
                || !stepInstance.getMetadata().get("destination").equals(metadata.get("destination"))) {
            log.debug("origin and destination is not matching. perform FWBC_A011");
            return;
        }

        //  make plan as Live. send plan RMP/MUP
        log.debug("changing the plan state to Live");
        activePlan.setStatus(Constants.STATUS_LIVE);
        boolean updateProcessInstance = false;
        final Map<String, Integer> deviations = fwbStepService.getDeviations(tenant, processInstanceId, stepInstance.getId());
        log.debug("FWB weight and RM weight comparison");
        final Integer wtDeviation = deviations.get("WT_DEVIATION");

        if (wtDeviation > 1) {
            log.debug("FWB step weight is above maximum acceptable weight for this shipment");
            // send alert;
            return;
        }
        if (wtDeviation <= 0) {
            // update the rm weight
            metadata.put("reservationWeight", stepInstance.getMetadata().get("reservationWeight"));
            processInstance.setMetadata(metadata);
            updateProcessInstance = true;
            log.debug("FWB step weight is equal to or below maximum acceptable weight for this shipment");
            // send RMP
        }
        final Integer pcsDeviation = deviations.get("PCS_DEVIATION");
        if (pcsDeviation != 0) {
            metadata.put("reservationPieces", stepInstance.getMetadata().get("reservationPieces"));
            processInstance.setMetadata(metadata);
            updateProcessInstance = true;
            log.debug("Update the latest RM with the pieces from the FWB step");
            // send RMP
        }

        final Integer timeDeviation = deviations.get("TIME_DEVIATION");
        final String stepStatus = timeDeviation > 0 ? "F" : "P";
        final ArrayListMultimap<String, StepPlanInstance> stepPlanInstances = activePlan.getStepPlanInstances();
        stepPlanInstances.forEach((s, stepPlanInstance) -> {
            if (s.equals("FWB")) {
                stepPlanInstance.setStatus(stepStatus);
            }
        });

        if (updateProcessInstance) {
            this.processInstanceService.saveProcessInstance(processInstance);
        }
        this.planner.updatePlan(tenant, activePlan);
        log.debug("FWB pieces and RM pieces comparison");

    }

    /**
     * utility method to convert from collection to stream
     *
     * @param collection input collection
     * @return return stream
     */
    private Stream<StepInstance> collectionToStream(Collection<StepInstance> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }
}
