# Future Promotion Scaling Roadmap

**Date:** 2025-01-27  
**Version:** 1.0  
**Status:** 🚀 **FUTURE ENHANCEMENT**

---

## Executive Summary

This document outlines a scalable, distributed promotion system that enables the MIKROS Bot to handle thousands of campaigns across hundreds of Discord servers without oversaturation or coordination overhead. The proposed architecture uses server registration, randomized campaign allocation, and independent server-side scheduling to achieve true horizontal scalability.

**Key Benefits:**
- ✅ Handle 10,000+ global campaigns without performance degradation
- ✅ Prevent oversaturation by limiting campaigns per server
- ✅ Align with business model (pay-per-exposure)
- ✅ Natural engagement distribution across servers
- ✅ Independent server scheduling (no global coordination)
- ✅ Horizontal scalability (add servers without bottlenecks)

---

## Current System Limitations

### Current Architecture
- **Global Campaign Pool:** All servers receive all active campaigns
- **Centralized Fetching:** Single `/getAllApps` endpoint returns all campaigns
- **Server-Side Filtering:** Each server filters and schedules independently
- **No Campaign Limits:** Server could theoretically post 100+ campaigns

### Scalability Issues
1. **API Response Size:** `/getAllApps` returns all campaigns (could be 1000+)
2. **Memory Usage:** Each server stores all campaigns in memory
3. **Processing Overhead:** Each server processes entire campaign list
4. **Oversaturation Risk:** Large servers could post too many promotions
5. **Business Model Mismatch:** Can't limit campaigns per server based on payment

### When Current System Breaks
- **100+ active campaigns:** API response becomes large, processing slows
- **50+ servers:** Each server processes same large dataset
- **Business requirements:** Need to limit campaigns per server (e.g., 3-5 per game)

---

## Proposed Architecture

### Core Concept: Distributed Campaign Allocation

Instead of all servers receiving all campaigns, the backend intelligently allocates campaigns to servers in batches. Each server only manages its assigned subset of campaigns, enabling true horizontal scalability.

```
┌─────────────────────────────────────────────────────────────┐
│                    Backend (Centralized)                     │
│  - Maintains global campaign pool (10,000+)                │
│  - Tracks active servers                                     │
│  - Allocates campaigns to servers (3-5 per game)           │
│  - Provides /getServerCampaigns endpoint                    │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ Allocates batches
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Server A    │   │  Server B    │   │  Server C    │
│  (UUID-123)  │   │  (UUID-456)  │   │  (UUID-789)  │
│              │   │              │   │              │
│  Batch: 15   │   │  Batch: 12   │   │  Batch: 18   │
│  campaigns   │   │  campaigns   │   │  campaigns   │
│              │   │              │   │              │
│  Schedules   │   │  Schedules   │   │  Schedules   │
│  independently│  │  independently│  │  independently│
└──────────────┘   └──────────────┘   └──────────────┘
```

---

## Implementation Phases

### Phase 1: Server Registration System

**Goal:** Enable backend to track active servers and assign campaigns.

#### 1.1 Server UUID Generation

**File:** `src/main/java/com/tatumgames/mikros/services/ServerRegistrationService.java` (NEW)

**Purpose:** Generate and manage unique server identifiers.

**Implementation:**
```java
public class ServerRegistrationService {
    private String serverUuid;
    private final String guildId;
    private final TatumGamesApiClient apiClient;
    
    /**
     * Generates or retrieves server UUID.
     * UUID is persistent per guild (stored in config or database).
     */
    public String getOrRegisterServerUuid() {
        if (serverUuid == null) {
            // Try to load from config/database
            serverUuid = loadStoredUuid();
            
            if (serverUuid == null) {
                // Generate new UUID
                serverUuid = UUID.randomUUID().toString();
                storeUuid(serverUuid);
            }
        }
        return serverUuid;
    }
    
    /**
     * Registers server with backend on bot startup.
     */
    public void registerWithBackend() {
        String uuid = getOrRegisterServerUuid();
        ServerRegistrationRequest request = new ServerRegistrationRequest(
            uuid,
            guildId,
            Instant.now(),
            getServerMetadata() // user count, activity level, etc.
        );
        
        apiClient.post("/registerServer", request, ServerRegistrationResponse.class);
    }
}
```

**Server Metadata:**
- Server UUID (persistent identifier)
- Guild ID (Discord server ID)
- Registration timestamp
- User count (optional)
- Activity level (optional)
- Promotion channel configured (boolean)

