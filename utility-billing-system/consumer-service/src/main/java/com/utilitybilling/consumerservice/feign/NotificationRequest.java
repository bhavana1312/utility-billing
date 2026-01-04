package com.utilitybilling.consumerservice.feign;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest{
    private String email;
    private String type;
    private String subject;
    private String message;
}
