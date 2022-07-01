package com.aktimetrix.service.meter.ciq.encore.repository;

import com.aktimetrix.service.meter.ciq.encore.model.LinesEncoreMaster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface LinesEncoreMasterRepository extends MongoRepository<LinesEncoreMaster, String> {

    /**
     * @param tenant
     * @param airport
     * @param forwarderCode
     * @param status
     * @param acCategory
     * @return
     */

    @Query("{ 'tenant': ?0 , 'airport' : ?1 , 'forwarderCode' : ?2, 'acCategory' : ?3 , 'eFreightInd': ?4 , 'status': ?5 }")
    List<LinesEncoreMaster> findBy(String tenant, String airport, String forwarderCode, String acCategory, String eFreightIndicator, String status);
}
