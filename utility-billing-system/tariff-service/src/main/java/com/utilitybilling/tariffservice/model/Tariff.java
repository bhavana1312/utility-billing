package com.utilitybilling.tariffservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "tariffs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tariff{

	@Id
	private String id;

	private UtilityType utilityType;

	private List<TariffPlanConfig> plans;
}

