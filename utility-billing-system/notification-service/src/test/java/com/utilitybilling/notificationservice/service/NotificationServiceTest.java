package com.utilitybilling.notificationservice.service;

import com.utilitybilling.notificationservice.dto.NotificationEventDTO;
import com.utilitybilling.notificationservice.enums.NotificationChannel;
import com.utilitybilling.notificationservice.enums.NotificationStatus;
import com.utilitybilling.notificationservice.enums.NotificationType;
import com.utilitybilling.notificationservice.exception.NotificationException;
import com.utilitybilling.notificationservice.model.Notification;
import com.utilitybilling.notificationservice.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

	@Mock
	JavaMailSender mailSender;

	@Mock
	NotificationRepository repository;

	@Mock
	MimeMessage mimeMessage;

	private NotificationService service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		service = new NotificationService(mailSender, repository);
	}

	@Test
	void send_success_without_attachment() {
		NotificationEventDTO dto = new NotificationEventDTO();
		dto.setEmail("a@test.com");
		dto.setType(NotificationType.BILL_GENERATED);
		dto.setSubject("Subject");
		dto.setMessage("Message");

		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

		service.send(dto);

		verify(mailSender).send(any(MimeMessage.class));
		verify(repository).save(
				argThat(n -> n.getStatus() == NotificationStatus.SENT && n.getChannel() == NotificationChannel.EMAIL));
	}

	@Test
	void send_success_with_attachment() {
		NotificationEventDTO dto = new NotificationEventDTO();
		dto.setEmail("a@test.com");
		dto.setType(NotificationType.INVOICE_PDF);
		dto.setSubject("Invoice");
		dto.setMessage("Attached");
		dto.setAttachmentBase64(Base64.getEncoder().encodeToString("pdf".getBytes()));
		dto.setAttachmentName("invoice.pdf");
		dto.setAttachmentType("application/pdf");

		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

		service.send(dto);

		verify(repository)
				.save(argThat(n -> n.getStatus() == NotificationStatus.SENT && n.getAttachmentBase64() != null));
	}

	@Test
	void send_attachment_defaults() {
		NotificationEventDTO dto = new NotificationEventDTO();
		dto.setEmail("a@test.com");
		dto.setType(NotificationType.INVOICE_PDF);
		dto.setSubject("Invoice");
		dto.setMessage("Attached");
		dto.setAttachmentBase64(Base64.getEncoder().encodeToString("pdf".getBytes()));

		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

		service.send(dto);

		verify(repository).save(any(Notification.class));
	}

	@Test
	void send_failure_path() {
		NotificationEventDTO dto = new NotificationEventDTO();
		dto.setEmail("a@test.com");
		dto.setType(NotificationType.PAYMENT_FAILED);
		dto.setSubject("Fail");
		dto.setMessage("Error");

		when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("mail down"));

		assertThrows(NotificationException.class, () -> service.send(dto));

		verify(repository).save(argThat(n -> n.getStatus() == NotificationStatus.FAILED));
	}
}
