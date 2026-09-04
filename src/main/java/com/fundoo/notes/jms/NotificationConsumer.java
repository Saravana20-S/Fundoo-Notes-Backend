package com.fundoo.notes.jms;

import com.fundoo.notes.config.JmsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @JmsListener(
            destination = JmsConfig.NOTIFICATION_QUEUE
    )
    public void receiveNotification(
            NotificationMessage message) {

        log.info(
                "Notification received: reminderId={}, email={}, message={}",
                message.getReminderId(),
                message.getEmail(),
                message.getMessage()
        );

        // Later this can be replaced with:
        // Email notification
        // SMS notification
        // Push notification

        log.info(
                "Notification processed successfully for {}",
                message.getEmail()
        );
    }
}