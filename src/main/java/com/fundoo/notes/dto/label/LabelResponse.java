package com.fundoo.notes.dto.label;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelResponse {

    private Long id;

    private String name;

    private Long userId;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}