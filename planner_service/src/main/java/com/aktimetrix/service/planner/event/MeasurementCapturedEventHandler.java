package com.aktimetrix.service.planner.event;

import com.aktimetrix.core.impl.DefaultContext;
import com.aktimetrix.core.model.MeasurementInstance;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.model.ProcessPlanInstance;
import com.aktimetrix.core.service.MeasurementInstanceService;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.aktimetrix.core.stereotypes.EventHandler;
import com.aktimetrix.core.transferobjects.Event;
import com.aktimetrix.core.transferobjects.Measurement;
import com.aktimetrix.core.transferobjects.ProcessInstanceDTO;
import com.aktimetrix.service.planner.Constants;
import com.aktimetrix.service.planner.api.Planner;
import com.aktimetrix.service.planner.service.RouteMapService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This class is responsible for handing the process created events from processor microservice.
 *
 * @author Arun.Kandakatla
 */
@Component
@EventHandler(eventType = Constants.MEASUREMENT_EVENT, eventCode = Constants.CREATED, version = com.aktimetrix.core.api.Constants.DEFAULT_VERSION)
@Slf4j
public class MeasurementCapturedEventHandler implements com.aktimetrix.core.api.EventHandler {

    @Autowired
    private MeasurementInstanceService measurementInstanceService;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private Planner planner;

    @Autowired
    private RouteMapService routeMapService;

    @Override
    public void handle(Event<?, ?> event) {
        log.debug("handing the process created event for entity {}-{}", event.getEntityType(), event.getEntityId());
        final Measurement dto = (Measurement) event.getEntity();
        log.debug("saving the measurement created event.");
        final MeasurementInstance measurementInstance = this.measurementInstanceService.saveMeasurementInstance(getMeasurementInstance(dto));

     /*   ProcessPlanInstance plan = planner.getActivePlan(event.getTenantKey(), measurementInstance.getProcessInstanceId());

        // if the plan exists. get the process instance
        if (plan == null) {
            log.debug("not plan exists. exiting from the flow after persisting measurement instance");
            return;
        }*/
        ProcessInstance processInstance = this.processInstanceService.getProcessInstance(event.getTenantKey(), dto.getProcessInstanceId());
        if (processInstance == null) {
            log.debug("no process instance exists. exiting from the flow after persisting measurement instance");
            return;
        }

        DefaultContext context = prepareContext(event, processInstance); //TODO change it as per aktimetrix core
      /*  boolean allMeasurementsCaptured = measurementInstanceService.isAllMeasurementsCaptured(context.getTenant(), processInstance, com.aktimetrix.service.planner.Constants.PLAN_MEASUREMENT_TYPE);
        log.debug("all measurements captured : {}", allMeasurementsCaptured);
*/
        if (processInstance.isComplete()) {
            routeMapService.createCompletePlan(context, processInstance);
        } else {
            routeMapService.upsertIncompletePlan(context, processInstance);
        }
        /*boolean allMeasurementsCaptured = this.routeMapService.isAllMeasurementsCaptured(event.getTenantKey(), processInstance, Constants.PLAN_MEASUREMENT_TYPE);
        if (allMeasurementsCaptured) {
            log.debug("everything is perfect. change the status of the process instance to original");
            processInstance.setStatus(Constants.STATUS_ORIGINAL);
            this.routeMapService.updateProcessInstance(processInstance);
            // update the process instance
            processInstance.setModifiedOn(LocalDateTime.now());
            processInstanceService.saveProcessInstance(processInstance);
        }*/
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
