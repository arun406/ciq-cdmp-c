package com.aktimetrix.service.notification;

import com.aktimetrix.core.api.EventHandler;
import com.aktimetrix.core.exception.EventHandlerNotFoundException;
import com.aktimetrix.core.exception.MultipleEventHandlersFoundException;
import com.aktimetrix.core.service.RegistryService;
import com.aktimetrix.core.transferobjects.Event;
import com.aktimetrix.core.transferobjects.ProcessPlanDTO;
import com.aktimetrix.service.notification.notification.SFTPProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.function.Consumer;

@ComponentScan(basePackages = {"com.aktimetrix.service", "com.aktimetrix.core"})
@SpringBootApplication
@EnableConfigurationProperties({CiQProperties.class, SFTPProperties.class})
@Slf4j
public class NotificationApplication implements ApplicationRunner {

    @Autowired
    private RegistryService registryService;


    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty(value = "aktimetrix.consumer.plan.enabled", havingValue = "true", matchIfMissing = false)
    public Consumer<Event<ProcessPlanDTO, Void>> processPlanConsumer() {
        return event -> {
            try {
                EventHandler eventHandler = this.registryService.getEventHandler(event.getEventType(), event.getEventCode());
                eventHandler.handle(event);
            } catch (EventHandlerNotFoundException | MultipleEventHandlersFoundException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        };
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("below ciq parameters are loaded....");
    }

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPath("classpath:/templates"); //defines the classpath location of the freemarker templates
        freeMarkerConfigurer.setDefaultEncoding("UTF-8"); // Default encoding of the template files
        return freeMarkerConfigurer;
    }
}