package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notifications")
    @PreAuthorize("hasAnyRole('GARAGE_ADMIN','ADMIN','MANAGER')")
    public ResponseEntity<Map<String, String>> sendNotification(@Valid @RequestBody NotificationRequest request) {
        notificationService.notifyEvent(
                request.eventType(),
                request.recipient(),
                request.channel(),
                request.subject(),
                request.message()
        );

        return ResponseEntity.accepted().body(Map.of("status", "queued"));
    }
}