#### 1.2 Backend Registration Endpoint

**Endpoint:** `POST /registerServer`

**Request:**
```json
{
  "serverUuid": "550e8400-e29b-41d4-a716-446655440000",
  "guildId": "123456789012345678",
  "registeredAt": 1735689600,
  "metadata": {
    "userCount": 150,
    "activityLevel": "HIGH",
    "hasPromotionChannel": true
  }
}
```

**Response:**
```json
{
  "status": {
    "statusCode": 200,
    "statusMessage": "SUCCESS"
  },
  "data": {
    "serverUuid": "550e8400-e29b-41d4-a716-446655440000",
    "registered": true,
    "message": "Server registered successfully"
  }
}
```

**Backend Actions:**
- Store server in active servers list
- Mark server as available for campaign allocation
- Use metadata for capacity weighting (optional)

---

### Phase 2: Campaign Allocation System

**Goal:** Backend intelligently allocates campaigns to servers in batches.

#### 2.1 Allocation Algorithm

**Backend Logic (Pseudo-code):**
```python
def allocate_campaigns_to_servers(active_campaigns, active_servers):
    """
    Allocates campaigns to servers based on business rules.
    
    Rules:
    - Each campaign appears in N servers (based on payment plan)
    - Servers receive balanced batches (similar campaign counts)
    - Randomization prevents clustering
    """
    server_batches = {server_uuid: [] for server_uuid in active_servers}
    
    for campaign in active_campaigns:
        # Determine how many servers this campaign should appear in
        target_servers = campaign.get_target_server_count()  # e.g., 3-5
        
        # Randomly select servers (with optional weighting)
        selected_servers = random.sample(
            active_servers, 
            min(target_servers, len(active_servers)),
            weights=get_server_weights(active_servers)  # Optional
        )
        
        # Add campaign to selected servers' batches
        for server_uuid in selected_servers:
            server_batches[server_uuid].append(campaign)
    
    return server_batches
```

**Allocation Strategies:**

1. **Random Allocation (Default):**
   - Each campaign randomly assigned to N servers
   - Ensures fair distribution
   - Prevents clustering

2. **Weighted Allocation (Optional):**
   - Larger servers (more users) get more campaigns
   - Maximizes engagement per campaign
   - Business can prioritize high-value servers

3. **Geographic Allocation (Future):**
   - Allocate based on server region/language
   - Match campaigns to target demographics

#### 2.2 Server Campaign Endpoint

**Endpoint:** `GET /getServerCampaigns?serverUuid={uuid}`

**Request:**
```
GET /getServerCampaigns?serverUuid=550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer {API_KEY}
```

**Response:**
```json
{
  "status": {
    "statusCode": 200,
    "statusMessage": "SUCCESS"
  },
  "data": {
    "serverUuid": "550e8400-e29b-41d4-a716-446655440000",
    "campaigns": [
      {
        "appId": "hv-nemesis",
        "appName": "Heroes Vs Villains: Nemesis",
        "campaign": {
          "campaignId": "cmp_hv_nemesis_jan",
          "startDate": 1735689600,
          "endDate": 1735776000,
          ...
        },
        ...
      },
      // ... more campaigns (only this server's batch)
    ],
    "allocationTimestamp": 1735689600,
    "nextAllocationCheck": 1735776000
  }
}
```

**Key Features:**
- Returns only campaigns allocated to this server
- Much smaller response (15-20 campaigns vs 1000+)
- Includes allocation metadata for cache invalidation

**Caching Strategy:**
- Server caches batch until `nextAllocationCheck` timestamp
- Reduces API calls (fetch once per allocation period)
- Backend can update allocations periodically (e.g., hourly)

---

### Phase 3: Server-Side Scheduler Enhancement

**Goal:** Each server schedules only its allocated batch independently.

#### 3.1 Enhanced Scheduler Logic

**File:** `src/main/java/com/tatumgames/mikros/services/scheduler/GamePromotionScheduler.java`

**Changes:**

1. **Fetch Server Batch Instead of All Campaigns:**
   ```java
   // OLD: Fetch all campaigns
   List<AppPromotion> allApps = gamePromotionService.fetchAllApps();
   
   // NEW: Fetch only this server's batch
   List<AppPromotion> serverBatch = gamePromotionService.fetchServerCampaigns(serverUuid);
   ```

