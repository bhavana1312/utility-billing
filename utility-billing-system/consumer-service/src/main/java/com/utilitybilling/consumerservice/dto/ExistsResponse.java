package com.utilitybilling.consumerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExistsResponse{
    private boolean exists;
}
