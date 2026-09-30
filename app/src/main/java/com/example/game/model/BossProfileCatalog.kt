package com.example.game.model

import androidx.compose.ui.graphics.Color

data class HoloTauntScript(
    val trigger1: String,
    val trigger2: String,
    val trigger3: String
)

data class HoloCommsExchange(
    val quickReplies: List<String>,
    val enemyCounterResponses: Map<String, String>,
    val defaultCounter: String
)

data class BossProfileSpec(
    val index: Int, // 1..24
    val id: String,
    val name: String,
    val epithet: String,
    val matchedBiomeId: String,
    val matchedBiomeName: String,
    val profileDescription: String,
    val primaryColor: Color,
    val accentColor: Color,
    val glowColor: Color,
    val weaponMoves: List<String>,
    val renderNotes: List<String>,
    val taunts: HoloTauntScript,
    val comms: HoloCommsExchange,
    val baseHealth: Float = 6000f,
    val baseShield: Float = 2500f
)

object BossProfileCatalog {
    val PROFILES: Map<String, BossProfileSpec> = listOf(
        // 01: APEX FALCON / NEON OVERLORD
        BossProfileSpec(
            index = 1,
            id = "neon_overlord",
            name = "NEON OVERLORD",
            epithet = "THE SKY DOMINATOR",
            matchedBiomeId = "neon_outpost",
            matchedBiomeName = "01. Neon Outpost",
            profileDescription = "Supreme aerial commander presiding over the Neon sector. Deploys quad laser barrages and plasma waves.",
            primaryColor = Color(0xFF00F0FF),
            accentColor = Color(0xFFFF0055),
            glowColor = Color(0x8800F0FF),
            weaponMoves = listOf("Quad Plasma Wave", "Neon Laser Grid", "Hunter Drone Salvo", "Speed Warp"),
            renderNotes = listOf("Neon wireframe glow", "Parallax skyscraper reflection", "Pulse engine bloom"),
            taunts = HoloTauntScript(
                trigger1 = "You entered the wrong airspace, pilot.",
                trigger2 = "All that speed won't save your hull.",
                trigger3 = "Kneel before the skyline."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("Clear the skies.", "You talk too much."),
                enemyCounterResponses = mapOf(
                    "Clear the skies." to "Try scraping me off.",
                    "You talk too much." to "My cannons speak louder."
                ),
                defaultCounter = "You won't leave this sector."
            )
        ),

        // 02: GOLIATH DREADNOUGHT
        BossProfileSpec(
            index = 2,
            id = "goliath_dreadnought",
            name = "GOLIATH DREADNOUGHT",
            epithet = "THE ARMORED BEHEMOTH",
            matchedBiomeId = "asteroid_belt",
            matchedBiomeName = "02. Asteroid Belt",
            profileDescription = "Massive floating fortress with dual heavy flak cannons and cluster torpedo batteries.",
            primaryColor = Color(0xFFEF4444),
            accentColor = Color(0xFFFF9500),
            glowColor = Color(0x88EF4444),
            weaponMoves = listOf("Port Heavy Flak", "Starboard Missile Salvo", "Rage Laser Beam", "Blast Shield"),
            renderNotes = listOf("Heavy metal weathering", "Reactor furnace core", "Kinetic impact spark fields"),
            taunts = HoloTauntScript(
                trigger1 = "Your peashooters can't crack my armor.",
                trigger2 = "Crushing you between the rocks.",
                trigger3 = "Shield integrity failing? Good."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("Big target, easy hit.", "Armor is just metal."),
                enemyCounterResponses = mapOf(
                    "Big target, easy hit." to "Then try piercing the core.",
                    "Armor is just metal." to "Metal that will bury you."
                ),
                defaultCounter = "Prepare for obliteration."
            )
        ),

        // 03: VOID STALKER
        BossProfileSpec(
            index = 3,
            id = "void_stalker",
            name = "VOID STALKER",
            epithet = "THE DIMENSIONAL PHANTOM",
            matchedBiomeId = "void_gate",
            matchedBiomeName = "03. Void Gate",
            profileDescription = "Tears through dimensional space using phase cloaking and gravitational displacement wells.",
            primaryColor = Color(0xFFA855F7),
            accentColor = Color(0xFF00F0FF),
            glowColor = Color(0x88A855F7),
            weaponMoves = listOf("Dimensional Cloak", "Singularity Orb", "Phase Warp Lance", "Gravity Snare"),
            renderNotes = listOf("Chromatic aberration edge", "Void particle suction", "Phase ghost afterimages"),
            taunts = HoloTauntScript(
                trigger1 = "You're chasing shadows in the dark.",
                trigger2 = "Did you think you were alone in the rift?",
                trigger3 = "Step into nothingness."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("Show yourself.", "I hunt ghosts."),
                enemyCounterResponses = mapOf(
                    "Show yourself." to "I'm right behind you.",
                    "I hunt ghosts." to "You're about to become one."
                ),
                defaultCounter = "Flesh bends in the void."
            )
        ),

        // 04: TOXIN HAZE (From Concept Art 04-06)
        BossProfileSpec(
            index = 4,
            id = "toxin_haze",
            name = "TOXIN HAZE",
            epithet = "THE CORROSIVE AMBUSHER",
            matchedBiomeId = "toxic_sector",
            matchedBiomeName = "04. Toxic Sector",
            profileDescription = "Corrosive ambusher. Sickly smooth, taunting, enjoys decay. Uses poison gas clouds and corrosion beams in industrial wastelands.",
            primaryColor = Color(0xFF22C55E),
            accentColor = Color(0xFF84CC16),
            glowColor = Color(0x8822C55E),
            weaponMoves = listOf("Poison Gas Cloud (Area Damage)", "Corrosion Beam (Armor Reduce)", "Acid Drones (Seek & Harass)", "Toxic Trail (Damages Over Time)"),
            renderNotes = listOf("Acid fog, green hazard glow", "Corroded metal, leaking pipes", "Toxic particles", "Chemical distortion"),
            taunts = HoloTauntScript(
                trigger1 = "Breathe deep. That's defeat you're tasting.",
                trigger2 = "Armor peeling already? Good.",
                trigger3 = "By the time you reach me, you're already dying."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("I'll outlast you.", "Filters on."),
                enemyCounterResponses = mapOf(
                    "I'll outlast you." to "Then I'll melt the filters.",
                    "Filters on." to "Then I'll melt the filters."
                ),
                defaultCounter = "Rot from the inside out."
            )
        ),

        // 05: FROST NOVA (From Concept Art 04-06)
        BossProfileSpec(
            index = 5,
            id = "frost_nova",
            name = "FROST NOVA",
            epithet = "THE ICY TACTICIAN",
            matchedBiomeId = "ice_fortress",
            matchedBiomeName = "05. Ice Fortress",
            profileDescription = "Icy tactician. Regal, calm, freezing voice. Uses freeze pulses and ice shards around glacial battlements.",
            primaryColor = Color(0xFF38BDF8),
            accentColor = Color(0xFFE0F2FE),
            glowColor = Color(0x8838BDF8),
            weaponMoves = listOf("Freeze Pulse (Slows Player)", "Ice Shards (Spread Shot)", "Frost Drones (Orbital)", "Cryo Field (Area Slow)"),
            renderNotes = listOf("Frost bloom, crystalline ice walls", "Drifting snow, blue-white highlights", "Frozen vapor", "Sharp reflections"),
            taunts = HoloTauntScript(
                trigger1 = "Cold slows panic into clarity. Notice it?",
                trigger2 = "Beautiful, isn't it? The moment before you freeze.",
                trigger3 = "Kneel, and I may preserve you in perfect ice."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("Thaw this.", "I'm not slowing down."),
                enemyCounterResponses = mapOf(
                    "Thaw this." to "Everyone slows down.",
                    "I'm not slowing down." to "Everyone slows down."
                ),
                defaultCounter = "Your thermal core will expire."
            )
        ),

        // 06: SOLAR FLARE (From Concept Art 04-06)
        BossProfileSpec(
            index = 6,
            id = "solar_flare",
            name = "SOLAR FLARE",
            epithet = "THE RADIANT ZEALOT",
            matchedBiomeId = "solar_core",
            matchedBiomeName = "06. Solar Core",
            profileDescription = "Radiant zealot. Booming, confident, star-forged presence. Uses burning trails and solar beams near the core.",
            primaryColor = Color(0xFFF59E0B),
            accentColor = Color(0xFFFBBF24),
            glowColor = Color(0x88F59E0B),
            weaponMoves = listOf("Burning Trail (Damage Over Time)", "Solar Beam (Piercing)", "Sun Drones (Orbit & Assault)", "Solar Nova (Screen Wide)"),
            renderNotes = listOf("Solar corona bloom, molten gold light", "Heat haze, lens flare", "Ember trails, radiant plasma energy"),
            taunts = HoloTauntScript(
                trigger1 = "You fly through my dawn.",
                trigger2 = "Too bright? Good. Burn in it.",
                trigger3 = "Face the core and be remade as ash."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("I don't kneel to stars.", "Blind me if you can."),
                enemyCounterResponses = mapOf(
                    "I don't kneel to stars." to "I won't blind you. I'll brand you.",
                    "Blind me if you can." to "I won't blind you. I'll brand you."
                ),
                defaultCounter = "You will burn to cinders."
            )
        ),

        // 07: VOID REAPER (From Concept Art 07-09)
        BossProfileSpec(
            index = 7,
            id = "void_reaper",
            name = "VOID REAPER",
            epithet = "THE ABYSS PREDATOR",
            matchedBiomeId = "cyber_city",
            matchedBiomeName = "07. Cyber City",
            profileDescription = "Abyss predator. Distorted, ominous, black-hole obsessed. Uses singularity pulls and void orbs through neon high-rises.",
            primaryColor = Color(0xFF9333EA),
            accentColor = Color(0xFFC084FC),
            glowColor = Color(0x889333EA),
            weaponMoves = listOf("Black Hole (Pulls Player)", "Void Orbs (Explode on Contact)", "Singularity Pull (Area Control)", "Gravity Distortion (Slows Player)"),
            renderNotes = listOf("Dark void lensing", "Purple singularity glow", "Cyber skyline collapsing light", "Glitch distortion", "Gravity particles"),
            taunts = HoloTauntScript(
                trigger1 = "City lights vanish beautifully.",
                trigger2 = "Did you feel that pull? That's me saying hello.",
                trigger3 = "I will fold this skyline around your grave."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("I've escaped worse.", "Try harder."),
                enemyCounterResponses = mapOf(
                    "I've escaped worse." to "Harder is all I know.",
                    "Try harder." to "Harder is all I know."
                ),
                defaultCounter = "Feed the dark."
            )
        ),

        // 08: BLADE STORM (From Concept Art 07-09)
        BossProfileSpec(
            index = 8,
            id = "blade_storm",
            name = "BLADE STORM",
            epithet = "THE RAZOR MERCENARY",
            matchedBiomeId = "junkyard",
            matchedBiomeName = "08. Junkyard",
            profileDescription = "Razor mercenary. Cocky, sharp, ruthless. Uses spinning blades and ricochet shots in debris fields.",
            primaryColor = Color(0xFFF97316),
            accentColor = Color(0xFFFB923C),
            glowColor = Color(0x88F97316),
            weaponMoves = listOf("Spinning Blades (Circular)", "Ricochet Shot (Bounces)", "Blade Storm (Area Damage)", "Metal Wave (Reflects Player Shot)"),
            renderNotes = listOf("Scrap storms, sawblade motion trails", "Orange sparks, metal fragments", "Ricochet impacts, gritty industrial shadows"),
            taunts = HoloTauntScript(
                trigger1 = "Careful where you turn. Everything here cuts.",
                trigger2 = "Metal ricochets. So do regrets.",
                trigger3 = "I'll strip your ship to sparks."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("Catch this.", "You missed the cockpit."),
                enemyCounterResponses = mapOf(
                    "Catch this." to "On purpose. I wanted you nervous.",
                    "You missed the cockpit." to "On purpose. I wanted you nervous."
                ),
                defaultCounter = "Slice and scatter."
            )
        ),

        // 09: NEON PHANTOM (From Concept Art 07-09)
        BossProfileSpec(
            index = 9,
            id = "neon_phantom",
            name = "NEON PHANTOM",
            epithet = "THE FLASH ASSASSIN",
            matchedBiomeId = "black_hole",
            matchedBiomeName = "09. Black Hole",
            profileDescription = "Flash assassin. Smug, stylish, nightlife attitude. Uses phase dashes and neon lasers at the event horizon.",
            primaryColor = Color(0xFFEC4899),
            accentColor = Color(0xFFF472B6),
            glowColor = Color(0x88EC4899),
            weaponMoves = listOf("Phase Dash (Fast Movement)", "Neon Lasers (Wide Spread)", "Phase Afterimages (Confuse Player)", "Time Warp (Brief Invisibility)"),
            renderNotes = listOf("Magenta neon flares", "Phase afterimages, black-hole glow", "Luminous trails, deep contrast, warped starfield"),
            taunts = HoloTauntScript(
                trigger1 = "Blink and you lose me.",
                trigger2 = "Phase, flash, vanish. You're chasing smoke.",
                trigger3 = "At the edge of the hole, only my light remains."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("Stand still.", "Pretty lights."),
                enemyCounterResponses = mapOf(
                    "Stand still." to "Pretty enough to kill you.",
                    "Pretty lights." to "Pretty enough to kill you."
                ),
                defaultCounter = "Faster than your optic sensors."
            )
        ),

        // 10: OMEGA DRONE (From Concept Art 10-12)
        BossProfileSpec(
            index = 10,
            id = "omega_drone",
            name = "OMEGA DRONE",
            epithet = "THE SYNTHETIC COMMANDER",
            matchedBiomeId = "lava_planet",
            matchedBiomeName = "10. Lava Planet",
            profileDescription = "Synthetic commander. Cold, analytical, machine-perfect. Uses drone swarms and laser grids above lava storms.",
            primaryColor = Color(0xFFEF4444),
            accentColor = Color(0xFFDC2626),
            glowColor = Color(0x88EF4444),
            weaponMoves = listOf("Summon Drones (3 Mini Ships)", "Laser Grid (Cross Pattern)", "Magma Balls (Explosive)", "Lava Trail (Damage on Touch)"),
            renderNotes = listOf("Molten lava rivers, orange-black smoke", "Drone formations, precision laser lines", "Heat shimmer, industrial silhouettes"),
            taunts = HoloTauntScript(
                trigger1 = "Threat index updated: still insufficient.",
                trigger2 = "Deploying reinforcement layer.",
                trigger3 = "Grid lock established. Termination is efficient."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("Bad math.", "I'll scrap every drone."),
                enemyCounterResponses = mapOf(
                    "Bad math." to "Replacements are already airborne.",
                    "I'll scrap every drone." to "Replacements are already airborne."
                ),
                defaultCounter = "Calculated outcome: your destruction."
            )
        ),

        // 11: LIQUID METAL (From Concept Art 10-12)
        BossProfileSpec(
            index = 11,
            id = "liquid_metal",
            name = "LIQUID METAL",
            epithet = "THE ADAPTIVE MIMIC",
            matchedBiomeId = "orbital_array",
            matchedBiomeName = "11. Orbital Array",
            profileDescription = "Adaptive mimic. Smooth, calm, unnervingly confident. Uses morph forms and reflective metal waves in orbital defenses.",
            primaryColor = Color(0xFF38BDF8),
            accentColor = Color(0xFFE2E8F0),
            glowColor = Color(0x8838BDF8),
            weaponMoves = listOf("Morph Form (Shape Shift)", "Metal Wave (Reflects Player Shot)", "Spinning Blades (Circular)", "Razor Darts (High Speed)"),
            renderNotes = listOf("Mercury-like reflections, orbital station rings", "Silver highlights, morphing silhouettes", "Refractive metal surfaces, zero-g polish"),
            taunts = HoloTauntScript(
                trigger1 = "Why hold one shape when any will do?",
                trigger2 = "Turrets track patterns. I erase them.",
                trigger3 = "Strike me once, and I become the counter."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("Then adapt to this.", "You're just copy paste."),
                enemyCounterResponses = mapOf(
                    "Then adapt to this." to "Copying winners is practical.",
                    "You're just copy paste." to "Copying winners is practical."
                ),
                defaultCounter = "Form is fluid, victory is certain."
            )
        ),

        // 12: SAND VIPER (From Concept Art 10-12)
        BossProfileSpec(
            index = 12,
            id = "sand_viper",
            name = "SAND VIPER",
            epithet = "THE DESERT AMBUSHER",
            matchedBiomeId = "sand_wastes",
            matchedBiomeName = "12. Sand Wastes",
            profileDescription = "Desert ambusher. Sly, dry, venomous. Uses sand tornadoes and razor darts across dune canyons.",
            primaryColor = Color(0xFFEAB308),
            accentColor = Color(0xFFCA8A04),
            glowColor = Color(0x88EAB308),
            weaponMoves = listOf("Sand Tornado (Area Pull)", "Razor Darts (High Speed)", "Sand Cloud (Reduces Visibility)", "Burrowing Strike (Ambush)"),
            renderNotes = listOf("Dust plumes, desert haze, sand vortexes", "Gold-brown lighting, wind streaks, dune shadows"),
            taunts = HoloTauntScript(
                trigger1 = "The storm hides me better than you.",
                trigger2 = "Dry throat? That's fear and dust.",
                trigger3 = "I strike fast, then let the dunes bury the lesson."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("Come slither closer.", "Sand won't save you."),
                enemyCounterResponses = mapOf(
                    "Come slither closer." to "I only need the first sting.",
                    "Sand won't save you." to "I only need the first sting."
                ),
                defaultCounter = "Swallowed by the wastes."
            )
        ),

        // 16: MAGMA BRUTE (From Concept Art 16-18)
        BossProfileSpec(
            index = 16,
            id = "magma_brute",
            name = "MAGMA BRUTE",
            epithet = "THE MOLTEN BRUISER",
            matchedBiomeId = "machine_world",
            matchedBiomeName = "16. Machine World",
            profileDescription = "Molten bruiser. Loud, brutal, laughing pyromaniac. Uses magma volleys and lava trails through factory cores.",
            primaryColor = Color(0xFFEA580C),
            accentColor = Color(0xFFFF2200),
            glowColor = Color(0x88EA580C),
            weaponMoves = listOf("Magma Balls (Explosive)", "Lava Trail (Damage on Touch)", "Summon Drones (3 Mini Ships)", "Laser Grid (Cross Pattern)"),
            renderNotes = listOf("Factory foundries, molten seams, red-orange heat", "Conveyor sparks, smoke columns, industrial glow"),
            taunts = HoloTauntScript(
                trigger1 = "Hear that furnace? It's cheering for me.",
                trigger2 = "Machines melt same as flesh.",
                trigger3 = "Walk through the fire or crawl."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("I've got coolant.", "Big talk, brute."),
                enemyCounterResponses = mapOf(
                    "I've got coolant." to "Good. It'll hiss when you crack.",
                    "Big talk, brute." to "Good. It'll hiss when you crack."
                ),
                defaultCounter = "Into the furnace with you."
            )
        ),

        // 17: CYBER HAWK (From Concept Art 16-18)
        BossProfileSpec(
            index = 17,
            id = "cyber_hawk",
            name = "CYBER HAWK",
            epithet = "THE PRECISION INTERCEPTOR",
            matchedBiomeId = "crystal_caverns",
            matchedBiomeName = "17. Crystal Caverns",
            profileDescription = "Precision interceptor. Military, clipped, ruthless. Uses target lock and missile swarms in refractive crystal tunnels.",
            primaryColor = Color(0xFF0284C7),
            accentColor = Color(0xFF38BDF8),
            glowColor = Color(0x880284C7),
            weaponMoves = listOf("Target Lock (Tracks Player)", "Missile Swarm (5 Homing)", "Chain Lightning (Bounces)", "EMP Burst (Disables Player Shot)"),
            renderNotes = listOf("Teal crystal reflections, sharp cave facets", "Radar overlays, missile trails, cool specular highlights"),
            taunts = HoloTauntScript(
                trigger1 = "Target acquired. Range closing.",
                trigger2 = "Those crystals echo your mistakes.",
                trigger3 = "Lock tone means you're already late."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("You still need a hit.", "Take your shot."),
                enemyCounterResponses = mapOf(
                    "You still need a hit." to "Missile swarm en route.",
                    "Take your shot." to "Missile swarm en route."
                ),
                defaultCounter = "Tone acquired. Firing."
            )
        ),

        // 18: QUANTUM SHIFT (From Concept Art 16-18)
        BossProfileSpec(
            index = 18,
            id = "quantum_shift",
            name = "QUANTUM SHIFT",
            epithet = "THE TIME-BENDER",
            matchedBiomeId = "storm_front",
            matchedBiomeName = "18. Storm Front",
            profileDescription = "Time-bender. Eerie, intelligent, slightly amused. Uses time warp and quantum blades within electric storm clouds.",
            primaryColor = Color(0xFF8B5CF6),
            accentColor = Color(0xFFA78BFA),
            glowColor = Color(0x888B5CF6),
            weaponMoves = listOf("Time Warp (Slows Time)", "Quantum Blades (Teleporting Attack)", "Black Hole (Pulls Player)", "Phase Dash (Fast Movement)"),
            renderNotes = listOf("Purple-blue storm flashes, temporal ghosting", "Lightning curtains, clock-like distortion, blade afterimages"),
            taunts = HoloTauntScript(
                trigger1 = "I remember you failing this part.",
                trigger2 = "Time slows for the clever and breaks for the weak.",
                trigger3 = "By the time you see me, I've already won."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("Then explain why I'm here.", "Future's changing."),
                enemyCounterResponses = mapOf(
                    "Then explain why I'm here." to "Only by a few seconds.",
                    "Future's changing." to "Only by a few seconds."
                ),
                defaultCounter = "Chronology restored."
            )
        ),

        // 24: NEXUS OBLITERATOR (Colosseum Final Boss)
        BossProfileSpec(
            index = 24,
            id = "nexus_obliterator",
            name = "NEXUS OBLITERATOR",
            epithet = "THE APEX SOVEREIGN",
            matchedBiomeId = "thunder_dome",
            matchedBiomeName = "24. Thunder Dome",
            profileDescription = "The supreme champion of the Thunder Dome. Wields all combined combat disciplines in the colosseum center.",
            primaryColor = Color(0xFFFFD700),
            accentColor = Color(0xFFFF0055),
            glowColor = Color(0x88FFD700),
            weaponMoves = listOf("Colosseum Thunderstrike", "Quantum Blade Ring", "Supernova Discharge", "Annihilation Beam"),
            renderNotes = listOf("Thunder colosseum lightning", "Golden armor refraction", "Total screen illumination"),
            taunts = HoloTauntScript(
                trigger1 = "Welcome to the Thunder Dome. Few leave.",
                trigger2 = "Your legend ends where mine began.",
                trigger3 = "Witness perfection in motion."
            ),
            comms = HoloCommsExchange(
                quickReplies = listOf("I'm taking the crown.", "Show me perfection."),
                enemyCounterResponses = mapOf(
                    "I'm taking the crown." to "You will take only dirt.",
                    "Show me perfection." to "Gladly. Watch closely."
                ),
                defaultCounter = "The Dome claims another."
            ),
            baseHealth = 12000f,
            baseShield = 5000f
        )
    ).associateBy { it.matchedBiomeId }

    fun getForBiome(biomeId: String): BossProfileSpec {
        return PROFILES[biomeId] ?: PROFILES["asteroid_belt"]!!
    }

    fun getById(bossId: String): BossProfileSpec? {
        return PROFILES.values.find { it.id == bossId }
    }
}
