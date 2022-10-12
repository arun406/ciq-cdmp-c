package com.aktimetrix.service.processor.ciq.cdmpc.impl.state;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.ProcessInstanceState;
import com.aktimetrix.core.api.StateChangePublisher;

/**
 * Step Complete state. When the actual event received for the process instance the state is changed to process Step Completed.
 */
public class StepCompletedState implements ProcessInstanceState, StateChangePublisher {

    private String stepId;
    private String stepCode;

    /**
     * Change the current state of the process instance
     *
     * @param context
     */
    @Override
    public void updateState(Context context) {

    }

    /**
     * Publish the State change to the observers
     *
     * @param context
     */
    @Override
    public void publishState(Context context) {

    }
}
