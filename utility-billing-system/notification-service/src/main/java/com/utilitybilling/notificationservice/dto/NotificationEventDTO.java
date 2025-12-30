package com.utilitybilling.notificationservice.dto;

import com.utilitybilling.notificationservice.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEventDTO {

	@NotBlank
	private String email;

	@NotNull
	private NotificationType type;

	@NotBlank
	private String subject;

	@NotBlank
	private String message;

	private String attachmentBase64;
	private String attachmentName;
	private String attachmentType;
}
