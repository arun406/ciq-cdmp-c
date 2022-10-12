package com.aktimetrix.service.planner.processor;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.Processor;
import com.aktimetrix.core.impl.publisher.ProcessPlanInstancePublisher;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractProcessInstanceProcessor implements Processor {

    final protected ProcessInstanceService processInstanceService;
    final protected ObjectMapper objectMapper;
    final protected ProcessPlanInstancePublisher processPlanInstancePublisher;

    public AbstractProcessInstanceProcessor(ProcessInstanceService processInstanceService,
                                            ObjectMapper objectMapper,
                                            ProcessPlanInstancePublisher processPlanInstancePublisher) {
        this.processInstanceService = processInstanceService;
        this.objectMapper = objectMapper;
        this.processPlanInstancePublisher = processPlanInstancePublisher;
    }

    /**
     * @param context process context
     */
    @Override
    public void process(Context context) {
        // call pre processors
        log.debug("executing the pre processors");
        executePreProcessors(context);
        // call the processor
        doProcess(context);
        // post processors
        log.debug("executing post processors");
        executePostProcessors(context);
    }

    protected abstract void doProcess(Context context);

    protected void executePreProcessors(Context context) {
    }

    protected void executePostProcessors(Context context) {
        this.processPlanInstancePublisher.publish(context);
    }
}
