# Thunder Dome Web Portal + Game Network

This directory is a clean web companion for the Android game in the same repository. It does **not** replace the Android project.

## What is included

- `index.html` — mobile-first public game site with 24-stage catalog, live stats panel, leaderboard UI, multiplayer section, and reserved ad placement.
- `levels.json` — public stage catalog used by the site.
- `privacy.html`, `terms.html`, `support.html` — launch policy/support surfaces. Review them against the final production configuration before publishing.
- `worker/` — Cloudflare Worker API, D1 schema, and Durable Object WebSocket room relay.

## Recommended production stack

The design intentionally uses free-tier-first/open technology where practical, without promising that any third-party free tier will remain free forever.

| Need | Recommended component | License / cost model |
|---|---|---|
| Static site | Cloudflare Pages or any static host | Hosting plan dependent |
| Realtime room relay | Cloudflare Workers + Durable Objects | Platform service; usage limits/pricing apply |
| SQL stats | Cloudflare D1 | Platform service; usage limits/pricing apply |
| Player authentication | Supabase Auth | Supabase is open source; hosted free tier may be available |
| Android networking | OkHttp / Kotlin coroutines | Apache-2.0 |
| JSON | kotlinx.serialization | Apache-2.0 |
| Optional full game backend alternative | Nakama | Apache-2.0, self-hostable |
| Optional dedicated realtime alternative | Colyseus | MIT, self-hostable |
| Crash reporting | Firebase Crashlytics if already used by the app | Google service terms |

Do not put a database service-role key, Cloudflare token, AdSense secret, or other privileged credential in the Android APK or public JavaScript.

## Dynamic game data flow

1. The Android app authenticates the player.
2. The client sends a short-lived bearer token to `POST /api/game-event`.
3. The Worker validates the token against Supabase Auth.
4. Only allow-listed event types are accepted.
5. D1 stores event rows and updates public aggregate tables.
6. The website refreshes `/api/public-stats` and `/api/leaderboard` every 30 seconds.

Current event types: `sortie_complete`, `perfect_run`, `boss_defeated`, `multiplayer_complete`.

For competitive rewards, do not trust a client-provided score by itself. Move score calculation or verification to authoritative match logic before ranked launch.

## Multiplayer

`/ws/room/{roomId}` is a WebSocket room endpoint backed by a Durable Object. It authenticates the upgrade request and relays a strict allow-list of small JSON messages.

Allowed messages: `input`, `state`, `shot`, `hit`, `powerup`, `ready`, `ping`.

The current room is a transport foundation, not a finished anti-cheat authoritative simulation. Before ranked competitive launch, move hit validation, cooldowns, position bounds, match timers, scoring, disconnect outcomes, and reward decisions to server-authoritative room code.

## D1 setup

Create a D1 database with Wrangler, add its generated binding to `worker/wrangler.toml`, then apply:

```bash
wrangler d1 execute thunder-dome --file=./worker/schema.sql --remote
```

Set Worker secrets/vars outside source:

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `PUBLIC_SITE_ORIGIN`

## Website API URL

The static site intentionally ships with no invented production API address. At deployment time set:

```js
localStorage.setItem('thunderDomeApiBase', 'https://YOUR_REAL_DEPLOYED_WORKER_HOST')
```

For production, replace that runtime convenience with the final verified worker origin in a tiny deployment transform or same-origin route so users never need to set it themselves.

## AdSense readiness

The page contains a reserved ad area but intentionally does not load Google ads before an approved publisher ID exists.

Before requesting/using AdSense:

- publish substantial original Thunder Dome content beyond a single landing page;
- keep navigation, Privacy, Terms, and Support pages reachable;
- add a real support/contact route;
- publish accurate game screenshots, stage guides, patch notes, developer posts, and help content;
- add required cookie/consent handling for applicable regions;
- add the exact approved AdSense script only after a real publisher ID exists;
- keep ad placement away from gameplay-like buttons and deceptive UI;
- follow Google publisher and Google Play policies as they exist at launch time.

AdSense approval cannot be guaranteed by code alone.

## Monetization ideas that fit this site

- AdSense on content/guide pages after approval.
- First-party cosmetic showcase pages linking users back to the Google Play app where required.
- Sponsorship placements clearly labeled as sponsored.
- Affiliate links for gaming hardware only when disclosed and relevant.
- Community supporter page for non-app digital benefits, subject to platform/payment rules.
- Search-friendly stage guides, boss guides, ship-build articles, patch notes, tournaments, and developer logs to create durable organic traffic.

## Google Play alignment

The website can be used as the app's public support/privacy destination, but the final Play listing and app must match the site's claims. Digital goods sold inside the Android app must use the purchase method required by current Google Play policy unless an applicable exception exists.
