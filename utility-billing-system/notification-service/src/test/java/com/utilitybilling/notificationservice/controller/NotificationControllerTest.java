package com.utilitybilling.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.notificationservice.dto.NotificationEventDTO;
import com.utilitybilling.notificationservice.enums.NotificationType;
import com.utilitybilling.notificationservice.service.NotificationService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

	private MockMvc mockMvc;
	private NotificationService service;

	@BeforeEach
	void setup() {
		service = Mockito.mock(NotificationService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new NotificationController(service)).build();
	}

	@Test
	void send_ok() throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		NotificationEventDTO dto = new NotificationEventDTO();
		dto.setEmail("a@test.com");
		dto.setType(NotificationType.PAYMENT_SUCCESS);
		dto.setSubject("Subject");
		dto.setMessage("Message");

		doNothing().when(service).send(Mockito.any());

		mockMvc.perform(post("/notifications/send").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
	}
}
