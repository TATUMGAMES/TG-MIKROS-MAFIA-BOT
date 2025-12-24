# API: MIKROS Campaign Message Detector & Marketing Discount Offer

## Feature Overview

This feature automatically detects when a Discord user mentions their game's launch or release date, using AI-powered
natural language understanding. When detected, the bot offers them a promotional code for MIKROS Marketing services,
collects their email, and registers them in the marketing campaign system.

## Why This Feature Needs AI (Google Generative AI)

Traditional keyword matching cannot reliably detect intent. Consider these examples:

**Should Trigger:**

- "My game releases on November 5th!"
- "We're launching our indie game next month"
- "Our game drops on Steam this Friday"
- "Can't wait for our release date: Dec 15"

**Should NOT Trigger:**

- "What game releases on Nov 5?" (asking, not announcing)
- "I wish my game was releasing soon" (hypothetical)
- "Game releases are expensive" (talking about concept)
- "Releases from prison next month" (different context)

**Google Generative AI (Gemini)** can:

1. Understand context and intent
2. Detect variations of phrasing
3. Distinguish between announcements and questions
4. Handle multiple languages
5. Adapt to gaming terminology
6. Learn from false positives

---

## Implementation Note

**⚠️ DO NOT IMPLEMENT THIS FEATURE YET**

This document serves as a specification for future implementation. The feature requires:

- Google Cloud Project with Generative AI API enabled
- Gemini API key
- Message event listener
- Email collection workflow
- Integration with MIKROS Marketing API

---

## System Architecture

### 1. Message Detection (Google Gemini API)

```
User posts message in Discord
          ↓
Bot receives message event
          ↓
Message sent to Gemini API for intent classification
          ↓
If "game_launch_announcement" detected → Proceed
          ↓
Bot replies with promo code offer
```

### 2. Email Collection Workflow

```
Bot: "🎮 Would you like a MIKROS Marketing promo code?"
          ↓
User: "Yes" (button click)
          ↓
Bot: "Please DM me your email address"
          ↓
User DMs email
          ↓
Validate email format
          ↓
Call MIKROS Marketing API
          ↓
Send promo code to user
```

---

## Google Generative AI API Integration

### Request to Gemini API

**Endpoint**: `https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent`

**Headers:**

```
Content-Type: application/json
x-goog-api-key: YOUR_GEMINI_API_KEY
```

**Request Body:**

```json
{
  "contents": [{
    "parts": [{
      "text": "Analyze this Discord message and determine if the user is announcing their own game's release/launch date. Respond with JSON: {\"is_game_launch_announcement\": true/false, \"confidence\": 0-100, \"detected_date\": \"date or null\", \"reasoning\": \"brief explanation\"}\n\nMessage: \"My indie game is releasing on Steam next Friday!\""
    }]
  }],
  "generationConfig": {
    "temperature": 0.2,
    "topK": 1,
    "topP": 1,
    "maxOutputTokens": 200
  }
}
```

**Response:**

```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "{\"is_game_launch_announcement\": true, \"confidence\": 95, \"detected_date\": \"next Friday\", \"reasoning\": \"User is announcing their own game's release on Steam with a specific timeframe.\"}"
      }]
    }
  }]
}
```

### Prompt Engineering

**System Prompt:**

```
You are an AI assistant helping a Discord bot detect game launch announcements. 

Analyze Discord messages to determine if:
1. The user is announcing THEIR OWN game (not asking about others)
2. They mention a RELEASE or LAUNCH date (specific or approximate)
3. The context is about publishing/releasing a game

Return JSON with:
- is_game_launch_announcement (boolean)
- confidence (0-100)
- detected_date (string or null)
- reasoning (brief explanation)

Examples of TRUE:
- "My game launches Nov 5"
- "We're releasing our indie game next month"
- "Our game drops on Steam tomorrow"

Examples of FALSE:
- "What game releases Nov 5?" (question)
- "I wish my game was releasing" (hypothetical)
- "Game releases are expensive" (not about their game)
```

---

## MIKROS Marketing Discount API

### Endpoint: Register for Promo Code

**Method**: `POST`  
**URL**: `https://api.tatumgames.com/marketing/promo-code-request`

### Request Body

```json
{
  "discord_id": "293488128372",
  "discord_username": "IndieDev123",
  "email": "dev@example.com",
  "game_name": "Awesome Quest",
  "release_date": "2025-11-05",
  "platform": "Steam",
  "server_id": "01929381238",
  "server_name": "Indie Dev Community",
  "detected_message": "My game Awesome Quest releases on Steam Nov 5!",
  "timestamp": "2025-10-07T22:00:00Z",
  "campaign_source": "discord_bot_auto_detect"
}
```

### Response

**Status: 200 OK**

```json
{
  "success": true,
  "promo_code": "MIKROS-INDIE-A7B9C2",
  "discount_percentage": 20,
  "valid_until": "2025-12-31T23:59:59Z",
  "message": "Congratulations! Your promo code gives you 20% off MIKROS Marketing services.",
  "terms_url": "https://tatumgames.com/promo-terms",
  "how_to_redeem": "Visit tatumgames.com/marketing and enter code at checkout",
  "email_sent": true
}
```

