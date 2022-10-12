package com.aktimetrix.service.processor.ciq.cdmpc.impl.generator;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.impl.generator.StepEventGenerator;
import com.aktimetrix.core.transferobjects.Event;
import com.aktimetrix.core.transferobjects.StepInstanceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Primary
@Qualifier("StepEventGenerator")
@Slf4j
@Component
public class CiQStepEventGenerator extends StepEventGenerator {

    @Override
    public Event<StepInstanceDTO, Void> generate(Object... object) {
        Event<StepInstanceDTO, Void> event = super.generate(object[0]);
        Context context = (Context) object[1];

        Map<String, Object> metadata = context.getProcessInstance().getMetadata();

        addShipmentMetadata(event, metadata);
        return event;
    }

    /**
     * add shipment level metadata
     *
     * @param event
     * @param metadata
     */
    private void addShipmentMetadata(Event<StepInstanceDTO, Void> event, Map<String, Object> metadata) {
        log.debug("adding additional shipment level properties");
        event.getEntity().getMetadata().putIfAbsent("forwarderCode", metadata.get("forwarderCode"));
        event.getEntity().getMetadata().putIfAbsent("productCode", metadata.get("productCode"));
        event.getEntity().getMetadata().putIfAbsent("eFreightCode", metadata.get("eFreightCode"));
        event.getEntity().getMetadata().putIfAbsent("reservationPieces", metadata.get("reservationPieces"));
        event.getEntity().getMetadata().putIfAbsent("reservationWeight", metadata.get("reservationWeight"));
        event.getEntity().getMetadata().putIfAbsent("reservationWeightUnit", metadata.get("reservationWeightUnit"));
        event.getEntity().getMetadata().putIfAbsent("reservationVolume", metadata.get("reservationVolume"));
        event.getEntity().getMetadata().putIfAbsent("reservationVolumeUnit", metadata.get("reservationVolumeUnit"));
        event.getEntity().getMetadata().putIfAbsent("origin", metadata.get("origin"));
        event.getEntity().getMetadata().putIfAbsent("destination", metadata.get("destination"));
        event.getEntity().getMetadata().putIfAbsent("documentNumber", metadata.get("documentNumber"));
        event.getEntity().getMetadata().putIfAbsent("documentType", metadata.get("documentType"));
    }
}
