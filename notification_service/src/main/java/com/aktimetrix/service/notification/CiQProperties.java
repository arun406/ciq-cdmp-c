package com.aktimetrix.service.notification;

import com.aktimetrix.service.ciq.cdmpc.messaging.CdmpType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "ciq")
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

    public String getCdmpIdentifier() {
        return cdmpIdentifier;
    }

    public void setCdmpIdentifier(String cdmpIdentifier) {
        this.cdmpIdentifier = cdmpIdentifier;
    }

    public CdmpType getCdmpType() {
        return cdmpType;
    }

    public void setCdmpType(CdmpType cdmpType) {
        this.cdmpType = cdmpType;
    }

    public String getPhaseNumber() {
        return phaseNumber;
    }

    public void setPhaseNumber(String phaseNumber) {
        this.phaseNumber = phaseNumber;
    }

    public int getMessageVersion() {
        return messageVersion;
    }

    public void setMessageVersion(int messageVersion) {
        this.messageVersion = messageVersion;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public String getTransferMode() {
        return transferMode;
    }

    public void setTransferMode(String transferMode) {
        this.transferMode = transferMode;
    }

    public String getTemporaryFileSuffix() {
        return temporaryFileSuffix;
    }

    public void setTemporaryFileSuffix(String temporaryFileSuffix) {
        this.temporaryFileSuffix = temporaryFileSuffix;
    }

    public String getSingleOrMultipleIndicator() {
        return singleOrMultipleIndicator;
    }

    public void setSingleOrMultipleIndicator(String singleOrMultipleIndicator) {
        this.singleOrMultipleIndicator = singleOrMultipleIndicator;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public Cdmpc getCdmpc() {
        return cdmpc;
    }

    public void setCdmpc(Cdmpc cdmpc) {
        this.cdmpc = cdmpc;
    }

    static public class Cdmpc {
        private Encore encore;

        public Encore getEncore() {
            return encore;
        }

        public void setEncore(Encore encore) {
            this.encore = encore;
        }

        @Override
        public String toString() {
            return "Cdmpc{" +
                    "encore=" + encore +
                    '}';
        }
    }

    static public class Encore {
        private Map<String, Long> defaultLineOffsets;

        public Map<String, Long> getDefaultLineOffsets() {
            return defaultLineOffsets;
        }

        public void setDefaultLineOffsets(Map<String, Long> defaultLineOffsets) {
            this.defaultLineOffsets = defaultLineOffsets;
        }

        @Override
        public String toString() {
            return "Encore{" +
                    "defaultLineOffsets=" + defaultLineOffsets +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CiQProperties{" +
                "cdmpIdentifier='" + cdmpIdentifier + '\'' +
                ", cdmpType=" + cdmpType +
                ", phaseNumber='" + phaseNumber + '\'' +
                ", messageVersion=" + messageVersion +
                ", emailId='" + emailId + '\'' +
                ", timestampFormat='" + timestampFormat + '\'' +
                ", transferMode='" + transferMode + '\'' +
                ", temporaryFileSuffix='" + temporaryFileSuffix + '\'' +
                ", singleOrMultipleIndicator='" + singleOrMultipleIndicator + '\'' +
                ", fileFormat='" + fileFormat + '\'' +
                ", cdmpc=" + cdmpc +
                '}';
    }
}
