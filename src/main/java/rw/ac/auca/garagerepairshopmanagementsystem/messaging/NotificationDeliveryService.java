package rw.ac.auca.garagerepairshopmanagementsystem.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Delivers notification events to their channel. Email goes out over SMTP when
 * {@code app.mail.enabled=true}; otherwise the full message is written to the log
 * so local development still shows the invitation link.
 */
@Service
public class NotificationDeliveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean mailEnabled;
    private final String fromAddress;

    public NotificationDeliveryService(ObjectProvider<JavaMailSender> mailSender,
                                       @Value("${app.mail.enabled:false}") boolean mailEnabled,
                                       @Value("${app.mail.from:Pitlane <no-reply@pitlane.local>}") String fromAddress) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    public void deliver(NotificationEvent event) {
        if (!"EMAIL".equalsIgnoreCase(event.channel())) {
            LOGGER.info("No delivery channel configured for {} [{}] to {}; message: {}",
                    event.channel(), event.correlationId(), event.recipient(), event.message());
            return;
        }
        JavaMailSender sender = mailEnabled ? mailSender.getIfAvailable() : null;
        if (sender == null) {
            LOGGER.info("""
                    Email delivery is disabled (set MAIL_ENABLED=true to send). Email [{}]:
                    To: {}
                    Subject: {}

                    {}""", event.correlationId(), event.recipient(), event.subject(), event.message());
            return;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromAddress);
        mail.setTo(event.recipient());
        mail.setSubject(event.subject());
        mail.setText(event.message());
        sender.send(mail);
        LOGGER.info("Sent {} email [{}] to {}", event.eventType(), event.correlationId(), event.recipient());
    }
}
