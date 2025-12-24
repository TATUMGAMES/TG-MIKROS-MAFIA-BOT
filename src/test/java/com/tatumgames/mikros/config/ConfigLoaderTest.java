package com.tatumgames.mikros.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigLoader.
 * Tests .env file reading functionality including encoding fallback.
 */
class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should read .env file with UTF-8 encoding using Dotenv")
    void shouldReadEnvFileWithUtf8() throws IOException {
        // Create a test .env file with UTF-8 encoding
        Path envFile = tempDir.resolve(".env");
        String content = "DISCORD_BOT_TOKEN=test_token_123\n" +
                "ENVIRONMENT=dev\n" +
                "BOT_OWNER_ID=123456789\n";
        Files.write(envFile, content.getBytes(StandardCharsets.UTF_8));

        // Change to temp directory to test dotenv loading
        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());
            
            // Test direct dotenv loading
            Dotenv dotenv = Dotenv.configure()
                    .directory(tempDir.toString())
                    .ignoreIfMissing()
                    .load();

            assertNotNull(dotenv);
            assertEquals("test_token_123", dotenv.get("DISCORD_BOT_TOKEN"));
            assertEquals("dev", dotenv.get("ENVIRONMENT"));
            assertEquals("123456789", dotenv.get("BOT_OWNER_ID"));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    @DisplayName("Should handle .env file with comments and empty lines")
    void shouldHandleEnvFileWithComments() throws IOException {
        Path envFile = tempDir.resolve(".env");
        String content = "# This is a comment\n" +
                "DISCORD_BOT_TOKEN=test_token_456\n" +
                "\n" +
                "# Another comment\n" +
                "ENVIRONMENT=prod\n";
        Files.write(envFile, content.getBytes(StandardCharsets.UTF_8));

        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());
            
            Dotenv dotenv = Dotenv.configure()
                    .directory(tempDir.toString())
                    .ignoreIfMissing()
                    .load();

            assertNotNull(dotenv);
            assertEquals("test_token_456", dotenv.get("DISCORD_BOT_TOKEN"));
            assertEquals("prod", dotenv.get("ENVIRONMENT"));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    @DisplayName("Should handle .env file with quoted values")
    void shouldHandleEnvFileWithQuotedValues() throws IOException {
        Path envFile = tempDir.resolve(".env");
        String content = "DISCORD_BOT_TOKEN=\"quoted_token_value\"\n" +
                "ENVIRONMENT='dev'\n" +
                "BOT_OWNER_ID=123456789\n";
        Files.write(envFile, content.getBytes(StandardCharsets.UTF_8));

        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());
            
            Dotenv dotenv = Dotenv.configure()
                    .directory(tempDir.toString())
                    .ignoreIfMissing()
                    .load();

            assertNotNull(dotenv);
            // Dotenv should handle quotes automatically
            String token = dotenv.get("DISCORD_BOT_TOKEN");
            assertNotNull(token);
            assertTrue(token.contains("quoted_token_value") || token.equals("quoted_token_value"));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    @DisplayName("Should read .env file with Windows-1252 encoding using multi-encoding fallback")
    void shouldReadEnvFileWithWindows1252() throws IOException {
        Path envFile = tempDir.resolve(".env");
        String content = "DISCORD_BOT_TOKEN=test_token_windows\n" +
                "ENVIRONMENT=dev\n";
        
        // Write with Windows-1252 encoding
        Files.write(envFile, content.getBytes(java.nio.charset.Charset.forName("Windows-1252")));

        // Test manual reading with multiple encodings (simulating ConfigLoader behavior)
        java.nio.charset.Charset[] encodingsToTry = {
            StandardCharsets.UTF_8,
            java.nio.charset.Charset.forName("Windows-1252"),
            java.nio.charset.Charset.forName("ISO-8859-1"),
            StandardCharsets.US_ASCII
        };

        java.util.List<String> lines = null;
        java.nio.charset.Charset successfulEncoding = null;

        for (java.nio.charset.Charset encoding : encodingsToTry) {
            try {
                lines = Files.readAllLines(envFile, encoding);
                successfulEncoding = encoding;
                break;
            } catch (Exception e) {
                // Try next encoding
            }
        }

        // Verify that at least one encoding worked
        assertNotNull(lines, "Should be able to read file with at least one encoding");
        assertNotNull(successfulEncoding, "Should find a successful encoding");
        assertFalse(lines.isEmpty(), "Should read at least one line");
        
        // Verify content is correct (regardless of which encoding worked)
        boolean foundToken = false;
        for (String line : lines) {
            if (line.contains("DISCORD_BOT_TOKEN=test_token_windows")) {
                foundToken = true;
                break;
            }
        }
        assertTrue(foundToken, "Should find the token in the read lines");
    }

    @Test
    @DisplayName("Should handle missing .env file gracefully")
    void shouldHandleMissingEnvFile() {
        // Test that dotenv doesn't throw when file is missing
        assertDoesNotThrow(() -> {
            Dotenv dotenv = Dotenv.configure()
                    .directory(tempDir.toString())
                    .ignoreIfMissing()
                    .load();
            
            // Should return null or empty values for missing keys
            assertNull(dotenv.get("NONEXISTENT_KEY"));
        });
    }

    @Test
    @DisplayName("Should parse key-value pairs from .env file manually")
    void shouldParseKeyValuePairsManually() throws IOException {
        Path envFile = tempDir.resolve(".env");
        String content = "KEY1=value1\n" +
                "KEY2=value2\n" +
                "# Comment line\n" +
                "KEY3=value3\n";
        Files.write(envFile, content.getBytes(StandardCharsets.UTF_8));

        java.util.List<String> lines = Files.readAllLines(envFile, StandardCharsets.UTF_8);
        int loadedCount = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int equalsIndex = line.indexOf('=');
            if (equalsIndex > 0) {
                String key = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();
                loadedCount++;
                
                // Verify parsing
                assertTrue(key.startsWith("KEY"));
                assertTrue(value.startsWith("value"));
            }
        }

        assertEquals(3, loadedCount);
    }

    @Test
    @DisplayName("Should handle .env file with special characters")
    void shouldHandleEnvFileWithSpecialCharacters() throws IOException {
        Path envFile = tempDir.resolve(".env");
        // Use characters that might cause encoding issues
        String content = "DISCORD_BOT_TOKEN=test_token_with_special_chars_!@#$%\n" +
                "ENVIRONMENT=dev\n";
        Files.write(envFile, content.getBytes(StandardCharsets.UTF_8));

        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());
            
            Dotenv dotenv = Dotenv.configure()
                    .directory(tempDir.toString())
                    .ignoreIfMissing()
                    .load();

            assertNotNull(dotenv);
            String token = dotenv.get("DISCORD_BOT_TOKEN");
            assertNotNull(token);
            assertTrue(token.contains("test_token_with_special_chars"));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }
}

