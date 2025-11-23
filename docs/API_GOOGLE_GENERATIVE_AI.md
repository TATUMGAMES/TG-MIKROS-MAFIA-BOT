# API: Google Generative AI Integration

## Overview
Integrate Google Generative AI (Gemini) for improved message classification and intent detection in promotional lead generation.

**Status:** 🔴 NOT IMPLEMENTED - Future Enhancement

---

## Purpose
Replace/augment regex-based pattern matching with AI-powered message understanding for:
- More accurate launch intent detection
- Multi-language support
- Context-aware classification
- Sentiment analysis
- Urgency detection

---

## Google AI API

### Endpoint
```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
```

### Authentication
```
Authorization: Bearer <GOOGLE_AI_API_KEY>
```

Or use API key in URL:
```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=<API_KEY>
```

---

## Request Format

### Example: Message Classification
```json
{
  "contents": [{
    "parts":[{
      "text": "Classify this Discord message for promotional intent. Message: 'We're launching our game on Steam next week! Been working on it for 2 years.' Response format: JSON with fields: has_launch_intent (boolean), campaign_type (string), urgency (string: low/medium/high), confidence (0-1)"
    }]
  }],
  "generationConfig": {
    "temperature": 0.2,
    "topK": 1,
    "topP": 1,
    "maxOutputTokens": 256
  }
}
```

### Response
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "{\n  \"has_launch_intent\": true,\n  \"campaign_type\": \"Game Launch\",\n  \"urgency\": \"high\",\n  \"confidence\": 0.95\n}"
      }],
      "role": "model"
    },
    "finishReason": "STOP"
  }]
}
```

---

## Integration Plan

### 1. Add Dependency
```gradle
dependencies {
    implementation 'com.google.cloud:google-cloud-aiplatform:3.30.0'
    // or
    implementation 'com.google.ai.client.generativeai:generativeai:0.1.0'
}
```

### 2. Service Enhancement

```java
// In PromoDetectionService

private static final String GOOGLE_AI_API_KEY = System.getenv("GOOGLE_AI_API_KEY");

/**
 * Uses Google Generative AI to detect launch intent.
 * Falls back to regex if API unavailable.
 * 
 * @param message the message text
 * @return classification result
 */
public AIClassificationResult detectsLaunchIntentAI(String message) {
    try {
        // Build prompt
        String prompt = String.format(
            "Classify this Discord message for game/product launch promotional intent.\n" +
            "Message: \"%s\"\n\n" +
            "Respond with JSON:\n" +
            "{\n" +
            "  \"has_launch_intent\": boolean,\n" +
            "  \"campaign_type\": string (Game Launch|Beta|Kickstarter|Marketing Help|None),\n" +
            "  \"urgency\": string (low|medium|high),\n" +
            "  \"confidence\": number (0-1),\n" +
            "  \"reasoning\": string\n" +
            "}",
            message
        );
        
        // Call API
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + GOOGLE_AI_API_KEY))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(prompt)))
            .build();
            
        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());
            
        if (response.statusCode() == 200) {
            return parseAIResponse(response.body());
        } else {
            logger.warn("AI API returned {}, falling back to regex", response.statusCode());
            return fallbackToRegex(message);
        }
        
    } catch (Exception e) {
        logger.error("Error calling AI API, falling back to regex", e);
        return fallbackToRegex(message);
    }
}

private AIClassificationResult fallbackToRegex(String message) {
    boolean detected = detectsLaunchIntent(message); // Current regex method
    return new AIClassificationResult(
        detected,
        detected ? "Unknown" : "None",
        "medium",
        detected ? 0.7 : 0.3,
        "Regex-based detection"
    );
}
```

### 3. Classification Result Model
```java
public class AIClassificationResult {
    private final boolean hasLaunchIntent;
    private final String campaignType;
    private final String urgency;
    private final double confidence;
    private final String reasoning;
    
    // Constructor, getters...
}
```

---

## Prompt Engineering

### Best Practices
1. **Be Specific:** Clearly define what you're looking for
2. **Provide Context:** Include domain knowledge (game dev, launches)
3. **Request Structure:** Ask for JSON or structured output
4. **Few-Shot Examples:** Include examples in prompt for better accuracy

### Optimized Prompt
```
You are an expert at detecting promotional intent in Discord messages from game developers.

