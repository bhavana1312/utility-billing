//package com.utilitybilling.billingservice.service;
//
//import com.utilitybilling.billingservice.model.*;
//import com.utilitybilling.billingservice.repository.BillRepository;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//@ExtendWith(MockitoExtension.class)
//class BillingQueryServiceTest {
//
//	@Mock
//	BillRepository billRepo;
//
//	@InjectMocks
//	BillingQueryService service;
//
//	@Test
//	void shouldReturnConsumerBills() {
//		Bill bill = new Bill();
//		bill.setId("1");
//		bill.setStatus(BillStatus.DUE);
//		bill.setGeneratedAt(Instant.now());
//
//		when(billRepo.findByConsumerIdOrderByGeneratedAtDesc("c1")).thenReturn(List.of(bill));
//
//		assertEquals(1, service.consumerBills("c1").size());
//	}
//
//	@Test
//	void shouldReturnAllBillsWhenStatusNull() {
//		Bill bill = new Bill();
//	    bill.setStatus(BillStatus.DUE);
//	    when(billRepo.findAll()).thenReturn(List.of(bill));
//		assertEquals(1, service.all(null).size());
//	}
//
//	@Test
//	void shouldReturnOutstandingAmount() {
//		Bill b1 = new Bill();
//		b1.setTotalAmount(100);
//
//		Bill b2 = new Bill();
//		b2.setTotalAmount(200);
//
//		when(billRepo.findByConsumerIdAndStatusIn(eq("c1"), anyList())).thenReturn(List.of(b1, b2));
//
//		var response = service.outstanding("c1");
//
//		assertEquals(300, response.getOutstandingAmount());
//	}
//
//	@Test
//	void shouldThrowIfBillNotFound() {
//		
//		when(billRepo.findById("x")).thenReturn(Optional.empty());
//
//		assertThrows(RuntimeException.class, () -> service.getById("x"));
//	}
//}
