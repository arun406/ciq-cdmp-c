package com.aktimetrix.service.processor.ciq.cdmpc.service;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.impl.StepEventGenerator;
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
        log.debug("adding additional shipment level properties");
        event.getEntity().getMetadata().put("forwarderCode", metadata.get("forwarderCode"));
        event.getEntity().getMetadata().put("productCode", metadata.get("productCode"));
        event.getEntity().getMetadata().put("eFreightCode", metadata.get("eFreightCode"));
        event.getEntity().getMetadata().put("reservationPieces", metadata.get("reservationPieces"));
        event.getEntity().getMetadata().put("reservationWeight", metadata.get("reservationWeight"));
        event.getEntity().getMetadata().put("reservationWeightUnit", metadata.get("reservationWeightUnit"));
        event.getEntity().getMetadata().put("reservationVolume", metadata.get("reservationVolume"));
        event.getEntity().getMetadata().put("reservationVolumeUnit", metadata.get("reservationVolumeUnit"));
        event.getEntity().getMetadata().put("origin", metadata.get("origin"));
        event.getEntity().getMetadata().put("destination", metadata.get("destination"));
        event.getEntity().getMetadata().put("documentNumber", metadata.get("documentNumber"));
        event.getEntity().getMetadata().put("documentType", metadata.get("documentType"));
        return event;
    }
}
