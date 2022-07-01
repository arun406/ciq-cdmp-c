package com.aktimetrix.service.notification.messaging;


import com.aktimetrix.service.notification.exception.NoGeneratorFoundException;

/**
 * @author Arun.Kandakatla
 */
public interface MessageGeneratorFactory {

    /**
     * returns the appropriate generator based on the message type
     *
     * @param type
     * @return
     */
    MessageGenerator getGenerator(String type) throws NoGeneratorFoundException;

    /**
     * returns the appropriate generator based on the message type and version number
     *
     * @param type
     * @param version
     * @return
     */
    MessageGenerator getGenerator(String type, int version) throws NoGeneratorFoundException;
}
