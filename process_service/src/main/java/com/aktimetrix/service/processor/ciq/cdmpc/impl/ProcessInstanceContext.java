package com.aktimetrix.service.processor.ciq.cdmpc.impl;

import com.aktimetrix.core.api.Context;
import com.aktimetrix.core.api.ProcessInstanceState;
import com.aktimetrix.core.model.ProcessInstance;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Process Instance Context
 *
 * @author arun.kandakatla
 */
public class ProcessInstanceContext {

    private ProcessInstanceState currentState;
    private Context context;

    /**
     * Constructor
     *
     * @param currentState
     * @param processInstance
     */
    public ProcessInstanceContext(ProcessInstanceState currentState, ProcessInstance processInstance, Context context) {
        this.currentState = currentState;
        this.context = context;
        // initialise the state as Created
        if (currentState == null) {
            this.currentState = new CreatedState(LocalDateTime.now(ZoneOffset.UTC));
        }
    }

    /**
     * getter for current state
     *
     * @return
     */
    public ProcessInstanceState getCurrentState() {
        return currentState;
    }

    /**
     * setter for current state
     *
     * @param currentState
     */
    public void setCurrentState(ProcessInstanceState currentState) {
        this.currentState = currentState;
    }

    /**
     * getter for context
     *
     * @return
     */
    public Context getContext() {
        return context;
    }

    /**
     * setter for context
     *
     * @param context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Update the process instance state
     */
    public void update() {
        currentState.updateState(this.context);
    }
}
