package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    List<Label> findByUser(User user);

    Optional<Label> findByIdAndUser(
            Long id,
            User user
    );

    boolean existsByNameIgnoreCaseAndUser(
            String name,
            User user
    );

    Optional<Label> findByNameIgnoreCaseAndUser(
            String name,
            User user
    );
}