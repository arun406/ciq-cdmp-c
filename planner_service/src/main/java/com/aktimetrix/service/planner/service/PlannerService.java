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
        plan.setTenant(tenant);
        return this.repository.save(plan);
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

    /**
     * @param tenant
     * @param entityId
     * @param entityType
     * @param pageable
     * @return
     */
    public List<ProcessPlanInstance> getPlanByEntityIdAndEntityType(String tenant, String entityId, String entityType, PageRequest pageable) {
        return this.repository.findPlanByEntityIdAndEntityType(tenant, entityId, entityType, pageable);
    }

    /**
     * @param tenant
     * @param processInstanceId
     * @param isComplete
     * @return
     */
    public ProcessPlanInstance getPlan(String tenant, String processInstanceId, boolean isComplete) {
        return this.repository.findPlanByProcessInstanceIdAndCompleteIndicator(tenant, processInstanceId, isComplete ? "Y" : "N");
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
     * @param tenant
     * @param id
     * @return
     */
    public List<ProcessPlanInstance> getPlanByProcessId(String tenant, String id) {
        return this.repository.findPlanByProcessInstanceId(tenant, id);
    }
}
