# THUNDER DOME — Antigravity 24-Stage Master Build Directive

**Repository:** `3000Studios/Thunderdome`  
**Production branch:** `main`  
**Game:** Android top-down vertical combat flight game  
**Owner:** 3000 Studios

This is an implementation directive for Google Antigravity. It extends the existing `ANTIGRAVITY_STAGE_DESIGN_BRIEF.md`; it does not replace the existing Kotlin/Compose/game architecture.

## Non-negotiable execution rules

1. Re-read `AGENTS.md`, `git status`, and current diffs before editing.
2. Preserve owner/other-agent work. Do not reset, stash, overwrite, or create competing gameplay systems.
3. Extend existing `BiomeCatalog`, `BossProfileCatalog`, `SectorSupportCatalog`, `EnvironmentSystem`, `GameEngine`, `GameRenderer`, VFX, and mission UI instead of building duplicate catalogs.
4. Build each environment as reusable 3D/procedural kits, decals, PBR materials, instanced props, pooled hazards, and optimized particles. Do **not** use one giant raster background as the map.
5. Player/hazard silhouettes must remain readable under weather and bloom.
6. Target 60 FPS minimum on intended Android hardware. Use LOD, culling, batching/instancing, pooled FX, bounded particle counts, compressed textures, and reduced-motion support.
7. Plane graphics are real material/texture work: separate albedo, normal, metallic, roughness, emissive, decal masks. Avoid baked lighting in texture art.
8. Implement and validate one stage end-to-end first, then reuse the pipeline across all 24.
9. Do not claim completion until the relevant Gradle checks pass and gameplay is visually verified on an authorized device/emulator.

## Shared full-stage route layout

Every stage is a vertically scrolling combat theater using normalized progress. Geometry can vary, but the major encounter rhythm is the source of truth unless existing level logic requires a safe adjustment.

| Progress | Encounter |
|---:|---|
| 0% | Spawn, loadout lock, stage intro |
| 12% | Wave A |
| 24% | Obstacle gate |
| 40% | Stage-specific boost zone |
| 53% | Elite wave |
| 65% | Weather escalation |
| 76% | Stage-specific war-speed zone |
| 85% | Miniboss / checkpoint |
| 90% | Secret perfect-run wormhole eligibility window |
| 100% | Boss arena; lock forward scrolling until resolved |

### Secret wormhole rule — all 24 stages

At 90% progress, spawn the hidden wormhole only when all of these are true:

```text
stageProgress >= 0.90
AND damageTaken == 0
AND enemiesKilled == enemiesSpawned
AND playerAlive == true
```

Behavior:
- entrance is visible for about 4 seconds or an equivalent travel window;
- visual is a gold/blue torus gate with lightning filaments, depth distortion, particle suction, and strong but readable bloom;
- never seize player steering before explicit overlap/entry;
- reward path may contain bonus currency/cosmetic progression, but ranked/multiplayer rewards must be server validated;
- missing the gate continues to the normal boss encounter;
- the wormhole must not make normal stage completion impossible.

## Plane art rules

### Hero plane
- Keep one recognizable hero silhouette across stages.
- Each stage applies a biome skin kit rather than changing the craft identity.
- 4K source textures for hero/boss assets where useful; runtime resolution can scale by device tier.
- Decals cannot cross UV seams without a tested projection method.
- Emissive graphics must remain visible without turning the whole craft into a glowing blob.
- Add subtle animated material channels where appropriate: circuit pulse, heat flow, frost crawl, energy rings, lightning crawl, bio-vein pulse.

### Boss ships
- Each boss must have a distinct top-down silhouette before particle effects are applied.
- Telegraph major attacks with geometry/material animation, not color alone.
- Keep collision/hitbox readable and slightly conservative compared with decorative fins/effects.
- Multi-phase bosses should expose visible damage or material-state changes between phases.

## 24 stage build cards

