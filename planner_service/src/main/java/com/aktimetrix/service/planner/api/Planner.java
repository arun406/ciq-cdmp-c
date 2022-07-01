package com.aktimetrix.service.planner.api;


import com.aktimetrix.core.model.ProcessPlanInstance;

import java.util.List;

/**
 * @author arun kumar kandakatla
 */
public interface Planner {

    ProcessPlanInstance createPlan(String tenant, ProcessPlanInstance plan);

    ProcessPlanInstance updatePlan(String tenant, ProcessPlanInstance plan);

    void cancelPlan();

    ProcessPlanInstance getPlan(String tenant, String planId);

    List<ProcessPlanInstance> getPlans(String tenant, String entityId, String entityType);

    List<ProcessPlanInstance> getPlans(String tenant, String entityId, String entityType, String status);

    List<ProcessPlanInstance> getActivePlans(String tenant, String entityId, String entityType);

    List<ProcessPlanInstance> getActivePlans(String tenant, String entityId, String entityType, String version);

    List<ProcessPlanInstance> getPlans(String tenant, String processInstanceId);

    ProcessPlanInstance getActivePlan(String tenant, String processInstanceId);

    List<ProcessPlanInstance> getAllActivePlans(String tenant);

    void activatePlan(String tenant, String planId);

    void deactivatePlan(String tenant, String planId);

    boolean isActive(String tenant, String planId);

    ProcessPlanInstance getIncompletePlan(String tenant, String id);

    boolean isStepCompleted(String tenant, String processInstanceId, String stepCode);
}
