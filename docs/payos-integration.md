# PayOS Payment Integration

**Payment Gateway for Game Account Shop Platform**

---

## Overview

PayOS is a comprehensive Vietnamese payment gateway that generates VietQR codes with pre-defined amounts and content. Unlike VietQR.io, PayOS provides a complete merchant platform with transaction tracking and payment status checking.

**Key Differences from VietQR.io:**
- **No webhook required** for QR generation only
- **Free tier** - Unlimited transactions, 1% fee only when > 1M VND/month
- **Built-in transaction tracking** - Check payment status anytime
- **Simple API** - Clear responses with checkout URL

---

## Getting Started

### Step 1: Register for PayOS Account

1. Visit [https://my.payos.vn](https://my.payos.vn)
2. **Sign up** with email, phone, or SSO
3. **Verify your business**:
   - **Individual**: Upload ID card (CMND)
   - **Business**: Upload business license
4. **Create Payment Channel**:
   - Select your bank to link
   - Receive **3 important keys**:
     - **Client ID** (`x-client-id`)
     - **API Key** (`x-api-key`)
     - **Checksum Key** - For HMAC SHA256 signature

### Step 2: Get API Credentials

After logging in to my.payos.vn, navigate to the Payment Channels section to get:

- **Client ID** - Used for authentication
- **API Key** - Used for authentication
- **Checksum Key** - For advanced signature verification (optional for basic QR generation)

---

## API Configuration

### Endpoint

```
POST https://api-merchant.payos.vn/v2/payment-requests
```

### Headers (Required)

| Header | Required | Description |
|--------|----------|-------------|
| `x-client-id` | Yes | Your Client ID from my.payos.vn |
| `x-api-key` | Yes | Your API Key from my.payos.vn |
| `Content-Type` | Yes | `application/json` |

---

## Request Parameters

### Required Fields

| Field | Type | Description | Notes |
|-------|------|-------------|-------|
| `orderCode` | int | Unique order ID | Use timestamp or random UUID |
| `amount` | int | Amount in VND | Max 13 digits |
| `description` | string | Payment content | 9-25 chars depending on bank |

### Optional Fields

| Field | Type | Description |
|-------|------|-------------|
| `items` | array | Product details (not required) |
| `cancelUrl` | string | Redirect URL when cancelled |
| `returnUrl` | string | Redirect URL on success |
| `expiredAt` | timestamp | Expiration time (Unix timestamp) |

### Request Body Example

```json
{
  "orderCode": 12345,
  "amount": 100000,
  "description": "Thanh toan don hang ABC",
  "items": [
    {
      "name": "Game Account - Gold Rank",
      "quantity": 1,
      "price": 100000
    }
  ]
}
```

---

## Response Format

### Success Response

```json
{
  "code": "00",
  "desc": "success",
  "data": {
    "bin": "970422",
    "accountNumber": "113366668888",
    "accountName": "QUY VAC XIN PHONG CHONG COVID",
    "amount": 10000,
    "description": "THANH TOAN DON HANG 123",
    "orderCode": 123,
    "paymentLinkId": "124c33293c934a85be5b7f8761a27a07",
    "checkoutUrl": "https://checkout.payos.vn/web/124c33293c934a85be5b7f8761a27a07",
    "qrCode": "00020101021238570010A000000727012700069704220113113366668888...",
    "status": "PENDING"
  }
}
```

### Important Response Fields

| Field | Description |
|-------|-------------|
| `qrCode` | QR string (use to render as image) |
| `checkoutUrl` | Payment link (can open directly or redirect) |
| `paymentLinkId` | Payment link ID (save for status checking) |
| `status` | Payment status: `PENDING`, `PAID`, `CANCELLED` |

---

## Bank BIN Codes

Common Vietnamese bank codes:

| Bank | BIN Code |
|------|----------|
| Agribank | 970403 |
| VietcomBank | 970405 |
| Techcombank | 970407 |
| BIDV | 970410 |
| Vietinbank | 970415 |
| VIB | 970420 |
| ACB | 970425 |
| MB Bank | 970443 |

---

## Implementation Examples

### Using cURL

```bash
curl -X POST https://api-merchant.payos.vn/v2/payment-requests \
  -H "x-client-id: YOUR_CLIENT_ID" \
  -H "x-api-key: YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "orderCode": 1234567890,
    "amount": 100000,
    "description": "Thanh toan don hang ABC",
    "items": [{
      "name": "Game Account",
      "quantity": 1,
      "price": 100000
    }]
  }'
```

### Java/Spring Boot Example

```java
@Service
@Slf4j
public class PayOSService {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public PayOSResponse createPayment(Long amount, String orderCode, String description) {
        String url = baseUrl + "/v2/payment-requests";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        PayOSRequest request = PayOSRequest.builder()
            .orderCode(Integer.parseInt(orderCode))
            .amount(amount.intValue())
            .description(description)
            .items(List.of(
                PayOSItem.builder()
                    .name("Game Account Purchase")
                    .quantity(1)
                    .price(amount.intValue())
                    .build()
            ))
            .build();

        HttpEntity<PayOSRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<PayOSApiResponse> response = restTemplate.postForEntity(
                url, entity, PayOSApiResponse.class
            );

            if (response.getBody() != null && "00".equals(response.getBody().getCode())) {
                log.info("PayOS payment created successfully for order: {}", orderCode);
                return response.getBody().getData();
            }

            throw new RuntimeException("PayOS API error: " +
                (response.getBody() != null ? response.getBody().getDesc() : "Unknown error"));

        } catch (RestClientException e) {
            log.error("Failed to call PayOS API", e);
            throw new RuntimeException("Payment service unavailable", e);
        }
    }

    public PaymentStatus checkPaymentStatus(String paymentLinkId) {
        String url = baseUrl + "/v2/payment-requests/" + paymentLinkId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PayOSApiResponse> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, PayOSApiResponse.class
        );

        if (response.getBody() != null && response.getBody().getData() != null) {
            String status = response.getBody().getData().getStatus();
            return PaymentStatus.valueOf(status);
        }

        return PaymentStatus.UNKNOWN;
    }
}
```

### Request DTOs

```java
@Data
@Builder
public class PayOSRequest {
    private Integer orderCode;
    private Integer amount;
    private String description;
    private String cancelUrl;
    private String returnUrl;
    private Long expiredAt;
    private List<PayOSItem> items;
}

@Data
@Builder
public class PayOSItem {
    private String name;
    private Integer quantity;
    private Integer price;
}

@Data
public class PayOSApiResponse {
    private String code;
    private String desc;
    private PayOSResponse data;
}

@Data
public class PayOSResponse {
    private String bin;
    private String accountNumber;
    private String accountName;
    private Integer amount;
    private String description;
    private Integer orderCode;
    private String paymentLinkId;
    private String checkoutUrl;
    private String qrCode;
    private String status;
}

public enum PaymentStatus {
    PENDING,
    PAID,
    CANCELLED,
    UNKNOWN
}
```

---

## Display QR Code on Web

### Method 1: QuickChart API (No Library Required)

```javascript
const qrCode = data.data.qrCode; // From PayOS response
const qrImageUrl = `https://quickchart.io/qr?text=${encodeURIComponent(qrCode)}&size=300`;

// Display
document.getElementById('qr').src = qrImageUrl;
```

```html
<img id="qr" style="width:300px;height:300px;">
```

### Method 2: Using QRCode.js Library

```html
<div id="qr-container" style="width:300px;height:300px;"></div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js"></script>
<script>
  const qrCode = data.data.qrCode;
  new QRCode(document.getElementById('qr-container'), qrCode);
</script>
```

### Complete JavaScript Example

```javascript
async function createPayOSPayment(orderCode, amount, description) {
  const clientId = 'YOUR_CLIENT_ID';
  const apiKey = 'YOUR_API_KEY';

  const payload = {
    orderCode: orderCode,
    amount: amount,
    description: description,
    items: [{
      name: 'Game Account',
      quantity: 1,
      price: amount
    }]
  };

  try {
    const response = await fetch(
      'https://api-merchant.payos.vn/v2/payment-requests',
      {
        method: 'POST',
        headers: {
          'x-client-id': clientId,
          'x-api-key': apiKey,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      }
    );

    const data = await response.json();

    if (data.code === '00' && data.data) {
      // Generate QR image URL
      const qrImageUrl = `https://quickchart.io/qr?text=${encodeURIComponent(data.data.qrCode)}&size=300`;

      // Display QR
      document.getElementById('qr').src = qrImageUrl;
      document.getElementById('checkoutUrl').href = data.data.checkoutUrl;

      console.log('Payment Link ID:', data.data.paymentLinkId);
      console.log('Checkout URL:', data.data.checkoutUrl);
      console.log('Status:', data.data.status);

    } else {
      console.error('Error:', data.desc);
      alert('Failed to create payment: ' + data.desc);
    }
  } catch (error) {
    console.error('Connection error:', error);
    alert('Payment service unavailable');
  }
}

// Usage:
createPayOSPayment(Date.now(), 100000, 'Thanh toan don hang ABC');
```

---

## Check Payment Status

No webhook required - you can check status anytime:

```javascript
async function checkPaymentStatus(paymentLinkId) {
  const clientId = 'YOUR_CLIENT_ID';
  const apiKey = 'YOUR_API_KEY';

  const response = await fetch(
    `https://api-merchant.payos.vn/v2/payment-requests/${paymentLinkId}`,
    {
      method: 'GET',
      headers: {
        'x-client-id': clientId,
        'x-api-key': apiKey
      }
    }
  );

  const data = await response.json();

  if (data.data.status === 'PAID') {
    console.log('Payment successful!');
    console.log('Amount:', data.data.amount);
    // Update your backend, send credentials, etc.
  } else if (data.data.status === 'PENDING') {
    console.log('Waiting for payment...');
  }
}

// Usage:
checkPaymentStatus('124c33293c934a85be5b7f8761a27a07');
```

---

## Application Configuration

### application.yml

```yaml
payos:
  client-id: ${PAYOS_CLIENT_ID:your-client-id}
  api-key: ${PAYOS_API_KEY:your-api-key}
  checksum-key: ${PAYOS_CHECKSUM_KEY:your-checksum-key}
  base-url: https://api-merchant.payos.vn
  webhook-url: ${PAYOS_WEBHOOK_URL:https://yourdomain.com/api/payments/webhook}
```

### application.properties

```properties
# PayOS Configuration
payos.client-id=${PAYOS_CLIENT_ID:your-client-id}
payos.api-key=${PAYOS_API_KEY:your-api-key}
payos.checksum-key=${PAYOS_CHECKSUM_KEY:your-checksum-key}
payos.base-url=https://api-merchant.payos.vn
payos.webhook-url=${PAYOS_WEBHOOK_URL:https://yourdomain.com/api/payments/webhook}
```

---

## Controller Endpoint

```java
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PayOSService payOSService;
    private final TransactionService transactionService;

    @PostMapping("/create/{transactionId}")
    public ResponseEntity<Map<String, Object>> createPayment(
        @PathVariable Long transactionId
    ) {
        Transaction transaction = transactionService.getById(transactionId);

        PayOSResponse response = payOSService.createPayment(
            transaction.getAmount(),
            "TX" + transactionId,
            "Thanh toan don hang " + transactionId
        );

        // Save paymentLinkId for status checking
        transaction.setPaymentLinkId(response.getPaymentLinkId());
        transactionService.update(transaction);

        return ResponseEntity.ok(Map.of(
            "qrCode", response.getQrCode(),
            "checkoutUrl", response.getCheckoutUrl(),
            "paymentLinkId", response.getPaymentLinkId(),
            "status", response.getStatus()
        ));
    }

    @GetMapping("/status/{paymentLinkId}")
    public ResponseEntity<Map<String, String>> checkStatus(
        @PathVariable String paymentLinkId
    ) {
        PaymentStatus status = payOSService.checkPaymentStatus(paymentLinkId);
        return ResponseEntity.ok(Map.of("status", status.name()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
        @RequestBody PayOSWebhookPayload payload
    ) {
        // Verify signature using checksum key
        if (!payOSService.verifyWebhookSignature(payload)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Process payment
        if ("PAID".equals(payload.getData().getStatus())) {
            transactionService.markAsPaid(
                payload.getData().getOrderCode(),
                payload.getData().getPaymentLinkId()
            );
        }

        return ResponseEntity.ok().build();
    }
}
```

---

## Payment Flow

### Story 3.1: Buyer Clicks "Buy Now"

1. User clicks "Buy" on a listing
2. Frontend calls `/api/v1/payments/create/{transactionId}`
3. Backend creates transaction record (status: PENDING)
4. Backend calls PayOS API to generate QR code
5. Frontend displays QR code to user
6. User scans QR with banking app and pays
7. **Optional**: Webhook notifies backend of successful payment
8. **Or**: Admin manually checks payment status
9. Backend updates transaction to VERIFIED
10. System sends account credentials to buyer's email

### Story 3.2: Admin Verify Payment & Send Gmail

**Manual Verification Flow:**

1. Admin views pending transactions
2. Admin clicks "Check Status" - calls PayOS status API
3. If status is `PAID`:
   - Updates transaction.status to VERIFIED
   - Updates game_accounts.status to SOLD
   - Sends email with account credentials to buyer

**Automatic Webhook Flow (Optional):**

1. PayOS sends webhook to your endpoint
2. Backend verifies signature using checksum key
3. Updates transaction status automatically
4. Sends credentials email

---

## Security Best Practices

### 1. Environment Variables

**NEVER** commit API keys to git. Use environment variables:

```bash
# .env file (DO NOT commit)
PAYOS_CLIENT_ID=your_actual_client_id
PAYOS_API_KEY=your_actual_api_key
PAYOS_CHECKSUM_KEY=your_actual_checksum_key
PAYOS_WEBHOOK_URL=https://yourdomain.com/api/payments/webhook
```

### 2. Backend-Only Calls

**Security Model:**
```
Frontend (Web/App)
    ↓
Your Backend (Node/Java/Python)
    ↓
PayOS API (with API Key + Checksum Key)
```

- **Always** call PayOS API from backend
- **Never** expose API Key in frontend JavaScript
- Store credentials securely in server environment
- Use Spring Cloud Config or AWS Secrets Manager for production

### 3. Webhook Signature Verification

```java
@Service
public class WebhookSecurityService {

    @Value("${payos.checksum-key}")
    private String checksumKey;

    public boolean verifyWebhookSignature(PayOSWebhookPayload payload) {
        String expectedSignature = hmacSHA256(
            payload.getData().toString(),
            checksumKey
        );
        return expectedSignature.equals(payload.getSignature());
    }

    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed", e);
        }
    }
}
```

### 4. Input Validation

```java
@Component
public class PaymentValidator {

    private static final int MAX_AMOUNT = 999999999999L;
    private static final Pattern DESCRIPTION_PATTERN =
        Pattern.compile("^[a-zA-Z0-9\\s]{9,25}$");

    public void validatePaymentRequest(Long amount, String description) {
        if (amount == null || amount <= 0 || amount > MAX_AMOUNT) {
            throw new ValidationException("Invalid amount");
        }
        if (description == null ||
            !DESCRIPTION_PATTERN.matcher(description).matches()) {
            throw new ValidationException(
                "Description must be 9-25 characters, no special chars"
            );
        }
    }
}
```

### 5. Rate Limiting

```java
@RateLimiter(name = "payos", fallbackMethod = "paymentFallback")
public PayOSResponse createPayment(Long amount, String orderCode, String description) {
    // API call
}

private PayOSResponse paymentFallback(Long amount, String orderCode,
                                       String description, Exception e) {
    throw new ServiceUnavailableException("Payment service temporarily unavailable");
}
```

---

## Testing

### Unit Test

```java
@ExtendWith(MockitoExtension.class)
class PayOSServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PayOSService payOSService;

    @Test
    void createPayment_Success() {
        // Given
        PayOSApiResponse apiResponse = new PayOSApiResponse();
        apiResponse.setCode("00");
        apiResponse.setData(new PayOSResponse());

        when(restTemplate.postForEntity(
            anyString(),
            any(HttpEntity.class),
            eq(PayOSApiResponse.class)
        )).thenReturn(ResponseEntity.ok(apiResponse));

        // When
        PayOSResponse result = payOSService.createPayment(100000L, "12345", "Test");

        // Then
        assertNotNull(result);
    }

    @Test
    void checkPaymentStatus_Paid() {
        // Given
        PayOSApiResponse apiResponse = new PayOSApiResponse();
        PayOSResponse data = new PayOSResponse();
        data.setStatus("PAID");
        apiResponse.setData(data);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(PayOSApiResponse.class)
        )).thenReturn(ResponseEntity.ok(apiResponse));

        // When
        PaymentStatus status = payOSService.checkPaymentStatus("link123");

        // Then
        assertEquals(PaymentStatus.PAID, status);
    }
}
```

---

## Troubleshooting

| Error Code | Description | Solution |
|------------|-------------|----------|
| `00` | Success | - |
| `error_invalid_signature` | Invalid signature | Check checksum key |
| `error_invalid_amount` | Invalid amount | Amount must be > 0 and <= 13 digits |
| `error_invalid_order_code` | Duplicate order code | Use unique orderCode (timestamp + random) |
| `error_account_not_found` | Account not found | Verify payment channel setup |
| `error_expired_payment` | Payment expired | Set appropriate expiredAt timestamp |

---

## PayOS Features

### Advantages

- No webhook required for QR generation only
- High free tier (unlimited transactions)
- Simple, clear API responses
- Payment status checking anytime
- Built-in checkout page

### Considerations

- Checksum key needed for advanced signature verification
- For security, call API from backend only (not frontend)
- 1% fee applies when monthly transactions exceed 1,000,000 VND

---

## Related Files

- Database Schema: `docs/database-schema.md`
- Transaction Table: Uses `transactions` table with status PENDING → VERIFIED
- Email Service: Used to send credentials after payment verification

---

## Additional Resources

- [PayOS Official Website](https://payos.vn)
- [PayOS Merchant Portal](https://my.payos.vn)
- [PayOS API Documentation](https://docs.payos.vn)
- [VietQR Information](https://www.vietqr.io/danh-sach-api/link-tao-ma-nhanh/api-tao-ma-qr)

---

**Getting Started:**
1. Register at my.payos.vn
2. Create payment channel
3. Copy your keys
4. Replace in code
5. Test QR generation!
