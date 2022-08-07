package com.aktimetrix.service.planner.service;

import com.aktimetrix.core.model.MeasurementInstance;
import com.aktimetrix.service.planner.api.DeviationCalculator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FWBTimeDeviationCalculator implements DeviationCalculator {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * @param plan
     * @param actual
     * @return
     */
    @Override
    public int calculate(MeasurementInstance plan, MeasurementInstance actual) {
        final LocalDateTime planTime = LocalDateTime.parse(plan.getValue(), formatter);
        final LocalDateTime actTime = LocalDateTime.parse(actual.getValue(), formatter);
        return actTime.compareTo(planTime);
    }
}
