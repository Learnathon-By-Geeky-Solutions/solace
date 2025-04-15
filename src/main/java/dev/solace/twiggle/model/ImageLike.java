package dev.solace.twiggle.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

/**
 * ImageLike entity that maps to the existing 'image_likes' table in Supabase.
 */
@Entity
@Table(name = "image_likes")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageLike {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "image_id", nullable = false, columnDefinition = "uuid")
    private UUID imageId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
