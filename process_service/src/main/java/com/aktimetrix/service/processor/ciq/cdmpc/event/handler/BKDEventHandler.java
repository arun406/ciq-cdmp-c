package com.aktimetrix.service.processor.ciq.cdmpc.event.handler;

import com.aktimetrix.core.event.handler.AbstractEventHandler;
import com.aktimetrix.core.impl.DefaultProcessDefinitionProvider;
import com.aktimetrix.core.impl.DefaultStepDefinitionProvider;
import com.aktimetrix.core.service.RegistryService;
import com.aktimetrix.core.stereotypes.EventHandler;
import org.springframework.stereotype.Component;

/**
 * @author arun kumar k
 */
@Component
@EventHandler(eventType = "FSU", eventCode = "BKD", version = "16")
public class BKDEventHandler extends AbstractEventHandler {

    public BKDEventHandler(RegistryService registryService, DefaultProcessDefinitionProvider processDefinitionProvider, DefaultStepDefinitionProvider stepDefinitionProvider) {
        super(registryService, processDefinitionProvider, stepDefinitionProvider);
    }
}
