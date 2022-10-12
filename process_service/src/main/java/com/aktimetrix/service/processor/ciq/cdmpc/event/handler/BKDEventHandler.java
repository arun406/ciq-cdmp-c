package com.aktimetrix.service.processor.ciq.cdmpc.event.handler;

import com.aktimetrix.core.stereotypes.EventHandler;
import org.springframework.stereotype.Component;

/**
 * @author arun kumar k
 */
@Component
@EventHandler(eventType = "FSU", eventCode = "BKD", version = "16")
public class BKDEventHandler extends AbstractEventHandler {

}
