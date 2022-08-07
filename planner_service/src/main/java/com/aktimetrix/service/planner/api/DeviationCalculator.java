package com.aktimetrix.service.planner.api;

import com.aktimetrix.core.model.MeasurementInstance;

@FunctionalInterface
public interface DeviationCalculator {

    /**
     * Returns:
     * the value 0 if plan == actual; a value less than 0 if plan < actual and a value greater than 0 if plan > actual
     *
     * @param plan
     * @param actual
     * @return
     */
    int calculate(MeasurementInstance plan, MeasurementInstance actual);
}
