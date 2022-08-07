package com.aktimetrix.service.planner.event;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.event.handler.AbstractStepEventHandler;
import com.aktimetrix.core.service.RegistryService;
import com.aktimetrix.core.stereotypes.EventHandler;
import com.aktimetrix.core.transferobjects.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EventHandler(eventType = Constants.STEP_EVENT, eventCode = Constants.STEP_COMPLETED)
public class StepCompletedEventHandler extends AbstractStepEventHandler {

    public StepCompletedEventHandler(RegistryService registryService) {
        super(registryService);
    }

    @Override
    public String getProcessCode(Event<?, ?> event) {
        return Constants.STEP_INSTANCE_COMPLETED;
    }

    @Override
    public String getProcessType(Event<?, ?> event) {
        return Constants.STEP_INSTANCE_TYPE;
    }
}
