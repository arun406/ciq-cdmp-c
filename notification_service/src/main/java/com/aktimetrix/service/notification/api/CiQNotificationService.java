package com.aktimetrix.service.notification.api;


import com.aktimetrix.core.transferobjects.ProcessPlanDTO;

/**
 * @author Arun.Kandakatla
 */
public interface CiQNotificationService {
    void sendRMP(ProcessPlanDTO plan);

    void sendMUP();

    void sendCAN(ProcessPlanDTO plan);

    void sendERR();
}
