package com.aktimetrix.service.processor.ciq.cdmpc.impl.state;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.ProcessInstanceState;
import com.aktimetrix.core.api.StateChangePublisher;

/**
 * Incomplete process state, Process instance state is in-complete when all the parts of the shipment is not received yet.
 */
public class IncompleteState implements ProcessInstanceState, StateChangePublisher {

    /**
     * Change the current state of the process instance
     *
     * @param context
     */
    @Override
    public void updateState(Context context) {
        context.setCurrentState(new IncompleteState());
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
