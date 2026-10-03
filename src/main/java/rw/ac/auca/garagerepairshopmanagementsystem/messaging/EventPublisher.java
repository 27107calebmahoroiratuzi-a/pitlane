package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

public interface EventPublisher {

    void publish(NotificationEvent event);
}
