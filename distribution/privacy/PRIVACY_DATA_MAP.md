# THUNDER DOME — Google Play Data Safety & Privacy Data Map

## Google Play Data Safety Declaration Summary

| Data Category | Data Type | Collected? | Shared? | Purpose | Ephemeral / Stored |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Location** | Approximate / Precise | **No** | **No** | N/A | N/A |
| **Personal Info** | Name, Email, Phone, User IDs | **No** | **No** | Local callsign stored locally in Room DB only | Locally stored on device |
| **Financial Info** | Credit Card, Bank Info | **No** | **No** | Handled exclusively by Google Play Billing | Not accessible by App |
| **App Activity** | In-Game Events, Sortie Stats | **Yes (Anonymous)** | **No** | Analytics, Game Progression, Overclock Balancing | Stored locally / Aggregated |
| **App Performance** | Crash Logs, ANR Diagnostics | **Yes (Anonymous)** | **No** | App Functionality & Crash Debugging | Ephemeral crash buffer |
| **Device Identifiers** | Advertising ID (GAID) | **Yes** | **Yes (Google AdMob)** | Advertising & Fraud Prevention | Managed by Google Mobile Ads SDK |

---

## SDK Privacy Compliances

1. **Google Play Billing Library (`com.android.billingclient:billing:7.1.1`):**
   - Transacts purchases securely via Google Play APIs.
   - The app never captures, transmits, or stores credit card numbers, billing addresses, or banking credentials.
2. **Google Mobile Ads SDK (`com.google.android.gms:play-services-ads:23.6.0`):**
   - Uses Advertising ID for ad serving, frequency capping, and ad measurement.
   - Respects user consent (GDPR / UMP / CCPA) and family policy settings.
3. **Local Database Storage (Room v6):**
   - Player profiles, currency balances, unlocked aircraft, and settings are stored strictly in the private app sandbox on the local device.
4. **Cloud Save & Export:**
   - Cloud save JSON exports are strictly user-initiated and encrypted with standard JSON formatting, containing only gameplay level progress and equipment unlocks.
