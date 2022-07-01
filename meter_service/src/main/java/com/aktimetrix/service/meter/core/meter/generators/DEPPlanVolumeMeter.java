package com.aktimetrix.service.meter.core.meter.generators;

import com.aktimetrix.core.meter.impl.AbstractMeter;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.stereotypes.Measurement;
import org.springframework.stereotype.Component;

@Component
@Measurement(code = "VOL", stepCode = "DEP")
public class DEPPlanVolumeMeter extends AbstractMeter {
    @Override
    protected String getMeasurementUnit(String tenant, StepInstance step) {
        return (String) step.getMetadata().get("reservationVolumeUnit");
    }

    @Override
    protected String getMeasurementValue(String tenant, StepInstance step) {
        return String.valueOf(step.getMetadata().get("reservationVolume"));
    }
}