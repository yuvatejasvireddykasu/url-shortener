package com.projects.tinyurl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ShortenUrlRequest {
    @NotBlank(message = "longUrl is required")
    @Pattern(
            regexp = "^(http|https)://.*$",
            message = "longUrl must start with http:// or https://"
    )
    private String longUrl;
}
