package com.aktimetrix.service.planner.event.handler.step;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.stereotypes.EventHandler;
import org.springframework.stereotype.Component;

@Component
@EventHandler(eventType = Constants.STEP_EVENT, eventCode = Constants.STEP_COMPLETED)
public class StepCompletedEventHandler extends AbstractStepEventHandler {
}
