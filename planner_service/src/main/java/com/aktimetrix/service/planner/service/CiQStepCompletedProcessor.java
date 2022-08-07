package com.aktimetrix.service.planner.service;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.aktimetrix.core.stereotypes.Processor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Processor(processCode = Constants.STEP_INSTANCE_COMPLETED, processType = Constants.STEP_INSTANCE_TYPE)
public class CiQStepCompletedProcessor implements com.aktimetrix.core.api.Processor {

    private final ProcessInstanceService processInstanceService;

    public CiQStepCompletedProcessor(ProcessInstanceService processInstanceService) {
        this.processInstanceService = processInstanceService;
    }

    /**
     * @param context process context
     */
    @Override
    public void process(Context context) {
        final String tenant = context.getTenant();
        ProcessInstance request = context.getProcessInstance();
        log.info("tenant :{} , process instance id : {}", tenant, request.getId());
        ProcessInstance processInstance = this.processInstanceService.getProcessInstance(tenant, request.getId());

        final List<StepInstance> stepInstances = request.getSteps().stream()
                .filter(stepInstance -> Constants.STEP_COMPLETED.equals(stepInstance.getStatus()))
                .collect(Collectors.toList());
        processInstance.getSteps().addAll(stepInstances);
        log.info("step instances : {}", stepInstances);
        this.processInstanceService.saveProcessInstance(processInstance);
        processInstance.getSteps().forEach(si -> {
            if ("FWB".equals(si.getStepCode())) {
                new FWBStepInstanceProcessor().process(context);
            }
        });
    }
}
