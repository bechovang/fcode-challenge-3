# Story 2.6: Image Upload for Listing

Status: todo

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a **seller creating a listing**,
I want **to upload an image showcasing the game account**,
So that **buyers can see visual proof of the account quality**.

## Acceptance Criteria

**Given** I am on the create listing page
**When** I view the form
**Then** I see an image upload field
**And** the field accepts image files (JPG, PNG)
**And** there is a preview area to show the uploaded image

**Given** I select an image file and submit the form
**When** the upload completes
**Then** the image is uploaded to ImgBB via API
**And** the image URL is stored in game_accounts.image_url
**And** a thumbnail is generated for the listing card
**And** the full-size image is available on the detail page

**Given** I do not upload an image
**When** I attempt to submit the form
**Then** an error message "Vui lòng tải lên ảnh minh họa" is displayed
**And** no listing is created

**Technical Notes:**
- ImgBB API: `POST https://api.imgbb.com/1/upload`
- API Key from application.yml: `imgbb.api-key`
- Convert image to Base64 before uploading
- Store returned URL in database
- Thumbnail: Use CSS to downscale image on listing cards (max-width: 200px)
- Max file size: 32MB (ImgBB limit)
- Form: `<input type="file" name="image" accept="image/*" required/>`

**application.yml configuration:**
```yaml
imgbb:
  api-key: 326b60d80445ca87cc53be21178a4c62
```

## Tasks / Subtasks

