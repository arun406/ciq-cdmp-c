package com.aktimetrix.service.processor.ciq.cdmpc.event.handler;

import com.aktimetrix.core.event.handler.AbstractEventHandler;
import com.aktimetrix.core.impl.DefaultProcessDefinitionProvider;
import com.aktimetrix.core.impl.DefaultStepDefinitionProvider;
import com.aktimetrix.core.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@com.aktimetrix.core.stereotypes.EventHandler(eventType = "FSU", eventCode = "FWB", version = "16")
public class FWBEventHandler extends AbstractEventHandler {

    public FWBEventHandler(RegistryService registryService, DefaultProcessDefinitionProvider processDefinitionProvider, DefaultStepDefinitionProvider stepDefinitionProvider) {
        super(registryService, processDefinitionProvider, stepDefinitionProvider);
    }
}
