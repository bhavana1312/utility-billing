package com.utilitybilling.notificationservice.service;

import com.utilitybilling.notificationservice.dto.NotificationEventDTO;
import com.utilitybilling.notificationservice.enums.*;
import com.utilitybilling.notificationservice.exception.NotificationException;
import com.utilitybilling.notificationservice.model.Notification;
import com.utilitybilling.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final JavaMailSender mailSender;
	private final NotificationRepository repository;

	public void send(NotificationEventDTO dto) {

		try {
			SimpleMailMessage mail = new SimpleMailMessage();
			mail.setTo(dto.getEmail());
			mail.setSubject(dto.getSubject());
			mail.setText(dto.getMessage());
			mailSender.send(mail);

			repository.save(Notification.builder().recipient(dto.getEmail()).type(dto.getType())
					.channel(NotificationChannel.EMAIL).status(NotificationStatus.SENT).subject(dto.getSubject())
					.body(dto.getMessage()).createdAt(Instant.now()).build());

		} catch (Exception ex) {
			repository.save(Notification.builder().recipient(dto.getEmail()).type(dto.getType())
					.channel(NotificationChannel.EMAIL).status(NotificationStatus.FAILED).subject(dto.getSubject())
					.body(dto.getMessage()).createdAt(Instant.now()).build());

			throw new NotificationException("Email delivery failed");
		}
	}
}
