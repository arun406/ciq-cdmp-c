package com.aktimetrix.service.processor.ciq.cdmpc.service;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.PostProcessor;
import com.aktimetrix.core.exception.DefinitionNotFoundException;
import com.aktimetrix.core.exception.MultiplePostProcessFoundException;
import com.aktimetrix.core.exception.PostProcessorNotFoundException;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.aktimetrix.core.service.RegistryService;
import com.aktimetrix.core.service.StepInstanceService;
import com.aktimetrix.core.stereotypes.Processor;
import com.aktimetrix.service.processor.ciq.cdmpc.event.exception.StepNotFoundException;
import com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.fwb.FWBEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Processor(processCode = "FWB", processType = "CiQ")
public class CiQA2AFWBProcessor implements com.aktimetrix.core.api.Processor {

    private ProcessInstanceService processInstanceService;
    private StepInstanceService stepInstanceService;
    private ObjectMapper objectMapper;
    private RegistryService registryService;

    @Autowired
    public CiQA2AFWBProcessor(ProcessInstanceService processInstanceService, StepInstanceService stepInstanceService,
                              ObjectMapper objectMapper, RegistryService registryService) {
        this.processInstanceService = processInstanceService;
        this.stepInstanceService = stepInstanceService;
        this.objectMapper = objectMapper;
        this.registryService = registryService;
    }

    /**
     * process the request
     *
     * @param context process context
     */
    @Override
    public void process(Context context) {
        // call pre processors
        executePreProcessors(context);
        // call do process
        try {
            doProcess(context);
        } catch (DefinitionNotFoundException e) {
            log.error(e.getLocalizedMessage(), e);
            // set errors in the context
        }
        // post processors
        executePostProcessors(context);
    }

    protected void executePreProcessors(Context context) {
        log.debug("executing pre processors");
    }

    public void executePostProcessors(Context context) {
        log.debug("executing post processors");
        try {
            final PostProcessor siPublisher = registryService.getPostProcessor("SI_PUBLISHER");
            siPublisher.process(context);
        } catch (PostProcessorNotFoundException | MultiplePostProcessFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @param context process context
     */
    public void doProcess(Context context) {

        final List<ProcessInstance> processInstances = processInstanceService
                .getProcessInstances(context.getTenant(), "CiQ", "A2ATRANSPORT",
                        (String) context.getProperty("entityType"), (String) context.getProperty("entityId"));

        if (processInstances == null || processInstances.isEmpty()) {
            log.debug("no process instance exists already, create dummy process instance");
        }
        ProcessInstance processInstance = processInstances.get(0);
        String stepCode = (String) context.getProperty("stepCode");
        // change the state of the step to complete.
        StepInstance created = processInstance.getSteps().stream()
                .filter(si -> si.getStepCode().equals(stepCode))
                .findFirst()
                .orElseThrow(() -> new StepNotFoundException("step instance not found."));

        StepInstance completed = createStepInstance(context, created);
        this.stepInstanceService.save(completed);

        processInstance.getSteps().add(completed);
        this.processInstanceService.saveProcessInstance(processInstance);
        context.setProcessInstance(processInstance);

        context.setStepInstances(Arrays.asList(completed));
    }

    /**
     * Create step instance
     *
     * @param context
     * @param existingStep
     * @return
     */
    private StepInstance createStepInstance(Context context, StepInstance existingStep) {
        // create a new step instance for the actual, keep the original state immutable
        StepInstance newStep = new StepInstance();
        newStep.setId(newStep.getId());
        newStep.setStatus(Constants.COMPLETED);
        newStep.setStepCode(existingStep.getStepCode());
        newStep.setVersion(existingStep.getVersion() + 1);
        newStep.setCreatedOn(existingStep.getCreatedOn());
        newStep.setGroupCode(existingStep.getGroupCode());
        newStep.setFunctionalCtx(existingStep.getFunctionalCtx());
        newStep.setLocationCode(existingStep.getLocationCode()); // take it from actual
        newStep.setModifiedOn(LocalDateTime.now(ZoneOffset.UTC));
        newStep.setTenant(context.getTenant());
        newStep.setProcessInstanceId(existingStep.getProcessInstanceId());
        try {
            newStep.setMetadata(createMetadata(context.getProperty("entity")));
        } catch (JsonProcessingException e) {

        }
        return newStep;
    }

    /**
     * Create the metadata for the actual step
     *
     * @param entity
     * @return
     */
    private Map<String, Object> createMetadata(Object entity) throws JsonProcessingException {
        Map<String, Object> metadata = new HashMap<>();
        final String value = this.objectMapper.writeValueAsString(entity);
        FWBEntity fwb = this.objectMapper.readValue(value, new TypeReference<>() {
        });

        metadata.put("origin", fwb.getOrigin());
        metadata.put("destination", fwb.getDestination());
        metadata.put("documentType", "AWB");
        metadata.put("documentNumber", getDocumentNumber(fwb));
        metadata.put("shcs", fwb.getSpecialHandlingCodes() != null ? fwb.getSpecialHandlingCodes()
                .stream().collect(Collectors.joining("-")) : null);
        metadata.put("reservationPieces", fwb.getQuantity().getPieces());
        metadata.put("reservationWeight", fwb.getQuantity().getWeight());
        metadata.put("reservationWeightUnit", fwb.getQuantity().getWeightCode());
        metadata.put("reservationVolume", fwb.getQuantity().getVolume());
        metadata.put("reservationVolumeUnit", fwb.getQuantity().getVolumeCode());
        metadata.put("forwarderCode", fwb.getAgent().getCode());
        return metadata;
    }

    private String getDocumentNumber(FWBEntity fwb) {
        return Arrays.asList(fwb.getAirWaybillPrefix(), fwb.getAirWaybillSerialNumber())
                .stream().collect(Collectors.joining("-"));
    }
}
