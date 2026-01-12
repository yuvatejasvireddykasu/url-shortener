package com.projects.tinyurl.controller;

import com.projects.tinyurl.dto.ShortenUrlRequest;
import com.projects.tinyurl.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<String> shortenUrl(@Valid  @RequestBody ShortenUrlRequest request) {
        String longUrl = request.getLongUrl();
        String shortCode = urlService.createShortUrl(longUrl);

        String shortUrl = "http://localhost:8085/" + shortCode;
        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {

        String longUrl = urlService.getLongUrl(shortCode);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }
}
