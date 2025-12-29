package com.utilitybilling.meterservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilitybilling.meterservice.dto.CreateConnectionRequest;
import com.utilitybilling.meterservice.dto.CreateMeterReadingRequest;
import com.utilitybilling.meterservice.dto.MeterDetailsResponse;
import com.utilitybilling.meterservice.dto.MeterReadingResponse;
import com.utilitybilling.meterservice.dto.RejectConnectionRequest;
import com.utilitybilling.meterservice.model.ConnectionRequest;
import com.utilitybilling.meterservice.model.Meter;
import com.utilitybilling.meterservice.model.UtilityType;
import com.utilitybilling.meterservice.service.MeterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = MeterController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
class MeterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeterService meterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void all_returns200() throws Exception {
        when(meterService.getAllRequests()).thenReturn(List.of(new ConnectionRequest()));

        mockMvc.perform(get("/meters/connection-requests"))
                .andExpect(status().isOk());
    }

    @Test
    void getMeter_returns200() throws Exception {
        when(meterService.getMeter("m-1")).thenReturn(new MeterDetailsResponse());

        mockMvc.perform(get("/meters/m-1"))
                .andExpect(status().isOk());
    }

    @Test
    void byConsumer_returns200() throws Exception {
        when(meterService.getMetersByConsumer("c-1"))
                .thenReturn(List.of(new Meter()));

        mockMvc.perform(get("/meters/consumer/c-1"))
                .andExpect(status().isOk());
    }

    @Test
    void deactivate_returns204() throws Exception {
        mockMvc.perform(delete("/meters/m-1"))
                .andExpect(status().isNoContent());

        verify(meterService).deactivateMeter("m-1");
    }

    @Test
    void add_returns201() throws Exception {
        CreateMeterReadingRequest dto = new CreateMeterReadingRequest();
        dto.setMeterNumber("m-1");
        dto.setReadingValue(200);

        when(meterService.addReading(any())).thenReturn(new MeterReadingResponse());

        mockMvc.perform(post("/meters/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void last_returns200() throws Exception {
        when(meterService.getLastReading("m-1")).thenReturn(300.0);

        mockMvc.perform(get("/meters/m-1/last-reading"))
                .andExpect(status().isOk())
                .andExpect(content().string("300.0"));
    }
    
    @Test
    void request() throws Exception {
        CreateConnectionRequest dto = new CreateConnectionRequest();
        dto.setConsumerId("c1");
        dto.setUtilityType(UtilityType.WATER);
        dto.setAddress("addr");

        mockMvc.perform(post("/meters/connection-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(meterService).requestConnection(dto);
    }

    @Test
    void approve() throws Exception {
        mockMvc.perform(post("/meters/connection-requests/1/approve"))
                .andExpect(status().isNoContent());

        verify(meterService).approve("1");
    }

    @Test
    void reject() throws Exception {
        RejectConnectionRequest dto = new RejectConnectionRequest();
        dto.setReason("bad");

        mockMvc.perform(post("/meters/connection-requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        verify(meterService).reject("1", "bad");
    }
}
