package com.fundoo.notes.dto.reminder;

import com.fundoo.notes.enums.ReminderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderResponse {

    private Long id;

    private Long noteId;

    private Long userId;

    private LocalDateTime reminderTime;

    private ReminderStatus status;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}