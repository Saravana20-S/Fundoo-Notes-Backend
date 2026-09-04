package com.fundoo.notes.service;

import com.fundoo.notes.dto.note.NoteRequest;
import com.fundoo.notes.dto.note.NoteResponse;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    private User user;
    private Note note;
    private Note note2;
    private NoteRequest noteRequest;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .mobile("9876543210")
                .build();

        note = Note.builder()
                .id(1L)
                .title("Test Note")
                .content("Test Content")
                .user(user)
                .pinned(false)
                .archived(false)
                .trashed(false)
                .build();

        note2 = Note.builder()
                .id(2L)
                .title("Second Note")
                .content("Second Content")
                .user(user)
                .pinned(false)
                .archived(false)
                .trashed(false)
                .build();

        noteRequest = NoteRequest.builder()
                .title("Test Note")
                .content("Test Content")
                .build();
    }

    // =========================================================
    // CREATE NOTE
    // =========================================================

    @Test
    void shouldCreateNote() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.save(any(Note.class)))
                .thenReturn(note);

        NoteResponse response =
                noteService.createNote(
                        noteRequest,
                        "john@example.com"
                );

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Note", response.getTitle());
        assertEquals("Test Content", response.getContent());
        assertEquals(1L, response.getUserId());

        assertFalse(response.isPinned());
        assertFalse(response.isArchived());
        assertFalse(response.isTrashed());

        verify(userRepository)
                .findByEmail("john@example.com");

        verify(noteRepository)
                .save(any(Note.class));
    }

    // =========================================================
    // GET MY NOTES
    // =========================================================

    @Test
    void shouldGetMyNotes() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByUserAndTrashedFalse(user))
                .thenReturn(List.of(note, note2));

        List<NoteResponse> result =
                noteService.getMyNotes("john@example.com");

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(userRepository)
                .findByEmail("john@example.com");

        verify(noteRepository)
                .findByUserAndTrashedFalse(user);
    }

    // =========================================================
    // GET NOTE BY ID
    // =========================================================

    @Test
    void shouldGetNoteById() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository
                .findByIdAndUserAndTrashedFalse(1L, user))
                .thenReturn(Optional.of(note));

        NoteResponse response =
                noteService.getNoteById(
                        1L,
                        "john@example.com"
                );

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Note", response.getTitle());
        assertEquals("Test Content", response.getContent());
        assertEquals(1L, response.getUserId());

        verify(noteRepository)
                .findByIdAndUserAndTrashedFalse(1L, user);
    }

    // =========================================================
    // GET NOTE - NOT FOUND
    // =========================================================

    @Test
    void shouldThrowExceptionWhenNoteNotFound() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository
                .findByIdAndUserAndTrashedFalse(1L, user))
                .thenReturn(Optional.empty());

        assertThrows(
                NoteNotFoundException.class,
                () -> noteService.getNoteById(
                        1L,
                        "john@example.com"
                )
        );

        verify(noteRepository)
                .findByIdAndUserAndTrashedFalse(1L, user);
    }

    // =========================================================
    // UPDATE NOTE
    // =========================================================

    @Test
    void shouldUpdateNote() {

        NoteRequest updateRequest = NoteRequest.builder()
                .title("Updated Note")
                .content("Updated Content")
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(note))
                .thenReturn(note);

        NoteResponse response =
                noteService.updateNote(
                        1L,
                        updateRequest,
                        "john@example.com"
                );

        assertNotNull(response);

        assertEquals("Updated Note", note.getTitle());
        assertEquals("Updated Content", note.getContent());

        verify(noteRepository)
                .findByIdAndUser(1L, user);

        verify(noteRepository)
                .save(note);
    }

    // =========================================================
    // DELETE NOTE -> TRASH
    // =========================================================

    @Test
    void shouldDeleteNote() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(note))
                .thenReturn(note);

        noteService.deleteNote(
                1L,
                "john@example.com"
        );

        assertTrue(note.isTrashed());
        assertFalse(note.isPinned());
        assertFalse(note.isArchived());

        verify(noteRepository)
                .findByIdAndUser(1L, user);

        verify(noteRepository)
                .save(note);
    }

    // =========================================================
    // PIN
    // =========================================================

    @Test
    void shouldPinNote() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(note))
                .thenReturn(note);

        NoteResponse response =
                noteService.pinNote(
                        1L,
                        "john@example.com"
                );

        assertTrue(note.isPinned());
        assertTrue(response.isPinned());

        verify(noteRepository)
                .save(note);
    }

    // =========================================================
    // UNPIN
    // =========================================================

    @Test
    void shouldUnpinNote() {

        note.setPinned(true);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(note))
                .thenReturn(note);

        NoteResponse response =
                noteService.unpinNote(
                        1L,
                        "john@example.com"
                );

        assertFalse(note.isPinned());
        assertFalse(response.isPinned());

        verify(noteRepository)
                .save(note);
    }

    // =========================================================
    // ARCHIVE
    // =========================================================

    @Test
    void shouldArchiveNote() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(note))
                .thenReturn(note);

        NoteResponse response =
                noteService.archiveNote(
                        1L,
                        "john@example.com"
                );

        assertTrue(note.isArchived());
        assertTrue(response.isArchived());

        verify(noteRepository)
                .save(note);
    }

    // =========================================================
    // UNARCHIVE
    // =========================================================

    @Test
    void shouldUnarchiveNote() {

        note.setArchived(true);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(note))
                .thenReturn(note);

        NoteResponse response =
                noteService.unarchiveNote(
                        1L,
                        "john@example.com"
                );

        assertFalse(note.isArchived());
        assertFalse(response.isArchived());

        verify(noteRepository)
                .save(note);
    }

    // =========================================================
    // TRASH
    // =========================================================

    @Test
    void shouldTrashNote() {

        note.setPinned(true);
        note.setArchived(true);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(note))
                .thenReturn(note);

        NoteResponse response =
                noteService.trashNote(
                        1L,
                        "john@example.com"
                );

        assertTrue(note.isTrashed());

        // Trashed note should no longer be pinned/archived
        assertFalse(note.isPinned());
        assertFalse(note.isArchived());

        assertTrue(response.isTrashed());

        verify(noteRepository)
                .save(note);
    }

    // =========================================================
    // RESTORE
    // =========================================================

    @Test
    void shouldRestoreNote() {

        note.setTrashed(true);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(note))
                .thenReturn(note);

        NoteResponse response =
                noteService.restoreNote(
                        1L,
                        "john@example.com"
                );

        assertFalse(note.isTrashed());
        assertFalse(response.isTrashed());

        verify(noteRepository)
                .save(note);
    }

    // =========================================================
    // PERMANENT DELETE
    // =========================================================

    @Test
    void shouldPermanentlyDeleteNote() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        noteService.permanentlyDeleteNote(
                1L,
                "john@example.com"
        );

        verify(noteRepository)
                .findByIdAndUser(1L, user);

        verify(noteRepository)
                .delete(note);
    }

    // =========================================================
    // PIN - NOT FOUND
    // =========================================================

    @Test
    void shouldThrowExceptionWhenPinningMissingNote() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.empty());

        assertThrows(
                NoteNotFoundException.class,
                () -> noteService.pinNote(
                        1L,
                        "john@example.com"
                )
        );

        verify(noteRepository)
                .findByIdAndUser(1L, user);

        verify(noteRepository, never())
                .save(any(Note.class));
    }

    // =========================================================
    // TRASH - ALREADY TRASHED
    // =========================================================

    @Test
    void shouldThrowExceptionWhenAlreadyTrashed() {

        note.setTrashed(true);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        assertThrows(
                NoteNotFoundException.class,
                () -> noteService.trashNote(
                        1L,
                        "john@example.com"
                )
        );

        verify(noteRepository, never())
                .save(any(Note.class));
    }

    // =========================================================
    // RESTORE - NOT IN TRASH
    // =========================================================

    @Test
    void shouldThrowExceptionWhenRestoringActiveNote() {

        note.setTrashed(false);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        assertThrows(
                NoteNotFoundException.class,
                () -> noteService.restoreNote(
                        1L,
                        "john@example.com"
                )
        );

        verify(noteRepository, never())
                .save(any(Note.class));
    }

    // =========================================================
    // PIN TRASHED NOTE
    // =========================================================

    @Test
    void shouldNotPinTrashedNote() {

        note.setTrashed(true);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        assertThrows(
                NoteNotFoundException.class,
                () -> noteService.pinNote(
                        1L,
                        "john@example.com"
                )
        );

        verify(noteRepository, never())
                .save(any(Note.class));
    }

    // =========================================================
    // ARCHIVE TRASHED NOTE
    // =========================================================

    @Test
    void shouldNotArchiveTrashedNote() {

        note.setTrashed(true);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(note));

        assertThrows(
                NoteNotFoundException.class,
                () -> noteService.archiveNote(
                        1L,
                        "john@example.com"
                )
        );

        verify(noteRepository, never())
                .save(any(Note.class));
    }
}