2. **Dynamic Interval Based on Batch Size:**
   ```java
   private long calculateDynamicCooldown(int batchSize) {
       int minIntervalMinutes = 5;
       int maxIntervalMinutes = 60;
       int maxBatchSize = 50;  // Threshold for max interval
       
       // Scale interval based on batch size
       double baseInterval = minIntervalMinutes + 
           ((maxIntervalMinutes - minIntervalMinutes) * 
            (batchSize / (double) maxBatchSize));
       
       // Clamp to min/max
       baseInterval = Math.max(minIntervalMinutes, 
                      Math.min(maxIntervalMinutes, baseInterval));
       
       // Add randomization (±20%)
       double randomFactor = 0.8 + (random.nextDouble() * 0.4);
       return (long) (baseInterval * randomFactor);
   }
   ```

3. **Batch Rotation:**
   ```java
   private AppPromotion getNextCampaignFromBatch(String guildId, List<AppPromotion> batch) {
       GameRotationState state = rotationStates.computeIfAbsent(guildId, k -> {
           GameRotationState newState = new GameRotationState();
           newState.gameQueue = new LinkedList<>();
           return newState;
       });
       
       // Rebuild queue if batch changed or empty
       if (state.gameQueue.isEmpty() || batchChanged(batch, state.lastBatchHash)) {
           rebuildQueueFromBatch(state, batch);
           state.lastBatchHash = calculateBatchHash(batch);
       }
       
       // Get next campaign (respects cooldown)
       return getNextFromQueue(state, batch);
   }
   ```

#### 3.2 Independent Scheduling

**Key Benefits:**
- Each server schedules independently (no global coordination)
- Natural staggering (servers post at different times)
- No single point of failure
- Easy horizontal scaling (add servers without impact)

**Scheduling Flow:**
```
Server A: [Campaign 1] → wait 12min → [Campaign 2] → wait 15min → [Campaign 3]
Server B: [Campaign 4] → wait 8min → [Campaign 5] → wait 20min → [Campaign 6]
Server C: [Campaign 7] → wait 10min → [Campaign 1] → wait 18min → [Campaign 8]

Result: Natural distribution, no coordination needed
```

---

## Business Model Alignment

### Pay-Per-Exposure Model

**Current Problem:**
- All servers receive all campaigns
- Can't limit exposure based on payment
- Oversaturation in large servers

**Proposed Solution:**
- Backend controls campaign allocation
- Each campaign specifies target server count (e.g., 3-5)
- Business pays for X exposures, gets exactly X server allocations
- No oversaturation, clear ROI tracking

**Example:**
```
Campaign: "Heroes Vs Villains: Nemesis"
Payment Plan: "Promote in 5 servers"
Backend Allocation: Randomly selects 5 servers
Result: Campaign appears in exactly 5 servers, no more
```

### Campaign Tiers

**Tier 1: Premium (10-20 servers)**
- Higher exposure
- Higher cost
- More server allocations

**Tier 2: Standard (3-5 servers)**
- Standard exposure
- Standard cost
- Default allocation

**Tier 3: Budget (1-2 servers)**
- Limited exposure
- Lower cost
- Minimal allocation

---

## Optional Enhancements

### 1. Server Capacity Weighting

**Concept:** Allocate more campaigns to servers with higher engagement.

**Implementation:**
```java
public class ServerCapacityWeight {
    private final int userCount;
    private final String activityLevel; // LOW, MEDIUM, HIGH
    private final boolean hasPromotionChannel;
    
    public double calculateWeight() {
        double weight = 1.0; // Base weight
        
        // User count multiplier
        if (userCount > 500) weight *= 2.0;
        else if (userCount > 200) weight *= 1.5;
        else if (userCount > 100) weight *= 1.2;
        
        // Activity level multiplier
        switch (activityLevel) {
            case "HIGH": weight *= 1.5; break;
            case "MEDIUM": weight *= 1.2; break;
            case "LOW": weight *= 0.8; break;
        }
        
        // Promotion channel configured
        if (!hasPromotionChannel) weight *= 0.1; // Almost exclude
        
        return weight;
    }
}
```

**Backend Allocation:**
- Use weights when randomly selecting servers
- Higher weight = higher probability of selection
- Ensures campaigns reach engaged audiences

### 2. Campaign Prioritization

**Problem:** Some campaigns might get "neglected" if allocation is purely random.

