package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final EventPublisher eventPublisher;

    public NotificationService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notifyEmail(String recipient, String subject, String message) {
        notifyEmail("EMAIL", recipient, subject, message);
    }

    public void notifyEmail(String eventType, String recipient, String subject, String message) {
        publish(new NotificationEvent(eventType, recipient, "EMAIL", subject, message));
    }

    public void notifySms(String recipient, String message) {
        publish(new NotificationEvent(
                "SMS",
                recipient,
                "SMS",
                "Garage SMS notification",
                message
        ));
    }

    public void notifyEvent(String eventType, String recipient, String channel, String subject, String message) {
        publish(new NotificationEvent(
                eventType,
                recipient,
                channel,
                subject,
                message
        ));
    }

    /**
     * Inside a transaction the event is published only after commit, so a rolled-back
     * invitation never produces an email. Publishing failures are logged rather than
     * failing a request whose data is already committed.
     */
    private void publish(NotificationEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            eventPublisher.publish(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    eventPublisher.publish(event);
                } catch (RuntimeException exception) {
                    LOGGER.error("Could not publish {} notification [{}] for {}",
                            event.eventType(), event.correlationId(), event.recipient(), exception);
                }
            }
        });
    }
}
