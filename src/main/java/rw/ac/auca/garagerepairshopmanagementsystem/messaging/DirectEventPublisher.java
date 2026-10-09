package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Used when RabbitMQ is disabled: delivers each event in-process instead of queueing it,
 * so invitation emails still go out (or are logged) during local development.
 */
@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbitmq", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DirectEventPublisher implements EventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectEventPublisher.class);

    private final NotificationDeliveryService deliveryService;

    public DirectEventPublisher(NotificationDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Override
    public void publish(NotificationEvent event) {
        LOGGER.info("RabbitMQ is disabled; delivering {} [{}] directly", event.eventType(), event.correlationId());
        deliveryService.deliver(event);
    }
}
