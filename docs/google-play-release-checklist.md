# THUNDER DOME // 3000 STUDIOS
## Google Play Store Production & Beta Release Specification

**Document Version:** 1.0.0  
**Target Release Date:** Q4 2026 / 2027  
**Distribution Platform:** Google Play Store (Production Track & Closed/Open Beta)  
**Promotional Funnels:** TikTok (@3000studios), Gumroad, YouTube Short-Form  

---

### 1. Application & Package Identification
* **Application / Package ID:** `com.aistudio.aerostrike.xrkfpz` *(Preserved to retain existing Google Play Console registry mapping and service linking)*
* **Public Game Title:** `THUNDER DOME`
* **Developer / Publisher Entity:** `3000 STUDIOS`
* **Compile SDK:** `36` (Android 16 API Level 36 Ready)
* **Target SDK:** `36` (Android 16 API Level 36 Ready)
* **Minimum SDK:** `26` (Android 8.0 Oreo)
* **Target Architecture:** `arm64-v8a`, `armeabi-v7a`, `x86_64`
* **Current Version Code:** `1` (or incremental CI/CD release counter)
* **Current Version Name:** `1.0.0`

---

### 2. Store Listing Metadata

#### Title:
`Thunder Dome`

#### Short Description (80 chars max):
`High-octane futuristic arcade combat flight. Dominate the skies in Thunder Dome!`

#### Full Description:
```text
WELCOME TO THE THUNDER DOME // 3000 STUDIOS

Take command of the ultimate combat warbirds in high-octane futuristic arcade dogfights across 24 hostile planetary sectors. Dodge bullet-hell salvos with silky-smooth relative touch controls, unleash devastating secondary weapon systems, execute evasive barrel rolls, and confront 24 colosseum overlords culminating in the cataclysmic NEXUS OBLITERATOR.

KEY FEATURES:

• 24 HANDCRAFTED SECTORS: Fly through Neon Metropolises, Volcanic Canyons, Toxic Biospheres, Dimensional Rifts, and the core Arena.
• 26 PILOTABLE WARBIRDS: Unlock distinct fighters including tactical prototypes and legendary secret craft (Queen Bee Jerica & Apex Sovereign Jadon).
• 24 COLOSSEUM BOSS OVERLORDS: Multi-phase tactical boss battles with destructible armor hardpoints, unique attack routines, and holo-taunt comms.
• DEEP HANGAR UPGRADES & PERK DRAFTING: Customize primary plasma cannons, swarm seeker missiles, EMP shockwaves, and tactical overclocks.
• HIGH-FIDELITY FLIGHT VIEWS: Toggle seamlessly between Cinematic 3rd-Person Chase View, Cockpit 1st-Person HUD, and Top-Down Recon.
• PURE ARCADE ACTION: Zero paywalls to win. Fair economy with rewarding mission payouts and daily combat challenges.

Gear up, pilot. The Thunder Dome awaits.
```

---

### 3. Google Play Data Safety Declarations

* **Data Collected:** None. (Zero personal info, zero location tracking, zero contact list access).
* **Data Shared:** None.
* **Security Practices:**
  * All player progression, currency balances, and ship unlocks are stored locally on-device using encrypted Room SQLite database storage (`aerostrike_database.db`).
  * Cloud save synchronization utilizes standard authenticated Google Play Games Services / Firebase Firestore endpoints.
* **Advertising / Analytics:**
  * Uses Google Mobile Ads SDK (AdMob) for optional rewarded continue & double-credit rewards.
  * Compliant with Google Play Advertising ID policy.

---

### 4. AdMob Ad Unit Configuration (Test & Production)

| Ad Type | Google Test Unit ID | Production Placement ID |
| :--- | :--- | :--- |
| **Rewarded Video (Revive / 2x Loot)** | `ca-app-pub-3940256099942544/5224354917` | `ca-app-pub-xxxxxxxxxxxxxxxx/revive_slot` |
| **Interstitial (Mission End)** | `ca-app-pub-3940256099942544/1033173712` | `ca-app-pub-xxxxxxxxxxxxxxxx/interstitial_slot` |
| **Banner (Hangar / Bottom)** | `ca-app-pub-3940256099942544/6300978111` | `ca-app-pub-xxxxxxxxxxxxxxxx/banner_slot` |

---

### 5. Google Play In-App Billing (IAP) SKU Catalog

| Product ID | SKU Type | Display Title | Price Tier | Description |
| :--- | :--- | :--- | :--- | :--- |
| `bundle_founder_edition` | Non-Consumable | Founder Edition All-Access | $9.99 | Permanent Ad-Free, 50k Credits, 500 Cores, Exclusive Gold Hangar Skins |
| `pilot_credits_small` | Consumable | 25,000 Tactical Credits | $0.99 | Instant credit boost for hangar upgrades |
| `pilot_credits_medium` | Consumable | 100,000 Tactical Credits | $2.99 | Premium credit cache |
| `pilot_credits_large` | Consumable | 500,000 Tactical Credits | $9.99 | Ultimate combat war chest |
| `plasma_cores_pack` | Consumable | 500 Plasma Cores | $4.99 | Overclock cores for advanced weapon modifications |
| `battlepass_premium` | Subscription / IAP | Vanguard Combat Pass | $4.99 | Seasonal tier rewards and exclusive craft unlocks |

---

### 6. Build Artifacts & Verification

* **Debug APK Build:** `app/build/outputs/apk/debug/app-debug.apk`
* **Production Release AAB (Android App Bundle):** `app/build/outputs/bundle/release/app-release.aab`
* **Signing Config:** Configure production keystore via environment variables (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) or Google Play App Signing.