Analyze this message and determine if the user is:
1. Launching a game/product
2. Running a campaign (Kickstarter, beta, etc.)
3. Seeking marketing help
4. None of the above

Message: "{message}"

Consider phrases like:
- "launching", "releasing", "going live"
- Mentions of Steam, Epic, platforms
- Campaign deadlines
- Marketing requests

Respond with JSON only:
{
  "has_launch_intent": boolean,
  "campaign_type": "Game Launch" | "Beta" | "Kickstarter" | "Marketing Help" | "None",
  "urgency": "low" | "medium" | "high",
  "confidence": 0.0 to 1.0,
  "key_phrases": ["phrase1", "phrase2"],
  "reasoning": "brief explanation"
}
```

---

## Rate Limits & Costs

### Google AI (Gemini Pro)
- **Free Tier:** 60 requests/minute
- **Paid Tier:** Higher limits based on plan
- **Cost:** ~$0.00025 per 1K characters (input + output)

### Recommendations
1. **Cache Results:** Store classifications for 24h to reduce API calls
2. **Batch Processing:** Process multiple messages if needed
3. **Confidence Threshold:** Only use AI for borderline cases; regex for obvious ones
4. **Fallback:** Always have regex as backup

---

## Multi-Language Support

### Language Detection + Translation
```json
{
  "contents": [{
    "parts":[{
      "text": "Detect language and translate to English if needed, then classify: '{message}'"
    }]
  }]
}
```

### Supported Languages
- English, Spanish, French, German, Portuguese
- Japanese, Korean, Chinese
- 100+ total languages

---

## Testing

### Test Prompts
```
1. "We're launching our game on Steam October 15th!"
   Expected: has_launch_intent=true, campaign_type="Game Launch", urgency="high"

2. "Our Kickstarter ends in 2 days, help us reach the goal!"
   Expected: has_launch_intent=true, campaign_type="Kickstarter", urgency="high"

3. "Looking for marketing help for my indie game"
   Expected: has_launch_intent=true, campaign_type="Marketing Help", urgency="medium"

4. "I love playing games on weekends"
   Expected: has_launch_intent=false, campaign_type="None", urgency="low"
```

### Accuracy Metrics
- Track false positives/negatives
- Compare AI vs regex detection rates
- Adjust confidence thresholds based on results

---

## Privacy & Ethics

### Data Handling
- ✅ Only send message content, not user IDs
- ✅ No PII (personally identifiable information)
- ✅ Messages processed in real-time, not stored by Google
- ✅ Comply with Discord ToS and privacy policies

### Opt-Out
- Users in servers with disabled promo detection won't have messages analyzed
- Admin controls via `/setup-promotions`

---

## Alternative: Self-Hosted Models

For complete control, consider:
1. **Hugging Face Transformers**
   - BERT, RoBERTa for classification
   - Self-hosted, no external API calls
   - Requires GPU/CPU resources

2. **OpenAI API**
   - GPT-3.5/4 for classification
   - Higher accuracy, higher cost
   - Similar integration to Google AI

3. **Local LLMs**
   - LLaMA, Mistral for on-premise deployment
   - No API costs
   - Requires significant infrastructure

---

## Implementation Checklist

- [ ] Obtain Google AI API key
- [ ] Add AI client library dependency
- [ ] Implement AIClassificationResult model
- [ ] Create AI detection method in PromoDetectionService
- [ ] Add fallback to regex if AI fails
- [ ] Implement caching layer
- [ ] Add configuration for AI enable/disable
- [ ] Test with diverse message samples
- [ ] Monitor API costs and usage
- [ ] Document accuracy metrics

---

## References
- [Google AI Studio](https://makersuite.google.com/app/apikey)
- [Gemini API Docs](https://ai.google.dev/docs)
- [Best Practices](https://ai.google.dev/docs/prompt_best_practices)
- [Pricing](https://ai.google.dev/pricing)

---

**Last Updated:** 2025-10-08  
**Status:** Planned for Future Implementation  
**Priority:** Medium (Regex currently sufficient)




