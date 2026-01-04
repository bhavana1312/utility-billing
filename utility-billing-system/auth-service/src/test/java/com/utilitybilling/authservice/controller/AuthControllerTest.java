package com.utilitybilling.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.authservice.dto.*;
import com.utilitybilling.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	AuthService service;

	@Autowired
	ObjectMapper mapper;

	@Test
	void register_created() throws Exception {
		RegisterRequest r = new RegisterRequest();
		r.setUsername("u");
		r.setEmail("e");
		r.setPassword("p");

		mockMvc.perform(
				post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(r)))
				.andExpect(status().isCreated());
	}

	@Test
	void login_ok() throws Exception {
		LoginRequest r = new LoginRequest();
		r.setUsername("u");
		r.setPassword("p");

		LoginResponse response = new LoginResponse(null);
		response.setToken("jwt");

		when(service.login(any())).thenReturn(response);

		mockMvc.perform(
				post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(r)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.token").value("jwt"));
	}

	@Test
	void changePassword_noContent() throws Exception {
		ChangePasswordRequest r = new ChangePasswordRequest();
		r.setUsername("u");
		r.setOldPassword("o");
		r.setNewPassword("n");

		mockMvc.perform(post("/auth/change-password").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(r))).andExpect(status().isNoContent());
	}

	@Test
	void forgotPassword_noContent() throws Exception {
		ForgotPasswordRequest r = new ForgotPasswordRequest();
		r.setEmail("a@b.com");

		mockMvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(r))).andExpect(status().isNoContent());

		verify(service).forgotPassword(any(ForgotPasswordRequest.class));
	}

	@Test
	void resetPassword_noContent() throws Exception {
		ResetPasswordRequest r = new ResetPasswordRequest();
		r.setResetToken("token");
		r.setNewPassword("new");

		mockMvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(r))).andExpect(status().isNoContent());

		verify(service).resetPassword(any(ResetPasswordRequest.class));
	}

}
