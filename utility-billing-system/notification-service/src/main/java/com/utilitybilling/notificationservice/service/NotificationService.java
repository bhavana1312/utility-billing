package com.utilitybilling.notificationservice.service;

import com.utilitybilling.notificationservice.dto.NotificationEventDTO;
import com.utilitybilling.notificationservice.enums.*;
import com.utilitybilling.notificationservice.exception.NotificationException;
import com.utilitybilling.notificationservice.model.Notification;
import com.utilitybilling.notificationservice.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final JavaMailSender mailSender;
	private final NotificationRepository repository;

	public void send(NotificationEventDTO dto) {

		try {
			MimeMessage message = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					"UTF-8");

			helper.setTo(dto.getEmail());
			helper.setSubject(dto.getSubject());

			helper.setText(dto.getMessage(), false);

			if (dto.getAttachmentBase64() != null && !dto.getAttachmentBase64().isBlank()) {

				byte[] file = Base64.getDecoder().decode(dto.getAttachmentBase64());

				String name = dto.getAttachmentName();
				if (name == null || name.isBlank()) {
					name = "attachment.pdf";
				}

				String type = dto.getAttachmentType();
				if (type == null || type.isBlank()) {
					type = "application/pdf";
				}

				helper.addAttachment(name, new ByteArrayResource(file), type);
			}

			mailSender.send(message);

			repository.save(Notification.builder().recipient(dto.getEmail()).type(dto.getType())
					.channel(NotificationChannel.EMAIL).status(NotificationStatus.SENT).subject(dto.getSubject())
					.body(dto.getMessage()).attachmentBase64(dto.getAttachmentBase64())
					.attachmentName(dto.getAttachmentName()).attachmentType(dto.getAttachmentType())
					.createdAt(Instant.now()).build());

		} catch (Exception ex) {

			repository.save(Notification.builder().recipient(dto.getEmail()).type(dto.getType())
					.channel(NotificationChannel.EMAIL).status(NotificationStatus.FAILED).subject(dto.getSubject())
					.body(dto.getMessage()).createdAt(Instant.now()).build());

			throw new NotificationException("Email delivery failed");
		}
	}
}
