# API: Game Promotion Schedule

## Feature Overview

This API delivers a list of active indie games that should be promoted by the MIKROS Bot. The data includes timing control via deadlines, custom promotional messages, and asset links for rich embeds. The bot periodically fetches this data and posts promotions to configured Discord channels based on server-specific verbosity settings.

## Why This API is Needed

- **Automated Marketing**: Indie developers get automatic exposure across Discord communities
- **Timing Control**: Promotions only post after specified deadlines (e.g., after launch)
- **Customization**: Developers can provide custom messages or let the bot generate them
- **Tracking**: `isPushed` flag prevents duplicate promotions
- **Scalability**: Centralized system manages promotions across all MIKROS Bot instances
- **Quality Control**: Marketing team can curate which games get promoted and when

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/active-promotions`

### Query Parameters

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `limit` | integer | No | Maximum number of promotions to return (default: 50) |
| `since_id` | integer | No | Return only promotions with game_id greater than this |
| `platform` | string | No | Filter by platform: "steam", "itch", "epic", "all" (default: "all") |

### Authentication

**Method**: Bearer Token

```
Authorization: Bearer YOUR_BOT_API_KEY
```

---

## Sample Request

```http
GET /active-promotions?limit=10 HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_BOT_API_KEY
Content-Type: application/json
```

---

## Sample Response

**Status: 200 OK**

```json
[
  {
    "game_id": 1021,
    "game_name": "ShadowSprint",
    "description": "A neon-drenched, parkour runner set in a dystopian Tokyo.",
    "promotion_url": "https://store.steampowered.com/app/00000/shadowsprint",
    "promotion_message": null,
    "image_url": "https://cdn.example.com/shadowsprint.png",
    "deadline": "2025-10-08T18:00:00Z",
    "isPushed": false,
    "platform": "steam",
    "genre": "action",
    "developer": "NeonStudio",
    "price": "$14.99"
  },
  {
    "game_id": 1022,
    "game_name": "Pixel Raiders",
    "description": "Squad up and raid dungeons in this SNES-style online RPG.",
    "promotion_url": "https://tatumgames.com/pixel-raiders",
    "promotion_message": "🔥 Pixel Raiders is now live! Team up, loot up, and dive into the pixel madness! 🎮 Play now: https://tatumgames.com/pixel-raiders",
    "image_url": null,
    "deadline": "2025-10-10T16:00:00Z",
    "isPushed": false,
    "platform": "itch",
    "genre": "rpg",
    "developer": "PixelForge Games",
    "price": "Free"
  },
  {
    "game_id": 1023,
    "game_name": "Cosmic Harvest",
    "description": "Build and manage your own space farm on distant planets.",
    "promotion_url": "https://store.steampowered.com/app/00001/cosmic-harvest",
    "promotion_message": null,
    "image_url": "https://cdn.example.com/cosmic-harvest.jpg",
    "deadline": "2025-10-05T12:00:00Z",
    "isPushed": true,
    "platform": "steam",
    "genre": "simulation",
    "developer": "Orbital Studios",
    "price": "$19.99"
  }
]
```

---

## Response Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `game_id` | integer | Yes | Unique identifier for the game |
| `game_name` | string | Yes | Title of the game |
| `description` | string | Yes | Short marketing pitch or summary (max 500 chars) |
| `promotion_url` | string | Yes | Steam link, itch.io page, or MIKROS marketing landing page |
| `promotion_message` | string | No | Pre-written promotional message. If null, bot generates from template |
| `image_url` | string | No | Cover art, banner, or screenshot URL for embed image |
| `deadline` | ISO 8601 string | Yes | UTC datetime - only promote if current time is past this |
| `isPushed` | boolean | Yes | Whether promotion has been marked as pushed |
| `platform` | string | No | Game platform: "steam", "itch", "epic", etc. |
| `genre` | string | No | Game genre for categorization |
| `developer` | string | No | Developer/studio name |
| `price` | string | No | Game price (can be "Free", "$9.99", etc.) |

---

## Bot Behavior Logic

### When to Fetch

The bot fetches active promotions based on guild verbosity settings:

- **LOW**: Every 24 hours
- **MEDIUM**: Every 12 hours (default)
- **HIGH**: Every 6 hours

### Filtering Logic

For each returned promotion, the bot checks:

```java
if (!promotion.isPushed() && Instant.now().isAfter(promotion.getDeadline())) {
    // Ready to promote
    postPromotion(promotion);
    markAsPromoted(promotion.getGameId());
}
```

**Conditions:**
1. `isPushed == false` - Not yet promoted globally
2. Current UTC time is **after** `deadline`
3. Not already promoted in this specific guild (local tracking)

### Message Formatting

**If `promotion_message` is provided:**
```
Use the custom message as-is in the embed description
```

**If `promotion_message` is null:**
```
Generate template:

🚨 New indie gem alert!

{description}

👉 Play it here: {promotion_url}
```

**Embed Format:**
```java
EmbedBuilder embed = new EmbedBuilder();
embed.setTitle("🎮 " + gameName);
embed.setDescription(promotionMessage);
embed.addField("🔗 Link", promotionUrl, false);
if (imageUrl != null) {
    embed.setImage(imageUrl);
}
embed.setFooter("Powered by MIKROS Marketing");
```

---

## Marking Games as Pushed

After successfully posting a promotion, the bot should notify the backend:

**Endpoint**: `POST /mark-pushed`

**Request Body:**
```json
{
  "game_id": 1021,
  "bot_id": "your_bot_discord_id",
  "timestamp": "2025-10-07T22:30:00Z",
  "guilds_promoted": 15
}
```

**Response:**
```json
{
  "success": true,
  "message": "Game marked as pushed",
  "game_id": 1021,
  "total_reach": 45000
}
```

This allows the backend to:
- Update `isPushed` to `true`
- Track promotion reach and effectiveness
- Generate analytics for developers

---

## Error Handling

### HTTP 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Invalid or missing API key"
}
```

