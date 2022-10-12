package com.aktimetrix.service.planner.processor;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.Processor;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.service.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@com.aktimetrix.core.stereotypes.Processor(processType = "core", processCode = {"step-instance-completed"})
public class CiQStepCompletedProcessor implements Processor {

    private final ProcessInstanceService processInstanceService;

    /**
     * process
     *
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
