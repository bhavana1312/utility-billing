package com.utilitybilling.meterservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.meterservice.dto.*;
import com.utilitybilling.meterservice.model.*;
import com.utilitybilling.meterservice.service.MeterService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeterController.class)
class MeterControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private MeterService service;

	@Test
	void requestConnection() throws Exception {
		CreateConnectionRequest r = new CreateConnectionRequest();
		r.setConsumerId("c1");
		r.setUtilityType(UtilityType.ELECTRICITY);
		r.setTariffPlan(TariffPlan.DOMESTIC);

		mockMvc.perform(post("/meters/connection-requests").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(r))).andExpect(status().isCreated());
	}

	@Test
	void getAllRequests() throws Exception {
		Mockito.when(service.getAllRequests()).thenReturn(List.of(new ConnectionRequest()));

		mockMvc.perform(get("/meters/connection-requests")).andExpect(status().isOk());
	}

	@Test
	void approveRequest() throws Exception {
		mockMvc.perform(post("/meters/connection-requests/1/approve")).andExpect(status().isNoContent());
	}

	@Test
	void rejectRequest() throws Exception {
		RejectConnectionRequest r = new RejectConnectionRequest();
		r.setReason("invalid");

		mockMvc.perform(post("/meters/connection-requests/1/reject").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(r))).andExpect(status().isNoContent());
	}

	@Test
	void getMeter() throws Exception {
		Mockito.when(service.getMeter("M1")).thenReturn(new MeterDetailsResponse());

		mockMvc.perform(get("/meters/M1")).andExpect(status().isOk());
	}

	@Test
	void getAllMeters() throws Exception {
		Mockito.when(service.getAllMeters()).thenReturn(List.of(new Meter()));

		mockMvc.perform(get("/meters/all")).andExpect(status().isOk());
	}

	@Test
	void getMetersByConsumer() throws Exception {
		Mockito.when(service.getMetersByConsumer("c1")).thenReturn(List.of(new Meter()));

		mockMvc.perform(get("/meters/consumer/c1")).andExpect(status().isOk());
	}

	@Test
	void deactivateMeter() throws Exception {
		mockMvc.perform(delete("/meters/M1")).andExpect(status().isNoContent());
	}

	@Test
	void addReading() throws Exception {
		CreateMeterReadingRequest r = new CreateMeterReadingRequest();
		r.setMeterNumber("M1");
		r.setReadingValue(100);

		Mockito.when(service.addReading(Mockito.any())).thenReturn(new MeterReadingResponse());

		mockMvc.perform(post("/meters/readings").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(r))).andExpect(status().isCreated());
	}

	@Test
	void getLastReading() throws Exception {
		Mockito.when(service.getLastReading("M1")).thenReturn(50.0);

		mockMvc.perform(get("/meters/M1/last-reading")).andExpect(status().isOk()).andExpect(content().string("50.0"));
	}
}
