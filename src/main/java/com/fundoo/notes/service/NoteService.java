package com.fundoo.notes.service;

import com.fundoo.notes.dto.label.LabelResponse;
import com.fundoo.notes.dto.note.NoteRequest;
import com.fundoo.notes.dto.note.NoteResponse;
import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.LabelNotFoundException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.exception.UserNotFoundException;
import com.fundoo.notes.repository.LabelRepository;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//phase 4
import com.fundoo.notes.dto.note.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;

    // =========================
    // CREATE
    // =========================

    public NoteResponse createNote(NoteRequest request, String email) {

        User user = getUserByEmail(email);

        Note note = Note.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .user(user)
                .pinned(false)
                .archived(false)
                .trashed(false)
                .build();

        Note savedNote = noteRepository.save(note);

        log.info("Note created. noteId={}, userId={}",
                savedNote.getId(), user.getId());

        return mapToResponse(savedNote);
    }

    // =========================
    // GET ALL NORMAL NOTES
    // =========================

    @Transactional(readOnly = true)
    public List<NoteResponse> getMyNotes(String email) {

        User user = getUserByEmail(email);

        return noteRepository.findByUserAndTrashedFalse(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // GET NOTE BY ID
    // =========================

    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long id, String email) {

        User user = getUserByEmail(email);

        Note note = noteRepository
                .findByIdAndUserAndTrashedFalse(id, user)
                .orElseThrow(() ->
                        new NoteNotFoundException(
                                "Note not found with id: " + id));

        return mapToResponse(note);
    }

    // =========================
    // UPDATE
    // =========================

    public NoteResponse updateNote(
            Long id,
            NoteRequest request,
            String email) {

        User user = getUserByEmail(email);

        Note note = getOwnedNote(id, user);

        if (note.isTrashed()) {
            throw new NoteNotFoundException(
                    "Cannot update a trashed note");
        }

        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent().trim());

        Note updatedNote = noteRepository.save(note);

        log.info("Note updated. noteId={}, userId={}",
                id, user.getId());

        return mapToResponse(updatedNote);
    }

    // =========================
    // DELETE -> TRASH
    // =========================

    public NoteResponse trashNote(Long id, String email) {

        User user = getUserByEmail(email);

        Note note = getOwnedNote(id, user);

        if (note.isTrashed()) {
            throw new NoteNotFoundException(
                    "Note is already in trash");
        }

        note.setTrashed(true);

        // A trashed note should not remain pinned/archived
        note.setPinned(false);
        note.setArchived(false);

        Note savedNote = noteRepository.save(note);

        log.info("Note moved to trash. noteId={}, userId={}",
                id, user.getId());

        return mapToResponse(savedNote);
    }

    // =========================
    // RESTORE
    // =========================

    public NoteResponse restoreNote(Long id, String email) {

        User user = getUserByEmail(email);

        Note note = getOwnedNote(id, user);

        if (!note.isTrashed()) {
            throw new NoteNotFoundException(
                    "Note is not in trash");
        }

        note.setTrashed(false);

        Note restoredNote = noteRepository.save(note);

        log.info("Note restored. noteId={}, userId={}",
                id, user.getId());

        return mapToResponse(restoredNote);
    }

    // =========================
    // PERMANENT DELETE
    // =========================

    public void permanentlyDeleteNote(Long id, String email) {

        User user = getUserByEmail(email);

        Note note = getOwnedNote(id, user);

        noteRepository.delete(note);

        log.info("Note permanently deleted. noteId={}, userId={}",
                id, user.getId());
    }

    // =========================
    // PIN
    // =========================

    public NoteResponse pinNote(Long id, String email) {

        User user = getUserByEmail(email);

        Note note = getOwnedNote(id, user);

        validateNotTrashed(note);

        note.setPinned(true);

        Note savedNote = noteRepository.save(note);

        log.info("Note pinned. noteId={}, userId={}",
                id, user.getId());

        return mapToResponse(savedNote);
    }

    // =========================
    // UNPIN
    // =========================

    public NoteResponse unpinNote(Long id, String email) {

        User user = getUserByEmail(email);

        Note note = getOwnedNote(id, user);

        validateNotTrashed(note);

        note.setPinned(false);

        Note savedNote = noteRepository.save(note);

        log.info("Note unpinned. noteId={}, userId={}",
                id, user.getId());

        return mapToResponse(savedNote);
    }

    // =========================
    // ARCHIVE
    // =========================

    public NoteResponse archiveNote(Long id, String email) {

        User user = getUserByEmail(email);

        Note note = getOwnedNote(id, user);

        validateNotTrashed(note);

        note.setArchived(true);

        Note savedNote = noteRepository.save(note);

        log.info("Note archived. noteId={}, userId={}",
                id, user.getId());

        return mapToResponse(savedNote);
    }

    // =========================
    // UNARCHIVE
    // =========================

    public NoteResponse unarchiveNote(Long id, String email) {

        User user = getUserByEmail(email);

        Note note = getOwnedNote(id, user);

        validateNotTrashed(note);

        note.setArchived(false);

        Note savedNote = noteRepository.save(note);

        log.info("Note unarchived. noteId={}, userId={}",
                id, user.getId());

        return mapToResponse(savedNote);
    }

    // =========================
    // HELPER METHODS
    // =========================

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email));
    }

    private Note getOwnedNote(Long id, User user) {

        return noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new NoteNotFoundException(
                                "Note not found with id: " + id));
    }

    private void validateNotTrashed(Note note) {

        if (note.isTrashed()) {
            throw new NoteNotFoundException(
                    "Cannot modify a trashed note");
        }
    }

    private NoteResponse mapToResponse(Note note) {

        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .userId(note.getUser().getId())
                .pinned(note.isPinned())
                .archived(note.isArchived())
                .trashed(note.isTrashed())
                .labelIds(
                        note.getLabels()
                                .stream()
                                .map(Label::getId)
                                .collect(java.util.stream.Collectors.toSet())
                )
                .createdDate(note.getCreatedDate())
                .updatedDate(note.getUpdatedDate())
                .build();
    }

    public void deleteNote(Long id, String email) {
        trashNote(id, email);
    }



    //Phase 4
    public PageResponse<NoteResponse> searchAndFilterNotes(
            String email,
            String search,
            Boolean pinned,
            Boolean archived,
            int page,
            int size,
            String sortBy,
            String direction) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        ));

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Page size must be greater than 0"
            );
        }

        if (size > 100) {
            throw new IllegalArgumentException(
                    "Page size cannot exceed 100"
            );
        }

        Sort.Direction sortDirection =
                "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<Note> notePage = noteRepository.searchAndFilter(
                user,
                search,
                pinned,
                archived,
                pageable
        );

        List<NoteResponse> responses = notePage
                .getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<NoteResponse>builder()
                .content(responses)
                .page(notePage.getNumber())
                .size(notePage.getSize())
                .totalElements(notePage.getTotalElements())
                .totalPages(notePage.getTotalPages())
                .first(notePage.isFirst())
                .last(notePage.isLast())
                .build();
    }


    //phase 5
    public NoteResponse addLabelToNote(
            Long noteId,
            Long labelId,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        ));

        Note note = noteRepository.findByIdAndUser(
                        noteId,
                        user
                )
                .orElseThrow(() ->
                        new NoteNotFoundException(
                                "Note not found with id: " + noteId
                        ));

        validateNotTrashed(note);

        Label label = labelRepository.findByIdAndUser(
                        labelId,
                        user
                )
                .orElseThrow(() ->
                        new LabelNotFoundException(
                                "Label not found with id: " + labelId
                        ));

        note.getLabels().add(label);

        Note savedNote = noteRepository.save(note);

        return mapToResponse(savedNote);
    }



    public NoteResponse removeLabelFromNote(
            Long noteId,
            Long labelId,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        ));

        Note note = noteRepository.findByIdAndUser(
                        noteId,
                        user
                )
                .orElseThrow(() ->
                        new NoteNotFoundException(
                                "Note not found with id: " + noteId
                        ));

        validateNotTrashed(note);

        Label label = labelRepository.findByIdAndUser(
                        labelId,
                        user
                )
                .orElseThrow(() ->
                        new LabelNotFoundException(
                                "Label not found with id: " + labelId
                        ));

        note.getLabels().remove(label);

        Note savedNote = noteRepository.save(note);

        return mapToResponse(savedNote);
    }


    @Transactional(readOnly = true)
    public List<LabelResponse> getLabelsForNote(
            Long noteId,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        ));

        Note note = noteRepository.findByIdAndUser(
                        noteId,
                        user
                )
                .orElseThrow(() ->
                        new NoteNotFoundException(
                                "Note not found with id: " + noteId
                        ));

        validateNotTrashed(note);

        return note.getLabels()
                .stream()
                .map(label -> LabelResponse.builder()
                        .id(label.getId())
                        .name(label.getName())
                        .userId(label.getUser().getId())
                        .createdDate(label.getCreatedDate())
                        .updatedDate(label.getUpdatedDate())
                        .build())
                .toList();
    }
}