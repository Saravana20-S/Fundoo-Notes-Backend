package com.fundoo.notes.jms;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage implements Serializable {

    private Long reminderId;

    private Long noteId;

    private Long userId;

    private String email;

    private String title;

    private String message;
}