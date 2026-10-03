package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final EventPublisher eventPublisher;

    public NotificationService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notifyEmail(String recipient, String subject, String message) {
        eventPublisher.publish(new NotificationEvent(
                "EMAIL",
                recipient,
                "EMAIL",
                subject,
                message
        ));
    }

    public void notifySms(String recipient, String message) {
        eventPublisher.publish(new NotificationEvent(
                "SMS",
                recipient,
                "SMS",
                "Garage SMS notification",
                message
        ));
    }

    public void notifyEvent(String eventType, String recipient, String channel, String subject, String message) {
        eventPublisher.publish(new NotificationEvent(
                eventType,
                recipient,
                channel,
                subject,
                message
        ));
    }
}