**Bot Action**: Log error, retry with backoff, alert admin if persistent

### HTTP 429 Too Many Requests
```json
{
  "error": "Rate limit exceeded",
  "retry_after": 60
}
```

**Bot Action**: Respect `retry_after` header, skip this check cycle

### HTTP 500 Server Error
```json
{
  "error": "Internal server error",
  "message": "Unable to fetch promotions"
}
```

**Bot Action**: Log error, retry after 5 minutes, continue normal operation

### Empty Response
```json
[]
```

**Bot Action**: No promotions available, this is normal behavior

---

## Rate Limiting

- **Standard**: 60 requests/hour per bot
- **Premium**: 300 requests/hour per bot
- **Burst**: 10 requests/minute

Rate limit headers:
```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 58
X-RateLimit-Reset: 1696723200
```

---

## Scalability Considerations

### Backend Infrastructure
- **Caching**: CDN-cached responses with 5-minute TTL
- **Database**: Indexed on `deadline`, `isPushed` for fast queries
- **Load Balancing**: Multiple API servers for redundancy
- **Monitoring**: Track API response times and error rates

### Bot Optimization
- **Batch Fetching**: Fetch all guilds' promotions in one request
- **Local Caching**: Cache fetched promotions for 1 hour
- **Async Processing**: Post promotions asynchronously
- **Error Recovery**: Continue operating even if API is down

### Future Enhancements
1. **WebSockets**: Real-time promotion push instead of polling
2. **Personalization**: Tailor promotions by guild genre preferences
3. **A/B Testing**: Test different message formats
4. **Analytics**: Track which promotions get the most engagement
5. **Scheduling**: Allow developers to schedule specific post times

---

## Security Notes

### API Keys
- Unique per bot instance
- Rotatable via dashboard
- Scope-limited to read-only for this endpoint
- Encrypted in transit (TLS 1.3)

### Data Privacy
- No personal user data in responses
- Guild IDs not shared with API
- Promotion tracking is anonymous

### Content Moderation
- All games reviewed before adding to promotion queue
- Profanity/spam filters applied to descriptions
- Admin dashboard to remove inappropriate content

---

## Example Bot Implementation

```java
public List<GamePromotion> fetchActivePromotions() {
    try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tatumgames.com/active-promotions?limit=50"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
            List<GamePromotion> promotions = new ArrayList<>();
            
            for (JsonElement element : jsonArray) {
                JsonObject json = element.getAsJsonObject();
                
                GamePromotion promotion = new GamePromotion(
                        json.get("game_id").getAsInt(),
                        json.get("game_name").getAsString(),
                        json.get("description").getAsString(),
                        json.get("promotion_url").getAsString(),
                        json.has("promotion_message") && !json.get("promotion_message").isJsonNull()
                                ? json.get("promotion_message").getAsString()
                                : null,
                        json.has("image_url") && !json.get("image_url").isJsonNull()
                                ? json.get("image_url").getAsString()
                                : null,
                        Instant.parse(json.get("deadline").getAsString()),
                        json.get("isPushed").getAsBoolean()
                );
                
                promotions.add(promotion);
            }
            
            logger.info("Fetched {} active promotions from API", promotions.size());
            return promotions;
            
        } else if (response.statusCode() == 429) {
            logger.warn("Rate limit exceeded, will retry later");
            return new ArrayList<>();
        } else {
            logger.error("Failed to fetch promotions: HTTP {}", response.statusCode());
            return new ArrayList<>();
        }
        
    } catch (Exception e) {
        logger.error("Error fetching promotions from API", e);
        return new ArrayList<>();
    }
}
```

---

## Testing & Development

### Mock Data for Development

Create a local mock endpoint or JSON file for testing:

```json
{
  "promotions": [
    {
      "game_id": 9999,
      "game_name": "Test Game",
      "description": "A test game for development",
      "promotion_url": "https://example.com/test",
      "promotion_message": null,
      "image_url": "https://via.placeholder.com/400x200",
      "deadline": "2025-01-01T00:00:00Z",
      "isPushed": false
    }
  ]
}
```

### Test Scenarios

1. **Normal Flow**: Promotion with passed deadline and isPushed=false
2. **Future Deadline**: Promotion that shouldn't post yet
3. **Already Pushed**: Promotion with isPushed=true
4. **Custom Message**: Promotion with custom promotional message
5. **No Image**: Promotion without image_url
6. **Empty Response**: API returns empty array
7. **API Error**: API returns 500 error

---

## Integration Checklist

- [ ] API endpoint URL configured
- [ ] API key stored securely in environment variables
- [ ] HTTP client configured with timeout (10s recommended)
- [ ] JSON parsing implemented
- [ ] Error handling for all HTTP status codes
- [ ] Rate limit respect implemented
- [ ] Local duplicate prevention (guild-level tracking)
- [ ] Promotion message formatting logic
- [ ] Embed creation with optional image
- [ ] Logging for debugging and monitoring
- [ ] Mark-as-pushed API call (if backend supports)

---

**Version**: 1.0  
**Last Updated**: October 7, 2025  
**Status**: 📋 Specification Complete - Implementation Pending  
**Integration Status**: TODOs placed in bot code for future implementation  
**Estimated Integration Time**: 2-4 hours once API is live

