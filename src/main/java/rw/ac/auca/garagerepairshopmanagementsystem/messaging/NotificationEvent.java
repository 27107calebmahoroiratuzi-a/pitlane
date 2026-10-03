package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import java.io.Serializable;
import java.util.UUID;

public record NotificationEvent(
        String eventType,
        String recipient,
        String channel,
        String subject,
        String message,
        String correlationId
) implements Serializable {

    public NotificationEvent(String eventType, String recipient, String channel, String subject, String message) {
        this(eventType, recipient, channel, subject, message, UUID.randomUUID().toString());
    }
}
