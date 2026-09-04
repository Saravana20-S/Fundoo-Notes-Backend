package com.fundoo.notes.dto.reminder;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderRequest {

    @NotNull(message = "Reminder time is required")
    @Future(message = "Reminder time must be in the future")
    private LocalDateTime reminderTime;
}