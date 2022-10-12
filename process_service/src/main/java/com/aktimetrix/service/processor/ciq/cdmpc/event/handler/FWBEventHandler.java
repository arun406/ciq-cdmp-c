package com.aktimetrix.service.processor.ciq.cdmpc.event.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@com.aktimetrix.core.stereotypes.EventHandler(eventType = "FSU", eventCode = "FWB", version = "16")
public class FWBEventHandler extends AbstractEventHandler {
}