**Solution:** Rotate campaigns that haven't been posted recently.

**Implementation:**
```java
public class CampaignPrioritizer {
    /**
     * Prioritizes campaigns that haven't been posted recently.
     */
    public List<AppPromotion> prioritizeCampaigns(
            List<AppPromotion> campaigns,
            Map<String, Instant> lastPostTimes) {
        
        return campaigns.stream()
            .sorted((a, b) -> {
                Instant lastA = lastPostTimes.getOrDefault(a.getAppId(), Instant.EPOCH);
                Instant lastB = lastPostTimes.getOrDefault(b.getAppId(), Instant.EPOCH);
                return lastA.compareTo(lastB); // Older last post = higher priority
            })
            .collect(Collectors.toList());
    }
}
```

**Backend Logic:**
- Track last allocation time per campaign
- Prioritize campaigns with older allocations
- Ensures all campaigns get fair rotation

### 3. Dynamic Reallocation

**Problem:** Server goes offline or campaign expires, leaving gaps.

**Solution:** Backend automatically reassigns campaigns.

**Scenarios:**

1. **Server Goes Offline:**
   ```
   Server A (UUID-123) allocated 15 campaigns
   Server A goes offline (no heartbeat for 1 hour)
   Backend detects offline status
   Reallocates 15 campaigns to other active servers
   ```

2. **Campaign Expires:**
   ```
   Campaign "Game X" expires
   Backend removes from all server batches
   Servers fetch updated batch on next check
   ```

3. **New Campaign Added:**
   ```
   New campaign "Game Y" created
   Backend allocates to N servers
   Servers fetch updated batch on next check
   ```

**Implementation:**
- Backend maintains allocation state
- Periodic reallocation check (e.g., every 15 minutes)
- Servers fetch fresh batch periodically (e.g., hourly)

### 4. A/B Testing Support

**Concept:** Test different promotion strategies per server.

**Implementation:**
```json
{
  "campaigns": [
    {
      "appId": "game-a",
      "variants": [
        {
          "variantId": "variant-1",
          "messageTemplate": "template-a",
          "allocatedServers": ["server-1", "server-2"]
        },
        {
          "variantId": "variant-2",
          "messageTemplate": "template-b",
          "allocatedServers": ["server-3", "server-4"]
        }
      ]
    }
  ]
}
```

**Benefits:**
- Test message effectiveness
- Optimize promotion performance
- Data-driven campaign improvements

### 5. Analytics Integration

**Track Per Server:**
- Campaigns posted
- Engagement metrics (clicks, conversions)
- Server performance (best performing servers)

**Backend Analytics:**
- Global campaign performance
- Server allocation effectiveness
- ROI per campaign tier

**Endpoint:** `POST /trackPromotionEvent`
```json
{
  "serverUuid": "550e8400-...",
  "campaignId": "cmp_hv_nemesis_jan",
  "eventType": "POSTED|CLICKED|CONVERTED",
  "timestamp": 1735689600
}
```

---

## Migration Path

### Phase 1: Dual Mode (Backward Compatible)

**Duration:** 2-4 weeks

**Implementation:**
- Keep existing `/getAllApps` endpoint working
- Add new `/getServerCampaigns` endpoint
- Bot can use either (configurable)
- Test new system in parallel

**Configuration:**
```java
// In BotMain or config
boolean useDistributedAllocation = 
    config.getEnv("USE_DISTRIBUTED_ALLOCATION", "false").equals("true");

if (useDistributedAllocation) {
    // Use new system
    gamePromotionService = new DistributedGamePromotionService(apiClient, serverUuid);
} else {
    // Use existing system
    gamePromotionService = new RealGamePromotionService(apiClient);
}
```

### Phase 2: Gradual Rollout

**Duration:** 4-8 weeks

**Strategy:**
1. Enable for 10% of servers (beta testers)
2. Monitor performance and engagement
3. Gradually increase to 50%, then 100%
4. Keep old system as fallback

### Phase 3: Full Migration

**Duration:** 2 weeks

**Actions:**
- All servers use distributed allocation
- Deprecate `/getAllApps` for promotions (keep for admin tools)
- Remove old code paths
- Update documentation

---

## Technical Requirements

### Backend Requirements

1. **Server Registration Endpoint:**
   - `POST /registerServer` - Register/update server
   - `GET /getServerStatus?serverUuid={uuid}` - Check server status
   - `DELETE /unregisterServer?serverUuid={uuid}` - Remove server

