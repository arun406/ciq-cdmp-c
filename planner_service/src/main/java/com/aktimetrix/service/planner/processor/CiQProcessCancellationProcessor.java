package com.aktimetrix.service.planner.processor;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.aktimetrix.core.stereotypes.Processor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@Processor(processType = "core", processCode = {"process-instance-cancelled"})
public class CiQProcessCancellationProcessor implements com.aktimetrix.core.api.Processor {

    @Autowired
    private ProcessInstanceService processInstanceService;

    /**
     * @param context process context
     */
    @Override
    public void process(Context context) {
        // call pre processors
        executePreProcessors(context);
        // call do process
        doProcess(context);
        // post processors
        executePostProcessors(context);
    }

    private void executePostProcessors(Context context) {
        log.debug("executing post processors");
    }

    private void doProcess(Context context) {
        ProcessInstance processInstance = context.getProcessInstance();
        log.debug("process instance id :{}", processInstance.getId());
        Map<String, Object> eventData = (Map<String, Object>) context.getProperty("eventData");
        String cancellationReason = (String) eventData.get("cancellationReason");
        log.debug("cancellation reason: {}", cancellationReason);
        processInstanceService.cancelProcessInstance(context.getTenant(), processInstance.getId(), cancellationReason);
    }

    private void executePreProcessors(Context context) {
        log.debug("executing pre processors");
    }
}
