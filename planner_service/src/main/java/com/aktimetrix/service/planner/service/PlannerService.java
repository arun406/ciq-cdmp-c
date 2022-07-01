package com.aktimetrix.service.planner.service;

import com.aktimetrix.core.model.ProcessPlanInstance;
import com.aktimetrix.core.repository.ProcessPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlannerService {

    @Autowired
    private ProcessPlanRepository repository;

    /**
     * @param plan
     */
    public ProcessPlanInstance save(String tenant, ProcessPlanInstance plan) {
        return this.repository.save(plan);
    }

    /**
     * @param tenant
     * @param id
     * @param activeIndicator
     * @return
     */
    public ProcessPlanInstance getPlanByProcessId(String tenant, String id, String activeIndicator) {
        return this.repository.findPlanByProcessInstanceIdAndActiveIndicator(tenant, id, activeIndicator);
    }

    /**
     * update the existing plan
     *
     * @param plan
     * @return
     */
    public ProcessPlanInstance updatePlan(String tenant, ProcessPlanInstance plan) {
        // update the plan;
        plan.setModifiedOn(LocalDateTime.now());
        return this.repository.save(plan);
    }

    public List<ProcessPlanInstance> getPlanByEntityIdAndEntityType(String tenant, String entityId, String entityType, PageRequest pageable) {
        return this.repository.findPlanByEntityIdAndEntityType(tenant, entityId, entityType, pageable);
    }

    public ProcessPlanInstance getPlan(String tenant, String processInstanceId, boolean isComplete) {
        return this.repository.findPlanByProcessInstanceIdAndCompleteIndicator(tenant, processInstanceId, isComplete ? "Y" : "N");
    }

}
