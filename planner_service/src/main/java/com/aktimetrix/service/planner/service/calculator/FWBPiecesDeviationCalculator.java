package com.aktimetrix.service.planner.service.calculator;

import com.aktimetrix.core.model.MeasurementInstance;
import com.aktimetrix.service.planner.api.DeviationCalculator;
import org.springframework.stereotype.Component;

@Component
public class FWBPiecesDeviationCalculator implements DeviationCalculator {

    /**
     * @param plan
     * @param actual
     * @return
     */
    @Override
    public int calculate(MeasurementInstance plan, MeasurementInstance actual) {
        final int planPieces = Integer.parseInt(plan.getValue());
        final int actualPieces = Integer.parseInt(actual.getValue());
        return Integer.compare(actualPieces, planPieces);
    }
}
