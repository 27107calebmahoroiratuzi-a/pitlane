package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumes notification events from RabbitMQ and delivers them. A delivery that keeps
 * failing (for example, the SMTP server is down) is retried and then dead-lettered.
 */
@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbitmq", name = "enabled", havingValue = "true")
public class NotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationDeliveryService deliveryService;

    public NotificationConsumer(NotificationDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = "${app.messaging.rabbitmq.queue:garage.notifications}")
    public void onNotification(NotificationEvent event) {
        LOGGER.info("Received {} [{}] from RabbitMQ", event.eventType(), event.correlationId());
        deliveryService.deliver(event);
    }
}
