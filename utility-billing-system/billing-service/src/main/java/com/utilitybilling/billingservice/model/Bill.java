package com.utilitybilling.billingservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Data
@Document(collection="bills")
public class Bill{

    @Id
    private String id;

    private String consumerId;
    private String email;
    private String meterNumber;
    private String utilityType;

    private double previousReading;
    private double currentReading;
    private double unitsConsumed;

    private double energyCharge;
    private double fixedCharge;
    private double taxAmount;

    private double penaltyAmount; 
    private double totalAmount;
    
    private Date dueDate;     
    private Instant lastUpdatedAt;

    private BillStatus status;
    private Instant generatedAt=Instant.now();
}
