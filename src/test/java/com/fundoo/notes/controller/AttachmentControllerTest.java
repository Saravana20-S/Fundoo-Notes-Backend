package com.fundoo.notes.controller;

import com.fundoo.notes.dto.attachment.AttachmentResponse;
import com.fundoo.notes.service.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentControllerTest {

    @Mock
    private AttachmentService attachmentService;

    private AttachmentController attachmentController;

    private Authentication authentication;

    @BeforeEach
    void setUp() {

        attachmentController =
                new AttachmentController(
                        attachmentService
                );

        authentication =
                new TestingAuthenticationToken(
                        "john@example.com",
                        null
                );
    }

    @Test
    void shouldUploadAttachment() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        "Hello".getBytes()
                );

        AttachmentResponse response =
                AttachmentResponse.builder()
                        .id(1L)
                        .noteId(1L)
                        .fileName("test.txt")
                        .fileType("text/plain")
                        .fileSize(5L)
                        .build();

        when(attachmentService.uploadAttachment(
                1L,
                file,
                "john@example.com"
        )).thenReturn(response);

        ResponseEntity<AttachmentResponse> result =
                attachmentController.uploadAttachment(
                        1L,
                        file,
                        authentication
                );

        assertEquals(201, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(
                "test.txt",
                result.getBody().getFileName()
        );
    }

    @Test
    void shouldGetAttachments() {

        AttachmentResponse response =
                AttachmentResponse.builder()
                        .id(1L)
                        .noteId(1L)
                        .fileName("test.txt")
                        .fileType("text/plain")
                        .fileSize(5L)
                        .build();

        when(attachmentService.getAttachments(
                1L,
                "john@example.com"
        )).thenReturn(List.of(response));

        ResponseEntity<List<AttachmentResponse>> result =
                attachmentController.getAttachments(
                        1L,
                        authentication
                );

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void shouldDownloadAttachment() {

        Resource resource =
                new ByteArrayResource(
                        "Hello".getBytes()
                );

        when(attachmentService.downloadAttachment(
                1L,
                "john@example.com"
        )).thenReturn(resource);

        ResponseEntity<Resource> result =
                attachmentController.downloadAttachment(
                        1L,
                        authentication
                );

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());

        verify(attachmentService)
                .downloadAttachment(
                        1L,
                        "john@example.com"
                );
    }

    @Test
    void shouldDeleteAttachment() {

        doNothing().when(attachmentService)
                .deleteAttachment(
                        1L,
                        "john@example.com"
                );

        ResponseEntity<Void> result =
                attachmentController.deleteAttachment(
                        1L,
                        authentication
                );

        assertEquals(204, result.getStatusCode().value());

        verify(attachmentService)
                .deleteAttachment(
                        1L,
                        "john@example.com"
                );
    }
}