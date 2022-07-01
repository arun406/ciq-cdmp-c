package com.aktimetrix.service.meter.ciq.encore.resource;

import com.aktimetrix.service.meter.ciq.encore.model.RouteEncoreMaster;
import com.aktimetrix.service.meter.ciq.encore.service.RouteEncoreMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequestMapping("/reference-data/encore/routes")
@RestController
public class RouteEncoreMasterResource {

    @Autowired
    RouteEncoreMasterService service;

    @GetMapping
    public List<RouteEncoreMaster> list(@RequestParam(value = "tenant", required = false) String tenant,
                                        @RequestParam(value = "airline", required = false) String airline,
                                        @RequestParam(value = "forwarder", required = false) String forwarderCode,
                                        @RequestParam(value = "origin", required = false) String origin,
                                        @RequestParam(value = "destination", required = false) String destination) {

        if (StringUtils.hasText(forwarderCode) && StringUtils.hasText(origin) && StringUtils.hasText(destination)) {
            return service.getRouteByAirlineCodeAndOriginAndDestinationAndForwarderCode(tenant, airline, origin, destination, forwarderCode);
        }
        return service.list();
    }

    @PostMapping
    public ResponseEntity add(RouteEncoreMaster encoreMaster) {
        service.add(encoreMaster);
        return ResponseEntity.created(URI.create("/reference-data/encore/routes/" + encoreMaster.getId())).build();
    }
}
