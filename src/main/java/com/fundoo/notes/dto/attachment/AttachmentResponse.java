package com.fundoo.notes.dto.attachment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {

    private Long id;

    private Long noteId;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private LocalDateTime createdDate;
}