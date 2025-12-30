package com.utilitybilling.notificationservice.model;

import com.utilitybilling.notificationservice.enums.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	private String id;

	private String recipient;
	private NotificationType type;
	private NotificationChannel channel;
	private NotificationStatus status;
	private String subject;
	private String body;
	private String attachmentBase64;
	private String attachmentName;
	private String attachmentType;
	private Instant createdAt;
}
