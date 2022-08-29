package com.aktimetrix.service.planner;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.api.EventHandler;
import com.aktimetrix.core.exception.EventHandlerNotFoundException;
import com.aktimetrix.core.exception.MultipleEventHandlersFoundException;
import com.aktimetrix.core.service.RegistryService;
import com.aktimetrix.core.transferobjects.Event;
import com.aktimetrix.core.transferobjects.Measurement;
import com.aktimetrix.core.transferobjects.ProcessInstanceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.function.Consumer;

@ComponentScan(basePackages = {"com.aktimetrix.service", "com.aktimetrix.core"})
@SpringBootApplication
public class PlannerApplication {

    final private static Logger logger = LoggerFactory.getLogger(PlannerApplication.class.getName());

    @Autowired
    private RegistryService registryService;

    public static void main(String[] args) {
        SpringApplication.run(PlannerApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty(value = "aktimetrix.consumer.process.enabled", havingValue = "true", matchIfMissing = false)
    public Consumer<Event<ProcessInstanceDTO, Void>> processConsumer() {
        return event -> {
            final ProcessInstanceDTO dto = event.getEntity();
            final String tenantKey = event.getTenantKey();
            logger.debug("event type {}, event code : {}", event.getEventType(), event.getEventCode());
            logger.debug("process event received {} from tenant {}", dto, tenantKey);

            try {
                //
                EventHandler eventHandler = this.registryService.getEventHandler(event.getEventType(), event.getEventCode());
                eventHandler.handle(event);
            } catch (EventHandlerNotFoundException | MultipleEventHandlersFoundException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        };
    }

    @Bean
    @ConditionalOnProperty(value = "aktimetrix.consumer.measurement.enabled", havingValue = "true", matchIfMissing = false)
    public Consumer<Event<Measurement, Void>> measurementConsumer() {
        return event -> {
            final Measurement dto = event.getEntity();
            logger.debug("measurement event received {}", dto);
            try {
                EventHandler eventHandler = this.registryService.getEventHandler(event.getEventType(), event.getEventCode());
                eventHandler.handle(event);
            } catch (EventHandlerNotFoundException | MultipleEventHandlersFoundException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        };
    }

    @Bean
    @ConditionalOnProperty(value = "aktimetrix.consumer.step.enabled", havingValue = "true", matchIfMissing = false)
    public Consumer<Event<Measurement, Void>> stepConsumer() {
        return event -> {
            logger.debug("event: {}", event);
            try {
                this.registryService.getEventHandler(Constants.STEP_EVENT, event.getEventCode())
                        .handle(event);
            } catch (EventHandlerNotFoundException | MultipleEventHandlersFoundException e) {
                logger.error("Something happened bad. please contact system administrator.", e);
            }
        };
    }
}