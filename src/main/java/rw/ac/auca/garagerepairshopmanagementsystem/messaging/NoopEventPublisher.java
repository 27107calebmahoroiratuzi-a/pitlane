package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbitmq", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopEventPublisher implements EventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoopEventPublisher.class);

    @Override
    public void publish(NotificationEvent event) {
        LOGGER.info("RabbitMQ is disabled; event {} for {} was dropped in-memory.", event.eventType(), event.recipient());
    }
}