### 01 — Neon Outpost
Weather: **NEON_RAIN**. Boss: **Neon Overlord**. Build wet neon city canyons, rooftop pylons, anti-air nests, holographic billboards, laser-road gates, and drone traffic. Boost at ~40% through a cyan maglev lane; war-speed at ~76% through a rail-sling corridor. Boss: crossfire laser lattice, hologram decoy wings, radial micro-missile burst. Hero skin: black chrome, cyan emissive edge, magenta circuit filigree, reflective rain clearcoat.

### 02 — Asteroid Belt
Weather: **SPACE_DUST**. Boss: **Goliath Dreadnought**. Use rotating asteroid clusters, magnetic purple mines, rock arches, debris fields, tumbling boulders. Boost with a debris slingshot; war-speed through twin-asteroid gravity assist. Boss: broadside cannon walls, gravity tractor cone, breakable armor phases. Hero: gunmetal, amber hazard striping, violet anti-grav cores, rock-scar decals.

### 03 — Void Gate
Weather: **VOID_WARP**. Boss: **Void Stalker**. Broken obsidian, gravity shear rings, void spikes, warp orbs, portal turbulence. Boost via portal sling; war-speed via vortex acceleration tunnel. Boss: teleport ambush, void clone split, screen-edge gravity scythe. Hero: obsidian ceramic, violet plasma veins, magenta portal glyphs, starfield panels.

### 04 — Toxic Sector
Weather: **TOXIC_MIST**. Boss: **Toxin Haze**. Corroded industry, caustic gas clouds, acid vents, sludge channels, pipe towers, biohazard fans. Boost through pressure vents; war-speed in reactor exhaust channel. Boss: poison bloom, corrosion beam, toxic clone spores. Hero: matte black, luminous green vents, yellow warning chevrons, biohazard stencils.

### 05 — Ice Fortress
Weather: **BLIZZARD**. Boss: **Frost Nova**. Glacier citadel, ice spikes, frost turrets, crevasses, wind shear, frozen arches. Boost in ice-canyon slipstream; war-speed on frozen launch rail. Boss: freeze pulse, ice-shard fan, crystal armor rebuild. Hero: brushed steel, ice-blue emissive ribs, white frost fade, faceted wing tips.

### 06 — Solar Core
Weather: **SOLAR_FLARES**. Boss: **Solar Flare**. Corona platform, solar flare curtains, plasma jets, heat rings, burning trails, shockwaves. Boost in plasma draft; war-speed via corona slingshot. Boss: piercing solar beam, burning-trail cage, corona overload. Hero: mirror black, molten gold trim, orange heat vents, sunburst wing graphics.

### 07 — Cyber City
Weather: **NEON_RAIN**. Boss: **Void Reaper**. Mega-city trench, skyscraper canyon, traffic drones, holo-ad mines, service bridges, electric roof fences. Boost in maglev corridor; war-speed through neon transit lane. Boss: black-hole pull, void orb barrage, shadow dash ram. Hero: carbon fiber, cyan/magenta racing graphics, animated equalizer strips.

### 08 — Junkyard
Weather: **RUST_STORM**. Boss: **Blade Storm**. Capital hull wrecks, sawblade debris, scrap ambushes, explosive piles, crane arms. Boost with salvage catapult; war-speed through turbine corridor. Boss: spinning blade halo, ricochet shot, scrap cyclone. Hero: weathered titanium, orange weld seams, serial stencils, patchwork armor.

### 09 — Black Hole
Weather: **GRAVITY_PULL**. Boss: **Neon Phantom**. Singularity pull, lensing rings, distorted asteroids, event-horizon lanes, tidal debris. Boost via gravity-assist arc; war-speed by horizon surf. Boss: phase dash, neon laser sweep, afterimage swarm. Hero: ultra-black hull, purple lensing rings, violet star specks, gravity-distortion graphics.

