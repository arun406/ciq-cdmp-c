package com.aktimetrix.service.ciq.cdmpc.messaging.service;

import com.aktimetrix.core.transferobjects.Measurement;
import com.aktimetrix.core.transferobjects.ProcessPlanDTO;
import com.aktimetrix.core.transferobjects.StepInstanceDTO;
import com.aktimetrix.core.transferobjects.StepPlanDTO;
import com.aktimetrix.core.util.DateTimeUtil;
import com.aktimetrix.service.ciq.cdmpc.messaging.*;
import com.aktimetrix.service.notification.CiQProperties;
import com.aktimetrix.service.notification.messaging.CiQFileNameGenerator;
import com.aktimetrix.service.notification.messaging.Message;
import com.aktimetrix.service.notification.messaging.MessageGenerator;
import com.aktimetrix.service.notification.service.MessageCounterService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This class generates the CiQMessage  based on the message type and the input object
 *
 * @author Arun.Kandakatla
 */
@Component
@Slf4j
public class CiQCAN12MessageGenerator implements MessageGenerator {

    public static final String CIQ_NOTIFICATION_RMP_TEMPLATE = "can.ftl";
    private String messageType = "CAN";
    private int messageVersion = 12;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private CiQProperties ciQProperties;

    @Autowired
    private MessageCounterService messageCounterService;

    @Override
    public Message generate(Object object) {
        File payload = generatePayload(object);
        Map<String, Object> headers = new HashMap<>();
        headers.put("file_name", payload.getName());
        headers.put("sequence_number", 1);
        return new CiQMessage(payload, headers);
    }

    /**
     * @param object
     * @return
     */
    private File generatePayload(Object object) {
        if (Arrays.asList("CAN").contains(messageType)) {
            // Route map level messages
            ProcessPlanDTO plan = (ProcessPlanDTO) object;
            CANMessageDTO canMessageDTO = prepareCANMessageDTO(plan, messageType);
            int messageCounter = getMessageCounter(canMessageDTO.getAirWaybillPrefix(), canMessageDTO.getAirWaybillNumber(), messageType);
            log.debug("message sequence {}", messageCounter);
            canMessageDTO.setMessageCounter(messageCounter + "");
            File file = getFile(canMessageDTO);
            if (file != null) return file;
        }
        return null;
    }

