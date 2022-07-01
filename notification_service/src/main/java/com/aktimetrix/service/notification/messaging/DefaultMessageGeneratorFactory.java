package com.aktimetrix.service.notification.messaging;

import com.aktimetrix.core.service.RegistryService;
import com.aktimetrix.service.ciq.cdmpc.messaging.service.CiQCAN12MessageGenerator;
import com.aktimetrix.service.ciq.cdmpc.messaging.service.CiQRMP12MessageGenerator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default factory class which returns the generator based on the message type
 *
 * @author Arun.Kandakatla
 */
public class DefaultMessageGeneratorFactory implements MessageGeneratorFactory {

    private static final int DEFAULT_MESSAGE_VERSION = 12;
    private static final DefaultMessageGeneratorFactory INSTANCE = new DefaultMessageGeneratorFactory();
    @Autowired
    private RegistryService registryService;

    @Autowired
    private CiQRMP12MessageGenerator ciQRMP12MessageGenerator;

    @Autowired
    private CiQCAN12MessageGenerator ciQCAN12MessageGenerator;

    /**
     * returns the appropriate generator based on the message type
     *
     * @param type
     * @return
     */
    @Override
    public MessageGenerator getGenerator(String type) {
        if (type.equals("RMP"))
            return ciQRMP12MessageGenerator;
        else if (type.equals("CAN")) {
            return ciQCAN12MessageGenerator;
        }
        return null;
    }

    /**
     * returns the appropriate generator based on the message type
     *
     * @param type
     * @return
     */
    @Override
    public MessageGenerator getGenerator(String type, int version) {
        return ciQRMP12MessageGenerator;
    }

    public static DefaultMessageGeneratorFactory getInstance() {
        return INSTANCE;
    }
}
