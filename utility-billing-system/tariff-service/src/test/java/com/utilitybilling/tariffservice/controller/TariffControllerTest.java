package com.utilitybilling.tariffservice.controller;

import com.utilitybilling.tariffservice.dto.*;
import com.utilitybilling.tariffservice.model.*;
import com.utilitybilling.tariffservice.service.TariffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TariffController.class)
@AutoConfigureMockMvc(addFilters = false)
class TariffControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	TariffService service;

	@Test
	void createPlan_ok() throws Exception {
		mockMvc.perform(post("/utilities/tariffs/plans").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "utilityType":"ELECTRICITY",
				  "plan":"DOMESTIC",
				  "slabs":[],
				  "fixedCharge":50,
				  "taxPercentage":10,
				  "overduePenaltySlabs":[],
				  "effectiveFrom":"2024-01-01"
				}
				""")).andExpect(status().isCreated());
	}

	@Test
	void getActivePlan_ok() throws Exception {
		TariffResponse r = TariffResponse.builder().utilityType(UtilityType.ELECTRICITY).plan(TariffPlan.DOMESTIC)
				.active(true).effectiveFrom(LocalDate.now()).build();

		when(service.getActivePlan(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC)).thenReturn(r);

		mockMvc.perform(get("/utilities/tariffs/ELECTRICITY/plans/DOMESTIC")).andExpect(status().isOk());
	}

	@Test
	void deactivatePlan_ok() throws Exception {
		mockMvc.perform(delete("/utilities/tariffs/ELECTRICITY/plans/DOMESTIC")).andExpect(status().isNoContent());

		verify(service).deactivatePlan(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC);
	}

	@Test
	void updatePlan_ok() throws Exception {
		mockMvc.perform(
				put("/utilities/tariffs/ELECTRICITY/plans/DOMESTIC").contentType(MediaType.APPLICATION_JSON).content("""
						{
						  "slabs":[],
						  "fixedCharge":100,
						  "taxPercentage":5,
						  "overduePenaltySlabs":[],
						  "effectiveFrom":"2024-01-01"
						}
						""")).andExpect(status().isNoContent());

		verify(service).updatePlan(eq(UtilityType.ELECTRICITY), eq(TariffPlan.DOMESTIC), any());
	}
}
