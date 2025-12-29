package com.utilitybilling.tariffservice.model;

import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OverduePenaltySlab {

    @Min(value = 0, message = "From day must be >= 0")
    private int fromDay;

    @Min(value = 1, message = "To day must be >= 1")
    private int toDay;

    @Min(value = 0, message = "Penalty percentage must be >= 0")
    private double penaltyPercentage;
}
