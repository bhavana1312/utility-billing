package com.utilitybilling.consumerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.consumerservice.dto.*;
import com.utilitybilling.consumerservice.service.ConsumerService;
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

@WebMvcTest(ConsumerController.class)
class ConsumerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ConsumerService service;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void approve_shouldCreateConsumer() throws Exception {
		ConsumerResponse response = ConsumerResponse.builder().id("1").fullName("John Doe").email("john@test.com")
				.password("secret").active(true).build();

		Mockito.when(service.createFromRequest("req1")).thenReturn(response);

		mockMvc.perform(post("/consumers/from-request/req1")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("1")).andExpect(jsonPath("$.email").value("john@test.com"));
	}

	@Test
	void get_shouldReturnConsumer() throws Exception {
		Mockito.when(service.getById("1")).thenReturn(ConsumerResponse.builder().id("1").build());

		mockMvc.perform(get("/consumers/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value("1"));
	}

	@Test
	void getAll_shouldReturnList() throws Exception {
		Mockito.when(service.getAll()).thenReturn(List.of(ConsumerResponse.builder().id("1").build()));

		mockMvc.perform(get("/consumers")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("1"));
	}

	@Test
	void update_shouldModifyConsumer() throws Exception {
		UpdateConsumerRequest r = new UpdateConsumerRequest();

		r.setFullName("Jane");
		r.setPhone("123");
		r.setAddressLine1("Addr");
		r.setCity("City");
		r.setState("State");
		r.setPostalCode("11111");

		Mockito.when(service.update(Mockito.eq("1"), Mockito.any()))
				.thenReturn(ConsumerResponse.builder().id("1").fullName("Jane").build());

		mockMvc.perform(
				put("/consumers/1").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(r)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Jane"));
	}

	@Test
	void deactivate_shouldReturn204() throws Exception {
		mockMvc.perform(delete("/consumers/1")).andExpect(status().isNoContent());

		Mockito.verify(service).deactivate("1");
	}

	@Test
	void exists_shouldReturnBoolean() throws Exception {
		Mockito.when(service.exists("1")).thenReturn(true);

		mockMvc.perform(get("/consumers/1/exists")).andExpect(status().isOk())
				.andExpect(jsonPath("$.exists").value(true));
	}
}
