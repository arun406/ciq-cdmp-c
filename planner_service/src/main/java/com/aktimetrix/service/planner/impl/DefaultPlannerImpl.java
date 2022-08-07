package com.aktimetrix.service.planner.impl;

import com.aktimetrix.core.model.MeasurementInstance;
import com.aktimetrix.core.model.ProcessPlanInstance;
import com.aktimetrix.core.service.MeasurementInstanceService;
import com.aktimetrix.service.planner.api.Planner;
import com.aktimetrix.service.planner.service.PlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultPlannerImpl implements Planner {

    final private PlannerService plannerService;
    final private MeasurementInstanceService measurementInstanceService;


    @Override
    public ProcessPlanInstance updatePlan(String tenant, ProcessPlanInstance plan) {
        return plannerService.updatePlan(tenant, plan);
    }

    @Override
    public void cancelPlan() {
    }

    @Override
    public ProcessPlanInstance getPlan(String tenant, String planId) {
        return null;
    }


    @Override
    public List<ProcessPlanInstance> getPlans(String tenant, String entityId, String entityType) {
        Sort sort = Sort.by(Sort.Direction.DESC, "version");
        PageRequest pageable = PageRequest.of(0, 1, sort);
        return plannerService.getPlanByEntityIdAndEntityType(tenant, entityId, entityType, pageable);
    }

    @Override
    public List<ProcessPlanInstance> getPlans(String tenant, String entityId, String entityType, String status) {
        return null;
    }

    @Override
    public List<ProcessPlanInstance> getActivePlans(String tenant, String entityId, String entityType) {
        Sort sort = Sort.by(Sort.Direction.DESC, "version");
        PageRequest pageable = PageRequest.of(0, 1, sort);
        return plannerService.getPlanByEntityIdAndEntityType(tenant, entityId, entityType, pageable);
    }

    @Override
    public List<ProcessPlanInstance> getActivePlans(String tenant, String entityId, String entityType, String version) {
        return null;
    }

    @Override
    public List<ProcessPlanInstance> getPlans(String tenant, String processInstanceId) {
        return new ArrayList<>();
    }

    @Override
    public ProcessPlanInstance getActivePlan(String tenant, String processInstanceId) {
        return plannerService.getPlanByProcessId(tenant, processInstanceId, "Y");
    }

    @Override
    public List<ProcessPlanInstance> getAllActivePlans(String tenant) {
        return null;
    }

    @Override
    public void activatePlan(String tenant, String planId) {

    }

    @Override
    public void deactivatePlan(String tenant, String planId) {

    }

    @Override
    public boolean isActive(String tenant, String planId) {
        return false;
    }

    @Override
    public ProcessPlanInstance getIncompletePlan(String tenant, String processInstanceId) {
        return this.plannerService.getPlan(tenant, processInstanceId, false);
    }

    @Override
    public boolean isStepCompleted(String tenant, String processInstanceId, String stepInstanceId) {
        List<MeasurementInstance> a = this.measurementInstanceService.getStepMeasurements(tenant, processInstanceId, stepInstanceId, "A");
        if (a != null && !a.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Saves the Plan
     *
     * @param plan
     * @return
     */
    public ProcessPlanInstance createPlan(String tenant, ProcessPlanInstance plan) {
        return this.plannerService.save(tenant, plan);
    }
}
