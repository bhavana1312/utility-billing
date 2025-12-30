//package com.utilitybilling.tariffservice.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.utilitybilling.tariffservice.dto.*;
//import com.utilitybilling.tariffservice.model.UtilityType;
//import com.utilitybilling.tariffservice.service.TariffService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(TariffController.class)
//class TariffControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private TariffService service;
//
//    @Autowired
//    private ObjectMapper mapper;
//
//    @Test
//    void create_returns201() throws Exception {
//        CreateTariffRequest r=new CreateTariffRequest();
//        r.setUtilityType(UtilityType.ELECTRICITY);
//        r.setSlabs(List.of());
//        r.setFixedCharge(50);
//        r.setTaxPercentage(18);
//        r.setEffectiveFrom(LocalDate.now());
//
//        mockMvc.perform(post("/utilities/tariffs")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(r)))
//                .andExpect(status().isCreated());
//    }
//
//    @Test
//    void getActive_returns200() throws Exception {
//        TariffResponse res=TariffResponse.builder()
//                .utilityType(UtilityType.WATER)
//                .build();
//
//        when(service.getActive(UtilityType.WATER)).thenReturn(res);
//
//        mockMvc.perform(get("/utilities/tariffs/WATER"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void delete_returns204() throws Exception {
//        mockMvc.perform(delete("/utilities/tariffs/1"))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    void update_returns204() throws Exception {
//        UpdateTariffRequest r=new UpdateTariffRequest();
//        r.setEffectiveFrom(LocalDate.now());
//
//        mockMvc.perform(put("/utilities/tariffs/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(r)))
//                .andExpect(status().isNoContent());
//    }
//}
