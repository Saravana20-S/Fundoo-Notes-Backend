package com.fundoo.notes.dto.note;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteResponse {

    private Long id;

    private String title;

    private String content;

    private Long userId;

    private boolean pinned;

    private boolean archived;

    private boolean trashed;

    private Set<Long> labelIds;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}