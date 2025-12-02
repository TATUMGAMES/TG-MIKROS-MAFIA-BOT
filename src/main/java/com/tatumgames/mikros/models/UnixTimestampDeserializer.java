package com.tatumgames.mikros.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Custom deserializer for date strings in format dd-MM-yy:HH:mm:ss to Instant.
 * Example: "29-11-25:08:56:40" = November 29, 2025 at 08:56:40
 */
public class UnixTimestampDeserializer extends JsonDeserializer<Instant> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yy:HH:mm:ss");

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();
        try {
            // Parse the date string in format dd-MM-yy:HH:mm:ss
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, FORMATTER);
            // Convert to Instant using system default timezone
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            throw new IOException("Failed to parse date: " + dateString + ". Expected format: dd-MM-yy:HH:mm:ss", e);
        }
    }
}