- [ ] Create ImageUploadService (AC: #2)
  - [ ] Add ImgBB API key configuration from application.yml
  - [ ] Create uploadImage method that accepts MultipartFile
  - [ ] Convert image to Base64
  - [ ] Make POST request to ImgBB API
  - [ ] Parse JSON response to extract image URL
  - [ ] Handle API errors gracefully

- [ ] Update GameAccount entity (AC: #2)
  - [ ] Add imageUrl field (VARCHAR 500)
  - [ ] Add getter/setter for imageUrl

- [ ] Update GameAccountDto (AC: #1)
  - [ ] Add MultipartFile field for image upload
  - [ ] Add @NotBlank validation for image

- [ ] Update GameAccountService (AC: #2)
  - [ ] Inject ImageUploadService
  - [ ] Call uploadImage before creating listing
  - [ ] Store returned imageUrl in GameAccount
  - [ ] Handle upload failures with proper error messages

- [ ] Update ListingController (AC: #1, #3)
  - [ ] Update create listing form to accept MultipartFile
  - [ ] Add image preview functionality
  - [ ] Validate image is uploaded before submission

- [ ] Update create listing form template (AC: #1)
  - [ ] Add file input for image upload
  - [ ] Add accept="image/*" attribute
  - [ ] Add required attribute
  - [ ] Add JavaScript for image preview
  - [ ] Style the upload field nicely

- [ ] Create database migration (AC: #2)
  - [ ] Add image_url column to game_accounts table
  - [ ] Set VARCHAR(500) to store ImgBB URL

- [ ] Update listing display templates (AC: #2)
  - [ ] Update home.html to show thumbnail (Story 2.2 already done)
  - [ ] Update listing-detail.html to show full-size image (Story 2.3 already done)

- [ ] Error handling (AC: #3)
  - [ ] Display error if image upload fails
  - [ ] Display error if image is too large
  - [ ] Display error if image format is invalid

---

## Dev Notes

### Previous Story Intelligence (Story 2.1)

**From Story 2.1 (Create Listing):**
- Create listing form exists at `/listing/create`
- Form uses GameAccountDto for data binding
- GameAccountService.createListing() handles form submission
- ListingController handles GET and POST for create form

### ImgBB API Integration

**API Endpoint:** `POST https://api.imgbb.com/1/upload`

**Request Format:**
```http
POST https://api.imgbb.com/1/upload
Content-Type: application/x-www-form-urlencoded

key=YOUR_API_KEY&image=BASE64_ENCODED_IMAGE
```

**Response Format:**
```json
{
  "data": {
    "url": "https://i.ibb.co/xxxxx/image.png",
    "display_url": "https://i.ibb.co/xxxxx/image.png",
    "delete_url": "https://ibb.co/xxxxx/delete",
    "width": 1920,
    "height": 1080,
    "size": 123456,
    "expiration": "0",
    "thumb": {
      "url": "https://i.ibb.co/xxxxx/thumb.png"
    }
  },
  "success": true,
  "status": 200
}
```

### Base64 Encoding

Convert MultipartFile to Base64:
```java
byte[] fileBytes = file.getBytes();
String base64Image = Base64.getEncoder().encodeToString(fileBytes);
```

### HTTP Client (Java 11+)

Using Java's built-in HttpClient:
```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.imgbb.com/1/upload"))
    .header("Content-Type", "application/x-www-form-urlencoded")
    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
    .build();
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
```

### Database Migration

**V3__Add_Image_URL.sql:**
```sql
ALTER TABLE game_accounts
ADD COLUMN image_url VARCHAR(500) NULL;
```

### Service Layer: ImageUploadService

**Create `ImageUploadService.java`:**

```java
package com.gameaccountshop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
@Slf4j
public class ImageUploadService {

    @Value("${imgbb.api-key}")
    private String imgbbApiKey;

    private static final String IMGBB_API_URL = "https://api.imgbb.com/1/upload";

    /**
     * Upload image to ImgBB and return the URL
     * @param file Image file to upload
     * @return Image URL from ImgBB
     * @throws IOException if upload fails
     */
    public String uploadImage(MultipartFile file) throws IOException {
        log.info("Uploading image to ImgBB: {}", file.getOriginalFilename());

        try {
            // Convert file to Base64
            byte[] fileBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);

            // Build request body
            String requestBody = "key=" + imgbbApiKey + "&image=" + base64Image;

            // Create HTTP client and request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(IMGBB_API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            // Send request
            HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            log.info("ImgBB response status: {}", response.statusCode());

            if (response.statusCode() != 200) {
                log.error("ImgBB upload failed: {}", response.body());
                throw new IOException("Failed to upload image to ImgBB");
            }

            // Parse JSON response (simple string parsing for MVP)
            String responseBody = response.body();
            String imageUrl = extractImageUrl(responseBody);

            log.info("Image uploaded successfully: {}", imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("Error uploading image to ImgBB", e);
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    /**
     * Extract image URL from ImgBB response
     * Simple JSON parsing for MVP - consider using Jackson/Gson for production
     */
    private String extractImageUrl(String jsonResponse) {
        // Simple parsing - find "url":" value
        int urlIndex = jsonResponse.indexOf("\"url\":\"");
        if (urlIndex == -1) {
            throw new RuntimeException("Invalid ImgBB response: no URL found");
        }

        int start = urlIndex + 7; // Skip "url":"
        int end = jsonResponse.indexOf("\"", start);

        if (end == -1) {
            throw new RuntimeException("Invalid ImgBB response: malformed URL");
        }

        return jsonResponse.substring(start, end);
    }
}
```

### Updated GameAccountDto

**Update `GameAccountDto.java`:**

```java
package com.gameaccountshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class GameAccountDto {

    @NotBlank(message = "Vui lòng nhập rank")
    @Size(max = 50, message = "Rank không được vượt quá 50 ký tự")
    private String accountRank;

    @NotNull(message = "Vui lòng nhập giá bán")
    @Min(value = 2001, message = "Giá bán phải lớn hơn 2,000 VNĐ")
    private Long price;

    @NotBlank(message = "Vui lòng nhập mô tả")
    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    @NotBlank(message = "Vui lòng nhập tên tài khoản game")
    @Size(max = 100, message = "Tên tài khoản không được vượt quá 100 ký tự")
    private String accountUsername;

    @NotBlank(message = "Vui lòng nhập mật khẩu tài khoản game")
    @Size(max = 100, message = "Mật khẩu không được vượt quá 100 ký tự")
    private String accountPassword;

    private MultipartFile image; // Image file upload

    // Getters and Setters
    public String getAccountRank() { return accountRank; }
    public void setAccountRank(String accountRank) { this.accountRank = accountRank; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccountUsername() { return accountUsername; }
    public void setAccountUsername(String accountUsername) { this.accountUsername = accountUsername; }

    public String getAccountPassword() { return accountPassword; }
    public void setAccountPassword(String accountPassword) { this.accountPassword = accountPassword; }

    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
}
```

### Updated GameAccountService

**Update `GameAccountService.java`:**

```java
@Transactional
public GameAccount createListing(GameAccountDto dto, Long sellerId) throws IOException {
    log.info("Creating listing for seller: {}", sellerId);

    // Upload image first
    String imageUrl = null;
    if (dto.getImage() != null && !dto.getImage().isEmpty()) {
        imageUrl = imageUploadService.uploadImage(dto.getImage());
        log.info("Image uploaded: {}", imageUrl);
    } else {
        throw new IllegalArgumentException("Vui lòng tải lên ảnh minh họa");
    }

    // Create listing
    GameAccount gameAccount = new GameAccount();
    gameAccount.setGameName("Liên Minh Huyền Thoại");
    gameAccount.setAccountRank(dto.getAccountRank());
    gameAccount.setPrice(dto.getPrice());
    gameAccount.setDescription(dto.getDescription());
    gameAccount.setAccountUsername(dto.getAccountUsername());
    gameAccount.setAccountPassword(dto.getAccountPassword());
    gameAccount.setImageUrl(imageUrl);
    gameAccount.setSellerId(sellerId);
    gameAccount.setStatus(ListingStatus.PENDING);

    GameAccount saved = gameAccountRepository.save(gameAccount);
    log.info("Listing created with ID: {}", saved.getId());
    return saved;
}
```

### Updated ListingController

**Update `ListingController.java`:**

```java
@PostMapping("/create")
public String createListing(
        @Valid GameAccountDto gameAccountDto,
        BindingResult bindingResult,
        Authentication authentication,
        RedirectAttributes redirectAttributes) throws IOException {

    if (bindingResult.hasErrors()) {
        return "listing/create";
    }

    // Validate image is uploaded
    if (gameAccountDto.getImage() == null || gameAccountDto.getImage().isEmpty()) {
        redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng tải lên ảnh minh họa");
        return "redirect:/listing/create";
    }

    User user = (User) authentication.getPrincipal();
    gameAccountService.createListing(gameAccountDto, user.getId());

    redirectAttributes.addFlashAttribute("successMessage", "Đăng bán thành công! Chờ admin duyệt.");
    return "redirect:/";
}

// Add new endpoint for image preview
@PostMapping("/preview-image")
public ResponseEntity<?> previewImage(@RequestParam("file") MultipartFile file) {
    // Return image data for preview
    try {
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(file.getBytes());
    } catch (IOException e) {
        return ResponseEntity.badRequest().build();
    }
}
```

### Updated Template: listing/create.html

**Add image upload field with preview:**

```html
<div class="form-group">
  <label for="image">Ảnh minh họa *</label>
  <input type="file"
         id="image"
         name="image"
         class="form-control"
         accept="image/*"
         required
         onchange="previewImage(event)" />
  <small class="text-muted">Định dạng: JPG, PNG. Tối đa: 32MB</small>

  <!-- Image Preview -->
  <div id="imagePreview" style="margin-top: 15px; display: none;">
    <img id="previewImg" src="" alt="Preview" style="max-width: 100%; max-height: 300px; border-radius: 8px;" />
  </div>

  <!-- Error message for no image -->
  <div th:if="${errorMessage}" class="text-danger" style="margin-top: 10px;">
    <span th:text="${errorMessage}"></span>
  </div>
</div>

<script>
function previewImage(event) {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = function(e) {
      document.getElementById('previewImg').src = e.target.result;
      document.getElementById('imagePreview').style.display = 'block';
    }
    reader.readAsDataURL(file);
  } else {
    document.getElementById('imagePreview').style.display = 'none';
  }
}
</script>
```

### Configuration: application.yml

**Add ImgBB API key:**

```yaml
# ImgBB Image Upload API
imgbb:
  api-key: 326b60d80445ca87cc53be21178a4c62
```

### Testing Checklist

- [ ] Form displays image upload field
- [ ] Image preview works when file is selected
- [ ] Submitting without image shows error message
- [ ] Valid image uploads successfully to ImgBB
- [ ] Image URL is stored in database
- [ ] Thumbnail displays on listing cards (Story 2.2)
- [ ] Full-size image displays on detail page (Story 2.3)
- [ ] Large files (>32MB) are rejected
- [ ] Invalid file types are rejected
- [ ] API errors are handled gracefully

### Error Scenarios

| Scenario | Expected Behavior |
|----------|-------------------|
| No image selected | Error: "Vui lòng tải lên ảnh minh họa" |
| Invalid file format | Error: "Chỉ chấp nhận file ảnh (JPG, PNG)" |
| File too large (>32MB) | Error: "Kích thước file quá lớn" |
| ImgBB API error | Error: "Không thể tải lên ảnh. Vui lòng thử lại." |
| Network error | Error: "Lỗi kết nối. Vui lòng thử lại." |

### Security Considerations

- Validate file type (MIME type check)
- Validate file size (max 32MB)
- Sanitize filename to prevent path traversal
- Use ImgBB for external storage (saves server space)
- Don't store images locally (security risk)

### References

- [Source: planning-artifacts/epics.md#Story 2.6: Image Upload for Listing]
- [ImgBB API Documentation](https://api.imgbb.com/)
- [Story 2.1: Create Listing - base form structure]
- [Story 2.2: Browse Listings - thumbnail display]
- [Story 2.3: Listing Details - full-size display]

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101 (glm-4.6)

### Completion Notes List

- **Story Status:** New story - not yet implemented
- **Dependencies:** Stories 2.1, 2.2, 2.3 must be completed first
- **Configuration Required:** ImgBB API key in application.yml
- **Migration Required:** V3__Add_Image_URL.sql

### File List

**Files to Create:**
- `src/main/java/com/gameaccountshop/service/ImageUploadService.java`
- `src/main/resources/db/migration/V3__Add_Image_URL.sql`

**Files to Modify:**
- `src/main/java/com/gameaccountshop/entity/GameAccount.java` - Add imageUrl field
- `src/main/java/com/gameaccountshop/dto/GameAccountDto.java` - Add MultipartFile field
- `src/main/java/com/gameaccountshop/service/GameAccountService.java` - Integrate ImageUploadService
- `src/main/java/com/gameaccountshop/controller/ListingController.java` - Handle image upload
- `src/main/resources/templates/listing/create.html` - Add image upload field with preview
- `src/main/resources/application.yml` - Add imgbb.api-key configuration
