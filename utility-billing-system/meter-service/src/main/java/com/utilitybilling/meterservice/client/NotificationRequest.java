package com.utilitybilling.meterservice.client;

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
