package com.utilitybilling.consumerservice.service;

import com.utilitybilling.consumerservice.client.AuthClient;
import com.utilitybilling.consumerservice.dto.UpdateConsumerRequest;
import com.utilitybilling.consumerservice.exception.NotFoundException;
import com.utilitybilling.consumerservice.model.Consumer;
import com.utilitybilling.consumerservice.model.ConsumerRequest;
import com.utilitybilling.consumerservice.repository.ConsumerRepository;
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
class ConsumerServiceTest {

    @Mock
    ConsumerRepository consumerRepo;

    @Mock
    ConsumerRequestRepository requestRepo;

    @Mock
    AuthClient authClient;

    @InjectMocks
    ConsumerService service;

    @Test
    void createFromRequest_shouldApprove() {
        ConsumerRequest request = ConsumerRequest.builder()
                .id("req1")
                .status("PENDING")
                .email("test@test.com")
                .fullName("John")
                .build();

        when(requestRepo.findById("req1")).thenReturn(Optional.of(request));
        when(consumerRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        assertNotNull(service.createFromRequest("req1"));

        verify(authClient).createUser(any());
        verify(requestRepo).save(request);
    }

    @Test
    void createFromRequest_shouldFailIfNotPending() {
        ConsumerRequest request = ConsumerRequest.builder()
                .status("APPROVED")
                .build();

        when(requestRepo.findById("1")).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class,
                () -> service.createFromRequest("1"));
    }

    @Test
    void createFromRequest_shouldFailIfNotFound() {
        when(requestRepo.findById("x")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.createFromRequest("x"));
    }

    @Test
    void getById_shouldReturnConsumer() {
        Consumer c = Consumer.builder().id("1").active(true).build();
        when(consumerRepo.findById("1")).thenReturn(Optional.of(c));

        assertEquals("1", service.getById("1").getId());
    }

    @Test
    void getById_shouldFailIfNotFound() {
        when(consumerRepo.findById("1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getById("1"));
    }

    @Test
    void getAll_shouldReturnList() {
        when(consumerRepo.findAll())
                .thenReturn(List.of(new Consumer(), new Consumer()));

        assertEquals(2, service.getAll().size());
    }

    @Test
    void update_shouldUpdateActiveConsumer() {
        Consumer c = Consumer.builder().id("1").active(true).build();

        when(consumerRepo.findById("1")).thenReturn(Optional.of(c));
        when(consumerRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateConsumerRequest r = new UpdateConsumerRequest();
        r.setFullName("New");

        assertEquals("New", service.update("1", r).getFullName());
    }

    @Test
    void update_shouldFailIfInactive() {
        Consumer c = Consumer.builder().active(false).build();

        when(consumerRepo.findById("1")).thenReturn(Optional.of(c));

        assertThrows(IllegalStateException.class,
                () -> service.update("1", new UpdateConsumerRequest()));
    }

    @Test
    void deactivate_shouldSetInactive() {
        Consumer c = Consumer.builder().id("1").active(true).build();

        when(consumerRepo.findById("1")).thenReturn(Optional.of(c));

        service.deactivate("1");

        assertFalse(c.isActive());
        verify(consumerRepo).save(c);
    }

    @Test
    void exists_shouldReturnTrue() {
        when(consumerRepo.existsById("1")).thenReturn(true);
        assertTrue(service.exists("1"));
    }
}
