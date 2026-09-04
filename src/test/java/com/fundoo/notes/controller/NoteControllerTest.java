package com.fundoo.notes.controller;

import com.fundoo.notes.dto.note.NoteRequest;
import com.fundoo.notes.dto.note.NoteResponse;
import com.fundoo.notes.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteControllerTest {

    @Mock
    private NoteService noteService;

    @Mock
    private Authentication authentication;

    private NoteController noteController;

    private NoteRequest noteRequest;
    private NoteResponse noteResponse;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        noteController =
                new NoteController(noteService);

        noteRequest = NoteRequest.builder()
                .title("Test Note")
                .content("Test Content")
                .build();

        noteResponse = NoteResponse.builder()
                .id(1L)
                .title("Test Note")
                .content("Test Content")
                .userId(1L)
                .pinned(false)
                .archived(false)
                .trashed(false)
                .build();

        when(authentication.getName())
                .thenReturn("john@example.com");
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void shouldCreateNote() {

        when(noteService.createNote(
                noteRequest,
                "john@example.com"
        )).thenReturn(noteResponse);

        ResponseEntity<NoteResponse> response =
                noteController.createNote(
                        noteRequest,
                        authentication
                );

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(1L, response.getBody().getId());
        assertEquals(
                "Test Note",
                response.getBody().getTitle()
        );

        verify(noteService)
                .createNote(
                        noteRequest,
                        "john@example.com"
                );
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    void shouldGetMyNotes() {

        List<NoteResponse> notes =
                List.of(noteResponse);

        when(noteService.getMyNotes(
                "john@example.com"
        )).thenReturn(notes);

        ResponseEntity<List<NoteResponse>> response =
                noteController.getMyNotes(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(noteService)
                .getMyNotes("john@example.com");
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Test
    void shouldGetNoteById() {

        when(noteService.getNoteById(
                1L,
                "john@example.com"
        )).thenReturn(noteResponse);

        ResponseEntity<NoteResponse> response =
                noteController.getNoteById(
                        1L,
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(noteService)
                .getNoteById(
                        1L,
                        "john@example.com"
                );
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void shouldUpdateNote() {

        when(noteService.updateNote(
                1L,
                noteRequest,
                "john@example.com"
        )).thenReturn(noteResponse);

        ResponseEntity<NoteResponse> response =
                noteController.updateNote(
                        1L,
                        noteRequest,
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(noteService)
                .updateNote(
                        1L,
                        noteRequest,
                        "john@example.com"
                );
    }

    // =========================================================
    // DELETE -> TRASH
    // =========================================================

    @Test
    void shouldDeleteNote() {

        doNothing().when(noteService)
                .deleteNote(
                        1L,
                        "john@example.com"
                );

        ResponseEntity<Void> response =
                noteController.deleteNote(
                        1L,
                        authentication
                );

        assertEquals(204, response.getStatusCode().value());

        verify(noteService)
                .deleteNote(
                        1L,
                        "john@example.com"
                );
    }

    // =========================================================
    // PIN
    // =========================================================

    @Test
    void shouldPinNote() {

        NoteResponse responseBody =
                NoteResponse.builder()
                        .id(1L)
                        .title("Test Note")
                        .content("Test Content")
                        .userId(1L)
                        .pinned(true)
                        .archived(false)
                        .trashed(false)
                        .build();

        when(noteService.pinNote(
                1L,
                "john@example.com"
        )).thenReturn(responseBody);

        ResponseEntity<NoteResponse> response =
                noteController.pinNote(
                        1L,
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isPinned());

        verify(noteService)
                .pinNote(
                        1L,
                        "john@example.com"
                );
    }

    // =========================================================
    // UNPIN
    // =========================================================

    @Test
    void shouldUnpinNote() {

        when(noteService.unpinNote(
                1L,
                "john@example.com"
        )).thenReturn(noteResponse);

        ResponseEntity<NoteResponse> response =
                noteController.unpinNote(
                        1L,
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());

        verify(noteService)
                .unpinNote(
                        1L,
                        "john@example.com"
                );
    }

    // =========================================================
    // ARCHIVE
    // =========================================================

    @Test
    void shouldArchiveNote() {

        NoteResponse responseBody =
                NoteResponse.builder()
                        .id(1L)
                        .title("Test Note")
                        .content("Test Content")
                        .userId(1L)
                        .pinned(false)
                        .archived(true)
                        .trashed(false)
                        .build();

        when(noteService.archiveNote(
                1L,
                "john@example.com"
        )).thenReturn(responseBody);

        ResponseEntity<NoteResponse> response =
                noteController.archiveNote(
                        1L,
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isArchived());

        verify(noteService)
                .archiveNote(
                        1L,
                        "john@example.com"
                );
    }

    // =========================================================
    // UNARCHIVE
    // =========================================================

    @Test
    void shouldUnarchiveNote() {

        when(noteService.unarchiveNote(
                1L,
                "john@example.com"
        )).thenReturn(noteResponse);

        ResponseEntity<NoteResponse> response =
                noteController.unarchiveNote(
                        1L,
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());

        verify(noteService)
                .unarchiveNote(
                        1L,
                        "john@example.com"
                );
    }

    // =========================================================
    // TRASH
    // =========================================================

    @Test
    void shouldTrashNote() {

        NoteResponse responseBody =
                NoteResponse.builder()
                        .id(1L)
                        .title("Test Note")
                        .content("Test Content")
                        .userId(1L)
                        .pinned(false)
                        .archived(false)
                        .trashed(true)
                        .build();

        when(noteService.trashNote(
                1L,
                "john@example.com"
        )).thenReturn(responseBody);

        ResponseEntity<NoteResponse> response =
                noteController.trashNote(
                        1L,
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isTrashed());

        verify(noteService)
                .trashNote(
                        1L,
                        "john@example.com"
                );
    }

    // =========================================================
    // RESTORE
    // =========================================================

    @Test
    void shouldRestoreNote() {

        when(noteService.restoreNote(
                1L,
                "john@example.com"
        )).thenReturn(noteResponse);

        ResponseEntity<NoteResponse> response =
                noteController.restoreNote(
                        1L,
                        authentication
                );

        assertEquals(200, response.getStatusCode().value());

        verify(noteService)
                .restoreNote(
                        1L,
                        "john@example.com"
                );
    }

    // =========================================================
    // PERMANENT DELETE
    // =========================================================

    @Test
    void shouldPermanentlyDeleteNote() {

        doNothing().when(noteService)
                .permanentlyDeleteNote(
                        1L,
                        "john@example.com"
                );

        ResponseEntity<Void> response =
                noteController.permanentlyDeleteNote(
                        1L,
                        authentication
                );

        assertEquals(204, response.getStatusCode().value());

        verify(noteService)
                .permanentlyDeleteNote(
                        1L,
                        "john@example.com"
                );
    }
}