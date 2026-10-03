package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.messaging.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitMqConfiguration {

    @Bean
    public TopicExchange garageEventsExchange(@Value("${app.messaging.rabbitmq.exchange:garage.events}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue garageNotificationsQueue(@Value("${app.messaging.rabbitmq.routing-key:garage.notifications}") String routingKey) {
        return new Queue(routingKey, true);
    }

    @Bean
    public Binding garageNotificationsBinding(Queue garageNotificationsQueue,
                                            TopicExchange garageEventsExchange) {
        return BindingBuilder.bind(garageNotificationsQueue)
                .to(garageEventsExchange)
                .with("garage.#");
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
