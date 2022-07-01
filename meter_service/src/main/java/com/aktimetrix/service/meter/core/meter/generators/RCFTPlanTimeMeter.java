package com.aktimetrix.service.meter.core.meter.generators;

import com.aktimetrix.core.meter.impl.AbstractMeter;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.stereotypes.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Measurement(code = "TIME", stepCode = "RCF-T")
public class RCFTPlanTimeMeter extends AbstractMeter {

    private static final Logger logger = LoggerFactory.getLogger(RCFTPlanTimeMeter.class);

    @Autowired
    private CDMPCImportStepMeasurementValueCalculator valueCalculator;

    @Override
    protected String getMeasurementUnit(String tenant, StepInstance step) {
        return "TIMESTAMP";
    }

    @Override
    protected String getMeasurementValue(String tenant, StepInstance step) {
        final String measurementValue = this.valueCalculator.calculate(tenant, step, stepCode());
        logger.info("Measurement value : {}", measurementValue);
        return measurementValue;
    }
}
