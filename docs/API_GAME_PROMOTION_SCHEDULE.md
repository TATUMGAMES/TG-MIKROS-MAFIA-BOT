# API: Game Promotion Schedule

## Feature Overview

This API delivers a list of active games/apps that should be promoted by the MIKROS Bot. The bot uses the `/getAllApps` endpoint to fetch app data with campaign information, then automatically schedules and posts promotions using a 4-step story format while respecting campaign dates and verbosity settings.

## Why This API is Needed

- **Automated Marketing**: Games get automatic exposure across Discord communities
- **Campaign Control**: Promotions only post during active campaign periods
- **4-Step Story Format**: Structured promotion flow (introduce → details → multi-game → final chance)
- **Spam Prevention**: Respects verbosity settings and minimum intervals
- **Scalability**: Centralized system manages promotions across all MIKROS Bot instances
- **Quality Control**: Marketing team can curate which games get promoted and when

---

## Request Details

**Method**: `GET`  
**URL**: `https://api.tatumgames.com/getAllApps`

### Authentication

**Method**: Bearer Token

```
Authorization: Bearer YOUR_BOT_API_KEY
```

---

## Sample Request

```http
GET /getAllApps HTTP/1.1
Host: api.tatumgames.com
Authorization: Bearer YOUR_BOT_API_KEY
Content-Type: application/json
```

---

## Sample Response

**Status: 200 OK**

```json
{
  "status": {
    "statusCode": 200,
    "statusMessage": "SUCCESS"
  },
  "data": {
    "apps": [
      {
        "appId": "hv-nemesis",
        "appGameId": "tg-nemesis-001",
        "appName": "Heroes Vs Villains: Nemesis",
        "shortDescription": "Auto-battler game with idle progression",
        "longDescription": "Guide your Guardian through levels, defeat enemies, collect rewards. Idle progression allows your character to grow while offline.",
        "gameGenre": "Action",
        "gameplayType": "Casual",
        "contentGenre": "Adventure",
        "contentTheme": "Fantasy",
        "campaign": {
          "campaignId": "cmp_hv_nemesis_jan",
          "campaignName": "January Promo",
          "startDate": 1735689600,
          "endDate": 1735776000,
          "images": [
            { "appLogo": "https://cdn.example.com/hv-nemesis.png" }
          ],
          "ctas": {
            "google_store": "https://play.google.com/store/apps/details?id=com.tatumgames.nemesis",
            "apple_store": "https://apps.apple.com/app/heroes-vs-villains-nemesis/id123456",
            "steam_store": "https://store.steampowered.com/app/123456/",
            "samsung_store": "https://apps.samsung.com/appquery/appDetail.as?appId=com.tatumgames.nemesis",
            "amazon_store": "http://www.amazon.com/gp/mas/dl/android?p=com.tatumgames.nemesis",
            "website": "https://tatumgames.com/",
            "other": "https://tatumgames.com/"
          },
          "screenshotUrls": [
            "https://cdn.example.com/ss1.png",
            "https://cdn.example.com/ss2.png"
          ],
          "videoUrls": [
            "https://cdn.example.com/vid1.mp4",
            "https://cdn.example.com/vid2.mp4"
          ],
          "socialMedia": {
            "facebook": "http://www.facebook.com/tatumgames",
            "x": "https://twitter.com/tatumgames",
            "instagram": "https://instagram.com/tatumgames",
            "linkedin": "https://www.linkedin.com/company/tatum-games-llc",
            "tiktok": "https://tiktok.com/@tatumgames",
            "youtube": "http://www.youtube.com/user/TatumGamesLLC",
            "discord": "https://discord.gg/tatumgames",
            "twitch": "https://twitch.tv/tatumgames"
          }
        }
      }
    ]
  }
}
```

---

## Response Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `status` | object | Yes | Response status information |
| `status.statusCode` | integer | Yes | HTTP status code (200 for success) |
| `status.statusMessage` | string | Yes | Status message ("SUCCESS") |
| `data` | object | Yes | Response data container |
| `data.apps` | array | Yes | List of app promotions |

### App Object Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `appId` | string | Yes | Unique app identifier (e.g., "hv-nemesis") |
| `appGameId` | string | Yes | Game ID (e.g., "tg-nemesis-001") |
| `appName` | string | Yes | Display name of the app |
| `shortDescription` | string | Yes | Brief description for step 1 and 4 |
| `longDescription` | string | Yes | Detailed description for step 2 |
| `gameGenre` | string | No | Game genre (e.g., "Action", "Strategy") |
| `gameplayType` | string | No | Gameplay type (e.g., "Casual", "Card") |
| `contentGenre` | string | No | Content genre (e.g., "Adventure") |
| `contentTheme` | string | No | Content theme (e.g., "Fantasy") |
| `campaign` | object | Yes | Campaign information |

