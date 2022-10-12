package com.aktimetrix.service.planner.service.calculator;

import com.aktimetrix.core.model.MeasurementInstance;
import com.aktimetrix.service.planner.api.DeviationCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FWBWeightDeviationCalculator implements DeviationCalculator {

    /**
     * @param plan
     * @param actual
     * @return
     */
    @Override
    public int calculate(MeasurementInstance plan, MeasurementInstance actual) {
        final BigDecimal planWeight = new BigDecimal(plan.getValue());
        final BigDecimal actWeight = new BigDecimal(actual.getValue());
        return actWeight.compareTo(planWeight);
    }
}
