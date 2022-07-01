package com.aktimetrix.service.meter.ciq.encore.service;

import com.aktimetrix.service.meter.ciq.encore.model.RouteEncoreMaster;
import com.aktimetrix.service.meter.ciq.encore.repository.RouteEncoreMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteEncoreMasterService {

    @Autowired
    RouteEncoreMasterRepository repository;


    /**
     * @return
     */
    public List<RouteEncoreMaster> list() {
        return repository.findAll();
    }

    /**
     * @param encoreMaster
     * @return
     */
    public RouteEncoreMaster add(RouteEncoreMaster encoreMaster) {
        repository.save(encoreMaster);
        return encoreMaster;
    }

    /**
     * @param tenant
     * @param airline
     * @param origin
     * @param destination
     * @param forwarderCode
     * @return
     */
    public List<RouteEncoreMaster> getRouteByAirlineCodeAndOriginAndDestinationAndForwarderCode(String tenant, String airline, String origin,
                                                                                                String destination, String forwarderCode) {
        return this.repository.findByAirlineAndOriginAndDestinationAndForwarder(tenant, airline, origin,
                destination, forwarderCode);

    }

}
