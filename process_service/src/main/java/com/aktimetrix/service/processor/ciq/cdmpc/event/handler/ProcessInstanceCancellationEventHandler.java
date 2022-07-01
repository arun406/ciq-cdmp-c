package com.aktimetrix.service.processor.ciq.cdmpc.event.handler;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.event.handler.AbstractProcessEventHandler;
import com.aktimetrix.core.service.RegistryService;
import com.aktimetrix.core.stereotypes.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EventHandler(eventType = Constants.PROCESS_EVENT, eventCode = Constants.PROCESS_CANCELLED, version = Constants.DEFAULT_VERSION)
public abstract class ProcessInstanceCancellationEventHandler extends AbstractProcessEventHandler {

    public ProcessInstanceCancellationEventHandler(RegistryService registryService) {
        super(registryService);
    }
}
