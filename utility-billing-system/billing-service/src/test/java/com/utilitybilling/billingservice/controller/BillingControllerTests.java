package com.utilitybilling.billingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.model.BillStatus;
import com.utilitybilling.billingservice.service.BillingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService service;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void generate_ok() throws Exception {
        when(service.generate(any())).thenReturn(new BillResponse());

        mockMvc.perform(post("/billing/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new GenerateBillRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void consumerBills_ok() throws Exception {
        Page<BillResponse> page = new PageImpl<>(List.of(new BillResponse()));

        when(service.consumerBills("C1",0,10)).thenReturn(page);

        mockMvc.perform(get("/billing/C1?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void all_withoutStatus_ok() throws Exception {
        Page<BillResponse> page = new PageImpl<>(List.of(new BillResponse()));

        when(service.all(null,0,10)).thenReturn(page);

        mockMvc.perform(get("/billing?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void all_withStatus_ok() throws Exception {
        Page<BillResponse> page = new PageImpl<>(List.of(new BillResponse()));

        when(service.all(BillStatus.DUE,0,10)).thenReturn(page);

        mockMvc.perform(get("/billing?status=DUE&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
