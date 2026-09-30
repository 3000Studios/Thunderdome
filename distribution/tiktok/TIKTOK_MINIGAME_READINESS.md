# THUNDER DOME — TikTok Mini Games / WebGL Feasibility & Migration Guide

## Executive Feasibility Overview

TikTok Mini Games (TikTok Playables & Mini Programs) run within embedded HTML5 / WebGL / WebAssembly webviews inside the TikTok client. 

| Dimension | Native Android (Current Build) | TikTok Mini Game (Target Format) | Status & Compatibility |
| :--- | :--- | :--- | :--- |
| **Language / Engine** | Kotlin + Jetpack Compose + Canvas | JavaScript / TypeScript / WebAssembly (Wasm) | Requires Web Engine Target (Kotlin/Wasm or PixiJS/Phaser) |
| **Render Pipeline** | Android Canvas2D / Hardware Accelerated | WebGL 2.0 / HTML5 Canvas2D | High compatibility (GameRenderer logic matches WebGL canvas calls 1:1) |
| **Max Payload Size** | Unlimited (APK / AAB: 20-50MB) | 5MB–10MB initial download limit | Audio tracks must be compressed to 64kbps OGG/Opus or streamed on-demand |
| **Input Controls** | Direct PointerInput Touch & Gestures | Touch Event API (`touchstart`, `touchmove`, `touchend`) | 100% Direct Match |
| **Monetization** | Google Play Billing + AdMob | TikTok Pangle Rewarded Video Ads | Direct API mapping for Rewarded Revives & 2x Multipliers |

---

## Technical Migration Path to TikTok Mini Games

### Phase 1: Kotlin Multiplatform (KMP) & Kotlin/Wasm Compilation
1. **Model & Game Logic Layer:**
   - The entire `com.example.game.engine`, `WeaponSystem`, `EnemySystem`, `PhysicsSystem`, and `BossProfileCatalog` are written in pure Kotlin with minimal Android dependencies.
   - Using Kotlin Multiplatform (KMP), compile the game engine into WebAssembly (`wasmJs`) with zero logic rewrites.
2. **Render Layer Adaptation:**
   - Port `GameRenderer.kt` drawing methods to the HTML5 Canvas2D Context / WebGL batch renderer.
   - All particle simulations, starfields, perspective grids, and laser glow effects use simple math offsets and alpha circles, making them run at 60 FPS in any modern mobile browser.

### Phase 2: Asset Optimization for TikTok Mini Game Constraints
1. **Audio Streaming:**
   - Package 2 core music loops (Title & Combat) in initial bundle (~1.2MB compressed).
   - Stream additional 3000 Studios radio soundtracks on-demand via Cloudflare R2 / CDN.
2. **Bundle Trimming:**
   - Compress vector icons and UI textures to achieve a sub-8MB total zip bundle.

### Phase 3: TikTok Playable Ad Integration (MRAID / TikTok SDK)
1. Inject the TikTok Playable Ad lifecycle hooks:
   ```javascript
   // TikTok Mini Game / Playable Ad End Callback
   function onSortieComplete(score) {
       if (window.playableSDK) {
           window.playableSDK.sendReward();
       }
   }
   ```
2. Enable one-tap download redirect to the Google Play Store listing upon victory or game over.
