package dev.solace.twiggle.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;

/**
 * Entity mapping for Supabase `auth.users` table.
 * Direct insert/update enabled (⚠ dangerous, be careful).
 */
@Getter
@Setter
@Entity
@Immutable
@DynamicUpdate
@Table(name = "users", schema = "auth")
public class AuthUser {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "instance_id", columnDefinition = "uuid")
    private UUID instanceId;

    @Column(length = 255)
    private String aud;

    @Column(length = 255)
    private String role;

    @Column(length = 255)
    private String email;

    @Column(name = "encrypted_password", length = 255)
    private String encryptedPassword;

    @Column(name = "email_confirmed_at")
    private Instant emailConfirmedAt;

    @Column(name = "invited_at")
    private Instant invitedAt;

    @Column(name = "confirmation_token", length = 255)
    private String confirmationToken;

    @Column(name = "confirmation_sent_at")
    private Instant confirmationSentAt;

    @Column(name = "recovery_token", length = 255)
    private String recoveryToken;

    @Column(name = "recovery_sent_at")
    private Instant recoverySentAt;

    @Column(name = "email_change_token_new", length = 255)
    private String emailChangeTokenNew;

    @Column(name = "email_change", length = 255)
    private String emailChange;

    @Column(name = "email_change_sent_at")
    private Instant emailChangeSentAt;

    @Column(name = "last_sign_in_at")
    private Instant lastSignInAt;

    @Column(name = "raw_app_meta_data", columnDefinition = "jsonb")
    private String rawAppMetaData;

    @Column(name = "raw_user_meta_data", columnDefinition = "jsonb")
    private String rawUserMetaData;

    @Column(name = "is_super_admin")
    private Boolean isSuperAdmin;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column
    private String phone;

    @Column(name = "phone_confirmed_at")
    private Instant phoneConfirmedAt;

    @Column(name = "phone_change")
    private String phoneChange;

    @Column(name = "phone_change_token", length = 255)
    private String phoneChangeToken;

    @Column(name = "phone_change_sent_at")
    private Instant phoneChangeSentAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "email_change_token_current", length = 255)
    private String emailChangeTokenCurrent;

    @Column(name = "email_change_confirm_status")
    private Short emailChangeConfirmStatus;

    @Column(name = "banned_until")
    private Instant bannedUntil;

    @Column(name = "reauthentication_token", length = 255)
    private String reauthenticationToken;

    @Column(name = "reauthentication_sent_at")
    private Instant reauthenticationSentAt;

    @Column(name = "is_sso_user")
    private Boolean isSsoUser;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    // Helper methods to convert between String and JsonNode
    public JsonNode getRawAppMetaDataAsJson() {
        try {
            return new ObjectMapper().readTree(rawAppMetaData);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public void setRawAppMetaDataFromJson(JsonNode jsonNode) {
        try {
            this.rawAppMetaData = new ObjectMapper().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            this.rawAppMetaData = null;
        }
    }

    public JsonNode getRawUserMetaDataAsJson() {
        try {
            return new ObjectMapper().readTree(rawUserMetaData);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public void setRawUserMetaDataFromJson(JsonNode jsonNode) {
        try {
            this.rawUserMetaData = new ObjectMapper().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            this.rawUserMetaData = null;
        }
    }
}
