package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Attachment;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository
        extends JpaRepository<Attachment, Long> {

    List<Attachment> findByNoteAndUser(
            Note note,
            User user
    );

    Optional<Attachment> findByIdAndUser(
            Long id,
            User user
    );

    boolean existsByIdAndUser(
            Long id,
            User user
    );
}