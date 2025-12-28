package com.utilitybilling.consumerservice.dto;

import lombok.*;

@Getter
@Builder
public class ConsumerRequestResponse{
    private String requestId;
    private String status;
}
