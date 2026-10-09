package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topology: notifications are published to the {@code garage.events} topic exchange and routed to
 * the notifications queue. Messages the consumer still cannot deliver after its retries are
 * rejected and routed by RabbitMQ to the dead-letter queue, where they can be inspected or replayed.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.messaging.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitMqConfiguration {

    @Bean
    public TopicExchange garageEventsExchange(@Value("${app.messaging.rabbitmq.exchange:garage.events}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public DirectExchange garageDeadLetterExchange(@Value("${app.messaging.rabbitmq.exchange:garage.events}") String exchangeName) {
        return new DirectExchange(exchangeName + ".dlx", true, false);
    }

    @Bean
    public Queue garageNotificationsQueue(@Value("${app.messaging.rabbitmq.queue:garage.notifications}") String queueName,
                                          DirectExchange garageDeadLetterExchange) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(garageDeadLetterExchange.getName())
                .deadLetterRoutingKey(queueName + ".dlq")
                .build();
    }

    @Bean
    public Queue garageNotificationsDeadLetterQueue(@Value("${app.messaging.rabbitmq.queue:garage.notifications}") String queueName) {
        return QueueBuilder.durable(queueName + ".dlq").build();
    }

    @Bean
    public Binding garageNotificationsBinding(Queue garageNotificationsQueue,
                                              TopicExchange garageEventsExchange) {
        return BindingBuilder.bind(garageNotificationsQueue)
                .to(garageEventsExchange)
                .with("garage.#");
    }

    @Bean
    public Binding garageNotificationsDeadLetterBinding(Queue garageNotificationsDeadLetterQueue,
                                                        DirectExchange garageDeadLetterExchange) {
        return BindingBuilder.bind(garageNotificationsDeadLetterQueue)
                .to(garageDeadLetterExchange)
                .with(garageNotificationsDeadLetterQueue.getName());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }
}
