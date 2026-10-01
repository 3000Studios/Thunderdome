# Thunder Dome — Antigravity 3D Stage Design Brief
**Repo:** `3000Studios/Thunderdome` (Gradle: AeroStrike / `com.aistudio.aerostrike.xrkfpz`)  
**Owner brand:** 3000 Studios  
**Camera:** Top-down (and TOP_DOWN_CHASE / 3RD FOLLOW / COCKPIT_1ST) vertical mobile scroller  
**Goal:** Implement 24 campaign stages + 1 BONUS warp-tunnel stage + polished home title screen using concept sheets + existing Kotlin catalogs.

## Deliverable images (give these to Antigravity)

| File | Content |
|------|---------|
| `TD_Stages_01-06_Antigravity_3D_Brief.jpg` | Stages 01–06 with palettes, weather, hazards, ground layers |
| `TD_Stages_07-12_Antigravity_3D_Brief.jpg` | Stages 07–12 |
| `TD_Stages_13-18_Antigravity_3D_Brief.jpg` | Stages 13–18 |
| `TD_Stages_19-24_Antigravity_3D_Brief.jpg` | Stages 19–24 (includes Apex Colosseum) |
| Existing repo art | `app/src/main/res/drawable/thunder_dome_24_missions.jpg` |
| | `thunder_dome_24_planes_roster.jpg` |
| | `thunder_dome_24_enemy_planes.jpg` |

**Style lock:** Neon cyber AAA, glossy fighters, high-contrast biome palettes, weather-forward, match plane roster materials (PBR metals, emissive edges, bloom).

**Shared 3D rules (all stages)**
- Vertical scroll playfield; player fighter scale ≈ 1.0 unit (silhouettes on sheets).
- Parallax layers: sky/weather → far props → mid hazards → ground scroll → near FX.
- Dynamic weather particles from `BiomeSpec.weatherType` (see EnvironmentSystem).
- Obstacle types already in code: `LASER_BARRIER`, `ASTEROID_PILLAR`, `GREEN_GOO`, `WIND_GUST` + biome-specific SectorSupportCatalog hazards.
- Target 60–120 FPS mobile; particle-optimized; no letterboxing unless cinematic intro.

## The 24 Stages (source of truth = BiomeCatalog + BossProfileCatalog)

| # | ID | Name | Weather | Boss | Key 3D look |
|---|----|------|---------|------|-------------|
| 01 | neon_outpost | Neon Outpost | NEON_RAIN | Neon Overlord | Wet neon canyons, AA turrets, holo ads |
| 02 | asteroid_belt | Asteroid Belt | SPACE_DUST | Goliath Dreadnought | Rock field, magnetic mines |
| 03 | void_gate | Void Gate | VOID_WARP | Void Stalker | Portal ring, gravity shear |
| 04 | toxic_sector | Toxic Sector | TOXIC_MIST | Toxin Haze | Green gas, corroded industry |
| 05 | ice_fortress | Ice Fortress | BLIZZARD | Frost Nova | Glacial citadel, ice spikes |
| 06 | solar_core | Solar Core | SOLAR_FLARES | Solar Flare | Corona platform, heat bloom |
| 07 | cyber_city | Cyber City | NEON_RAIN | Void Reaper | Mega trench skyscrapers |
| 08 | junkyard | Junkyard | RUST_STORM | Blade Storm | Capital hull scrap |
| 09 | black_hole | Black Hole | GRAVITY_PULL | Neon Phantom | Singularity pull, lensing |
| 10 | lava_planet | Lava Planet | LAVA_RAIN | Omega Drone | Magma rivers, factory silhouettes |
| 11 | orbital_array | Orbital Array | ION_DUST | Liquid Metal | Satellite ring, laser sweeps |
| 12 | sand_wastes | Sand Wastes | SAND_STORM | Sand Viper | Dunes, EM sand tornadoes |
| 13 | bio_labs | Bio Labs | BIO_SPORES | Biosynth Hydra | Mutagen domes, holy-green tubes |
| 14 | underwater_ruins | Underwater Ruins | HYDRO_STREAM | Aqua Strike | Abyss caustics, sunken city |
| 15 | sky_temple | Sky Temple | AURA_BREEZE | Celestial Guard | Floating isles, god rays |
| 16 | machine_world | Machine World | SMELTER_ASH | Magma Brute | Foundry core, crushers |
| 17 | crystal_caverns | Crystal Caverns | CRYSTAL_DUST | Cyber Hawk | Refractive crystals, laser bounce |
| 18 | storm_front | Storm Front | LIGHTNING | Quantum Shift | Lightning curtains, chrono clouds |
| 19 | alien_jungle | Alien Jungle | NEURO_MIST | Storm Lord | Bio canopy, vine snares |
| 20 | space_graveyard | Space Graveyard | DEBRIS_HAZARD | Gravity Titan | Wreck fleets, sniper corridors |
| 21 | dimension_rift | Dimension Rift | PHASE_SHIFT | Crystal Revenant | Fractured multiverse seams |
| 22 | the_citadel | The Citadel | FLAK_BURSTS | Citadel Commander | Fortress rings, flak walls |
| 23 | final_approach | Final Approach | WAR_HAZE | Inferno Rider | Flagship armada line |
| 24 | thunder_dome | Thunder Dome | COLOSSEUM_LIGHTNING | Nexus Obliterator | Circular golden 1v1 arena |

