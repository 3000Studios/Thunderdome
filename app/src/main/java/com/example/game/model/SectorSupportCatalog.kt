package com.example.game.model

import androidx.compose.ui.graphics.Color

data class MiniEnemySpec(
    val name: String,
    val role: String,
    val description: String,
    val color: Color
)

data class SectorObstacleSpec(
    val name: String,
    val description: String,
    val type: String, // TIMED_HAZARD, BLOCKS_PATH, EXPLOSIVE_HAZARD, PULL_FIELD, DAMAGING_LANE
    val color: Color
)

data class SectorPowerUpSpec(
    val name: String,
    val effectDescription: String,
    val category: String, // SHIELD, WEAPON_BUFF, BOOST_OVERCLOCK, HEAL
    val color: Color
)

data class SectorAtmosphereSpec(
    val weatherLightingSummary: String,
    val fxDetails: List<String>,
    val ambientFogColor: Color,
    val lightningArcColor: Color? = null
)

data class SectorSupportProfile(
    val sectorNumber: Int,
    val sectorName: String,
    val fleetName: String,
    val subtitle: String,
    val miniEnemies: List<MiniEnemySpec>,
    val obstacles: List<SectorObstacleSpec>,
    val powerUpsAndBoosts: List<SectorPowerUpSpec>,
    val atmosphere: SectorAtmosphereSpec
)

