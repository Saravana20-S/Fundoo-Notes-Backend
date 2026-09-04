package com.fundoo.notes.jms;

import com.fundoo.notes.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final JmsTemplate jmsTemplate;

    public void sendNotification(
            NotificationMessage message) {

        jmsTemplate.convertAndSend(
                JmsConfig.NOTIFICATION_QUEUE,
                message
        );

        log.info(
                "Notification sent to JMS queue: reminderId={}, email={}",
                message.getReminderId(),
                message.getEmail()
        );
    }
}