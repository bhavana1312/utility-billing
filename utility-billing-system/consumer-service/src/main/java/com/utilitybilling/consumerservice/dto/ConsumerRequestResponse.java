package com.utilitybilling.consumerservice.dto;

import lombok.*;

@Data
@Builder
public class ConsumerRequestResponse{
    private String requestId;
    private String status;
}
