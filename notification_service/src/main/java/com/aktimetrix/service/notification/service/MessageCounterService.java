package com.aktimetrix.service.notification.service;

import com.aktimetrix.service.notification.model.MessageSequence;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;

@Service
public class MessageCounterService {

    @Autowired
    private MongoOperations mongoOperations;

    /**
     * Returns the Message Sequence Number
     *
     * @param documentNumber
     * @param messageType
     * @return
     */
    public int getSequenceNumber(String documentNumber, String messageType) {
        Query query = new Query(Criteria.where("documentNumber").is(documentNumber));
        Update update = new Update().set("messageType", messageType).inc("sequence", 1);
        MessageSequence messageSequence = this.mongoOperations.findAndModify(query, update, options().returnNew(true).upsert(true), MessageSequence.class);
        int sequence = !Objects.isNull(messageSequence) ? messageSequence.getSequence() : 1;
        //reset counter
        if (messageType.equals("CAN")) {
            UpdateResult result = this.mongoOperations.updateFirst(new Query(Criteria.where("documentNumber").is(documentNumber)), new Update().set("sequence", 0), MessageSequence.class);
        }
        return sequence;
    }
}
