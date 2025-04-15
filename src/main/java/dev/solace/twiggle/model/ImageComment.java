package dev.solace.twiggle.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

/**
 * ImageComment entity that maps to the existing 'image_comments' table in Supabase.
 */
@Entity
@Table(name = "image_comments")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageComment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "image_id", nullable = false, columnDefinition = "uuid")
    private UUID imageId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
