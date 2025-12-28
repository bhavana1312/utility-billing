package com.utilitybilling.authservice.controller;

import com.utilitybilling.authservice.dto.LoginResponse;
import com.utilitybilling.authservice.service.AuthService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService service;

	@Test
	void register_201() throws Exception {
		mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
				{"username":"u","email":"e@mail.com","password":"p","roles":["ADMIN"]}
				""")).andExpect(status().isCreated());
	}

	@Test
	void login_200() throws Exception {
		LoginResponse res = new LoginResponse(null);
		res.setToken("jwt");
		when(service.login(any())).thenReturn(res);

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
				{"username":"u","password":"p"}
				""")).andExpect(status().isOk());
	}

	@Test
	void changePassword_204() throws Exception {
		mockMvc.perform(post("/auth/change-password").contentType(MediaType.APPLICATION_JSON).content("""
				{"username":"u","oldPassword":"o","newPassword":"n"}
				""")).andExpect(status().isNoContent());
	}

	@Test
	void forgotPassword_204() throws Exception {
		mockMvc.perform(post("/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("""
				{"email":"u@mail.com"}
				""")).andExpect(status().isNoContent());
	}

	@Test
	void resetPassword_204() throws Exception {
		mockMvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content("""
				{"resetToken":"t","newPassword":"n"}
				""")).andExpect(status().isNoContent());
	}
}