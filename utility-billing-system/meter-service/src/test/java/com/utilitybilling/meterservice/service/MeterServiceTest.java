package com.utilitybilling.meterservice.service;

import com.utilitybilling.meterservice.dto.*;
import com.utilitybilling.meterservice.feign.*;
import com.utilitybilling.meterservice.model.*;
import com.utilitybilling.meterservice.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeterServiceTest {

	@InjectMocks
	private MeterService service;

	@Mock
	private ConnectionRequestRepository connectionRepo;
	@Mock
	private MeterRepository meterRepo;
	@Mock
	private MeterReadingRepository readingRepo;
	@Mock
	private NotificationClient notificationClient;
	@Mock
	private ConsumerClient consumerClient;

	@Test
	void requestConnection_success() {
		CreateConnectionRequest r = new CreateConnectionRequest();
		r.setConsumerId("c1");
		r.setUtilityType(UtilityType.ELECTRICITY);
		r.setTariffPlan(TariffPlan.DOMESTIC);

		when(connectionRepo.existsByConsumerIdAndStatusInAndUtilityType(any(), any(), any())).thenReturn(false);

		service.requestConnection(r);

		verify(connectionRepo).save(any(ConnectionRequest.class));
	}

	@Test
	void requestConnection_duplicate() {
		when(connectionRepo.existsByConsumerIdAndStatusInAndUtilityType(any(), any(), any())).thenReturn(true);

		CreateConnectionRequest r = new CreateConnectionRequest();

		assertThrows(IllegalStateException.class, () -> service.requestConnection(r));
	}

	@Test
	void approve_success_and_notification_failure() {
		ConnectionRequest cr = new ConnectionRequest();
		cr.setId("1");
		cr.setStatus(ConnectionStatus.PENDING);
		cr.setConsumerId("c1");
		cr.setUtilityType(UtilityType.ELECTRICITY);
		cr.setTariffPlan(TariffPlan.DOMESTIC);

		when(connectionRepo.findById("1")).thenReturn(Optional.of(cr));
		when(meterRepo.findByConsumerIdAndUtilityTypeAndActiveTrue(any(), any())).thenReturn(Optional.empty());

		ConsumerResponse consumer = new ConsumerResponse();
		consumer.setId("c1");
		consumer.setEmail("a@b.com");

		when(consumerClient.get("c1")).thenReturn(consumer);
		doThrow(new RuntimeException()).when(notificationClient).send(any());

		service.approve("1");

		verify(meterRepo).save(any(Meter.class));
		verify(connectionRepo).save(cr);
	}

	@Test
	void approve_invalid_status() {
		ConnectionRequest cr = new ConnectionRequest();
		cr.setStatus(ConnectionStatus.APPROVED);

		when(connectionRepo.findById("1")).thenReturn(Optional.of(cr));

		assertThrows(IllegalStateException.class, () -> service.approve("1"));
	}

	@Test
	void approve_meter_already_exists() {
		ConnectionRequest cr = new ConnectionRequest();
		cr.setStatus(ConnectionStatus.PENDING);
		cr.setConsumerId("c1");
		cr.setUtilityType(UtilityType.ELECTRICITY);

		when(connectionRepo.findById("1")).thenReturn(Optional.of(cr));
		when(meterRepo.findByConsumerIdAndUtilityTypeAndActiveTrue(any(), any())).thenReturn(Optional.of(new Meter()));

		assertThrows(IllegalStateException.class, () -> service.approve("1"));
	}

	@Test
	void reject_success_and_notification_failure() {
		ConnectionRequest cr = new ConnectionRequest();
		cr.setStatus(ConnectionStatus.PENDING);
		cr.setConsumerId("c1");
		cr.setUtilityType(UtilityType.WATER);

		when(connectionRepo.findById("1")).thenReturn(Optional.of(cr));

		ConsumerResponse consumer = new ConsumerResponse();
		consumer.setId("c1");
		consumer.setEmail("a@b.com");

		when(consumerClient.get("c1")).thenReturn(consumer);
		doThrow(new RuntimeException()).when(notificationClient).send(any());

		service.reject("1", "reason");

		assertEquals(ConnectionStatus.REJECTED, cr.getStatus());
		assertEquals("reason", cr.getRejectionReason());
	}

	@Test
	void reject_invalid_status() {
		ConnectionRequest cr = new ConnectionRequest();
		cr.setStatus(ConnectionStatus.APPROVED);

		when(connectionRepo.findById("1")).thenReturn(Optional.of(cr));

		assertThrows(IllegalStateException.class, () -> service.reject("1", "x"));
	}

	@Test
	void getMeter_success() {
		Meter m = new Meter();
		m.setMeterNumber("M1");
		m.setConsumerId("c1");
		m.setUtilityType(UtilityType.ELECTRICITY);
		m.setTariffPlan(TariffPlan.DOMESTIC);
		m.setActive(true);
		m.setLastReading(100);

		when(meterRepo.findById("M1")).thenReturn(Optional.of(m));

		MeterDetailsResponse r = service.getMeter("M1");

		assertEquals("M1", r.getMeterNumber());
		assertEquals(100, r.getLastReading());
	}

	@Test
	void getMeter_not_found() {
		when(meterRepo.findById("M1")).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> service.getMeter("M1"));
	}

	@Test
	void deactivateMeter_success() {
		Meter m = new Meter();
		m.setActive(true);

		when(meterRepo.findById("M1")).thenReturn(Optional.of(m));

		service.deactivateMeter("M1");

		assertFalse(m.isActive());
		verify(meterRepo).save(m);
	}

	@Test
	void deactivateMeter_not_found() {
		when(meterRepo.findById("M1")).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> service.deactivateMeter("M1"));
	}

	@Test
	void addReading_success() {
		Meter m = new Meter();
		m.setActive(true);
		m.setLastReading(10);

		when(meterRepo.findById("M1")).thenReturn(Optional.of(m));

		CreateMeterReadingRequest r = new CreateMeterReadingRequest();
		r.setMeterNumber("M1");
		r.setReadingValue(20);

		MeterReadingResponse res = service.addReading(r);

		assertEquals(10, res.getPreviousReading());
		assertEquals(20, res.getCurrentReading());
		assertEquals(10, res.getUnitsUsed());
	}

	@Test
	void addReading_inactive_meter() {
		Meter m = new Meter();
		m.setActive(false);

		when(meterRepo.findById("M1")).thenReturn(Optional.of(m));

		CreateMeterReadingRequest r = new CreateMeterReadingRequest();
		r.setMeterNumber("M1");
		r.setReadingValue(20);

		assertThrows(IllegalStateException.class, () -> service.addReading(r));
	}

	@Test
	void addReading_invalid_value() {
		Meter m = new Meter();
		m.setActive(true);
		m.setLastReading(50);

		when(meterRepo.findById("M1")).thenReturn(Optional.of(m));

		CreateMeterReadingRequest r = new CreateMeterReadingRequest();
		r.setMeterNumber("M1");
		r.setReadingValue(10);

		assertThrows(IllegalStateException.class, () -> service.addReading(r));
	}

	@Test
	void addReading_meter_not_found() {
		when(meterRepo.findById("M1")).thenReturn(Optional.empty());

		CreateMeterReadingRequest r = new CreateMeterReadingRequest();
		r.setMeterNumber("M1");
		r.setReadingValue(10);

		assertThrows(IllegalArgumentException.class, () -> service.addReading(r));
	}

	@Test
	void getLastReading_success() {
		Meter m = new Meter();
		m.setLastReading(99);

		when(meterRepo.findById("M1")).thenReturn(Optional.of(m));

		assertEquals(99, service.getLastReading("M1"));
	}

	@Test
	void getLastReading_not_found() {
		when(meterRepo.findById("M1")).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> service.getLastReading("M1"));
	}

	@Test
	void getAllMeters() {
		when(meterRepo.findAll()).thenReturn(List.of(new Meter(), new Meter()));
		assertEquals(2, service.getAllMeters().size());
	}

	@Test
	void getMyMeters() {
		ConsumerResponse c = new ConsumerResponse();
		c.setId("c1");

		when(consumerClient.getByUsername("user")).thenReturn(c);
		when(meterRepo.findByConsumerId("c1")).thenReturn(List.of(new Meter()));

		assertEquals(1, service.getMyMeters("user").size());
	}
}