### 10 — Lava Planet
Weather: **LAVA_RAIN**. Boss: **Omega Drone**. Magma rivers, basalt spires, factory platforms, eruptions, heat pockets. Boost on magma updraft; war-speed through furnace jet. Boss: summon drones, laser grid, molten missile spread. Hero: charcoal armor, red-hot cracks, orange underside glow, volcanic fracture graphics.

### 11 — Orbital Array
Weather: **ION_DUST**. Boss: **Liquid Metal**. Satellite ring, laser sweeps, ion pulse nodes, station modules, antenna fields. Boost through ion conduit; war-speed via ring-orbit catapult. Boss: shape shift, reflective skin, metal-wave projectile. Hero: polished silver, electric-blue circuitry, white ion streaks, ring insignia.

### 12 — Sand Wastes
Weather: **SAND_STORM**. Boss: **Sand Viper**. Dunes, EM dust bursts, tornadoes, canyon spires, buried ruins, rock arches. Boost on dune tailwind; war-speed through canyon vent. Boss: sand-tornado pull, razor darts, burrow strike. Hero: desert tan, black belly, gold guards, viper-scale wing graphics.

### 13 — Bio Labs
Weather: **BIO_SPORES**. Boss: **Biosynth Hydra**. Genetic domes, transparent tubes, spore clouds, bio pods, slime channels, mutagen lighting. Boost through nutrient flow; war-speed through gene-tube rail. Boss: multi-head plasma spit, regeneration, homing bio missiles. Hero: gloss black, green vein lattice, translucent bio cells, helix graphics.

### 14 — Underwater Ruins
Weather: **HYDRO_STREAM**. Boss: **Aqua Strike**. Sunken towers, water currents, bubble mines, caustic pillars, collapsed arches, deep-sea parallax. Boost in current jet; war-speed in hydro tunnel. Boss: knockback water cannon, bubble shield, torpedo spiral. Hero: deep navy, cyan caustic shimmer, pearl trim, hydrodynamic scale pattern.

### 15 — Sky Temple
Weather: **AURA_BREEZE**. Boss: **Celestial Guard**. Floating islands, waterfalls, god rays, crystal shards, wind columns, temple gates. Boost on jetstream; war-speed through celestial launch beam. Boss: divine shield, holy pulse, wing-lance rain. Hero: pearl white, sky-blue inlays, fine gold trim, geometric feather graphics.

### 16 — Machine World
Weather: **SMELTER_ASH**. Boss: **Magma Brute**. Foundry core, crusher presses, conveyor belts, gear walls, molten drains, robotic arms. Boost using conveyor overdrive; war-speed in smelter exhaust. Boss: magma balls, lava-trail ram, hydraulic shockwave. Hero: blackened steel, copper welds, orange mechanical glyphs, gear-tooth striping.

### 17 — Crystal Caverns
Weather: **CRYSTAL_DUST**. Boss: **Cyber Hawk**. Refractive crystal fields, mirror shards, laser reflection paths, avalanches, prism gates, fracture pits. Boost through prism refraction; war-speed via crystal resonance. Boss: target-lock pursuit, missile swarm, reflective feather shield. Hero: dark violet, iridescent facets, cyan laser lines, holographic prism skin.

### 18 — Storm Front
Weather: **LIGHTNING**. Boss: **Quantum Shift**. Thunder squalls, lightning curtains, EMP arcs, storm vortices, cloud walls, temporal turbulence. Boost on thunderhead updraft; war-speed on lightning rail. Boss: time-slow field, quantum teleport, temporal blade. Hero: midnight blue, white lightning forks, violet quantum rings, pulsing tail texture.

### 19 — Alien Jungle
Weather: **NEURO_MIST**. Boss: **Storm Lord**. Predatory flora, layered canopy, vine snares, spore pods, root gates, acid flowers, bioluminescence. Boost through canopy wind tunnel; war-speed via bio-electric surge. Boss: random thunder strike, persistent storm field, charged wing dive. Hero: forest-black, acid-green veins, purple bio-lights, alien fractal graphics.

