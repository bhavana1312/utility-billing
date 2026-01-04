package com.utilitybilling.paymentservice.feign;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
	private String email;
	private String type;
	private String subject;
	private String message;

	private String attachmentBase64;
	private String attachmentName;
	private String attachmentType;
}
