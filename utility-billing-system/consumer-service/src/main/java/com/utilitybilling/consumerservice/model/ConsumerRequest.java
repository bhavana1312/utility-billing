package com.utilitybilling.consumerservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection="consumer_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerRequest{

    @Id
    private String id;

    private String fullName;
    private String email;
    private String phone;

    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;

    private String status; 
    private String rejectionReason;

    private Instant createdAt;
    private Instant updatedAt;
}