2. **Campaign Allocation Endpoint:**
   - `GET /getServerCampaigns?serverUuid={uuid}` - Get server's batch
   - `POST /reallocateCampaigns` - Trigger reallocation (admin)

3. **Allocation Algorithm:**
   - Random selection with optional weighting
   - Campaign-to-server mapping storage
   - Reallocation logic

4. **Monitoring:**
   - Active server count
   - Campaign allocation distribution
   - Server health (heartbeat tracking)

### Bot Requirements

1. **Server UUID Management:**
   - Generate and persist UUID per guild
   - Register on bot startup
   - Periodic heartbeat (optional)

2. **Batch Fetching:**
   - Call `/getServerCampaigns` instead of `/getAllApps`
   - Cache batch until next allocation check
   - Handle batch updates gracefully

3. **Scheduler Updates:**
   - Use batch size for dynamic cooldown
   - Rotate through batch (not global pool)
   - Independent scheduling per server

---

## Performance Benefits

### Current System (1000 campaigns, 50 servers)

**API Calls:**
- 50 servers × 1 call/hour = 50 calls/hour
- Each call returns 1000 campaigns = 50,000 campaign objects/hour

**Memory Usage:**
- 50 servers × 1000 campaigns = 50,000 campaigns in memory
- Each campaign ~5KB = ~250MB total

**Processing:**
- Each server processes 1000 campaigns
- Filtering, scheduling overhead per server

### Proposed System (1000 campaigns, 50 servers)

**API Calls:**
- 50 servers × 1 call/hour = 50 calls/hour
- Each call returns ~15 campaigns = 750 campaign objects/hour
- **98.5% reduction in data transfer**

**Memory Usage:**
- 50 servers × 15 campaigns = 750 campaigns in memory
- Each campaign ~5KB = ~3.75MB total
- **98.5% reduction in memory usage**

**Processing:**
- Each server processes ~15 campaigns
- **98.5% reduction in processing overhead**

**Scalability:**
- Can handle 10,000+ campaigns without performance impact
- Add servers without increasing per-server load
- True horizontal scalability

---

## Risk Mitigation

### Risk 1: Backend Allocation Failure

**Mitigation:**
- Fallback to `/getAllApps` if `/getServerCampaigns` fails
- Cache last successful batch
- Log errors and alert

### Risk 2: Server UUID Loss

**Mitigation:**
- Persist UUID in config/database
- Regenerate if lost (backend handles duplicate gracefully)
- Include guild ID in registration for recovery

### Risk 3: Uneven Campaign Distribution

**Mitigation:**
- Monitor allocation distribution
- Implement prioritization (see enhancements)
- Backend can rebalance periodically

### Risk 4: Campaign Oversaturation in Single Server

**Mitigation:**
- Dynamic cooldown based on batch size
- Maximum campaigns per server (backend configurable)
- Server capacity weighting prevents overload

---

## Success Metrics

### Engagement Metrics
- **Click-through rate:** Should maintain or improve
- **Conversion rate:** Should maintain or improve
- **User complaints:** Should decrease (less spam)

### Performance Metrics
- **API response time:** Should decrease (smaller payloads)
- **Memory usage:** Should decrease significantly
- **CPU usage:** Should decrease (less processing)

### Business Metrics
- **Campaign ROI:** Should improve (better targeting)
- **Server satisfaction:** Should improve (less spam)
- **Scalability:** Can handle 10x more campaigns

---

## Conclusion

The proposed distributed campaign allocation system provides a scalable, business-aligned solution for managing promotions across hundreds of Discord servers. By allocating campaigns in batches and enabling independent server-side scheduling, we achieve:

- ✅ **True Horizontal Scalability:** Handle 10,000+ campaigns
- ✅ **Business Model Alignment:** Pay-per-exposure control
- ✅ **Natural Engagement Distribution:** No oversaturation
- ✅ **Resilience:** Independent server operation
- ✅ **Performance:** 98%+ reduction in data transfer and memory

This architecture positions the MIKROS Bot for long-term growth while maintaining engagement quality and business flexibility.

---

## References

- [API Game Promotion Schedule](./API_GAME_PROMOTION_SCHEDULE.md) - Current API documentation
- [TASKS_28.md](../TASKS_28.md) - Current promotion system implementation
- [API Integration Status](./API_INTEGRATION_STATUS.md) - Overall API integration status

