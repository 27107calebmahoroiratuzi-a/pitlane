package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitEventPublisher implements EventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate,
                               @Value("${app.messaging.rabbitmq.exchange:garage.events}") String exchangeName,
                               @Value("${app.messaging.rabbitmq.routing-key:garage.notifications}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @Override
    public void publish(NotificationEvent event) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
        LOGGER.info("Published message to RabbitMQ: {} [{}]", event.eventType(), event.correlationId());
    }
}
