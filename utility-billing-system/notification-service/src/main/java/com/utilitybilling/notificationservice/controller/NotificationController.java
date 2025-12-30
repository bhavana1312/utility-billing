package com.utilitybilling.notificationservice.controller;

import com.utilitybilling.notificationservice.dto.NotificationEventDTO;
import com.utilitybilling.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService service;

	@PostMapping("/send")
	public ResponseEntity<Void> send(@Valid @RequestBody NotificationEventDTO dto) {
		service.send(dto);
		return ResponseEntity.ok().build();
	}
}
