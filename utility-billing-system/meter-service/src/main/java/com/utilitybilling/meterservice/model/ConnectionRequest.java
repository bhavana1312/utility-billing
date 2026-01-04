package com.utilitybilling.meterservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection="connection_requests")
public class ConnectionRequest{

    @Id
    private String id;

    private String consumerId;
    
    private UtilityType utilityType;
    
    private TariffPlan tariffPlan;

    private ConnectionStatus status=ConnectionStatus.PENDING;

    private String rejectionReason;

    private Instant createdAt=Instant.now();
}
