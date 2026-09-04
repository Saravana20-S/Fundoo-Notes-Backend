package com.fundoo.notes.service;

import com.fundoo.notes.dto.reminder.ReminderRequest;
import com.fundoo.notes.dto.reminder.ReminderResponse;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.Reminder;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.enums.ReminderStatus;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.exception.UserNotFoundException;
import com.fundoo.notes.jms.NotificationProducer;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.ReminderRepository;
import com.fundoo.notes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final NotificationProducer notificationProducer;

    public ReminderResponse createReminder(
            Long noteId,
            ReminderRequest request,
            String email) {

        User user = getUser(email);

        Note note = noteRepository
                .findByIdAndUser(noteId, user)
                .orElseThrow(() ->
                        new NoteNotFoundException(
                                "Note not found with id: " + noteId
                        ));

        if (note.isTrashed()) {
            throw new IllegalArgumentException(
                    "Cannot create reminder for a trashed note"
            );
        }

        Reminder reminder = Reminder.builder()
                .note(note)
                .user(user)
                .reminderTime(request.getReminderTime())
                .status(ReminderStatus.PENDING)
                .build();

        Reminder savedReminder =
                reminderRepository.save(reminder);

        log.info(
                "Reminder created: id={}, noteId={}, user={}",
                savedReminder.getId(),
                noteId,
                email
        );

        return mapToResponse(savedReminder);
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getMyReminders(
            String email) {

        User user = getUser(email);

        return reminderRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReminderResponse getReminderById(
            Long id,
            String email) {

        User user = getUser(email);

        Reminder reminder =
                getOwnedReminder(id, user);

        return mapToResponse(reminder);
    }

    public void deleteReminder(
            Long id,
            String email) {

        User user = getUser(email);

        Reminder reminder =
                getOwnedReminder(id, user);

        reminderRepository.delete(reminder);

        log.info(
                "Reminder deleted: id={}, user={}",
                id,
                email
        );
    }

    /**
     * Runs every 30 seconds.
     *
     * Finds pending reminders whose reminder time
     * has been reached and sends them to JMS.
     */
    @Scheduled(fixedDelay = 30000)
    public void processPendingReminders() {

        LocalDateTime now = LocalDateTime.now();

        List<Reminder> reminders =
                reminderRepository
                        .findByStatusAndReminderTimeLessThanEqual(
                                ReminderStatus.PENDING,
                                now
                        );

        for (Reminder reminder : reminders) {

            notificationProducer.sendNotification(
                    buildNotificationMessage(reminder)
            );

            reminder.setStatus(ReminderStatus.SENT);

            reminderRepository.save(reminder);

            log.info(
                    "Reminder sent to JMS: id={}",
                    reminder.getId()
            );
        }
    }

    private com.fundoo.notes.jms.NotificationMessage
    buildNotificationMessage(Reminder reminder) {

        return com.fundoo.notes.jms.NotificationMessage
                .builder()
                .reminderId(reminder.getId())
                .noteId(reminder.getNote().getId())
                .userId(reminder.getUser().getId())
                .email(reminder.getUser().getEmail())
                .title(reminder.getNote().getTitle())
                .message(
                        "Reminder: " +
                                reminder.getNote().getTitle()
                )
                .build();
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        ));
    }

    private Reminder getOwnedReminder(
            Long id,
            User user) {

        return reminderRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reminder not found with id: " + id
                        ));
    }

    private ReminderResponse mapToResponse(
            Reminder reminder) {

        return ReminderResponse.builder()
                .id(reminder.getId())
                .noteId(reminder.getNote().getId())
                .userId(reminder.getUser().getId())
                .reminderTime(reminder.getReminderTime())
                .status(reminder.getStatus())
                .createdDate(reminder.getCreatedDate())
                .updatedDate(reminder.getUpdatedDate())
                .build();
    }
}