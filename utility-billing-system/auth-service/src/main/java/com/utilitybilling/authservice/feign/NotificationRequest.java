package com.utilitybilling.authservice.feign;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest{

    @NotBlank
    private String email;

    @NotBlank
    private String type;

    @NotBlank
    private String subject;

    @NotBlank
    private String message;
}
