package com.aktimetrix.service.meter.ciq.encore;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "ciq")
@Data
public class CiQProperties {
    private String cdmpIdentifier;
    private CdmpType cdmpType = CdmpType.C;
    private String phaseNumber = "1";
    private int messageVersion = 12;
    private String emailId;
    private String timestampFormat = "yyyy-MM-ddThh:mi:ss";
    private String transferMode = "ASCII";
    private String temporaryFileSuffix = ".tmp";
    private String singleOrMultipleIndicator = "S";
    private String fileFormat = "${date}${sequenceNumber}_${singleOrMultiple}_${messageType}_${messageVersion}_${cdmpIdentifier}_${forwarderIdentifier}_${carrierCode}_${messageReference}";
    private Cdmpc cdmpc;

    @Data
    static public class Cdmpc {
        private Encore encore;

    }

    @Data
    static public class Encore {
        private Map<String, Long> defaultLineOffsets;
    }
}