object SectorSupportCatalog {
    val PROFILES: Map<Int, SectorSupportProfile> = listOf(
        // ── PROFILE 13: CELESTIAL GUARD / BIO LABS (Divine Science Complex) ──
        SectorSupportProfile(
            sectorNumber = 13,
            sectorName = "13. Bio Labs",
            fleetName = "Celestial Guard",
            subtitle = "Divine Science Complex",
            miniEnemies = listOf(
                MiniEnemySpec("Halo Sentinel", "Shield Escort", "Protects larger units with orbiting divine shields", Color(0xFFFFD700)),
                MiniEnemySpec("Choir Dart", "Piercing Scout", "High-speed reconnaissance firing piercing holy needles", Color(0xFFFFFBEB)),
                MiniEnemySpec("Purge Orbiter", "Stun Pulse Drone", "Disables craft control systems on hit with kinetic holy rings", Color(0xFFFDE047))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Cleansing Beam", "Timed celestial light strike hazard", "TIMED_HAZARD", Color(0xFFFEF08A)),
                SectorObstacleSpec("Lockdown Gate", "Sanctuary timed force barrier blocking flight lanes", "BLOCKS_PATH", Color(0xFFFACC15)),
                SectorObstacleSpec("Mutagen Tank", "Explosive biolab hazard releasing bio-contaminant spores", "EXPLOSIVE_HAZARD", Color(0xFF22C55E))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Sanctum Shield", "Deflects all incoming shots with temporary holy aegis", "SHIELD", Color(0xFFFFE066)),
                SectorPowerUpSpec("Judgment Ray", "Piercing blast dealing immense direct damage", "WEAPON_BUFF", Color(0xFFFFF085)),
                SectorPowerUpSpec("Grace Boost", "Fast engine recharge with short evasion cooldown", "BOOST_OVERCLOCK", Color(0xFFFDE047))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Gold-white radiance with green lab accents, sacred sparks & luminous haze",
                fxDetails = listOf("Holy energy rings", "Divine rays", "Bio-tube luminescence"),
                ambientFogColor = Color(0x33FDE047)
            )
        ),

        // ── PROFILE 14: RADIATION CORE / UNDERWATER RUINS (Submerged Ancient City) ──
        SectorSupportProfile(
            sectorNumber = 14,
            sectorName = "14. Underwater Ruins",
            fleetName = "Radiation Core",
            subtitle = "Toxic Reactor Zone & Submerged Ancient City",
            miniEnemies = listOf(
                MiniEnemySpec("Glow Eel", "Toxic Swimmer", "Erratic wavy movement with bioluminescent radiation trail", Color(0xFF38BDF8)),
                MiniEnemySpec("Reactor Ray", "Irradiated Beam Craft", "Wide wings emitting pulsing toxic radiation rays", Color(0xFF4ADE80)),
                MiniEnemySpec("Mutation Spore", "Drifting Bio-Bomber", "Floats silently and detonates on proximity contact", Color(0xFF22D3EE))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Pressure Vent", "Burst current jet violently pushing planes sideways", "PULL_FIELD", Color(0xFF0284C7)),
                SectorObstacleSpec("Toxic Coral", "Contact damage spires that dissolve hull plating", "DAMAGING_LANE", Color(0xFF16A34A)),
                SectorObstacleSpec("Ruin Beam", "Ancient sunken obelisks firing unstable energy pulses", "TIMED_HAZARD", Color(0xFF06B6D4))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Rad Cure", "Cleanses radioactive exposure and removes status debuffs", "HEAL", Color(0xFF22C55E)),
                SectorPowerUpSpec("Pressure Drive", "Mobility surge for tight handling through abyssal currents", "BOOST_OVERCLOCK", Color(0xFF0EA5E9)),
                SectorPowerUpSpec("Bio Surge", "Massive short-duration damage amplifier", "WEAPON_BUFF", Color(0xFF38BDF8))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Underwater caustics, radioactive green glow, drifting bubbles & abyssal shadows",
                fxDetails = listOf("Bioluminescent underwater caustics", "Floating brine spores", "Sunken ruin sonar rings"),
                ambientFogColor = Color(0x330284C7)
            )
        ),

        // ── PROFILE 15: AQUA STRIKE / SKY TEMPLE (Floating Water Sanctuary) ──
        SectorSupportProfile(
            sectorNumber = 15,
            sectorName = "15. Sky Temple",
            fleetName = "Aqua Strike",
            subtitle = "Water Element Fleet & Floating Sanctuary",
            miniEnemies = listOf(
                MiniEnemySpec("Mist Koi", "Dart Fighter", "Graceful agile flight weaving through waterfalls", Color(0xFF67E8F9)),
                MiniEnemySpec("Torrent Dart", "Water Burst Striker", "Fires rapid multi-spread water jet clusters", Color(0xFF0284C7)),
                MiniEnemySpec("Bubble Monk", "Protective Support", "Shields nearby mini-planes inside hydro bubbles", Color(0xFF93C5FD))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Waterfall Jet", "Heavy downward water current altering flight velocity", "PULL_FIELD", Color(0xFF38BDF8)),
                SectorObstacleSpec("Wind Bridge", "Shifting aerial cloud bridges requiring navigation timing", "BLOCKS_PATH", Color(0xFFE0F2FE)),
                SectorObstacleSpec("Floating Pillar", "Moving crystalline shrine spires causing collision trauma", "BLOCKS_PATH", Color(0xFF0284C7))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Bubble Guard", "Absorbs damage inside a resilient hydro sphere", "SHIELD", Color(0xFF60A5FA)),
                SectorPowerUpSpec("Tsunami Charge", "Full screen-clearing wave attack buff", "WEAPON_BUFF", Color(0xFF0284C7)),
                SectorPowerUpSpec("Sky Current Boost", "Faster cruising speed and instant afterburner refill", "BOOST_OVERCLOCK", Color(0xFF38BDF8))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Mist and cloud layers, piercing sun shafts, reflective pools & serene cyan lighting",
                fxDetails = listOf("Cascading waterfall mist", "Floating shrine petals", "Sun-shaft god rays"),
                ambientFogColor = Color(0x3338BDF8)
            )
        ),

        // ── PROFILE 16: MAGMA BRUTE / MACHINE WORLD (Factory Core) ──
        SectorSupportProfile(
            sectorNumber = 16,
            sectorName = "16. Machine World",
            fleetName = "Magma Brute",
            subtitle = "Foundry Complexes & Factory Core",
            miniEnemies = listOf(
                MiniEnemySpec("Cinder Imp", "Fire Burst Drone", "Rapid strafing unit firing molten embers", Color(0xFFFF5722)),
                MiniEnemySpec("Forge Wasp", "Welding Harasser", "Closes distance with high-heat cutting torches", Color(0xFFFF9800)),
                MiniEnemySpec("Conveyor Hound", "Track Attacker", "Heavy mechanical hound pacing the factory rails", Color(0xFFD32F2F))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Crusher Press", "Rhythmic mechanical smashers crushing anything below", "TIMED_HAZARD", Color(0xFFFF9800)),
                SectorObstacleSpec("Molten Belt", "Moving liquid slag conveyor dealing extreme burn damage", "DAMAGING_LANE", Color(0xFFFF3D00)),
                SectorObstacleSpec("Piston Wall", "Hydraulic steam pistons sealing flight pathways", "BLOCKS_PATH", Color(0xFFFF6E40))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Heat Armor", "Complete thermal and burn resistance", "SHIELD", Color(0xFFFF5722)),
                SectorPowerUpSpec("Molten Burst", "Explosive ammunition triggering volcanic secondary pops", "WEAPON_BUFF", Color(0xFFFF9800)),
                SectorPowerUpSpec("Overclock Boost", "Maximum fire rate and thruster velocity overdrive", "BOOST_OVERCLOCK", Color(0xFFFF3D00))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Foundry heat haze, smoke columns, molten splashes & red-orange furnace glow",
                fxDetails = listOf("Sparks from stamping presses", "Molten iron steam vents", "Heavy soot plumes"),
                ambientFogColor = Color(0x44FF3D00)
            )
        ),

        // ── PROFILE 17: CYBER HAWK / CRYSTAL CAVERNS (Crystal Fields) ──
        SectorSupportProfile(
            sectorNumber = 17,
            sectorName = "17. Crystal Caverns",
            fleetName = "Cyber Hawk",
            subtitle = "Refractive Fields & Precision Interceptors",
            miniEnemies = listOf(
                MiniEnemySpec("Prism Talon", "Precision Striker", "Fires reflective beam lances from cavern shadows", Color(0xFF00E5FF)),
                MiniEnemySpec("Echo Drone", "Sonar Scout", "Pings player position and reveals cloaked positions", Color(0xFF38BDF8)),
                MiniEnemySpec("Lock Mite", "Missile Tag Drone", "Attaches homing beacons to focus enemy fire", Color(0xFFA855F7))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Crystal Laser", "Automated prism turret with refracting split beams", "TIMED_HAZARD", Color(0xFF00E5FF)),
                SectorObstacleSpec("Mirror Shard", "Massive reflective crystals bouncing shots erratically", "BLOCKS_PATH", Color(0xFFE0F2FE)),
                SectorObstacleSpec("Resonance Gate", "Acoustic vibration trap that shocks over-boosting planes", "TIMED_HAZARD", Color(0xFF67E8F9))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Prism Shield", "Light-reflecting crystalline barrier", "SHIELD", Color(0xFF00E5FF)),
                SectorPowerUpSpec("Homing Pack", "Multi-missile upgrade pack with smart trajectory tracking", "WEAPON_BUFF", Color(0xFF38BDF8)),
                SectorPowerUpSpec("Echo Dash", "Short-range quantum teleport blink", "BOOST_OVERCLOCK", Color(0xFFA855F7))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Cool cyan reflections, crystal facets, specular highlights & blue laser lines",
                fxDetails = listOf("Dynamic crystal refraction", "Resonance wave rings", "Prismatic flares"),
                ambientFogColor = Color(0x3300E5FF)
            )
        ),

        // ── PROFILE 18: QUANTUM SHIFT / STORM FRONT (Thunder Storms) ──
        SectorSupportProfile(
            sectorNumber = 18,
            sectorName = "18. Storm Front",
            fleetName = "Quantum Shift",
            subtitle = "Electric Squalls & Temporal Clouds",
            miniEnemies = listOf(
                MiniEnemySpec("Tick Tern", "Timing Disruptor", "Emits chronal pulses that warp input responsiveness", Color(0xFFA855F7)),
                MiniEnemySpec("Chrono Pike", "Teleporting Lancer", "Blinks forward in a burst of purple lightning", Color(0xFFC084FC)),
                MiniEnemySpec("Tempest Blinker", "Storm Warp Scout", "Disappears into storm clouds and strikes from flanks", Color(0xFF818CF8))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Lightning Curtain", "Vertical electric wall crackling across flight paths", "TIMED_HAZARD", Color(0xFF818CF8)),
                SectorObstacleSpec("Time Pocket", "Localized temporal field slowing craft movement drastically", "PULL_FIELD", Color(0xFFA855F7)),
                SectorObstacleSpec("Floating Tower", "Rotating lightning spire broadcasting radial EMPs", "BLOCKS_PATH", Color(0xFFC084FC))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Time Bubble", "Slow-motion tactical advantage field", "SHIELD", Color(0xFFA855F7)),
                SectorPowerUpSpec("Storm Battery", "Lightning discharge on every primary shot hit", "WEAPON_BUFF", Color(0xFF818CF8)),
                SectorPowerUpSpec("Warp Boost", "Phase dash speed with immunity to shock barriers", "BOOST_OVERCLOCK", Color(0xFFC084FC))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Purple-blue storm flashes, clock-like distortion, lightning curtains & ghosting",
                fxDetails = listOf("Temporal afterimages", "Volumetric lightning forks", "Chronal wave distortion"),
                ambientFogColor = Color(0x44A855F7),
                lightningArcColor = Color(0xFFC084FC)
            )
        ),

        // ── PROFILE 19: STORM LORD / ALIEN JUNGLE ──
        SectorSupportProfile(
            sectorNumber = 19,
            sectorName = "19. Alien Jungle",
            fleetName = "Storm Lord",
            subtitle = "Bioluminescent Canopy & Predatory Creatures",
            miniEnemies = listOf(
                MiniEnemySpec("Thunder Finch", "Lightning Scout", "Rapid dive scout that shocks on pass", Color(0xFFFACC15)),
                MiniEnemySpec("Vine Raptor", "Jungle Ambusher", "Camouflages against bio foliage before lunging", Color(0xFF22C55E)),
                MiniEnemySpec("Rain Pike", "Fast Piercing Flyer", "Needle fighter taking advantage of storm squalls", Color(0xFF38BDF8))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Vine Snare", "Grasping bio vines that trap unwary wings", "BLOCKS_PATH", Color(0xFF15803D)),
                SectorObstacleSpec("Lightning Trunk", "Electrified ironwood tree discharging bolts", "TIMED_HAZARD", Color(0xFFFACC15)),
                SectorObstacleSpec("Falling Pod", "Explosive organic seed pod dropped from canopy", "EXPLOSIVE_HAZARD", Color(0xFFCA8A04))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Storm Coat", "Resistance against electric arc damage", "SHIELD", Color(0xFF38BDF8)),
                SectorPowerUpSpec("Jungle Scan", "Radar HUD tracking hidden enemies in foliage", "WEAPON_BUFF", Color(0xFF22C55E)),
                SectorPowerUpSpec("Leap Boost", "Instant directional burst out of hazards", "BOOST_OVERCLOCK", Color(0xFFFACC15))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Wet foliage reflections, torrential rain streaks & jungle lightning mist",
                fxDetails = listOf("Rain ripples on cockpit", "Luminous spore clouds", "Branch silhouette shadows"),
                ambientFogColor = Color(0x3315803D),
                lightningArcColor = Color(0xFFFACC15)
            )
        ),

        // ── PROFILE 20: CRYSTAL REVENANT / SPACE GRAVEYARD ──
        SectorSupportProfile(
            sectorNumber = 20,
            sectorName = "20. Space Graveyard",
            fleetName = "Crystal Revenant",
            subtitle = "Derelict Fleet & Cold Spectral Glow",
            miniEnemies = listOf(
                MiniEnemySpec("Shard Wraith", "Crystal Slicer", "Haunting geometric fighter slicing through hulls", Color(0xFFA855F7)),
                MiniEnemySpec("Mirror Flea", "Reflect-Shot Pest", "Swarming parasite reflecting low-caliber blaster fire", Color(0xFFC084FC)),
                MiniEnemySpec("Grave Cutter", "Wreckage Raider", "Heavy scavenger armed with cutting beams", Color(0xFF94A3B8))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Wreckage Arm", "Rotating capital ship hull fragment", "BLOCKS_PATH", Color(0xFF64748B)),
                SectorObstacleSpec("Crystal Spike", "Massive space-grown crystalline stalagmite", "BLOCKS_PATH", Color(0xFFA855F7)),
                SectorObstacleSpec("Phantom Mine", "Ghostly mine triggering when in line of sight", "EXPLOSIVE_HAZARD", Color(0xFFC084FC))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Reflect Shell", "Deflects enemy laser shots back at their origin", "SHIELD", Color(0xFFA855F7)),
                SectorPowerUpSpec("Salvage Missile", "High-damage explosive scavenged from wreckage", "WEAPON_BUFF", Color(0xFF38BDF8)),
                SectorPowerUpSpec("Grave Drift", "Inertial glide upgrade through dense debris", "BOOST_OVERCLOCK", Color(0xFFC084FC))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Dead-space purple ambience, shattered hulls & cold spectral luminescence",
                fxDetails = listOf("Drifting hull shrapnel", "Cold glowing crystal fractures", "Spectral phantom wisps"),
                ambientFogColor = Color(0x334C1D95)
            )
        ),

        // ── PROFILE 21: INFERNO RIDER / DIMENSION RIFT ──
        SectorSupportProfile(
            sectorNumber = 21,
            sectorName = "21. Dimension Rift",
            fleetName = "Inferno Rider",
            subtitle = "Collapsing Multiverse & Lava Wakes",
            miniEnemies = listOf(
                MiniEnemySpec("Rift Burner", "Fire Dash Escort", "Dashes leaving a burning rift scar", Color(0xFFFF5722)),
                MiniEnemySpec("Trail Jackal", "Lava Wake Chaser", "Accelerates behind player slipstreams", Color(0xFFFF3D00)),
                MiniEnemySpec("Portal Skimmer", "Warp Lane Scout", "Blinks between parallel reality seams", Color(0xFFA855F7))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Broken Road", "Floating shattered highway platforms collapsing down", "BLOCKS_PATH", Color(0xFFFF9800)),
                SectorObstacleSpec("Fire Ring", "Circular burn wall requiring precision fly-through", "TIMED_HAZARD", Color(0xFFFF3D00)),
                SectorObstacleSpec("Warp Crack", "Phase rupture tearing spacetime and deflecting shots", "DAMAGING_LANE", Color(0xFFA855F7))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Rift Stabilizer", "Total immunity to spatial and gravitational distortion", "SHIELD", Color(0xFFA855F7)),
                SectorPowerUpSpec("Nitro Flame", "Blazing ammunition and supercharged afterburner", "WEAPON_BUFF", Color(0xFFFF3D00)),
                SectorPowerUpSpec("Phase Drift", "Dimensional phasing to slip past all obstacles", "BOOST_OVERCLOCK", Color(0xFFFF5722))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Orange-purple multiverse glow, speed streaks, portal fractures & warped geometry",
                fxDetails = listOf("Dimensional fissure light", "Spacetime tearing particles", "Dual-color flame trails"),
                ambientFogColor = Color(0x44FF3D00)
            )
        ),

        // ── PROFILE 22: BIOSYNTH / THE CITADEL ──
        SectorSupportProfile(
            sectorNumber = 22,
            sectorName = "22. The Citadel",
            fleetName = "Biosynth",
            subtitle = "Living Synthetic Fortress & Adaptive Defense",
            miniEnemies = listOf(
                MiniEnemySpec("Medispike", "Healer Drone", "Repairs nearby allied craft in combat", Color(0xFF22C55E)),
                MiniEnemySpec("Spore Wing", "Poison Support", "Spreads caustic spore clouds across the arena", Color(0xFF84CC16)),
                MiniEnemySpec("Armor Leech", "Defense Stripper", "Siphons player shield energy on laser hit", Color(0xFFEC4899))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Living Wall", "Regenerative organic biomass wall blocking lanes", "BLOCKS_PATH", Color(0xFF16A34A)),
                SectorObstacleSpec("Repair Node", "Station healing enemy squadrons until destroyed", "TIMED_HAZARD", Color(0xFF22C55E)),
                SectorObstacleSpec("Bio Turret", "Living organic spire shooting corrosive rounds", "TIMED_HAZARD", Color(0xFF10B981))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Regen Amp", "Gradual continuous hull and shield self-repair", "HEAL", Color(0xFF22C55E)),
                SectorPowerUpSpec("Corrode Needle", "Armor-shredding ammunition melting boss defense", "WEAPON_BUFF", Color(0xFFEC4899)),
                SectorPowerUpSpec("Bio Dash", "Adaptive speed burst leaving a slowing tail", "BOOST_OVERCLOCK", Color(0xFF10B981))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Teal biotech glow, living architecture, armored towers & synthetic spores",
                fxDetails = listOf("Regenerating cellular walls", "Synthetic veins pulsing with biolight", "Emerald spores"),
                ambientFogColor = Color(0x3310B981)
            )
        ),

        // ── PROFILE 23: GRAVITY TITAN / FINAL APPROACH ──
        SectorSupportProfile(
            sectorNumber = 23,
            sectorName = "23. Final Approach",
            fleetName = "Gravity Titan",
            subtitle = "Armada Approach & Reshaped Gravity",
            miniEnemies = listOf(
                MiniEnemySpec("Mass Drone", "Weight Pulse Attacker", "Slows and pushes craft with heavy gravity waves", Color(0xFFA855F7)),
                MiniEnemySpec("Orbit Widow", "Circling Sniper", "Sniper maintaining long distance orbit", Color(0xFFC084FC)),
                MiniEnemySpec("Meteor Imp", "Falling-Rock Escort", "Drops superdense meteor cores as traps", Color(0xFFEF4444))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Gravity Well", "Singularity vortex dragging planes into its center", "PULL_FIELD", Color(0xFFA855F7)),
                SectorObstacleSpec("Meteor Belt", "Torrent of dense rocks raining down vertically", "DAMAGING_LANE", Color(0xFFEF4444)),
                SectorObstacleSpec("Assault Turret", "Heavy dreadnought turret firing sustained railgun slugs", "TIMED_HAZARD", Color(0xFFDC2626))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Inertia Break", "Negates gravity wells and allows zero-g flight", "BOOST_OVERCLOCK", Color(0xFF38BDF8)),
                SectorPowerUpSpec("Meteor Guard", "Impact shield absorbing heavy rock collisions", "SHIELD", Color(0xFFA855F7)),
                SectorPowerUpSpec("Titan Boost", "Crushing weapon damage boost scaling with mass", "WEAPON_BUFF", Color(0xFFC084FC))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Massive purple gravity wells, dynamic asteroid fields & black-violet lighting",
                fxDetails = listOf("Gravitational lensing circles", "Disintegrating meteor trails", "Titan warship silhouette"),
                ambientFogColor = Color(0x444C1D95)
            )
        ),

        // ── PROFILE 24: NEXUS OBLITERATOR / THUNDER DOME ──
        SectorSupportProfile(
            sectorNumber = 24,
            sectorName = "24. Thunder Dome",
            fleetName = "Nexus Obliterator",
            subtitle = "The Apex Colosseum & Reality Fracture",
            miniEnemies = listOf(
                MiniEnemySpec("Core Sentinel", "Elite Arena Guard", "Heavy firepower champion protecting the center ring", Color(0xFFFFD700)),
                MiniEnemySpec("Phase Scythe", "High-Speed Slicer", "Phases directly through attacks to deliver blade strikes", Color(0xFFFF0055)),
                MiniEnemySpec("Collapse Orb", "Unstable Sphere Bomber", "Detonates into a screen-shaking phase burst", Color(0xFFFF3300))
            ),
            obstacles = listOf(
                SectorObstacleSpec("Collapse Beam", "Cataclysmic sweeping doom ray traversing the entire arena", "TIMED_HAZARD", Color(0xFFFF0055)),
                SectorObstacleSpec("Reality Tear", "Dimensional rupture in the colosseum center that distorts time", "PULL_FIELD", Color(0xFFFFD700)),
                SectorObstacleSpec("Shockwave Pylon", "Radial colosseum pillars releasing expanding shock rings", "TIMED_HAZARD", Color(0xFFFF2200))
            ),
            powerUpsAndBoosts = listOf(
                SectorPowerUpSpec("Nexus Shield", "Ultimate impenetrable barrier with explosive reflection", "SHIELD", Color(0xFF00F0FF)),
                SectorPowerUpSpec("Final Burst", "Maximum cataclysmic weapon output", "WEAPON_BUFF", Color(0xFFFF0055)),
                SectorPowerUpSpec("Omega Drive", "Transcendent speed, zero heat, and instant boost recharge", "BOOST_OVERCLOCK", Color(0xFFFFD700))
            ),
            atmosphere = SectorAtmosphereSpec(
                weatherLightingSummary = "Apocalyptic red-blue lightning, cosmic shockwaves & golden colosseum core bloom",
                fxDetails = listOf("Floating arena battlements", "Reality fractures emitting plasma arcs", "Colosseum energy towers"),
                ambientFogColor = Color(0x44FF0055),
                lightningArcColor = Color(0xFFFFD700)
            )
        )
    ).associateBy { it.sectorNumber }

    fun getForSector(sectorNumber: Int): SectorSupportProfile? {
        return PROFILES[sectorNumber]
    }

    fun getForBiome(biomeId: String): SectorSupportProfile? {
        val num = when (biomeId) {
            "bio_labs" -> 13
            "underwater_ruins" -> 14
            "sky_temple" -> 15
            "machine_world" -> 16
            "crystal_caverns" -> 17
            "storm_front" -> 18
            "alien_jungle" -> 19
            "space_graveyard" -> 20
            "dimension_rift" -> 21
            "the_citadel" -> 22
            "final_approach" -> 23
            "thunder_dome" -> 24
            else -> null
        }
        return (num?.let { PROFILES[it] }) ?: PROFILES.values.first()
    }
}
