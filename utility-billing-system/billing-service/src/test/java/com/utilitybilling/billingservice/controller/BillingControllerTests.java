package com.utilitybilling.billingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.model.BillStatus;
import com.utilitybilling.billingservice.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillingController.class)
class BillingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    @MockBean
    private BillingQueryService queryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGenerateBill() throws Exception {
        GenerateBillRequest request = new GenerateBillRequest();
        request.setMeterNumber("MTR-1");

        BillResponse response = new BillResponse();
        response.setBillId("B1");
        response.setStatus(BillStatus.DUE.name());

        when(billingService.generate(any()))
                .thenReturn(response);

        mockMvc.perform(post("/billing/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.billId").value("B1"));
    }

    @Test
    void shouldReturnConsumerBills() throws Exception {
        when(queryService.consumerBills("C1"))
                .thenReturn(List.of(new BillResponse()));

        mockMvc.perform(get("/billing/C1"))
                .andExpect(status().isOk());
    }
}
