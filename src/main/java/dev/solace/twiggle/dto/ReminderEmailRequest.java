package dev.solace.twiggle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for sending reminder emails.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReminderEmailRequest {
    private String plantName;
    private String reminderType;
    private String reminderDate; // Date in format that can be parsed as a Date object
    private String reminderTime; // Added for time component
    private String notes;
    private String userEmail;
    private String imageUrl; // Added for plant image
    private String gardenSpaceName; // Added for garden space name
    private String gardenSpaceId; // Added for linking back to garden space
}
