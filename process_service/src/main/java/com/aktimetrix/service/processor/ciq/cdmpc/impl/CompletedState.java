package com.aktimetrix.service.processor.ciq.cdmpc.impl;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.ProcessInstanceState;
import com.aktimetrix.core.api.StateChangePublisher;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * process instance state. CompletedState all the parts of the air waybill is received.
 *
 * @author arun.kandakatla
 */
public class CompletedState implements ProcessInstanceState, StateChangePublisher {
    private LocalDateTime completedOn;

    /**
     * constructor
     *
     * @param completedOn
     */
    public CompletedState(LocalDateTime completedOn) {
        this.completedOn = completedOn;
    }

    /**
     * Change the current state of the process instance
     *
     * @param context
     */
    @Override
    public void updateState(Context context) {
        context.setCurrentState(new CompletedState(LocalDateTime.now(ZoneOffset.UTC)));
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
