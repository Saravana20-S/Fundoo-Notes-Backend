package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // Existing Phase 2 method
    List<Note> findByUser(User user);

    // Phase 3
    List<Note> findByUserAndTrashedFalse(User user);

    Optional<Note> findByIdAndUser(Long id, User user);

    Optional<Note> findByIdAndUserAndTrashedFalse(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);

    // Phase 4
    @Query("""
            SELECT n
            FROM Note n
            WHERE n.user = :user
              AND n.trashed = false
              AND (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))
              )
              AND (
                    :pinned IS NULL
                    OR n.pinned = :pinned
              )
              AND (
                    :archived IS NULL
                    OR n.archived = :archived
              )
            """)
    Page<Note> searchAndFilter(
            @Param("user") User user,
            @Param("search") String search,
            @Param("pinned") Boolean pinned,
            @Param("archived") Boolean archived,
            Pageable pageable
    );
}