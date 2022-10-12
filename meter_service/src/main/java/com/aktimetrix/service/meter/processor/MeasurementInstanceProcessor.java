package com.aktimetrix.service.meter.processor;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.Processor;
import com.aktimetrix.core.impl.publisher.MeasurementInstancePublisher;
import com.aktimetrix.core.meter.api.Meter;
import com.aktimetrix.core.model.MeasurementInstance;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.referencedata.model.StepDefinition;
import com.aktimetrix.core.referencedata.model.StepMeasurement;
import com.aktimetrix.core.referencedata.service.StepDefinitionService;
import com.aktimetrix.core.service.MeasurementInstanceService;
import com.aktimetrix.core.service.RegistryService;
import com.aktimetrix.core.util.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@com.aktimetrix.core.stereotypes.Processor(processType = "core", processCode = {"measurement-instance"})
public class MeasurementInstanceProcessor implements Processor {

    final private StepDefinitionService stepDefinitionService;
    final private MeasurementInstanceService measurementInstanceService;
    final private RegistryService registryService;
    @Autowired
    private MeasurementInstancePublisher publisher;

    /**
     * @param context process context
     */
    @Override
    public void process(Context context) {
        preProcess(context);
        doProcess(context);
        postProcess(context);
    }

    /**
     * post processor
     *
     * @param context
     */
    private void postProcess(Context context) {
        log.debug("in post process");
        if (publisher != null) {
            publisher.publish(context);
        }
    }

    /**
     * pre processor
     *
     * @param context
     */
    private void preProcess(Context context) {
        log.debug("in pre process");
    }

    /**
     * processor
     *
     * @param context
     */
    private void doProcess(Context context) {
        final StepInstance stepInstance = context.getStepInstances().get(0);
        final String stepCode = stepInstance.getStepCode();
        final Map<String, Object> metadata = stepInstance.getMetadata();
        if (metadata != null)
            metadata.forEach((key, value) -> log.debug("key :{} value :{}", key, value));

        // applicable step definitions
        log.debug("finding applicable step definition for the {} step", stepCode);
        StepDefinition stepDefinition = getStepDefinition(context.getTenant(), stepInstance.getStepCode());

        if (stepDefinition == null || CollectionUtil.isEmptyOrNull(stepDefinition.getMeasurements())) {
            return;
        }
        List<MeasurementInstance> measurementInstances = new ArrayList<>();
        for (StepMeasurement stepMeasurement : stepDefinition.getMeasurements()) {
            stepInstance.setFunctionalCtx(stepDefinition.getFunctionalCtxCode());
            final String measurementType = Constants.STEP_COMPLETED.equals(stepInstance.getStatus()) ? "P" : "A";
            final Meter meter = this.registryService.getMeter(stepDefinition.getStepCode(),
                    stepMeasurement.getMeasurementCode(), measurementType);
            if (meter == null) {
                continue;
            }
            log.info("Step Code: {}, functional Context: {}, Measurement Code: {} ",
                    stepInstance.getStepCode(), stepInstance.getFunctionalCtx(), stepMeasurement.getMeasurementCode());
            MeasurementInstance measurementInstance = measure(context, stepInstance, meter);
            if (measurementInstance != null) {
                measurementInstances.add(measurementInstance);
            }
        }
        if (!measurementInstances.isEmpty()) {
            this.measurementInstanceService.saveMeasurementInstances(measurementInstances);
            context.setMeasurementInstances(measurementInstances);
        }
    }

    /**
     * Take Measurements
     *
     * @param context
     * @param stepInstance
     * @param meter
     * @return
     */
    private MeasurementInstance measure(Context context, StepInstance stepInstance, Meter meter) {
        Objects.requireNonNull(meter);
        return meter.measure(context.getTenant(), stepInstance);
    }

    /**
     * Returns Step Definition
     *
     * @param tenant tenant
     * @param code   step code
     * @return step definition
     */
    private StepDefinition getStepDefinition(String tenant, String code) {
        return this.stepDefinitionService.get(tenant, code, "CONFIRMED");
    }
}