Code hooks:
- `BiomeCatalog.ALL_BIOMES` in `GameSpecs.kt`
- `BossProfileCatalog.PROFILES` (index 1..24)
- `SectorSupportCatalog.PROFILES` (mini-enemies, obstacles, powerups, atmosphere for 13+)
- `EnvironmentSystem.initWorld(biome)` for weather/obstacles

## BONUS Stage — Warp Tunnel (lights up)

**Name:** Bonus Warp Tunnel / Vortex Descent  
**Trigger:** When campaign warps between sectors (or after stage clear bonus roll).  
**Audio already in repo:** `app/src/main/res/raw/bonus_vortex_tunnel.wav`, `track_bonus_stage_trippy.mp3`

### Visual behavior (Antigravity must implement)
1. **Entry:** Player craft pitches into screen; camera FOV punches; world folds into tube.
2. **Lights up:** Tunnel rings start dim then cascade ignite cyan → magenta → gold as `warpProgress 0→1`.
3. **Mesh:** Cylinder / torus-stack with UV scroll + emissive hex grid; concentric rings pulse.
4. **Pickups:** Glowing credit/cores lanes down the tube center.
5. **Exit:** Whiteout flash → next Biome loads.

### Spec object
See `app/src/main/java/com/example/game/model/BonusStageSpec.kt`

### Antigravity checklist
- [ ] `EnvironmentSystem` mode `WARP_TUNNEL`
- [ ] `GameRenderer` tube + ring bloom path
- [ ] `VFXSystem` light-speed streaks
- [ ] Light-up intensity driven by warp progress
- [ ] Mission select or post-clear BONUS UNLOCKED UI pulse (gold border)

## Home Title Screen — design + UX polish

### Current state (`TitleScreen.kt`)
**Strengths:** Strong brand (3000 STUDIOS // CYBER DEFENSE), Canvas warbird wireframe, radar sweep, HUD resource module, mode buttons (Campaign / Co-op / 1v1), Hangar + Pass, Depot store CTA, camera and scale quick toggles.

**UX issues to fix**
1. Vertical overcrowding on short phones — CAM/SCALE row + 5 primary actions fight for space.
2. Primary hierarchy — Campaign should be largest; Co-op secondary; PvP tertiary; Hangar/Pass compact.
3. Missing: Settings entry on title, last-played sector, Continue Sortie, bonus tunnel teaser badge.
4. Accessibility: Ensure semantics on all buttons; keep ≥44dp height.
5. Motion: Respect reduced motion — gate radar/warbird yaw animations.
6. Mission select: mode chips too cramped; biome cards need stage thumbnail + weather icon strip; add progress lock icons.

### Polished title IA (top→bottom)
1. Brand lockup + version
2. HUD resource bar
3. Hero warbird / radar (reduced height ~28% screen)
4. Primary: Campaign Sortie (full width, cyan)
5. Secondary row: Co-op | 1v1 (half)
6. Tertiary row: Hangar | Pass | Settings
7. Depot founder strip
8. Optional: BONUS TUNNEL ARMED chip if flag set
9. Footer system ready

## Implementation order for Antigravity
1. Import 4 stage brief JPGs into drawable or docs.
2. Extend `EnvironmentSystem` with biome-specific ground kits (not only generic buildings).
3. Wire `SectorSupportCatalog` atmosphere colors into `GameRenderer` fog/sky.
4. Build BonusWarpTunnel environment mode + light-up.
5. Polish TitleScreen hierarchy + Settings entry + reduced motion.
6. Upgrade MissionSelectScreen with 24-stage grid + thumbnails + locks.
7. Validate: `.\gradlew.bat testDebugUnitTest lintDebug assembleDebug`

## UI/UX review summary

| Area | Grade | Action |
|------|-------|--------|
| Visual identity | A | Keep cyan/violet/carbon palette |
| Title hierarchy | B- | Restructure buttons; less density |
| Mission discovery | C+ | Need stage art thumbnails + lock/progress |
| Touch targets | B+ | Good; keep 48dp primary |
| Camera controls on title | B | Move to Settings or compact overflow |
| Bonus content surface | D | Add light-up teaser chip + tunnel stage |
| 3D stage variety | B (data) / D (runtime) | Catalogs rich; EnvironmentSystem still generic |

**Art direction tagline:** Neon cyber warbirds in weather-driven vertical arenas — every stage a readable top-down combat theater.
