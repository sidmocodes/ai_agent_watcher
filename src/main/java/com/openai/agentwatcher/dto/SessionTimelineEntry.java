package com.openai.agentwatcher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for session timeline visualization
 * Combines thoughts and actions in chronological order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionTimelineEntry {
    private String type; // THOUGHT or ACTION
    private Long id;
    private String timestamp;
    private String content;
    private String status;
    private String metadata;
}
