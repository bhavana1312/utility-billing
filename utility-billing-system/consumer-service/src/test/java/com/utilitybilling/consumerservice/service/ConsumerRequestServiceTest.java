package com.utilitybilling.consumerservice.service;

import com.utilitybilling.consumerservice.dto.CreateConsumerRequest;
import com.utilitybilling.consumerservice.exception.NotFoundException;
import com.utilitybilling.consumerservice.model.ConsumerRequest;
import com.utilitybilling.consumerservice.repository.ConsumerRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumerRequestServiceTest {

    @Mock
    ConsumerRequestRepository repository;

    @InjectMocks
    ConsumerRequestService service;

    @Test
    void submit_shouldFailIfExists() {
        CreateConsumerRequest r = new CreateConsumerRequest();
        r.setEmail("a@test.com");

        when(repository.existsByEmailAndStatusIn(any(), any()))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.submit(r));
    }

    @Test
    void submit_shouldCreateRequest() {
        CreateConsumerRequest r = new CreateConsumerRequest();
        r.setFullName("John");
        r.setEmail("a@test.com");
        r.setPhone("123");
        r.setAddressLine1("A");
        r.setCity("C");
        r.setState("S");
        r.setPostalCode("1");

        when(repository.existsByEmailAndStatusIn(any(), any()))
                .thenReturn(false);
        when(repository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        assertEquals("PENDING", service.submit(r).getStatus());
    }

    @Test
    void getAll_shouldReturnAll() {
        when(repository.findAll())
                .thenReturn(List.of(new ConsumerRequest()));

        assertEquals(1, service.getAll(null).size());
    }

    @Test
    void getAll_shouldReturnByStatus() {
        when(repository.findByStatus("PENDING"))
                .thenReturn(List.of(new ConsumerRequest()));

        assertEquals(1, service.getAll("PENDING").size());
    }

    @Test
    void getById_shouldReturn() {
        when(repository.findById("1"))
                .thenReturn(Optional.of(new ConsumerRequest()));

        assertNotNull(service.getById("1"));
    }

    @Test
    void getById_shouldFailIfNotFound() {
        when(repository.findById("1"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getById("1"));
    }

    @Test
    void reject_shouldRejectPending() {
        ConsumerRequest r = ConsumerRequest.builder()
                .id("1")
                .status("PENDING")
                .build();

        when(repository.findById("1"))
                .thenReturn(Optional.of(r));

        service.reject("1", "reason");

        assertEquals("REJECTED", r.getStatus());
        verify(repository).save(r);
    }

    @Test
    void reject_shouldFailIfAlreadyProcessed() {
        ConsumerRequest r = ConsumerRequest.builder()
                .status("APPROVED")
                .build();

        when(repository.findById("1"))
                .thenReturn(Optional.of(r));

        assertThrows(IllegalStateException.class,
                () -> service.reject("1", "reason"));
    }
}
