package com.fundoo.notes.controller;

import com.fundoo.notes.dto.label.LabelResponse;
import com.fundoo.notes.dto.note.NoteRequest;
import com.fundoo.notes.dto.note.NoteResponse;
import com.fundoo.notes.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

//phase 4
import com.fundoo.notes.dto.note.PageResponse;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Notes APIs")
@SecurityRequirement(name = "bearerAuth")
public class NoteController {

    private final NoteService noteService;

    // =========================
    // CREATE
    // =========================

    @PostMapping
    @Operation(summary = "Create note")
    public ResponseEntity<NoteResponse> createNote(
            @Valid @RequestBody NoteRequest request,
            Authentication authentication) {

        return ResponseEntity.status(201)
                .body(noteService.createNote(
                        request,
                        authentication.getName()));
    }

    // =========================
    // GET ALL
    // =========================

    @GetMapping
    @Operation(summary = "Get my notes")
    public ResponseEntity<List<NoteResponse>> getMyNotes(
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.getMyNotes(
                        authentication.getName()));
    }

    // =========================
    // GET BY ID
    // =========================

    @GetMapping("/{id}")
    @Operation(summary = "Get note by ID")
    public ResponseEntity<NoteResponse> getNoteById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.getNoteById(
                        id,
                        authentication.getName()));
    }

    // =========================
    // UPDATE
    // =========================

    @PutMapping("/{id}")
    @Operation(summary = "Update note")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.updateNote(
                        id,
                        request,
                        authentication.getName()));
    }

    // =========================
    // PIN
    // =========================

    @PostMapping("/{id}/pin")
    @Operation(summary = "Pin note")
    public ResponseEntity<NoteResponse> pinNote(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.pinNote(
                        id,
                        authentication.getName()));
    }

    // =========================
    // UNPIN
    // =========================

    @PostMapping("/{id}/unpin")
    @Operation(summary = "Unpin note")
    public ResponseEntity<NoteResponse> unpinNote(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.unpinNote(
                        id,
                        authentication.getName()));
    }

    // =========================
    // ARCHIVE
    // =========================

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive note")
    public ResponseEntity<NoteResponse> archiveNote(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.archiveNote(
                        id,
                        authentication.getName()));
    }

    // =========================
    // UNARCHIVE
    // =========================

    @PostMapping("/{id}/unarchive")
    @Operation(summary = "Unarchive note")
    public ResponseEntity<NoteResponse> unarchiveNote(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.unarchiveNote(
                        id,
                        authentication.getName()));
    }

    // =========================
    // TRASH
    // =========================

    @PostMapping("/{id}/trash")
    @Operation(summary = "Move note to trash")
    public ResponseEntity<NoteResponse> trashNote(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.trashNote(
                        id,
                        authentication.getName()));
    }

    // =========================
    // RESTORE
    // =========================

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore note from trash")
    public ResponseEntity<NoteResponse> restoreNote(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.restoreNote(
                        id,
                        authentication.getName()));
    }

    // =========================
    // PERMANENT DELETE
    // =========================

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete note")
    public ResponseEntity<Void> permanentlyDeleteNote(
            @PathVariable Long id,
            Authentication authentication) {

        noteService.permanentlyDeleteNote(
                id,
                authentication.getName());

        return ResponseEntity.noContent().build();
    }

    // =========================
    // PHYSICAL DELETE FROM PHASE 2
    // =========================

    @DeleteMapping("/{id}")
    @Operation(summary = "Move note to trash")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long id,
            Authentication authentication) {

        noteService.deleteNote(
                id,
                authentication.getName());

        return ResponseEntity.noContent().build();
    }



    //phase 4
    @GetMapping("/search")
    @Operation(summary = "Search, filter and paginate notes")
    public ResponseEntity<PageResponse<NoteResponse>> searchNotes(
            Authentication authentication,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Boolean pinned,

            @RequestParam(required = false)
            Boolean archived,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "createdDate")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction) {

        return ResponseEntity.ok(
                noteService.searchAndFilterNotes(
                        authentication.getName(),
                        search,
                        pinned,
                        archived,
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }

    //phase 5
    @PostMapping("/{noteId}/labels/{labelId}")
    @Operation(summary = "Add label to note")
    public ResponseEntity<NoteResponse> addLabelToNote(
            @PathVariable Long noteId,
            @PathVariable Long labelId,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.addLabelToNote(
                        noteId,
                        labelId,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{noteId}/labels/{labelId}")
    @Operation(summary = "Remove label from note")
    public ResponseEntity<NoteResponse> removeLabelFromNote(
            @PathVariable Long noteId,
            @PathVariable Long labelId,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.removeLabelFromNote(
                        noteId,
                        labelId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{noteId}/labels")
    @Operation(summary = "Get labels assigned to note")
    public ResponseEntity<List<LabelResponse>> getLabelsForNote(
            @PathVariable Long noteId,
            Authentication authentication) {

        return ResponseEntity.ok(
                noteService.getLabelsForNote(
                        noteId,
                        authentication.getName()
                )
        );
    }
}