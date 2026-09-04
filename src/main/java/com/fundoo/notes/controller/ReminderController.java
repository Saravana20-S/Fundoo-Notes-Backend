package com.fundoo.notes.controller;

import com.fundoo.notes.dto.reminder.ReminderRequest;
import com.fundoo.notes.dto.reminder.ReminderResponse;
import com.fundoo.notes.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@Tag(
        name = "Reminders",
        description = "Reminder management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping("/notes/{noteId}")
    @Operation(summary = "Create reminder for a note")
    public ResponseEntity<ReminderResponse> createReminder(
            @PathVariable Long noteId,
            @Valid @RequestBody ReminderRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(201)
                .body(
                        reminderService.createReminder(
                                noteId,
                                request,
                                authentication.getName()
                        )
                );
    }

    @GetMapping
    @Operation(summary = "Get my reminders")
    public ResponseEntity<List<ReminderResponse>> getMyReminders(
            Authentication authentication) {

        return ResponseEntity.ok(
                reminderService.getMyReminders(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reminder by ID")
    public ResponseEntity<ReminderResponse> getReminderById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                reminderService.getReminderById(
                        id,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reminder")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable Long id,
            Authentication authentication) {

        reminderService.deleteReminder(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}