    private File getFile(CANMessageDTO canMessageDTO) {
        FileWriter fileWriter = null;
        try {
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate(CIQ_NOTIFICATION_RMP_TEMPLATE);
            // write output into a file:
            String ciqMessageFileName = new CiQFileNameGenerator(ciQProperties).generateFileName(canMessageDTO);
            log.debug("ciq message filename: {}", ciqMessageFileName);

            File file = new File(ciqMessageFileName);
            fileWriter = new FileWriter(file);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("canMessageDTO", canMessageDTO);
            template.process(data, fileWriter);
            fileWriter.flush();
            return file;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        } catch (TemplateException e) {
            log.error(e.getLocalizedMessage(), e);
        } finally {
            if (null != fileWriter) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * @param airWaybillPrefix
     * @param airWaybillNumber
     */
    private int getMessageCounter(String airWaybillPrefix, String airWaybillNumber, String messageType) {
        return this.messageCounterService.getSequenceNumber(airWaybillPrefix + "-" + airWaybillNumber, messageType);
    }


    protected CANMessageDTO prepareCANMessageDTO(ProcessPlanDTO planDTO, String messageType) {
        if (planDTO == null) {
            return null;
        }
        CANMessageDTO canMessageDTO = new CANMessageDTO();
        canMessageDTO.setCarrierCode(planDTO.getTenant());
        canMessageDTO.setAirWaybillPrefix(planDTO.getEntityId().substring(0, 3));
        canMessageDTO.setAirWaybillNumber(planDTO.getEntityId().substring(4));
        canMessageDTO.setCdmpIdentifier(ciQProperties.getCdmpIdentifier());
        canMessageDTO.setPhaseNumber(ciQProperties.getPhaseNumber());
        canMessageDTO.setMessageVersion(ciQProperties.getMessageVersion());
        canMessageDTO.setMessageType(messageType);
        Map<String, Object> metadata = planDTO.getProcessInstance().getMetadata();
        canMessageDTO.setForwarderIdentifier((String) metadata.get("forwarderCode"));
        canMessageDTO.setMessageTimestamp(OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss")));
        String messageReference = UUID.randomUUID().toString().replace("-", "");
        canMessageDTO.setMessageReference(messageReference);

        return canMessageDTO;
    }

    /**
     * @param plan
     * @return
     */
    protected RMPMessageDTO prepareRMPMessageDTO(ProcessPlanDTO plan) {
        if (plan == null) {
            return null;
        }

        RMPMessageDTO rmpMessageDTO = new RMPMessageDTO();
        rmpMessageDTO.setCarrierCode(plan.getTenant());
        rmpMessageDTO.setAirWaybillPrefix(plan.getEntityId().substring(0, 3));
        rmpMessageDTO.setAirWaybillNumber(plan.getEntityId().substring(4));
        Map<String, Object> metadata = plan.getProcessInstance().getMetadata();
        rmpMessageDTO.setAirportOfReceipt((String) metadata.get("origin"));
        rmpMessageDTO.setAirportOfDelivery((String) metadata.get("destination"));
        rmpMessageDTO.setApprovedIndicator(plan.getApprovedIndicator());
        rmpMessageDTO.setDirectTruckingIndicator(plan.getDirectTruckingIndicator());
        rmpMessageDTO.setFlightSpecificIndicator(plan.getFlightSpecificIndicator());
        rmpMessageDTO.setForwarderIdentifier((String) metadata.get("forwarderCode"));
        rmpMessageDTO.setProductCode((String) metadata.get("productCode"));
        rmpMessageDTO.setRoutePlanNumber(plan.getPlanNumber());
        rmpMessageDTO.setTotalPieces((Integer) metadata.get("reservationPieces"));

        if (metadata.get("reservationWeight") != null) {
            if (metadata.get("reservationWeight") instanceof Integer) {
                rmpMessageDTO.setTotalWeight(((Integer) metadata.get("reservationWeight")).doubleValue());
            } else if (metadata.get("reservationWeight") instanceof Double) {
                rmpMessageDTO.setTotalWeight(((Double) metadata.get("reservationWeight")).doubleValue());
            }
        }

        if (metadata.get("reservationVolume") != null) {
            if (metadata.get("reservationVolume") instanceof Integer) {
                rmpMessageDTO.setTotalVolume(((Integer) metadata.get("reservationVolume")).doubleValue());
            } else if (metadata.get("reservationVolume") instanceof Double) {
                rmpMessageDTO.setTotalVolume(((Double) metadata.get("reservationVolume")).doubleValue());
            }
        }
//        rmpMessageDTO.setTotalVolume(Double.valueOf((String) metadata.get("reservationVolume")));
        rmpMessageDTO.setVolumeCode((String) metadata.get("reservationVolumeUnit"));
        rmpMessageDTO.setWeightCode((String) metadata.get("reservationWeightUnit"));
        if (plan.getUpdateFlag() != null) {
            rmpMessageDTO.setUpdateFlag(plan.getUpdateFlag());
        } else {
            rmpMessageDTO.setUpdateFlag("");
        }
        rmpMessageDTO.setMinimumVolumeTolerance("");
        rmpMessageDTO.setMinimumWeightTolerance("");
        rmpMessageDTO.setCdmpIdentifier(ciQProperties.getCdmpIdentifier());
        rmpMessageDTO.setPhaseNumber(ciQProperties.getPhaseNumber());
        rmpMessageDTO.setMessageVersion(ciQProperties.getMessageVersion());
        rmpMessageDTO.setMessageType("RMP");
        rmpMessageDTO.setMessageTimestamp(OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss")));
        String messageReference = UUID.randomUUID().toString().replace("-", "");
        rmpMessageDTO.setMessageReference(messageReference);

        List<StepInstanceDTO> steps = plan.getProcessInstance().getSteps();
        if (steps == null || steps.isEmpty()) {
            return rmpMessageDTO;
        }
        List<RMPMilestoneDTO> milestones = new ArrayList<>();

        for (Map.Entry<String, List<StepPlanDTO>> entry : plan.getStepPlans().entrySet()) {
            List<StepPlanDTO> stepPlans = entry.getValue();
            stepPlans.forEach(stepPlanDTO -> {
                LocalDateTime planTime = stepPlanDTO.getPlannedMeasurements().stream()
                        .filter(mi -> mi.getCode().equals("TIME") && mi.getUnit().equals("TIMESTAMP"))
                        .map(Measurement::getValue)
                        .map(s -> DateTimeUtil.getLocalDateTime(s, "yyyy-MM-dd'T'HH:mm:ss"))
                        .findFirst().orElse(null);
                RMPMilestoneDTO milestoneDTO = getRmpMilestoneDTO(getStepInstanceDTO(steps, stepPlanDTO.getStepInstanceId()), planTime);
                milestones.add(milestoneDTO);
            });

        }
        rmpMessageDTO.setMilestones(milestones);
        return rmpMessageDTO;
    }

    private StepInstanceDTO getStepInstanceDTO(List<StepInstanceDTO> steps, String stepInstanceId) {
        return steps.stream().filter(stepInstanceDTO -> stepInstanceDTO.getId().equals(stepInstanceId)).findFirst().orElse(null);
    }

    private RMPMilestoneDTO getRmpMilestoneDTO(StepInstanceDTO step, LocalDateTime planTime) {
        Map<String, Object> metadata = step.getMetadata();
        RMPMilestoneDTO milestoneDTO = new RMPMilestoneDTO();
        milestoneDTO.setAircraftType("");
        milestoneDTO.setMilestoneCode(step.getStepCode());
        milestoneDTO.setPlanTime(planTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss")));
        milestoneDTO.setChangeStatus(MilestoneChangeStatus.N);
        if (Arrays.asList("FWB", "LAT", "RCS", "NFD", "AWD", "DLV").contains(step.getStepCode())) {
            milestoneDTO.setAircraftCategory("");
            milestoneDTO.setCarrierCode("");
            milestoneDTO.setFlightNumber("");
            milestoneDTO.setFlightOriginAirport("");
            milestoneDTO.setFlightDestinationAirport("");
            milestoneDTO.setPieces("");
            milestoneDTO.setWeight("");
            milestoneDTO.setVolume("");
            milestoneDTO.setTypeOfTimeIndicator("");
            milestoneDTO.setFlightTimestamp("");
            return milestoneDTO;
        }
        if (metadata == null) {
            milestoneDTO.setAircraftCategory("");
            milestoneDTO.setCarrierCode("");
            milestoneDTO.setFlightNumber("");
            milestoneDTO.setFlightOriginAirport("");
            milestoneDTO.setFlightDestinationAirport("");
            milestoneDTO.setPieces("");
            milestoneDTO.setWeight("");
            milestoneDTO.setVolume("");
            milestoneDTO.setTypeOfTimeIndicator("");
            milestoneDTO.setFlightTimestamp("");
            return milestoneDTO;
        }
        milestoneDTO.setAircraftCategory((String) metadata.getOrDefault("acCategory", ""));
        milestoneDTO.setCarrierCode((String) metadata.getOrDefault("carrier", ""));
        milestoneDTO.setFlightNumber((String) metadata.getOrDefault("flightNumber", ""));
        milestoneDTO.setFlightOriginAirport((String) metadata.getOrDefault("boardPoint", ""));
        milestoneDTO.setFlightDestinationAirport((String) metadata.getOrDefault("offPoint", ""));
        milestoneDTO.setPieces(metadata.getOrDefault("pieces", 0) + "");

//        milestoneDTO.setWeight(Double.valueOf((String) metadata.get("wt")));
//        milestoneDTO.setVolume(Double.valueOf((String) metadata.get("vol")));


        if (metadata.get("wt") != null) {
            if (metadata.get("wt") instanceof Integer) {
                milestoneDTO.setWeight(((Integer) metadata.get("wt")).doubleValue() + "");
            } else if (metadata.get("wt") instanceof Double) {
                milestoneDTO.setWeight(((Double) metadata.get("wt")).doubleValue() + "");
            }
        }

        if (metadata.get("vol") != null) {
            if (metadata.get("vol") instanceof Integer) {
                milestoneDTO.setVolume(((Integer) metadata.get("vol")).doubleValue() + "");
            } else if (metadata.get("vol") instanceof Double) {
                milestoneDTO.setVolume(((Double) metadata.get("vol")).doubleValue() + "");
            }
        }
        if ("DEP".equals(step.getStepCode())) {
            if (metadata.get("std") != null) {
                final LocalDateTime std = DateTimeUtil.getLocalDateTime((String) metadata.get("std"), "yyyy-MM-dd'T'HH:mm:ss");
                milestoneDTO.setFlightTimestamp((String) metadata.get("std"));
                milestoneDTO.setTypeOfTimeIndicator("S");
            }
        } else if (Arrays.asList("ARR", "AWR", "RCF").contains(step.getStepCode())) {
            if (metadata.get("ata") != null) {
                final LocalDateTime ata = DateTimeUtil.getLocalDateTime((String) metadata.get("ata"), "yyyy-MM-dd'T'HH:mm:ss");
                milestoneDTO.setFlightTimestamp((String) metadata.get("std"));
                milestoneDTO.setTypeOfTimeIndicator("A");
            } else if (metadata.get("eta") != null) {
                final LocalDateTime eta = DateTimeUtil.getLocalDateTime((String) metadata.get("eta"), "yyyy-MM-dd'T'HH:mm:ss");
                milestoneDTO.setFlightTimestamp((String) metadata.get("std"));
                milestoneDTO.setTypeOfTimeIndicator("E");
            } else {
                final LocalDateTime sta = DateTimeUtil.getLocalDateTime((String) metadata.get("sta"), "yyyy-MM-dd'T'HH:mm:ss");
                milestoneDTO.setFlightTimestamp((String) metadata.get("std"));
                milestoneDTO.setTypeOfTimeIndicator("S");
            }
        } else {
            milestoneDTO.setFlightTimestamp("");
            milestoneDTO.setTypeOfTimeIndicator("");
        }
        return milestoneDTO;
    }
}
