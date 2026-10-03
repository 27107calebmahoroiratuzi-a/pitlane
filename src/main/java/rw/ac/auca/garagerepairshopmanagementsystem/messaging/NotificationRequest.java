package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @NotBlank(message = "Event type is required") String eventType,
        @NotBlank(message = "Recipient is required") String recipient,
        @NotBlank(message = "Channel is required") String channel,
        @NotBlank(message = "Subject is required") String subject,
        @NotBlank(message = "Message is required") String message
) {
}
