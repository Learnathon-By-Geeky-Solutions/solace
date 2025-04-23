package dev.solace.twiggle.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for sending reminder emails.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderEmailRequest {

    @NotBlank(message = "Plant name is required")
    @Size(max = 255, message = "Plant name must be less than 255 characters")
    private String plantName;

    @NotBlank(message = "Reminder type is required")
    @Size(max = 50, message = "Reminder type must be less than 50 characters")
    private String reminderType;

    @NotBlank(message = "Reminder date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in the format YYYY-MM-DD")
    private String reminderDate;

    @NotBlank(message = "Reminder time is required")
    @Pattern(regexp = "^([01]?\\d|2[0-3]):\\d{2}$", message = "Time must be in the format HH:MM (24-hour)")
    private String reminderTime;

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;

    @NotBlank(message = "User email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String userEmail;

    @Size(max = 1000, message = "Image URL must be less than 1000 characters")
    private String imageUrl;

    @NotBlank(message = "Garden space name is required")
    @Size(max = 255, message = "Garden space name must be less than 255 characters")
    private String gardenSpaceName;

    @NotNull(message = "Garden space ID is required") @Size(min = 36, max = 36, message = "Garden space ID must be a UUID string (36 characters)")
    private String gardenSpaceId;
}