### Campaign Object Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `campaignId` | string | Yes | Unique campaign identifier |
| `campaignName` | string | Yes | Campaign display name |
| `startDate` | integer | Yes | Unix timestamp (seconds) - campaign start |
| `endDate` | integer | Yes | Unix timestamp (seconds) - campaign end |
| `images` | array | No | List of image objects |
| `images[].appLogo` | string | No | App logo URL |
| `ctas` | object | Yes | Call-to-action links |
| `ctas.google_store` | string | No | Google Play Store URL |
| `ctas.apple_store` | string | No | Apple App Store URL |
| `ctas.steam_store` | string | No | Steam Store URL |
| `ctas.samsung_store` | string | No | Samsung Store URL |
| `ctas.amazon_store` | string | No | Amazon Appstore URL |
| `ctas.website` | string | No | Website URL |
| `ctas.other` | string | No | Other store/platform URL |
| `screenshotUrls` | array | No | List of screenshot URLs |
| `videoUrls` | array | No | List of video URLs |
| `socialMedia` | object | No | Social media links |
| `socialMedia.facebook` | string | No | Facebook page URL |
| `socialMedia.x` | string | No | Twitter/X profile URL |
| `socialMedia.instagram` | string | No | Instagram profile URL |
| `socialMedia.linkedin` | string | No | LinkedIn company URL |
| `socialMedia.tiktok` | string | No | TikTok profile URL |
| `socialMedia.youtube` | string | No | YouTube channel URL |
| `socialMedia.discord` | string | No | Discord server invite URL |
| `socialMedia.twitch` | string | No | Twitch channel URL |

---

## Bot Behavior Logic

### When to Fetch

The bot checks for new promotions every 60 minutes. However, actual posting respects guild verbosity settings:

- **LOW**: Posts only if 24+ hours since last check
- **MEDIUM**: Posts only if 12+ hours since last check (default)
- **HIGH**: Posts only if 6+ hours since last check

### 4-Step Promotion Story Format

The bot posts 4 promotions per app across the campaign period:

1. **Step 1: Introduce the game** (at campaign start)
   - Uses `shortDescription`
   - Template: "🎮 Introducing <app_name>! <short_description>"

2. **Step 2: Add more details** (33% through campaign)
   - Uses `longDescription`
   - Template: "Dive deeper into <app_name>: <long_description>"

3. **Step 3: Multiple games promotion** (66% through campaign, only if 2+ games exist)
   - Combines multiple active apps
   - Template: "🌟 MIKROS Top Picks for this month: <game_list>"
   - Only posts once per guild, not per app

4. **Step 4: Final chance** (90% through campaign)
   - Uses `shortDescription`
   - Template: "⏰ Last chance to check out <app_name>! <short_description>"

### Filtering Logic

For each app, the bot checks:

```java
// 1. Campaign is active
if (now.isAfter(campaign.startDate) && now.isBefore(campaign.endDate)) {
    // 2. Minimum 24-hour interval since last post
    if (lastPostTime == null || now.isAfter(lastPostTime.plus(24, HOURS))) {
        // 3. Step timing is correct
        if (now.isAfter(stepTargetTime)) {
            // Post promotion
        }
    }
}
```

**Conditions:**
1. Campaign is active (current time between `startDate` and `endDate`)
2. Minimum 24 hours since last post for this app in this guild
3. Step target time has been reached
4. Verbosity check passed (enough time since last check)

### Message Formatting

**Step 1, 2, 4 (Single App):**
- Random template selection from step-specific templates
- Placeholder replacement: `<app_name>`, `<short_description>`, `<long_description>`
- At least one CTA link (randomly selected from available CTAs)
- Optional social media link (~30% chance)
- App logo image if available

**Step 3 (Multi-Game):**
- Combines all active apps in campaign
- Lists each app with short description and primary CTA
- Social media links from first app

**Embed Format:**
```java
EmbedBuilder embed = new EmbedBuilder();
embed.setTitle("🎮 " + appName);
embed.setDescription(formattedMessage);
embed.addField("🔗 Links", ctaLinks, false);
if (socialMediaLink != null) {
    embed.addField("📱 Follow Us", socialMediaLink, false);
}
if (appLogo != null) {
    embed.setImage(appLogo);
}
embed.setFooter("Powered by MIKROS Marketing");
embed.setTimestamp(Instant.now());
```

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
  "message": "Unable to fetch apps"
}
```

**Bot Action**: Log error, retry after 5 minutes, continue normal operation

### Empty Response
```json
{
  "status": { "statusCode": 200, "statusMessage": "SUCCESS" },
  "data": { "apps": [] }
}
```

**Bot Action**: No apps available, this is normal behavior

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

## Current Implementation Status

- ✅ Stub JSON file (`/stubs/getAllApps.json`) for development
- ✅ AppPromotion model with nested Campaign, CTAs, SocialMedia classes
- ✅ 4-step promotion logic in `PromotionStepManager`
- ✅ Message templates (10 templates, developer to add 10 more)
- ✅ Verbosity enforcement in scheduler
- ⏳ Real API integration (TODO: Replace stub with HTTP client)

---

## Integration Checklist

- [x] Data models created (`AppPromotion`, `Campaign`, `CTAs`, `SocialMedia`)
- [x] Stub JSON loading implemented
- [x] 4-step promotion logic implemented
- [x] Verbosity enforcement implemented
- [ ] API endpoint URL configured
- [ ] API key stored securely in environment variables
- [ ] HTTP client configured with timeout (10s recommended)
- [ ] JSON parsing for real API response
- [ ] Error handling for all HTTP status codes
- [ ] Rate limit respect implemented
- [ ] Local duplicate prevention (guild-level tracking)
- [ ] Promotion message formatting logic
- [ ] Embed creation with optional image
- [ ] Logging for debugging and monitoring

---

**Version**: 2.0  
**Last Updated**: 2025-10-08  
**Status**: 📋 Specification Complete - Stub Implementation Complete  
**Integration Status**: Stub JSON in use, real API integration pending  
**Estimated Integration Time**: 1-2 hours once API is live
