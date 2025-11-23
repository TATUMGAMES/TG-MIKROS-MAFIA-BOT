# API: mikros-promo-submission

## Overview
Submits a Discord user's lead details when they request marketing help via the `/promo-help` command. This endpoint captures promotional interest from game developers and studios for MIKROS marketing services.

## Endpoint
```
POST https://api.tatumgames.com/promo-lead
```

## Authentication
```
Authorization: Bearer {API_KEY}
```

## Request Headers
```
Content-Type: application/json
Authorization: Bearer {API_KEY}
```

## Request Body Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| discord_id | string | Yes | Discord user ID of the requester |
| username | string | Yes | Discord username (e.g., "devguy#9999") |
| server_id | string | Yes | Discord server/guild ID where request originated |
| campaign_interest | string | Yes | Type of campaign (e.g., "Game Launch", "Beta Promo", "Store Launch") |
| email | string | No | Optional email address for follow-up |
| timestamp | ISO8601 | Yes | Time of request in ISO 8601 format |
| message_context | string | No | Optional: Original message that triggered detection |

## Response

### Success (200 OK)
```json
{
  "success": true,
  "lead_id": "lead_abc123xyz",
  "promo_code": "MIKROS2025-ABC123",
  "message": "Lead submitted successfully. Promo code generated."
}
```

### Error (400 Bad Request)
```json
{
  "success": false,
  "error": "Invalid request: missing required field 'discord_id'"
}
```

### Error (401 Unauthorized)
```json
{
  "success": false,
  "error": "Invalid or missing API key"
}
```

### Error (500 Internal Server Error)
```json
{
  "success": false,
  "error": "Internal server error. Please try again later."
}
```

## Example Request

```json
{
  "discord_id": "123456789012345678",
  "username": "devguy#9999",
  "server_id": "987654321098765432",
  "campaign_interest": "Game Launch",
  "email": "dev@example.com",
  "timestamp": "2025-10-08T15:30:00Z",
  "message_context": "We're launching our game next month!"
}
```

## Example cURL Request

```bash
curl -X POST https://api.tatumgames.com/promo-lead \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -d '{
    "discord_id": "123456789012345678",
    "username": "devguy#9999",
    "server_id": "987654321098765432",
    "campaign_interest": "Game Launch",
    "email": "dev@example.com",
    "timestamp": "2025-10-08T15:30:00Z"
  }'
```

## Response Fields

| Field | Type | Description |
|-------|------|-------------|
| success | boolean | Whether the request was successful |
| lead_id | string | Unique identifier for the lead (if successful) |
| promo_code | string | Generated promotional code for the user (if applicable) |
| message | string | Human-readable response message |
| error | string | Error message (only present if success is false) |

## Integration Notes

### Current Implementation Status
- ✅ Lead submission data model created
- ✅ `/promo-help` command collects lead information
- ⏳ API submission integration (TODO in code)
- ⏳ Promo code generation (future enhancement)
- ⏳ CRM integration (future enhancement)

### TODO: Implementation Steps
1. Add HTTP client dependency (e.g., OkHttp, Apache HttpClient)
2. Implement API client in `PromoHelpCommand` or dedicated service
3. Add API key configuration to `ConfigLoader`
4. Handle API errors gracefully with user feedback
5. Add retry logic for transient failures
6. Log all submissions for debugging

### Future Enhancements

#### Promo Code Generation
- Generate unique codes per lead
- Track code usage and redemption
- Set expiration dates
- Link codes to specific campaigns

#### CRM Integration
- Sync leads to Hubspot/Salesforce
- Tag leads by campaign type
- Track conversion funnel
- Automated follow-up sequences

#### Analytics
- Track detection rate
- Measure conversion (detection → lead submission)
- Monitor prompt effectiveness
- A/B test prompt messages

## Rate Limiting
- Recommended: 100 requests per minute per API key
- Rate limit headers included in response:
  - `X-RateLimit-Limit`: Maximum requests per window
  - `X-RateLimit-Remaining`: Remaining requests in current window
  - `X-RateLimit-Reset`: Unix timestamp when limit resets

## Error Handling

### Network Errors
- Implement exponential backoff retry
- Log errors for monitoring
- Queue failed submissions for retry
- Notify user of submission status

### Validation Errors
- Validate email format (if provided)
- Ensure required fields are present
- Sanitize user input
- Return clear error messages

## Security Considerations

1. **API Key Protection**
   - Store API key in environment variables
   - Never commit keys to version control
   - Rotate keys periodically

2. **Input Validation**
   - Sanitize all user inputs
   - Validate Discord IDs format
   - Limit message context length

3. **Rate Limiting**
   - Implement client-side rate limiting
   - Respect server rate limits
   - Handle 429 responses gracefully

## Testing

### Test Cases
1. Valid lead submission with all fields
2. Valid lead submission without email
3. Missing required fields
4. Invalid API key
5. Network timeout handling
6. Rate limit handling

### Mock Server
For development, use a mock server endpoint:
```
POST http://localhost:8080/mock/promo-lead
```

---

**Status:** 📋 Specification Complete  
**Implementation:** ⏳ Pending (marked with TODO in code)  
**Last Updated:** 2025-10-08
