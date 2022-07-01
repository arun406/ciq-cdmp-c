package com.aktimetrix.service.notification.event;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.stereotypes.EventHandler;
import com.aktimetrix.core.transferobjects.Event;
import com.aktimetrix.core.transferobjects.ProcessPlanDTO;
import com.aktimetrix.service.notification.api.CiQNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EventHandler(eventType = Constants.PLAN_EVENT, eventCode = Constants.PLAN_CANCELLED, version = Constants.DEFAULT_VERSION)
public class ProcessPlanCancelledEventHandler implements com.aktimetrix.core.api.EventHandler {

    @Autowired
    private CiQNotificationService ciQNotificationService;

    @Override
    public void handle(Event<?, ?> event) {
        log.debug("handling the plan created event for entity {}-{}", event.getEntityType(), event.getEntityId());
        ProcessPlanDTO plan = (ProcessPlanDTO) event.getEntity();
        log.debug("plan : {} ", plan);
        if ("C".equals(plan.getShipmentIndicator())) {
            ciQNotificationService.sendCAN(plan);
        }
    }
}
