package com.fundoo.notes.controller;

import com.fundoo.notes.dto.label.LabelRequest;
import com.fundoo.notes.dto.label.LabelResponse;
import com.fundoo.notes.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
@Tag(
        name = "Labels",
        description = "Label management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    @Operation(summary = "Create label")
    public ResponseEntity<LabelResponse> createLabel(
            @Valid @RequestBody LabelRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(201)
                .body(
                        labelService.createLabel(
                                request,
                                authentication.getName()
                        )
                );
    }

    @GetMapping
    @Operation(summary = "Get all my labels")
    public ResponseEntity<List<LabelResponse>> getMyLabels(
            Authentication authentication) {

        return ResponseEntity.ok(
                labelService.getMyLabels(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get label by ID")
    public ResponseEntity<LabelResponse> getLabelById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                labelService.getLabelById(
                        id,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update label")
    public ResponseEntity<LabelResponse> updateLabel(
            @PathVariable Long id,
            @Valid @RequestBody LabelRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                labelService.updateLabel(
                        id,
                        request,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete label")
    public ResponseEntity<Void> deleteLabel(
            @PathVariable Long id,
            Authentication authentication) {

        labelService.deleteLabel(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}