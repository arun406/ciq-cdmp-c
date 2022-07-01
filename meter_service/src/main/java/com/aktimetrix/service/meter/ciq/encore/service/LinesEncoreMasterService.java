package com.aktimetrix.service.meter.ciq.encore.service;

import com.aktimetrix.service.meter.ciq.encore.model.LinesEncoreMaster;
import com.aktimetrix.service.meter.ciq.encore.repository.LinesEncoreMasterRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class LinesEncoreMasterService {

    @Autowired
    LinesEncoreMasterRepository repository;

    /**
     * @param linesEncoreMaster
     * @return
     */
    public LinesEncoreMaster add(LinesEncoreMaster linesEncoreMaster) {
        repository.save(linesEncoreMaster);
        return linesEncoreMaster;
    }

    public List<LinesEncoreMaster> list() {
        return repository.findAll();
    }

    /**
     * @param tenant
     * @param forwarderCode
     * @param airport
     * @param acCategory
     * @param eFreight
     * @param flightNo
     * @param flightGroups
     * @param productCode
     * @param productGroup
     * @param exportInd
     * @param importInd
     * @param transitInd
     * @param dow
     * @return
     */
    public List<LinesEncoreMaster> getLines(String tenant, String forwarderCode, String airport, String acCategory,
                                            String eFreight, String flightNo, List<String> flightGroups, String productCode,
                                            String productGroup, String exportInd, String importInd, String transitInd,
                                            String dow) {

        final Function<String, Predicate<LinesEncoreMaster>> acPredicateFunction = acCat -> l ->
                StringUtils.isNotEmpty(acCat) && l.getAcCategory().equalsIgnoreCase(acCat) ? true : false;

        return this.repository.findBy(tenant, airport, forwarderCode, acCategory, eFreight,"CONFIRMED").stream()
                .filter(acPredicateFunction.apply(acCategory)
                ).filter(l -> StringUtils.equalsIgnoreCase("ANY", l.getDow()) || StringUtils.contains(l.getDow(), dow))
                .filter(l -> {
                    if (StringUtils.equalsIgnoreCase("ANY", l.getFlightNo()) || StringUtils.equalsIgnoreCase(flightNo, l.getFlightNo())) {
                        return true;
                    } else
                        return !StringUtils.equalsIgnoreCase("none", l.getFlightGroupCode()) && flightGroups.contains(l.getFlightGroupCode());
                }).filter(l -> {
                    if ((StringUtils.isEmpty(l.getProductCode()) && StringUtils.equalsIgnoreCase("GCR", productCode) ||
                            StringUtils.equalsIgnoreCase(l.getProductCode(), productCode))) {
                        return true;
                    } else
                        return StringUtils.equalsIgnoreCase("none", l.getProductCode()) && StringUtils.equalsIgnoreCase(l.getProductGroupCode(), productGroup);
                })
                .filter(l -> {
                    if (org.springframework.util.StringUtils.hasText(exportInd))
                        return StringUtils.equalsIgnoreCase(l.getExportInd(), exportInd);
                    else if (org.springframework.util.StringUtils.hasText(exportInd))
                        return StringUtils.equalsIgnoreCase(l.getImportInd(), importInd);
                    else
                        return StringUtils.equalsIgnoreCase(l.getTransitInd(), transitInd);

                })
                .collect(Collectors.toList());
    }
}
