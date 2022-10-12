package com.aktimetrix.service.processor.ciq.cdmpc.impl.state;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.ProcessInstanceState;
import com.aktimetrix.core.api.StateChangePublisher;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


/**
 * process instance state. When the booking event is received, initially the process instance status is created.
 *
 * @author arun.kandakatla
 */
public class CreatedState implements ProcessInstanceState, StateChangePublisher {
    private LocalDateTime createdOn;

    public CreatedState(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Change the current state of the process instance
     *
     * @param context
     */
    @Override
    public void updateState(Context context) {
        context.setCurrentState(new CreatedState(LocalDateTime.now(ZoneOffset.UTC)));
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
