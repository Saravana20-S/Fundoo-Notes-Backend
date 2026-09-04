package com.fundoo.notes.service;

import com.fundoo.notes.dto.attachment.AttachmentResponse;
import com.fundoo.notes.entity.Attachment;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.AttachmentNotFoundException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.exception.UserNotFoundException;
import com.fundoo.notes.repository.AttachmentRepository;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Value("${fundoo.attachments.upload-dir:uploads}")
    private String uploadDirectory;

    public AttachmentResponse uploadAttachment(
            Long noteId,
            MultipartFile file,
            String email) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File cannot be empty"
            );
        }

        User user = getUser(email);

        Note note = noteRepository
                .findByIdAndUser(noteId, user)
                .orElseThrow(() ->
                        new NoteNotFoundException(
                                "Note not found with id: " + noteId
                        ));

        if (note.isTrashed()) {
            throw new IllegalArgumentException(
                    "Cannot attach file to a trashed note"
            );
        }

        try {

            Path uploadPath =
                    Paths.get(uploadDirectory)
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(uploadPath);

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null
                    || originalFileName.isBlank()) {

                throw new IllegalArgumentException(
                        "Invalid file name"
                );
            }

            String storedFileName =
                    System.currentTimeMillis()
                            + "_"
                            + Path.of(originalFileName)
                            .getFileName();

            Path filePath =
                    uploadPath.resolve(storedFileName)
                            .normalize();

            if (!filePath.startsWith(uploadPath)) {
                throw new IllegalArgumentException(
                        "Invalid file path"
                );
            }

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            Attachment attachment =
                    Attachment.builder()
                            .fileName(originalFileName)
                            .fileType(
                                    file.getContentType() != null
                                            ? file.getContentType()
                                            : "application/octet-stream"
                            )
                            .fileSize(file.getSize())
                            .filePath(filePath.toString())
                            .note(note)
                            .user(user)
                            .build();

            try {

                Attachment savedAttachment =
                        attachmentRepository.save(attachment);

                log.info(
                        "Attachment uploaded: id={}, noteId={}, user={}",
                        savedAttachment.getId(),
                        noteId,
                        email
                );

                return mapToResponse(savedAttachment);

            } catch (Exception exception) {

                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException cleanupException) {
                    log.warn(
                            "Failed to cleanup uploaded file: {}",
                            filePath
                    );
                }

                throw exception;
            }

        } catch (IOException exception) {

            log.error(
                    "Failed to upload attachment for noteId={}",
                    noteId,
                    exception
            );

            throw new IllegalArgumentException(
                    "Unable to upload file"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachments(
            Long noteId,
            String email) {

        User user = getUser(email);

        Note note = noteRepository
                .findByIdAndUser(noteId, user)
                .orElseThrow(() ->
                        new NoteNotFoundException(
                                "Note not found with id: " + noteId
                        ));

        return attachmentRepository
                .findByNoteAndUser(note, user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadAttachment(
            Long attachmentId,
            String email) {

        User user = getUser(email);

        Attachment attachment =
                getOwnedAttachment(
                        attachmentId,
                        user
                );

        try {

            Path path =
                    Paths.get(attachment.getFilePath())
                            .toAbsolutePath()
                            .normalize();

            Resource resource =
                    new UrlResource(path.toUri());

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new AttachmentNotFoundException(
                        "Attachment file not found"
                );
            }

            return resource;

        } catch (MalformedURLException exception) {

            throw new AttachmentNotFoundException(
                    "Unable to read attachment"
            );
        }
    }

    public void deleteAttachment(
            Long attachmentId,
            String email) {

        User user = getUser(email);

        Attachment attachment =
                getOwnedAttachment(
                        attachmentId,
                        user
                );

        try {

            Files.deleteIfExists(
                    Paths.get(attachment.getFilePath())
            );

        } catch (IOException exception) {

            log.warn(
                    "Could not delete physical file: {}",
                    attachment.getFilePath()
            );
        }

        attachmentRepository.delete(attachment);

        log.info(
                "Attachment deleted: id={}, user={}",
                attachmentId,
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

    private Attachment getOwnedAttachment(
            Long id,
            User user) {

        return attachmentRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new AttachmentNotFoundException(
                                "Attachment not found with id: " + id
                        ));
    }

    private AttachmentResponse mapToResponse(
            Attachment attachment) {

        return AttachmentResponse.builder()
                .id(attachment.getId())
                .noteId(
                        attachment.getNote().getId()
                )
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .createdDate(
                        attachment.getCreatedDate()
                )
                .build();
    }
}