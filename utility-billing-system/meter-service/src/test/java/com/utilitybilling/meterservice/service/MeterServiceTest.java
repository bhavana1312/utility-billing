package com.utilitybilling.meterservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utilitybilling.meterservice.dto.CreateConnectionRequest;
import com.utilitybilling.meterservice.dto.CreateMeterReadingRequest;
import com.utilitybilling.meterservice.dto.MeterDetailsResponse;
import com.utilitybilling.meterservice.dto.MeterReadingResponse;
import com.utilitybilling.meterservice.model.ConnectionRequest;
import com.utilitybilling.meterservice.model.ConnectionStatus;
import com.utilitybilling.meterservice.model.Meter;
import com.utilitybilling.meterservice.model.UtilityType;
import com.utilitybilling.meterservice.repository.ConnectionRequestRepository;
import com.utilitybilling.meterservice.repository.MeterReadingRepository;
import com.utilitybilling.meterservice.repository.MeterRepository;

@ExtendWith(MockitoExtension.class)
class MeterServiceTest {

    @Mock
    private ConnectionRequestRepository connectionRepo;

    @Mock
    private MeterRepository meterRepo;

    @Mock
    private MeterReadingRepository readingRepo;

    @InjectMocks
    private MeterService meterService;

    private ConnectionRequest request;

    @BeforeEach
    void setup() {
        request = new ConnectionRequest();
        request.setId("req-1");
        request.setConsumerId("consumer-1");
        request.setUtilityType(UtilityType.ELECTRICITY);
        request.setStatus(ConnectionStatus.PENDING);
    }


    @Test
    void requestConnection_success() {
        CreateConnectionRequest dto = new CreateConnectionRequest();
        dto.setConsumerId("consumer-1");
        dto.setUtilityType(UtilityType.ELECTRICITY);
        dto.setAddress("address");

        when(connectionRepo.existsByConsumerIdAndStatusIn(any(), any()))
                .thenReturn(false);

        meterService.requestConnection(dto);

        verify(connectionRepo).save(any(ConnectionRequest.class));
    }

