package com.fundoo.notes.service;

import com.fundoo.notes.dto.label.LabelRequest;
import com.fundoo.notes.dto.label.LabelResponse;
import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.DuplicateLabelException;
import com.fundoo.notes.exception.LabelNotFoundException;
import com.fundoo.notes.exception.UserNotFoundException;
import com.fundoo.notes.repository.LabelRepository;
import com.fundoo.notes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LabelService {

    private final LabelRepository labelRepository;

    private final UserRepository userRepository;

    public LabelResponse createLabel(
            LabelRequest request,
            String email) {

        User user = getUser(email);

        String labelName = request.getName().trim();

        if (labelRepository.existsByNameIgnoreCaseAndUser(
                labelName,
                user)) {

            throw new DuplicateLabelException(
                    "Label already exists: " + labelName
            );
        }

        Label label = Label.builder()
                .name(labelName)
                .user(user)
                .build();

        Label savedLabel = labelRepository.save(label);

        log.info(
                "Label created successfully: id={}, user={}",
                savedLabel.getId(),
                email
        );

        return mapToResponse(savedLabel);
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> getMyLabels(String email) {

        User user = getUser(email);

        return labelRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LabelResponse getLabelById(
            Long id,
            String email) {

        User user = getUser(email);

        Label label = getOwnedLabel(id, user);

        return mapToResponse(label);
    }

    public LabelResponse updateLabel(
            Long id,
            LabelRequest request,
            String email) {

        User user = getUser(email);

        Label label = getOwnedLabel(id, user);

        String newName = request.getName().trim();

        if (!label.getName().equalsIgnoreCase(newName)
                && labelRepository.existsByNameIgnoreCaseAndUser(
                newName,
                user)) {

            throw new DuplicateLabelException(
                    "Label already exists: " + newName
            );
        }

        label.setName(newName);

        Label updatedLabel = labelRepository.save(label);

        return mapToResponse(updatedLabel);
    }

    public void deleteLabel(
            Long id,
            String email) {

        User user = getUser(email);

        Label label = getOwnedLabel(id, user);

        label.getNotes().forEach(
                note -> note.getLabels().remove(label)
        );

        labelRepository.delete(label);

        log.info(
                "Label deleted successfully: id={}, user={}",
                id,
                email
        );
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        ));
    }

    private Label getOwnedLabel(
            Long id,
            User user) {

        return labelRepository.findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new LabelNotFoundException(
                                "Label not found with id: " + id
                        ));
    }

    private LabelResponse mapToResponse(Label label) {

        return LabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .userId(label.getUser().getId())
                .createdDate(label.getCreatedDate())
                .updatedDate(label.getUpdatedDate())
                .build();
    }
}