package com.aktimetrix.service.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "message_sequences")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageSequence {

    @Id
    private String documentNumber;
    private String messageType;
    private int sequence;
}
