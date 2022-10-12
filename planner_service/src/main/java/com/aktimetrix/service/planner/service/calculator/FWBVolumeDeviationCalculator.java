package com.aktimetrix.service.planner.service.calculator;

import com.aktimetrix.core.model.MeasurementInstance;
import com.aktimetrix.service.planner.api.DeviationCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FWBVolumeDeviationCalculator implements DeviationCalculator {

    /**
     * @param plan
     * @param actual
     * @return
     */
    @Override
    public int calculate(MeasurementInstance plan, MeasurementInstance actual) {
        final BigDecimal planVolume = new BigDecimal(plan.getValue());
        final BigDecimal actVolume = new BigDecimal(actual.getValue());
        return actVolume.compareTo(planVolume);
    }
}
