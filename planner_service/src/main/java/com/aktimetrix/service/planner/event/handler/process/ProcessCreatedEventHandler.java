package com.aktimetrix.service.planner.event.handler.process;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.service.RegistryService;
import com.aktimetrix.core.stereotypes.EventHandler;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for handing the process created events from processor microservice.
 *
 * @author Arun.Kandakatla
 */
@Component
@EventHandler(eventType = Constants.PROCESS_EVENT, eventCode = Constants.PROCESS_CREATED)
public class ProcessCreatedEventHandler extends AbstractProcessEventHandler {
    public ProcessCreatedEventHandler(RegistryService registryService) {
        super(registryService);
    }
}
