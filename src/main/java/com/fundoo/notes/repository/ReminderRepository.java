package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Reminder;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository
        extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUser(User user);

    Optional<Reminder> findByIdAndUser(
            Long id,
            User user
    );

    List<Reminder> findByStatusAndReminderTimeLessThanEqual(
            ReminderStatus status,
            LocalDateTime time
    );
}