    @Test
    void requestConnection_duplicate() {
        CreateConnectionRequest dto = new CreateConnectionRequest();
        dto.setConsumerId("consumer-1");
        dto.setUtilityType(UtilityType.ELECTRICITY);
        dto.setAddress("addr");

        when(connectionRepo.existsByConsumerIdAndStatusIn(any(), any()))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> meterService.requestConnection(dto));
    }


    @Test
    void getAllRequests_success() {
        when(connectionRepo.findAll()).thenReturn(List.of(request));
        assertEquals(1, meterService.getAllRequests().size());
    }


    @Test
    void approve_success() {
        when(connectionRepo.findById("req-1")).thenReturn(Optional.of(request));
        when(meterRepo.findByConsumerIdAndUtilityTypeAndActiveTrue(
                "consumer-1", UtilityType.ELECTRICITY))
                .thenReturn(Optional.empty());

        meterService.approve("req-1");

        assertEquals(ConnectionStatus.APPROVED, request.getStatus());
        verify(meterRepo).save(any(Meter.class));
        verify(connectionRepo).save(request);
    }

    @Test
    void approve_notPending() {
        request.setStatus(ConnectionStatus.APPROVED);
        when(connectionRepo.findById("req-1")).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class,
                () -> meterService.approve("req-1"));
    }

    @Test
    void approve_meterAlreadyExists() {
        Meter m = new Meter();

        when(connectionRepo.findById("req-1")).thenReturn(Optional.of(request));
        when(meterRepo.findByConsumerIdAndUtilityTypeAndActiveTrue(
                "consumer-1", UtilityType.ELECTRICITY))
                .thenReturn(Optional.of(m));

        assertThrows(IllegalStateException.class,
                () -> meterService.approve("req-1"));
    }

    @Test
    void approve_notFound() {
        when(connectionRepo.findById("req-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> meterService.approve("req-1"));
    }


    @Test
    void reject_success() {
        when(connectionRepo.findById("req-1")).thenReturn(Optional.of(request));

        meterService.reject("req-1", "reason");

        assertEquals(ConnectionStatus.REJECTED, request.getStatus());
        assertEquals("reason", request.getRejectionReason());
        verify(connectionRepo).save(request);
    }

    @Test
    void reject_notPending() {
        request.setStatus(ConnectionStatus.APPROVED);
        when(connectionRepo.findById("req-1")).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class,
                () -> meterService.reject("req-1", "reason"));
    }

    @Test
    void reject_notFound() {
        when(connectionRepo.findById("req-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> meterService.reject("req-1", "reason"));
    }


    @Test
    void getMeter_success() {
        Meter m = new Meter();
        m.setMeterNumber("m-1");
        m.setConsumerId("consumer-1");
        m.setUtilityType(UtilityType.WATER);
        m.setLastReading(100);
        m.setActive(true);

        when(meterRepo.findById("m-1")).thenReturn(Optional.of(m));

        MeterDetailsResponse r = meterService.getMeter("m-1");

        assertEquals("m-1", r.getMeterNumber());
        assertEquals(100, r.getLastReading());
        assertTrue(r.isActive());
    }

    @Test
    void getMeter_notFound() {
        when(meterRepo.findById("m-1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> meterService.getMeter("m-1"));
    }


    @Test
    void getMetersByConsumer_success() {
        when(meterRepo.findByConsumerId("consumer-1"))
                .thenReturn(List.of(new Meter()));

        assertEquals(1,
                meterService.getMetersByConsumer("consumer-1").size());
    }


    @Test
    void deactivateMeter_success() {
        Meter m = new Meter();
        m.setActive(true);

        when(meterRepo.findById("m-1")).thenReturn(Optional.of(m));

        meterService.deactivateMeter("m-1");

        assertFalse(m.isActive());
        verify(meterRepo).save(m);
    }

    @Test
    void deactivateMeter_notFound() {
        when(meterRepo.findById("m-1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> meterService.deactivateMeter("m-1"));
    }


    @Test
    void addReading_success() {
        Meter m = new Meter();
        m.setMeterNumber("m-1");
        m.setActive(true);
        m.setLastReading(100);

        CreateMeterReadingRequest dto = new CreateMeterReadingRequest();
        dto.setMeterNumber("m-1");
        dto.setReadingValue(150);

        when(meterRepo.findById("m-1")).thenReturn(Optional.of(m));

        MeterReadingResponse response = meterService.addReading(dto);

        assertEquals(100, response.getPreviousReading());
        assertEquals(150, response.getCurrentReading());
        assertEquals(50, response.getUnitsUsed());
        verify(readingRepo).save(any());
        verify(meterRepo).save(m);
    }

    @Test
    void addReading_inactiveMeter() {
        Meter m = new Meter();
        m.setActive(false);

        CreateMeterReadingRequest dto = new CreateMeterReadingRequest();
        dto.setMeterNumber("m-1");
        dto.setReadingValue(100);

        when(meterRepo.findById("m-1")).thenReturn(Optional.of(m));

        assertThrows(IllegalStateException.class,
                () -> meterService.addReading(dto));
    }

    @Test
    void addReading_lowerValue() {
        Meter m = new Meter();
        m.setActive(true);
        m.setLastReading(200);

        CreateMeterReadingRequest dto = new CreateMeterReadingRequest();
        dto.setMeterNumber("m-1");
        dto.setReadingValue(100);

        when(meterRepo.findById("m-1")).thenReturn(Optional.of(m));

        assertThrows(IllegalStateException.class,
                () -> meterService.addReading(dto));
    }

    @Test
    void addReading_meterNotFound() {
        CreateMeterReadingRequest dto = new CreateMeterReadingRequest();
        dto.setMeterNumber("m-1");
        dto.setReadingValue(100);

        when(meterRepo.findById("m-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> meterService.addReading(dto));
    }


    @Test
    void getLastReading_success() {
        Meter m = new Meter();
        m.setLastReading(200);

        when(meterRepo.findById("m-1")).thenReturn(Optional.of(m));

        assertEquals(200,
                meterService.getLastReading("m-1"));
    }

    @Test
    void getLastReading_notFound() {
        when(meterRepo.findById("m-1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> meterService.getLastReading("m-1"));
    }
}