### 20 — Space Graveyard
Weather: **DEBRIS_HAZARD**. Boss: **Gravity Titan**. Wrecked fleets, hull fragments, debris collisions, sniper corridors, dead engines, floating armor plates. Boost from reactor remnant; war-speed through wreck gravity sling. Boss: gravity field, meteor drop, hull-fragment shield. Hero: cold gunmetal, spectral blue-violet exhaust, ghost fleet emblems, battle scars.

### 21 — Dimension Rift
Weather: **PHASE_SHIFT**. Boss: **Crystal Revenant**. Fractured multiverse seams, phase walls, broken chunks, glitch corridors, red-violet wake scars. Boost with phase skip; war-speed through multiverse tear. Boss: multi-direction crystal spikes, reflective armor, phase inversion. Hero: black-violet, magenta/red glitch slices, mirror panels, chromatic split graphics.

### 22 — The Citadel
Weather: **FLAK_BURSTS**. Boss: **Citadel Commander**. Fortress rings, command spires, flak walls, missile towers, armored doors, deep military parallax. Boost from launch-bay catapult; war-speed through reactor trench. Boss: flak grid command, shielded turret ring, command missile swarm. Hero: dark armor, crimson command stripes, metallic insignia, angular military geometry.

### 23 — Final Approach
Weather: **WAR_HAZE**. Boss: **Inferno Rider**. Flagship armada, missile walls, fighter swarms, capital beams, burning wreck paths. Boost on carrier launch wake; war-speed in final assault corridor. Boss: fire dash, inferno wave, flame-lance pursuit. Hero: satin black, deep red spear graphics, orange afterburner blades, campaign kill marks.

### 24 — Thunder Dome
Weather: **COLOSSEUM_LIGHTNING**. Boss: **Nexus Obliterator**. Circular gold arena, rotating rings, lightning spokes, moving pylons, energy walls, collapse zones. Boost on outer-ring accelerator; war-speed from inner-ring launch. Boss: universe-collapse pulse, multi-phase form change, nexus laser crown, arena-ring shockwave. Hero: mirror black, championship gold, electric-blue core lines, 3000 Studios thunder crest.

## Environment quality requirements

Each stage needs:
- 3–5 far-background layers and a distinct skyline/horizon treatment;
- at least 3 reusable midground prop families;
- at least 4 hazard families with readable telegraphs;
- biome-specific ambient particles and weather response;
- local lighting and shadow direction consistent with stage art;
- at least one moving environmental system that is not an enemy;
- a boost visual language distinct from the war-speed visual language;
- damage/destruction states on meaningful props where performance allows;
- no obvious repeated tile seam during normal play;
- safe-area/touch/UI visibility validation on narrow phones.

## Boss implementation gate

For every stage boss, Antigravity must document and verify:
1. silhouette and material set;
2. hitbox/collision volumes;
3. phase count and health thresholds;
4. attack cooldowns and telegraphs;
5. projectile/hazard pooling;
6. screen-edge safety and unavoidable-damage checks;
7. reduced-motion treatment;
8. destruction sequence;
9. reward/result handoff;
10. replay/continue behavior.

## Completion gate

Do not mark this work finished until:
- all 24 stages are selectable and load the intended biome;
- all 24 weather systems are visibly differentiated;
- boost and war-speed zones function on all stages;
- the 90% perfect-run wormhole condition is implemented and tested for positive and negative cases;
- all 24 bosses load and complete correctly;
- hero stage skins and boss material sets render without UV/material breakage;
- mission UI shows correct stage name/weather/boss/progression state;
- tests/lint/build pass for the touched code;
- representative low/mid/high Android performance tiers have been profiled;
- final visual evidence is captured for every stage group 01–06, 07–12, 13–18, and 19–24.