**Status: 400 Bad Request**

```json
{
  "success": false,
  "error": "invalid_email",
  "message": "The email address provided is invalid"
}
```

**Status: 429 Too Many Requests**

```json
{
  "success": false,
  "error": "rate_limit_exceeded",
  "message": "This user has already requested a promo code in the last 30 days",
  "retry_after": "2025-11-06T00:00:00Z"
}
```

---

## Bot Implementation Flow

### Step 1: Message Event Listener

```java
@Override
public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) return;
    
    String content = event.getMessage().getContentRaw();
    
    // Only check messages that might be game announcements
    if (!containsGameKeywords(content)) return;
    
    // Call Gemini API to classify intent
    GameAnnouncementResult result = classifyMessage(content);
    
    if (result.isGameLaunchAnnouncement() && result.getConfidence() > 80) {
        offerPromoCode(event.getChannel(), event.getAuthor());
    }
}
```

### Step 2: Intent Classification

```java
public class GameAnnouncementResult {
    private boolean isGameLaunchAnnouncement;
    private int confidence;
    private String detectedDate;
    private String reasoning;
    
    // Getters...
}

public GameAnnouncementResult classifyMessage(String message) {
    // TODO: Implement Gemini API call
    // See Google Generative AI documentation
    
    String prompt = buildPrompt(message);
    String response = callGeminiAPI(prompt);
    return parseGeminiResponse(response);
}
```

### Step 3: Offer Promo Code

```java
private void offerPromoCode(MessageChannel channel, User user) {
    EmbedBuilder embed = new EmbedBuilder();
    embed.setTitle("🎮 MIKROS Marketing Promo Code");
    embed.setDescription(
        "Congratulations on your upcoming game release! " +
        "Would you like a **20% discount** on MIKROS Marketing services?"
    );
    embed.setColor(Color.GREEN);
    embed.addField("What You Get", 
        "• Professional marketing campaign\n" +
        "• Social media promotion\n" +
        "• Streamer outreach\n" +
        "• Press release distribution", 
        false);
    
    Button yesButton = Button.success("promo_yes", "Yes, send me the code!");
    Button noButton = Button.secondary("promo_no", "No thanks");
    
    channel.sendMessageEmbeds(embed.build())
           .setActionRow(yesButton, noButton)
           .queue();
}
```

### Step 4: Email Collection

```java
@Override
public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
    if (event.getComponentId().equals("promo_yes")) {
        event.reply(
            "Great! Please send me a **Direct Message** with your email address, " +
            "and I'll send you the promo code right away."
        ).setEphemeral(true).queue();
        
        // Set up DM listener for this user
        awaitingEmailFrom.add(event.getUser().getId());
    }
}

@Override
public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
    String userId = event.getAuthor().getId();
    
    if (awaitingEmailFrom.contains(userId)) {
        String email = event.getMessage().getContentRaw().trim();
        
        if (isValidEmail(email)) {
            processPromoCodeRequest(event.getAuthor(), email);
            awaitingEmailFrom.remove(userId);
        } else {
            event.getChannel().sendMessage(
                "❌ That doesn't look like a valid email. Please try again."
            ).queue();
        }
    }
}
```

### Step 5: Call MIKROS Marketing API

```java
private void processPromoCodeRequest(User user, String email) {
    // TODO: Call MIKROS Marketing API
    
    JsonObject payload = new JsonObject();
    payload.addProperty("discord_id", user.getId());
    payload.addProperty("discord_username", user.getName());
    payload.addProperty("email", email);
    payload.addProperty("campaign_source", "discord_bot_auto_detect");
    
    // Make HTTP POST request...
    
    user.openPrivateChannel().queue(channel -> {
        channel.sendMessage(
            "✅ **Promo Code Sent!**\n\n" +
            "Check your email for your MIKROS Marketing promo code.\n" +
            "Code: `MIKROS-INDIE-A7B9C2`\n" +
            "Discount: 20% off\n\n" +
            "Visit: https://tatumgames.com/marketing"
        ).queue();
    });
}
```

---

## Required Dependencies

### Gradle (build.gradle.kts)

```kotlin
dependencies {
    // Google Generative AI
    implementation("com.google.cloud:google-cloud-aiplatform:3.30.0")
    // or
    implementation("com.google.ai.client.generativeai:generativeai:0.1.0")
    
    // HTTP Client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // JSON Processing
    implementation("com.google.code.gson:gson:2.10.1")
}
```

### Environment Variables

```env
GOOGLE_GEMINI_API_KEY=your_gemini_api_key_here
MIKROS_MARKETING_API_KEY=your_mikros_api_key_here
```

---

## Privacy & Compliance

### Data Collection

- Discord ID and username
- Email address (with consent)
- Message content (for AI analysis only, not stored)
- Server ID (for analytics)

