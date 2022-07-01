package com.aktimetrix.service.processor.ciq.cdmpc.service;

import com.aktimetrix.core.api.Constants;
import com.aktimetrix.core.api.Context;
import com.aktimetrix.service.processor.ciq.cdmpc.event.transferobjects.Cargo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CargoService {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @param context
     * @return
     */
    public Cargo getCargoFromContext(Context context) {

        try {
            String json = objectMapper.writeValueAsString(context.getProperty(Constants.ENTITY));
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("unable to convert and get entity.", e);
        }
        return null;
    }
}
