package com.aktimetrix.service.planner.event;

import com.aktimetrix.core.impl.DefaultContext;
import com.aktimetrix.core.model.MeasurementInstance;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.service.MeasurementInstanceService;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.aktimetrix.core.stereotypes.EventHandler;
import com.aktimetrix.core.transferobjects.Event;
import com.aktimetrix.core.transferobjects.Measurement;
import com.aktimetrix.service.planner.Constants;
import com.aktimetrix.service.planner.service.FWBStepInstanceProcessor;
import com.aktimetrix.service.planner.service.RouteMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Arun.Kandakatla
 */
@Slf4j
@RequiredArgsConstructor
@Component
@EventHandler(eventType = Constants.MEASUREMENT_EVENT, eventCode = Constants.CREATED)
public class MeasurementCreatedEventHandler implements com.aktimetrix.core.api.EventHandler {

    final private MeasurementInstanceService measurementInstanceService;
    final private ProcessInstanceService processInstanceService;
    final private RouteMapService routeMapService;

    @Override
    public void handle(Event<?, ?> event) {
        log.debug("handing the measurement created event for entity {}-{}, event code: {}, event type: {} ",
                event.getEntityType(), event.getEntityId(), event.getEventCode(), event.getEventType());

        final Measurement dto = (Measurement) event.getEntity();
        final MeasurementInstance measurementInstance = getMeasurementInstance(dto);
        log.debug("saving the measurement created event.");
        this.measurementInstanceService.saveMeasurementInstance(measurementInstance);
        ProcessInstance processInstance = this.processInstanceService
                .getProcessInstance(event.getTenantKey(), dto.getProcessInstanceId());
        if (processInstance == null) {
            log.debug("no process instance exists. exiting from the flow after persisting measurement instance");
            return;
        }
        DefaultContext context = prepareContext(event, processInstance); //TODO change it as per aktimetrix core
        // planned type measurement processing
        if ("P".equals(measurementInstance.getType())) {
            if (processInstance.isComplete()) {
                this.routeMapService.createCompletePlan(context, processInstance);
            } else {
                this.routeMapService.upsertIncompletePlan(context, processInstance);
            }
        }
        // actual type measurement processing
        else if ("A".equals(measurementInstance.getType())) {
            final String stepCode = measurementInstance.getStepCode();
            final String stepInstanceId = measurementInstance.getStepInstanceId();
            final String processInstanceId = measurementInstance.getProcessInstanceId();
            log.info("step code :{}, step instance id : {}, process instance id: {} ",
                    stepCode, stepInstanceId, processInstanceId);
            if ("FWB".equals(stepCode)) {
                new FWBStepInstanceProcessor().process(context);
            }
        }
    }

    /**
     * prepares the ProcessContext
     *
     * @param event event
     * @return Process Context
     */
    public DefaultContext prepareContext(Event<?, ?> event, ProcessInstance processInstance) {
        DefaultContext processContext = new DefaultContext();
        processContext.setProperty("entityId", entityId(event));
        processContext.setProperty("entityType", entityType(event));
        processContext.setProperty("event", event);
        final Measurement entity = (Measurement) event.getEntity();
        processContext.setProperty("entity", entity);
        processContext.setProperty("eventData", event.getEventDetails());
        processContext.setTenant(event.getTenantKey());
        if (entity != null) {
            processContext.setProcessInstance(processInstance);
        }
        return processContext;
    }

    protected String entityType(Event<?, ?> event) {
        return event.getEntityType();
    }

    public String entityId(Event<?, ?> event) {
        return event.getEntityId();
    }

    private List<MeasurementInstance> getMeasurementInstances(String tenantKey, ObjectId processInstanceId) {
        return measurementInstanceService.getProcessMeasurements(tenantKey, processInstanceId.toString());
    }

    private MeasurementInstance getMeasurementInstance(Measurement dto) {
        MeasurementInstance instance = new MeasurementInstance();
        instance.setStepInstanceId(dto.getStepInstanceId());
        instance.setProcessInstanceId(dto.getProcessInstanceId());
        instance.setStepCode(dto.getStepCode());
        instance.setType(dto.getType());
        instance.setMetadata(dto.getMetadata());
        instance.setUnit(dto.getUnit());
        instance.setCode(dto.getCode());
        instance.setValue(dto.getValue());
        instance.setId(dto.getId());
        instance.setCreatedOn(dto.getCreatedOn());
        instance.setMeasuredAt(dto.getMeasuredAt());
        instance.setTenant(dto.getTenant());
        return instance;
    }
}
