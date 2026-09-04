package com.fundoo.notes.service;

import com.fundoo.notes.dto.attachment.AttachmentResponse;
import com.fundoo.notes.entity.Attachment;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.AttachmentNotFoundException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.AttachmentRepository;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    private AttachmentService attachmentService;

    @TempDir
    Path tempDirectory;

    private User user;
    private Note note;

    @BeforeEach
    void setUp() {

        attachmentService =
                new AttachmentService(
                        attachmentRepository,
                        noteRepository,
                        userRepository
                );

        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
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

        org.springframework.test.util.ReflectionTestUtils.setField(
                attachmentService,
                "uploadDirectory",
                tempDirectory.toString()
        );
    }

    @Test
    void shouldUploadAttachment() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        "Hello Fundoo".getBytes()
                );

        when(userRepository.findByEmail(
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(
                1L,
                user
        )).thenReturn(Optional.of(note));

        when(attachmentRepository.save(
                any(Attachment.class)
        )).thenAnswer(invocation -> {

            Attachment attachment =
                    invocation.getArgument(0);

            attachment.setId(1L);

            return attachment;
        });

        AttachmentResponse response =
                attachmentService.uploadAttachment(
                        1L,
                        file,
                        "john@example.com"
                );

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getNoteId());
        assertEquals("test.txt", response.getFileName());
        assertEquals("text/plain", response.getFileType());

        verify(attachmentRepository).save(
                any(Attachment.class)
        );
    }

    @Test
    void shouldRejectEmptyFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        new byte[0]
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.uploadAttachment(
                        1L,
                        file,
                        "john@example.com"
                )
        );

        verifyNoInteractions(
                userRepository,
                noteRepository,
                attachmentRepository
        );
    }

    @Test
    void shouldThrowWhenNoteNotFound() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        "Hello".getBytes()
                );

        when(userRepository.findByEmail(
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(
                99L,
                user
        )).thenReturn(Optional.empty());

        assertThrows(
                NoteNotFoundException.class,
                () -> attachmentService.uploadAttachment(
                        99L,
                        file,
                        "john@example.com"
                )
        );

        verify(attachmentRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectAttachmentForTrashedNote() {

        note.setTrashed(true);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        "Hello".getBytes()
                );

        when(userRepository.findByEmail(
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(
                1L,
                user
        )).thenReturn(Optional.of(note));

        assertThrows(
                IllegalArgumentException.class,
                () -> attachmentService.uploadAttachment(
                        1L,
                        file,
                        "john@example.com"
                )
        );
    }

    @Test
    void shouldGetAttachments() {

        Attachment attachment =
                Attachment.builder()
                        .id(1L)
                        .fileName("test.txt")
                        .fileType("text/plain")
                        .fileSize(10L)
                        .filePath(
                                tempDirectory
                                        .resolve("test.txt")
                                        .toString()
                        )
                        .note(note)
                        .user(user)
                        .build();

        when(userRepository.findByEmail(
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(
                1L,
                user
        )).thenReturn(Optional.of(note));

        when(attachmentRepository.findByNoteAndUser(
                note,
                user
        )).thenReturn(List.of(attachment));

        List<AttachmentResponse> result =
                attachmentService.getAttachments(
                        1L,
                        "john@example.com"
                );

        assertEquals(1, result.size());
        assertEquals(
                "test.txt",
                result.get(0).getFileName()
        );
    }

    @Test
    void shouldReturnEmptyAttachmentList() {

        when(userRepository.findByEmail(
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser(
                1L,
                user
        )).thenReturn(Optional.of(note));

        when(attachmentRepository.findByNoteAndUser(
                note,
                user
        )).thenReturn(List.of());

        List<AttachmentResponse> result =
                attachmentService.getAttachments(
                        1L,
                        "john@example.com"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDownloadAttachment() throws Exception {

        Path filePath =
                tempDirectory.resolve("download.txt");

        java.nio.file.Files.writeString(
                filePath,
                "Download Test"
        );

        Attachment attachment =
                Attachment.builder()
                        .id(1L)
                        .fileName("download.txt")
                        .fileType("text/plain")
                        .fileSize(13L)
                        .filePath(filePath.toString())
                        .note(note)
                        .user(user)
                        .build();

        when(userRepository.findByEmail(
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(attachmentRepository.findByIdAndUser(
                1L,
                user
        )).thenReturn(Optional.of(attachment));

        Resource resource =
                attachmentService.downloadAttachment(
                        1L,
                        "john@example.com"
                );

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals(
                "download.txt",
                resource.getFilename()
        );
    }

    @Test
    void shouldThrowWhenAttachmentNotFound() {

        when(userRepository.findByEmail(
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(attachmentRepository.findByIdAndUser(
                99L,
                user
        )).thenReturn(Optional.empty());

        assertThrows(
                AttachmentNotFoundException.class,
                () -> attachmentService.downloadAttachment(
                        99L,
                        "john@example.com"
                )
        );
    }

    @Test
    void shouldDeleteAttachment() throws Exception {

        Path filePath =
                tempDirectory.resolve("delete.txt");

        java.nio.file.Files.writeString(
                filePath,
                "Delete Test"
        );

        Attachment attachment =
                Attachment.builder()
                        .id(1L)
                        .fileName("delete.txt")
                        .fileType("text/plain")
                        .fileSize(11L)
                        .filePath(filePath.toString())
                        .note(note)
                        .user(user)
                        .build();

        when(userRepository.findByEmail(
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(attachmentRepository.findByIdAndUser(
                1L,
                user
        )).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(
                1L,
                "john@example.com"
        );

        verify(attachmentRepository)
                .delete(attachment);

        assertFalse(
                java.nio.file.Files.exists(filePath)
        );
    }

    @Test
    void shouldThrowWhenDeletingNonExistingAttachment() {

        when(userRepository.findByEmail(
                "john@example.com"
        )).thenReturn(Optional.of(user));

        when(attachmentRepository.findByIdAndUser(
                99L,
                user
        )).thenReturn(Optional.empty());

        assertThrows(
                AttachmentNotFoundException.class,
                () -> attachmentService.deleteAttachment(
                        99L,
                        "john@example.com"
                )
        );

        verify(attachmentRepository, never())
                .delete(any());
    }
}