package com.aktimetrix.service.notification.messaging;

import com.aktimetrix.service.notification.CiQProperties;
import com.aktimetrix.service.ciq.cdmpc.messaging.CiQMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CiQFileNameGenerator implements FileNameGenerator {

    private CiQProperties ciqProperties;

    /**
     * Constructor
     *
     * @param ciqProperties
     */
    public CiQFileNameGenerator(CiQProperties ciqProperties) {
        this.ciqProperties = ciqProperties;
    }

    @Override
    public String generateFileName(MessageDTO message) {
        if (message instanceof CiQMessageDTO) {
            CiQMessageDTO ciqMessage = (CiQMessageDTO) message;
            return getCiQMessageFileName(ciqMessage);
        }
        return "";
    }

    private String getCiQMessageFileName(CiQMessageDTO messageDTO) {
        String fileFormat = ciqProperties.getFileFormat();
        log.debug("ciq message file format:{}", fileFormat);
        Map<String, String> data = new HashMap<>();
        String date = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyMMddhhmmss"));
        data.put("date", date);
        data.put("sequenceNumber", messageDTO.getMessageCounter());
        data.put("singleOrMultiple", ciqProperties.getSingleOrMultipleIndicator());
        data.put("messageType", messageDTO.getMessageType());
        data.put("messageVersion", messageDTO.getMessageVersion() + "");
        data.put("cdmpIdentifier", ciqProperties.getCdmpIdentifier());
        data.put("forwarderIdentifier", messageDTO.getForwarderIdentifier());
        data.put("carrierCode", messageDTO.getCarrierCode());
        data.put("messageReference", messageDTO.getMessageReference());
        log.debug("data:{}", data);
        return StringSubstitutor.replace(fileFormat, data);
    }
}