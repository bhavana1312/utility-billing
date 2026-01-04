package com.utilitybilling.consumerservice.service;

import com.utilitybilling.consumerservice.dto.*;
import com.utilitybilling.consumerservice.exception.NotFoundException;
import com.utilitybilling.consumerservice.feign.*;
import com.utilitybilling.consumerservice.model.*;
import com.utilitybilling.consumerservice.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumerServiceTest {

	@Mock
	ConsumerRepository consumerRepo;
	@Mock
	ConsumerRequestRepository requestRepo;
	@Mock
	AuthClient authClient;
	@Mock
	NotificationClient notificationClient;

	private ConsumerService service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		service = new ConsumerService(consumerRepo, requestRepo, authClient, notificationClient);
	}

	@Test
	void approve_success() {
		ConsumerRequest r = new ConsumerRequest();
		r.setId("1");
		r.setStatus("PENDING");
		r.setFullName("A");
		r.setEmail("e");

		when(requestRepo.findById("1")).thenReturn(Optional.of(r));
		when(consumerRepo.save(any())).thenAnswer(i -> {
			Consumer c = i.getArgument(0);
			c.setId("C1");
			return c;
		});

		ConsumerResponse res = service.approve("1");

		assertEquals("C1", res.getId());
		verify(authClient).createUser(any());
		verify(notificationClient).send(any());
	}

	@Test
	void approve_already_processed() {
		ConsumerRequest r = new ConsumerRequest();
		r.setStatus("APPROVED");

		when(requestRepo.findById("1")).thenReturn(Optional.of(r));

		assertThrows(IllegalStateException.class, () -> service.approve("1"));
	}

	@Test
	void approve_not_found_lambda() {
		when(requestRepo.findById("1")).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> service.approve("1"));
	}

	@Test
	void getById_success() {
		Consumer c = new Consumer();
		c.setId("C1");

		when(consumerRepo.findById("C1")).thenReturn(Optional.of(c));

		assertEquals("C1", service.getById("C1").getId());
	}

	@Test
	void getById_not_found_lambda() {
		when(consumerRepo.findById("C1")).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> service.getById("C1"));
	}

	@Test
	void getAll_success() {
		Consumer c = new Consumer();
		c.setId("C1");

		Page<Consumer> page = new PageImpl<>(List.of(c));

		when(consumerRepo.findAll(any(PageRequest.class))).thenReturn(page);

		assertEquals(1, service.getAll(0, 10).getContent().size());
	}

	@Test
	void update_success() {
		Consumer c = new Consumer();
		c.setId("C1");
		c.setActive(true);

		when(consumerRepo.findById("C1")).thenReturn(Optional.of(c));
		when(consumerRepo.save(any())).thenReturn(c);

		UpdateConsumerRequest r = new UpdateConsumerRequest();
		r.setFullName("X");

		assertEquals("C1", service.update("C1", r).getId());
	}

	@Test
	void update_inactive_consumer() {
		Consumer c = new Consumer();
		c.setActive(false);

		when(consumerRepo.findById("C1")).thenReturn(Optional.of(c));

		UpdateConsumerRequest r = new UpdateConsumerRequest();

		assertThrows(IllegalStateException.class, () -> service.update("C1", r));
	}

	@Test
	void deactivate_success() {
		Consumer c = new Consumer();
		c.setActive(true);

		when(consumerRepo.findById("C1")).thenReturn(Optional.of(c));

		service.deactivate("C1");

		assertFalse(c.isActive());
		verify(consumerRepo).save(c);
	}

	@Test
	void exists_true() {
		when(consumerRepo.existsById("1")).thenReturn(true);
		assertTrue(service.exists("1"));
	}

	@Test
	void getByUsername_success() {
		Consumer c = new Consumer();
		c.setId("C1");

		when(consumerRepo.findByFullName("u")).thenReturn(Optional.of(c));

		assertEquals("C1", service.getByUsername("u").getId());
	}

	@Test
	void getByUsername_not_found_lambda() {
		when(consumerRepo.findByFullName("u")).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> service.getByUsername("u"));
	}
}
