package dev.solace.twiggle.dto.plant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiseasePestImageDTO {
    private String license;

    @JsonProperty("license_name")
    private String licenseName;

    @JsonProperty("license_url")
    private String licenseUrl;

    @JsonProperty("original_url")
    private String originalUrl;

    @JsonProperty("regular_url")
    private String regularUrl;

    @JsonProperty("medium_url")
    private String mediumUrl;

    @JsonProperty("small_url")
    private String smallUrl;

    @JsonProperty("thumbnail")
    private String thumbnailUrl;
}
