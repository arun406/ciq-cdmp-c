package com.aktimetrix.service.meter.core.meter.generators;

import com.aktimetrix.service.meter.ciq.encore.CiQProperties;
import com.aktimetrix.service.meter.ciq.encore.model.LinesEncoreMaster;
import com.aktimetrix.service.meter.ciq.encore.service.FlightGroupEncoreMasterService;
import com.aktimetrix.service.meter.ciq.encore.service.LinesEncoreMasterService;
import com.aktimetrix.service.meter.ciq.encore.service.ProductGroupEncoreMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OffsetCalculator {

    private final LinesEncoreMasterService linesEncoreMasterService;
    private final FlightGroupEncoreMasterService flightGroupEncoreMasterService;
    private final ProductGroupEncoreMasterService productGroupEncoreMasterService;

    @Autowired
    private CiQProperties encoreProperties;

    /**
     * @param tenant   tenant key
     * @param metadata metadata
     * @param stepCode step code
     * @param type
     * @return offset value
     */
    public long getOffset(String tenant, Map<String, Object> metadata, String stepCode, String type) {
        long offset = 0;
        final List<String> flightGroupCodes = getFlightGroups((String) metadata.get("flightNumber"));
        log.info("Flight Groups: {}", flightGroupCodes);

        final String productGroupCode = getProductGroupCode((String) metadata.get("productCode"));
        log.info("Product Group Code: {}", productGroupCode);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate departureDate = LocalDate.parse((String) metadata.get("flightDate"), formatter);
        final String dow = String.valueOf(departureDate.getDayOfWeek().getValue());
        log.info("dow : {}", dow);

        List<LinesEncoreMaster> lines = getLines(tenant, metadata, flightGroupCodes, productGroupCode, dow, type);
        if (lines == null || lines.isEmpty()) {
            log.debug("lines record not found in master. Fetching the defaults");
            if ("E".equals(type) || "I".equals(type)) {
                Map<String, Long> defaultLineOffsets = encoreProperties.getCdmpc().getEncore().getDefaultLineOffsets();
                log.debug("default line offsets. {}", defaultLineOffsets);
                return defaultLineOffsets.get(stepCode);
            } else if ("T".equals(type)) {
                if ("DEP-T".equals(stepCode)) {
                    lines = getLines(tenant, metadata, flightGroupCodes, productGroupCode, dow, "E");
                } else if ("RCF-T".equals(stepCode) || "ARR-T".equals(stepCode)) {
                    lines = getLines(tenant, metadata, flightGroupCodes, productGroupCode, dow, "I");
                }
                if (lines == null || lines.isEmpty()) {
                    Map<String, Long> defaultLineOffsets = encoreProperties.getCdmpc().getEncore().getDefaultLineOffsets();
                    log.debug("default line offsets. {}", defaultLineOffsets);
                    return defaultLineOffsets.get(stepCode);
                }
            }
        }
        offset = getOffset(lines, stepCode);
        log.debug("offset of the {} step is {}", stepCode, offset);
        return offset;
    }

    /**
     * @param lines    lines
     * @param stepCode step code
     * @return offset of the step
     */
    private long getOffset(List<LinesEncoreMaster> lines, String stepCode) {
        String offset = null;
        final LinesEncoreMaster linesEncoreMaster = lines.get(0);

        switch (stepCode) {
            case "FWB":
                offset = (linesEncoreMaster.getFWB());
                break;
            case "LAT":
                offset = (linesEncoreMaster.getLAT());
                break;
            case "RCS":
                offset = (linesEncoreMaster.getRCS());
                break;
            case "DEP":
                offset = (linesEncoreMaster.getDEP());
                break;
            case "ARR":
                offset = (linesEncoreMaster.getARR());
                break;
            case "AWR":
                offset = (linesEncoreMaster.getAWR());
                break;
            case "RCF":
                offset = (linesEncoreMaster.getRCF());
                break;
            case "NFD":
                offset = (linesEncoreMaster.getNFD());
                break;
            case "AWD":
                offset = (linesEncoreMaster.getAWD());
                break;
            case "DLV":
                offset = (linesEncoreMaster.getDLV());
                break;
            case "RCF-T":
                offset = (linesEncoreMaster.getRCFT());
                break;
            case "TFD-T":
                offset = (linesEncoreMaster.getTFDT());
                break;
            case "ARR-T":
                offset = (linesEncoreMaster.getARRT());
                break;
            case "RCT-T":
                offset = (linesEncoreMaster.getRCTT());
                break;
            case "DEP-T":
                offset = (linesEncoreMaster.getDEPT());
                break;
        }
        /*final LineOffsetEncoreMaster lat = linesEncoreMaster.getOffsets().stream()
                .filter(lineOffsetEncoreMaster ->
                        lineOffsetEncoreMaster.getOffsetType().equalsIgnoreCase(stepCode))
                .collect(Collectors.toList())
                .stream().findFirst()
                .orElse(null);
        if (lat != null) {
            offset = (lat.getOffsetValue());
            log.info(" Step Offset: " + offset);
        }*/
        if (offset != null && StringUtils.hasText(offset.trim())) {
            return Long.parseLong(offset);
        }
        return 0;
    }

    /**
     * @param tenant           tenant key
     * @param metadata         metadata
     * @param flightGroupCodes flight group codes
     * @param productCode      product group code
     * @param dow              day of working
     * @param type
     * @return list of encore lines
     */
    private List<LinesEncoreMaster> getLines(String
                                                     tenant, Map<String, Object> metadata, List<String> flightGroupCodes,
                                             String productCode, String dow, String type) {
        String exportInd = "N", importInd = "N", transitInd = "N";

        String origin = (String) metadata.get("origin");
        String destination = (String) metadata.get("destination");
        String boardPoint = (String) metadata.get("boardPoint");
        String offPoint = (String) metadata.get("offPoint");
        log.debug("origin:{}, destination: {}, boardPoint: {}, offPoint:{}, step type: {}", origin, destination, boardPoint, offPoint, type);

        String airport = (String) metadata.get(type.equals("E") ? "boardPoint" : "offPoint");
        log.debug("step airport :{}", airport);
        if (airport.equals(origin)) {
            exportInd = "Y";
            importInd = "";
            transitInd = "";
        } else if (airport.equals(destination)) {
            importInd = "Y";
            exportInd = "";
            transitInd = "";
        } else {
            transitInd = "Y";
            exportInd = "";
            importInd = "";
        }

        log.debug("transfer indicators Export : {}, Import : {}, Transit : {}", exportInd, importInd, transitInd);
        return linesEncoreMasterService.getLines(tenant,
                (String) metadata.get("forwarderCode"),
                airport,
                (String) metadata.get("acCategory"),
                (String) metadata.get("eFreightCode"),
                (String) metadata.get("flightNumber"),
                flightGroupCodes,
                (String) metadata.get("productCode"),
                productCode,
                exportInd, importInd, transitInd,
                dow);
    }


    /**
     * @param productCode product code
     * @return product group code
     */
    private String getProductGroupCode(String productCode) {
        final String groupCode = this.productGroupEncoreMasterService.getProductGroupCode(productCode);
        log.info(" product code : {}, group code : {}", productCode, groupCode);
        return groupCode;
    }


    /**
     * @param flightNumber flight number
     * @return Flight Groups
     */
    private List<String> getFlightGroups(String flightNumber) {
        final List<String> flightGroupCodes = this.flightGroupEncoreMasterService
                .getFlightGroupCodes(flightNumber);
        log.info(" flightGroupCodes : " + flightGroupCodes);
        return flightGroupCodes;
    }
}
