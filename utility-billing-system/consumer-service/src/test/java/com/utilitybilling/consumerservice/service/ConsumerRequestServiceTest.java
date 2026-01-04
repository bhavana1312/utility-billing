package com.utilitybilling.consumerservice.service;

import com.utilitybilling.consumerservice.dto.*;
import com.utilitybilling.consumerservice.exception.NotFoundException;
import com.utilitybilling.consumerservice.feign.NotificationClient;
import com.utilitybilling.consumerservice.model.ConsumerRequest;
import com.utilitybilling.consumerservice.repository.ConsumerRequestRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumerRequestServiceTest {

	@Mock
	ConsumerRequestRepository repo;
	@Mock
	NotificationClient notificationClient;

	private ConsumerRequestService service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		service = new ConsumerRequestService(repo, notificationClient);
	}

	@Test
	void submit_success() {
		when(repo.existsByEmailAndStatusIn(any(), any())).thenReturn(false);
		when(repo.save(any())).thenAnswer(i -> {
			ConsumerRequest r = i.getArgument(0);
			r.setId("1");
			return r;
		});

		CreateConsumerRequest r = new CreateConsumerRequest();
		r.setFullName("A");
		r.setEmail("e");

		assertEquals("1", service.submit(r).getRequestId());
	}

	@Test
	void submit_duplicate_email() {
		when(repo.existsByEmailAndStatusIn(any(), any())).thenReturn(true);
		CreateConsumerRequest r = new CreateConsumerRequest();

		assertThrows(IllegalStateException.class, () -> service.submit(r));
	}

	@Test
	void getAll_without_status() {
		Page<ConsumerRequest> page = new PageImpl<>(List.of(new ConsumerRequest()));

		when(repo.findAll(any(PageRequest.class))).thenReturn(page);

		assertEquals(1, service.getAll(null, 0, 10).getContent().size());
	}

	@Test
	void getAll_with_status() {
		Page<ConsumerRequest> page = new PageImpl<>(List.of(new ConsumerRequest()));

		when(repo.findByStatus(eq("PENDING"), any(PageRequest.class))).thenReturn(page);

		assertEquals(1, service.getAll("PENDING", 0, 10).getContent().size());
	}

	@Test
	void getById_success() {
		ConsumerRequest r = new ConsumerRequest();
		r.setId("1");

		when(repo.findById("1")).thenReturn(Optional.of(r));

		assertEquals("1", service.getById("1").getId());
	}

	@Test
	void getById_not_found_lambda() {
		when(repo.findById("1")).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> service.getById("1"));
	}

	@Test
	void reject_success() {
		ConsumerRequest r = new ConsumerRequest();
		r.setStatus("PENDING");
		r.setEmail("e");

		when(repo.findById("1")).thenReturn(Optional.of(r));

		service.reject("1", "reason");

		assertEquals("REJECTED", r.getStatus());
		verify(notificationClient).send(any());
	}

	@Test
	void reject_already_processed() {
		ConsumerRequest r = new ConsumerRequest();
		r.setStatus("APPROVED");

		when(repo.findById("1")).thenReturn(Optional.of(r));

		assertThrows(IllegalStateException.class, () -> service.reject("1", "x"));
	}
}
