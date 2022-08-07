package com.aktimetrix.service.meter.core.meter.generators;

import com.aktimetrix.core.meter.impl.AbstractMeter;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.stereotypes.Measurement;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@Measurement(code = "TIME", stepCode = "FWB", type = "A")
public class FWBActualTimeMeter extends AbstractMeter {

    @Override
    protected String getMeasurementUnit(String tenant, StepInstance step) {
        return "TIMESTAMP";
    }

    @Override
    protected String getMeasurementValue(String tenant, StepInstance step) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(step.getModifiedOn());
    }
}
