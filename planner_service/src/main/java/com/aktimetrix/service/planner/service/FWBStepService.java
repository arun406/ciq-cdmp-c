package com.aktimetrix.service.planner.service;

import com.aktimetrix.core.model.MeasurementInstance;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.service.MeasurementInstanceService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FWBStepService {

    private MeasurementInstanceService measurementInstanceService;


    public FWBStepService(MeasurementInstanceService measurementInstanceService) {
        this.measurementInstanceService = measurementInstanceService;
    }

    public StepInstance getStepInstance(String processInstanceId) {
        return null;
    }

    public List<MeasurementInstance> getMeasurements(String tenant, String processInstanceId, String stepInstanceId) {
        return this.measurementInstanceService.getStepMeasurements(tenant, processInstanceId, stepInstanceId);
    }

    public List<MeasurementInstance> getPlannedMeasurements(String processInstanceId, String stepInstanceId) {
        return null;
    }

    public void updateStepInstance(StepInstance stepInstance) {

    }

    /**
     * calculates the measurement deviations ( actual - planned )
     *
     * @param tenant
     * @param processInstanceId
     * @param stepInstanceId
     * @return
     */
    public Map<String, Integer> getDeviations(String tenant, String processInstanceId, String stepInstanceId) {
        Map<String, Integer> deviations = new HashMap<>();

        final List<MeasurementInstance> measurements = getMeasurements(tenant, processInstanceId, stepInstanceId);

        if (measurements == null || measurements.isEmpty()) {
            return deviations;
        }

        final Map<String, List<MeasurementInstance>> map = measurements.stream()
                .collect(Collectors.toMap((m) -> m.getType(), (m) -> Collections.singletonList(m),
                        FWBStepService::mergeEntriesWithDuplicatedKeys));

        final List<MeasurementInstance> planned = map.get("P");
        final List<MeasurementInstance> actual = map.get("A");

        // piece deviation
        deviations.put("PCS_DEVIATION", getPcsDev(planned, actual));
        // weight deviation
        deviations.put("WT_DEVIATION", getWtDev(planned, actual));
        // volume deviation
        deviations.put("VOL_DEVIATION", getVolDev(planned, actual));
        // time deviation
        deviations.put("TIME_DEVIATION", getTimeDev(planned, actual));

        return deviations;
    }

    /**
     * @param planned
     * @param actual
     * @return
     */
    private int getTimeDev(List<MeasurementInstance> planned, List<MeasurementInstance> actual) {
        final MeasurementInstance planPieces = planned.stream()
                .filter(m1 -> "TIME".equals(m1.getCode())).findFirst().orElseGet(null);
        final MeasurementInstance actualPieces = actual.stream()
                .filter(m1 -> "TIME".equals(m1.getCode())).findFirst().orElseGet(null);

        return new FWBTimeDeviationCalculator().calculate(planPieces, actualPieces);
    }

    /**
     * @param planned
     * @param actual
     * @return
     */
    private int getPcsDev(List<MeasurementInstance> planned, List<MeasurementInstance> actual) {
        final MeasurementInstance planPieces = planned.stream()
                .filter(m1 -> "PCS".equals(m1.getCode())).findFirst().orElseGet(null);
        final MeasurementInstance actualPieces = actual.stream()
                .filter(m1 -> "PCS".equals(m1.getCode())).findFirst().orElseGet(null);

        return new FWBPiecesDeviationCalculator().calculate(planPieces, actualPieces);
    }

    /**
     * @param planned
     * @param actual
     * @return
     */
    private int getWtDev(List<MeasurementInstance> planned, List<MeasurementInstance> actual) {
        final MeasurementInstance planPieces = planned.stream()
                .filter(m1 -> "WT".equals(m1.getCode())).findFirst().orElseGet(null);
        final MeasurementInstance actualPieces = actual.stream()
                .filter(m1 -> "WT".equals(m1.getCode())).findFirst().orElseGet(null);

        return new FWBWeightDeviationCalculator().calculate(planPieces, actualPieces);
    }

    /**
     * @param planned
     * @param actual
     * @return
     */
    private int getVolDev(List<MeasurementInstance> planned, List<MeasurementInstance> actual) {
        final MeasurementInstance planPieces = planned.stream()
                .filter(m1 -> "VOL".equals(m1.getCode())).findFirst().orElseGet(null);
        final MeasurementInstance actualPieces = actual.stream()
                .filter(m1 -> "VOL".equals(m1.getCode())).findFirst().orElseGet(null);

        return new FWBVolumeDeviationCalculator().calculate(planPieces, actualPieces);
    }

    /**
     * @param existingResults
     * @param newResults
     * @return
     */
    private static List<MeasurementInstance> mergeEntriesWithDuplicatedKeys(List<MeasurementInstance> existingResults,
                                                                            List<MeasurementInstance> newResults) {
        List<MeasurementInstance> mergedResults = new ArrayList<>();
        mergedResults.addAll(existingResults);
        mergedResults.addAll(newResults);
        return mergedResults;
    }

}
