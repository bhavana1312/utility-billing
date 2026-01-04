package com.utilitybilling.billingservice.controller;

import com.utilitybilling.billingservice.service.BillingService;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InternalBillingControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        BillingService service=mock(BillingService.class);
        InternalBillingController controller=new InternalBillingController(service);
        mockMvc=MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void markPaid_ok() throws Exception {
        mockMvc.perform(put("/billing/internal/B1/mark-paid"))
                .andExpect(status().isNoContent());
    }
}