### User Consent

- Bot clearly states it will analyze messages
- User must explicitly click "Yes" button
- User must provide email in DM (additional consent step)
- Users can opt out any time

### Data Storage

- Emails encrypted at rest
- No message content stored permanently
- AI analysis logs retained for 7 days only
- Users can request data deletion (GDPR)

### Terms of Service

- User must accept MIKROS Marketing terms
- Promo codes are one per user per 30 days
- Codes expire after 90 days
- Standard refund policy applies

---

## Rate Limiting & Cost Management

### Gemini API Costs (Estimated)

- **Gemini Pro**: $0.0025 per 1K characters
- **Average message**: ~100 characters = $0.00025
- **1,000 messages**: ~$0.25

### Optimization Strategies

1. **Pre-filter with keywords**: Only send likely messages to API
2. **Cache results**: Cache classification for 24 hours
3. **Batch processing**: Analyze multiple messages together
4. **Rate limiting**: Max 100 classifications per hour per server
5. **Confidence threshold**: Only act on >80% confidence

### Message Pre-filtering

```java
private boolean containsGameKeywords(String message) {
    String lower = message.toLowerCase();
    return (lower.contains("game") || lower.contains("indie") || lower.contains("dev")) &&
           (lower.contains("release") || lower.contains("launch") || lower.contains("coming out"));
}
```

---

## Error Handling

```java
try {
    GameAnnouncementResult result = classifyMessage(content);
    if (result.isGameLaunchAnnouncement()) {
        offerPromoCode(channel, user);
    }
} catch (RateLimitException e) {
    logger.warn("Gemini API rate limit reached");
    // Fail silently - don't interrupt user experience
} catch (APIException e) {
    logger.error("Gemini API error", e);
    // Fall back to keyword matching as backup
} catch (Exception e) {
    logger.error("Unexpected error in game announcement detection", e);
}
```

---

## Testing Strategy

### Unit Tests

- Test email validation
- Test promo code format
- Test rate limiting logic

### Integration Tests

- Mock Gemini API responses
- Test full workflow with test Discord accounts
- Test error scenarios

### A/B Testing

- Compare AI detection vs keyword matching
- Measure false positive/negative rates
- Track conversion rates (offers → codes redeemed)

---

## Future Enhancements

1. **Multi-Language Support**: Detect game launches in any language
2. **Platform Detection**: Auto-detect Steam, Epic, itch.io, etc.
3. **Genre Detection**: Offer genre-specific marketing packages
4. **Budget Estimation**: AI suggests marketing budget based on game type
5. **Wishlist Integration**: Connect to Steam wishlists
6. **Launch Checklist**: Provide automated launch preparation checklist
7. **Community Building**: Offer Discord server setup services
8. **Streamer Matching**: Connect devs with relevant streamers
9. **Success Stories**: Share similar game launch case studies
10. **Analytics Dashboard**: Track marketing campaign performance

---

## Metrics to Track

1. **Detection Accuracy**
    - True positives
    - False positives
    - False negatives
    - Confidence score distribution

2. **Conversion Funnel**
    - Messages analyzed
    - Offers made
    - Buttons clicked
    - Emails collected
    - Codes redeemed
    - Marketing packages purchased

3. **User Experience**
    - Time to receive code
    - Email validation errors
    - User feedback/complaints
    - Opt-out rate

4. **Business Impact**
    - Revenue from promo codes
    - Cost per acquisition
    - Customer lifetime value
    - ROI of AI detection vs manual

---

## Example Message Scenarios

### Scenario 1: Clear Announcement ✅

**Message:** "Super excited! My first indie game 'Pixel Quest' launches on Steam November 15th!"

**AI Analysis:**

```json
{
  "is_game_launch_announcement": true,
  "confidence": 98,
  "detected_date": "November 15th",
  "reasoning": "User clearly announces their own indie game with specific platform and date"
}
```

**Bot Action:** ✅ Offer promo code

---

### Scenario 2: Question ❌

**Message:** "Does anyone know when Pixel Quest releases?"

**AI Analysis:**

```json
{
  "is_game_launch_announcement": false,
  "confidence": 95,
  "detected_date": null,
  "reasoning": "User is asking a question, not announcing their own game"
}
```

**Bot Action:** ❌ No action

---

### Scenario 3: Hypothetical ❌

**Message:** "I wish my game was releasing soon, but we're still in early development"

**AI Analysis:**

```json
{
  "is_game_launch_announcement": false,
  "confidence": 92,
  "detected_date": null,
  "reasoning": "User expresses desire but states game is not releasing soon"
}
```

**Bot Action:** ❌ No action

---

**Version**: 1.0  
**Last Updated**: October 7, 2025  
**Status**: 📋 Specification Complete - Implementation Pending  
**Dependencies**: Google Gemini API, MIKROS Marketing API  
**Estimated Implementation Time**: 2-3 weeks  
**Estimated Cost**: ~$10/month for AI API calls (at 10K messages/month)

