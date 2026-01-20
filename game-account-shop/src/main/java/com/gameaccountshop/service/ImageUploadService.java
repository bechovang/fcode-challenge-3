package com.gameaccountshop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Image Upload Service for ImgBB API integration
 * Story 2.6: Image Upload for Listing
 */
@Service
@Slf4j
public class ImageUploadService {

    @Value("${imgbb.api-key}")
    private String imgbbApiKey;

    private static final String IMGBB_API_URL = "https://api.imgbb.com/1/upload";
    private static final long MAX_FILE_SIZE = 32 * 1024 * 1024; // 32MB

    /**
     * Upload image to ImgBB and return the URL
     * @param file Image file to upload
     * @return Image URL from ImgBB
     * @throws IOException if upload fails
     */
    public String uploadImage(MultipartFile file) throws IOException {
        log.info("Uploading image to ImgBB: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());

        // Validate file
        validateImageFile(file);

        try {
            // Convert file to Base64
            byte[] fileBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);

            log.debug("Base64 encoded length: {}", base64Image.length());

            // URL-encode the Base64 string (important: +, /, = need to be encoded)
            String encodedImage = URLEncoder.encode(base64Image, StandardCharsets.UTF_8);

            // URL-encode the API key as well (though it typically doesn't have special chars)
            String encodedKey = URLEncoder.encode(imgbbApiKey, StandardCharsets.UTF_8);

            // Build request body with proper URL encoding
            String requestBody = "key=" + encodedKey + "&image=" + encodedImage;

            log.debug("Request body length: {}", requestBody.length());

            // Create HTTP client and request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(IMGBB_API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(java.time.Duration.ofSeconds(30))
                .build();

            // Send request
            HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            log.info("ImgBB response status: {}", response.statusCode());

            if (response.statusCode() != 200) {
                log.error("ImgBB upload failed. Response: {}", response.body());
                throw new IOException("Failed to upload image to ImgBB. Status: " + response.statusCode() + ", Response: " + response.body());
            }

            // Parse JSON response to extract URL
            String imageUrl = extractImageUrl(response.body());

            log.info("Image uploaded successfully: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("IO error uploading image to ImgBB", e);
            throw e;
        } catch (Exception e) {
            log.error("Error uploading image to ImgBB", e);
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    /**
     * Validate image file
     * - Check file size (max 32MB)
     * - Check content type (image/*)
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng tải lên ảnh minh họa");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file quá lớn. Tối đa: 32MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (JPG, PNG, GIF, WebP)");
        }

        log.debug("File validation passed: type={}, size={}", contentType, file.getSize());
    }

    /**
     * Extract image URL from ImgBB response
     * Simple JSON parsing for MVP
     * Response format: {"data": {"url": "https://i.ibb.co/xxxxx/image.png", ...}, "success": true}
     */
    private String extractImageUrl(String jsonResponse) {
        // Look for "url":" value in the response
        int dataIndex = jsonResponse.indexOf("\"data\":");
        if (dataIndex == -1) {
            throw new RuntimeException("Invalid ImgBB response: no data section found");
        }

        // Find "url":" after data section
        int urlIndex = jsonResponse.indexOf("\"url\":\"", dataIndex);
        if (urlIndex == -1) {
            throw new RuntimeException("Invalid ImgBB response: no URL found");
        }

        int start = urlIndex + 7; // Skip "url":"
        int end = jsonResponse.indexOf("\"", start);

        if (end == -1) {
            throw new RuntimeException("Invalid ImgBB response: malformed URL");
        }

        String url = jsonResponse.substring(start, end);
        log.debug("Extracted URL: {}", url);
        return url;
    }
}
