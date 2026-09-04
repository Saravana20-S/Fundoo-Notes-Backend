package com.fundoo.notes.controller;

import com.fundoo.notes.dto.attachment.AttachmentResponse;
import com.fundoo.notes.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Tag(
        name = "Attachments",
        description = "Note attachment APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(
            value = "/notes/{noteId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Upload attachment to a note")
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable Long noteId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        return ResponseEntity
                .status(201)
                .body(
                        attachmentService.uploadAttachment(
                                noteId,
                                file,
                                authentication.getName()
                        )
                );
    }

    @GetMapping("/notes/{noteId}")
    @Operation(summary = "Get attachments for a note")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(
            @PathVariable Long noteId,
            Authentication authentication) {

        return ResponseEntity.ok(
                attachmentService.getAttachments(
                        noteId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Download attachment")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long attachmentId,
            Authentication authentication) {

        Resource resource =
                attachmentService.downloadAttachment(
                        attachmentId,
                        authentication.getName()
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                resource.getFilename() +
                                "\""
                )
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Delete attachment")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId,
            Authentication authentication) {

        attachmentService.deleteAttachment(
                attachmentId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}