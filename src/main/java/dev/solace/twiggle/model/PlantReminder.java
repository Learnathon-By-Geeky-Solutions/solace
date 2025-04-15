package dev.solace.twiggle.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

/**
 * PlantReminder entity that maps to the existing 'plant_reminders' table in Supabase.
 */
@Entity
@Table(name = "plant_reminders")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlantReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "plant_id", nullable = false, columnDefinition = "uuid")
    private UUID plantId;

    @Column(name = "garden_plan_id", nullable = false, columnDefinition = "uuid")
    private UUID gardenPlanId;

    @Column(name = "reminder_type", nullable = false)
    private String reminderType;

    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
