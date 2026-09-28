package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

// Aircraft Data Definitions
data class AircraftSpec(
    val id: String,
    val name: String,
    val role: String,
    val description: String,
    val baseHealth: Float,
    val baseShield: Float,
    val baseSpeed: Float,
    val baseHandling: Float,
    val baseBoostCapacity: Float,
    val baseBoostRecharge: Float,
    val baseCritChance: Float,
    val unlockCostCredits: Long,
    val unlockCostCores: Int,
    val primaryColor: Color,
    val accentColor: Color,
    val abilityName: String = "",
    val abilityDescription: String = "",
    val defaultSpecialAbilityId: String = "chrono_overdrive"
)

object AircraftCatalog {
    val APEX_FALCON = AircraftSpec(
        id = "apex_falcon",
        name = "Falconix",
        role = "Balanced Fighter",
        description = "Balanced fighter focused on short-burst aerial damage.",
        baseHealth = 1000f,
        baseShield = 600f,
        baseSpeed = 780f,
        baseHandling = 1.15f,
        baseBoostCapacity = 100f,
        baseBoostRecharge = 30f,
        baseCritChance = 0.15f,
        unlockCostCredits = 0L,
        unlockCostCores = 0,
        primaryColor = Color(0xFFE2E8F0),
        accentColor = AeroCyan,
        abilityName = "Focus Fire",
        abilityDescription = "Increases weapon damage for a short burst.",
        defaultSpecialAbilityId = "chrono_overdrive"
    )

    val VALKYRIE_PHANTOM = AircraftSpec(
        id = "valkyrie_phantom",
        name = "BlazeHound",
        role = "Speed Interceptor",
        description = "Speed interceptor built for rapid evasive maneuvers.",
        baseHealth = 800f,
        baseShield = 750f,
        baseSpeed = 960f,
        baseHandling = 1.35f,
        baseBoostCapacity = 130f,
        baseBoostRecharge = 40f,
        baseCritChance = 0.25f,
        unlockCostCredits = 3500L,
        unlockCostCores = 10,
        primaryColor = Color(0xFF93C5FD),
        accentColor = AeroViolet,
        abilityName = "Afterburner Dash",
        abilityDescription = "Brief extreme speed boost with heat falloff.",
        defaultSpecialAbilityId = "warp_dash"
    )

    val TITAN_DREAD = AircraftSpec(
        id = "titan_dread",
        name = "Iron Bastion",
        role = "Heavy Defender",
        description = "Heavy defender that projects a defensive energy barrier.",
        baseHealth = 1600f,
        baseShield = 900f,
        baseSpeed = 650f,
        baseHandling = 0.95f,
        baseBoostCapacity = 90f,
        baseBoostRecharge = 22f,
        baseCritChance = 0.10f,
        unlockCostCredits = 7000L,
        unlockCostCores = 25,
        primaryColor = Color(0xFFFCA5A5),
        accentColor = AeroCrimson,
        abilityName = "Shield Dome",
        abilityDescription = "Projects a defensive energy barrier.",
        defaultSpecialAbilityId = "hyper_shield"
    )

    val SOLARIS_SPECTER = AircraftSpec(
        id = "solaris_specter",
        name = "Solaris",
        role = "Light Beam Craft",
        description = "Light beam craft that fires a piercing solar lance.",
        baseHealth = 950f,
        baseShield = 1100f,
        baseSpeed = 840f,
        baseHandling = 1.20f,
        baseBoostCapacity = 110f,
        baseBoostRecharge = 32f,
        baseCritChance = 0.20f,
        unlockCostCredits = 12000L,
        unlockCostCores = 45,
        primaryColor = Color(0xFFFDE047),
        accentColor = AeroEmerald,
        abilityName = "Radiant Lance",
        abilityDescription = "Fires a piercing solar beam.",
        defaultSpecialAbilityId = "nova_blast"
    )

    private fun rosterCraft(
        id: String,
        name: String,
        role: String,
        abilityName: String,
        abilityDescription: String,
        specialId: String,
        primaryColor: Color,
        accentColor: Color,
        health: Float,
        speed: Float,
        unlockCost: Long
    ) = AircraftSpec(
        id = id,
        name = name,
        role = role,
        description = "$role configured for $abilityName.",
        baseHealth = health,
        baseShield = health * 0.65f,
        baseSpeed = speed,
        baseHandling = (speed / 720f).coerceIn(0.9f, 1.45f),
        baseBoostCapacity = (speed / 7f).coerceIn(80f, 150f),
        baseBoostRecharge = (speed / 24f).coerceIn(22f, 45f),
        baseCritChance = (speed / 5000f).coerceIn(0.1f, 0.28f),
        unlockCostCredits = unlockCost,
        unlockCostCores = (unlockCost / 350L).toInt(),
        primaryColor = primaryColor,
        accentColor = accentColor,
        abilityName = abilityName,
        abilityDescription = abilityDescription,
        defaultSpecialAbilityId = specialId
    )

    val ROSTER_EXPANSION = listOf(
        rosterCraft("frostbite", "Frostbite", "Ice Control", "Cryo Blast", "Freezes enemies and slows projectiles.", "chrono_overdrive", Color(0xFFBFE9FF), AeroCyan, 920f, 810f, 5000L),
        rosterCraft("voltstrike", "Voltstrike", "Electric Assault", "Chain Lightning", "Arcs through multiple enemies.", "nova_blast", Color(0xFFFFCF3E), Color(0xFFFF8A00), 880f, 900f, 6500L),
        rosterCraft("shadowspine", "Shadowspine", "Stealth Recon", "Phase Cloak", "Temporarily vanish from enemy targeting.", "warp_dash", Color(0xFF37145C), AeroViolet, 760f, 1020f, 8000L),
        rosterCraft("pyroclast", "Pyroclast", "Fire Bomber", "Inferno Wave", "Launches a spreading wave of fire.", "nova_blast", Color(0xFFFF4A16), Color(0xFFFFD04A), 1260f, 700f, 9000L),
        rosterCraft("aquastorm", "Aquastorm", "Water Support", "Tidal Surge", "Knocks back enemies with a water shockwave.", "hyper_shield", Color(0xFF1EA7FF), AeroCyan, 1040f, 760f, 10500L),
        rosterCraft("toxinwing", "Toxinwing", "Biochem Assault", "Neuro Toxin", "Leaves a damaging cloud in its wake.", "nova_blast", Color(0xFF51D66B), Color(0xFFB8FF59), 900f, 850f, 12000L),
        rosterCraft("galestrider", "Galestrider", "Wind Maneuver", "Aerial Vortex", "Creates a vortex that pulls enemies.", "chrono_overdrive", Color(0xFFE9FAFF), AeroCyan, 790f, 1080f, 14000L),
        rosterCraft("magnorak", "Magnorak", "Gravity Control", "Gravity Well", "Traps enemies in a gravity field.", "hyper_shield", Color(0xFF7E27CC), AeroViolet, 1120f, 760f, 16000L),
        rosterCraft("nightraven", "Nightraven", "Dark Assassin", "Shadow Burst", "Teleports ahead with an explosive burst.", "warp_dash", Color(0xFF170F2A), AeroViolet, 830f, 980f, 18000L),
        rosterCraft("zephyrion", "Zephyrion", "Mobility Specialist", "Wind Step", "Instant directional dodge.", "warp_dash", Color(0xFFB7F5DF), AeroEmerald, 780f, 1100f, 20500L),
        rosterCraft("terrashock", "Terrashock", "Earth Breaker", "Seismic Slam", "Creates a shockwave that damages and stuns.", "nova_blast", Color(0xFFA56A39), AeroAmber, 1450f, 620f, 23000L),
        rosterCraft("emberlash", "Emberlash", "Plasma Gunship", "Plasma Nova", "Unleashes a close-range plasma blast.", "nova_blast", Color(0xFFFF6330), AeroAmber, 1200f, 780f, 25500L),
        rosterCraft("cryospear", "Cryospear", "Sniper", "Ice Shot", "Ultra-long range armor-piercing shot.", "chrono_overdrive", Color(0xFFD4F3FF), AeroCyan, 860f, 920f, 28000L),
        rosterCraft("stormrider", "Stormrider", "Weather Dominator", "Thunder Field", "Calls down random lightning strikes.", "hyper_shield", Color(0xFF7865FF), AeroViolet, 980f, 890f, 31000L),
        rosterCraft("voidrunner", "Voidrunner", "Dimensional", "Portal Shift", "Creates a short-range teleport portal.", "warp_dash", Color(0xFF7624FF), AeroViolet, 820f, 1050f, 34000L),
        rosterCraft("ruinhawk", "Ruinhawk", "Demolition", "Cluster Reign", "Drops multiple explosive missiles.", "nova_blast", Color(0xFF858A96), AeroOrange, 1320f, 680f, 37000L),
        rosterCraft("nebulus", "Nebulus", "Support Drone", "Drone Swarm", "Deploys a protective drone formation.", "hyper_shield", Color(0xFFE2B2FF), AeroViolet, 900f, 850f, 40000L),
        rosterCraft("helix", "Helix", "Adaptive", "Morph Mode", "Cycles between tactical configurations.", "chrono_overdrive", Color(0xFF54F5E6), AeroCyan, 960f, 900f, 44000L),
        rosterCraft("omegashard", "Omegashard", "Disruption", "System Break", "Disables enemy weapons temporarily.", "nova_blast", Color(0xFFE92C3E), AeroCrimson, 1080f, 860f, 48000L),
        rosterCraft("apex_nova", "Apex Nova", "Ultimate Class", "Nova Catastrophe", "Massive screen-clearing energy eruption.", "nova_blast", Color(0xFFFFF0C4), AeroAmber, 1400f, 980f, 60000L)
    )

    val ALL_AIRCRAFT = listOf(APEX_FALCON, VALKYRIE_PHANTOM, TITAN_DREAD, SOLARIS_SPECTER) + ROSTER_EXPANSION

    fun getById(id: String): AircraftSpec = ALL_AIRCRAFT.firstOrNull { it.id == id } ?: APEX_FALCON
}

// Paint Schemes
data class PaintScheme(
    val id: String,
    val name: String,
    val bodyColor: Color,
    val trimColor: Color,
    val costCredits: Long
)

object PaintCatalog {
    val ALL = listOf(
        PaintScheme("stealth_black", "Midnight Stealth", Color(0xFF1E293B), AeroCyan, 0L),
        PaintScheme("cobalt_frost", "Cobalt Frost", Color(0xFF1E3A8A), Color(0xFF67E8F9), 800L),
        PaintScheme("crimson_war", "Crimson Warpath", Color(0xFF7F1D1D), AeroAmber, 1200L),
        PaintScheme("solar_flare", "Solar Prototype", Color(0xFF78350F), Color(0xFFFBBF24), 2000L),
        PaintScheme("cyber_neon", "Cyberpunk Phantom", Color(0xFF581C87), AeroEmerald, 3500L)
    )

    fun getById(id: String): PaintScheme = ALL.firstOrNull { it.id == id } ?: ALL.first()
}

// Engine Plumes
data class ExhaustFlame(
    val id: String,
    val name: String,
    val coreColor: Color,
    val outerColor: Color,
    val costCredits: Long
)

object ExhaustCatalog {
    val ALL = listOf(
        ExhaustFlame("cyan_flame", "Ion Cyan", Color(0xFFE0F7FA), AeroCyan, 0L),
        ExhaustFlame("violet_flame", "Antimatter Violet", Color(0xFFF3E8FF), AeroViolet, 600L),
        ExhaustFlame("amber_flame", "Hyper Orange", Color(0xFFFEF3C7), AeroOrange, 1000L),
        ExhaustFlame("emerald_flame", "Tachyon Emerald", Color(0xFFDCFCE7), AeroEmerald, 1500L)
    )

    fun getById(id: String): ExhaustFlame = ALL.firstOrNull { it.id == id } ?: ALL.first()
}

// Weapons
enum class WeaponSlot { PRIMARY, SECONDARY, SPECIAL }

data class WeaponSpec(
    val id: String,
    val name: String,
    val slot: WeaponSlot,
    val description: String,
    val baseDamage: Float,
    val fireRate: Float, // rounds per second
    val projectileSpeed: Float,
    val spreadAngle: Float,
    val heatPerShot: Float,
    val projectileCount: Int,
    val isHoming: Boolean = false,
    val isPiercing: Boolean = false,
    val projectileColor: Color,
    val glowColor: Color
)

object WeaponCatalog {
    // Primary
    val PLASMA_GATLING = WeaponSpec(
        id = "plasma_gatling",
        name = "Dual Plasma Gatling",
        slot = WeaponSlot.PRIMARY,
        description = "High-velocity magnetic plasma repeater with rapid cyclical rate of fire.",
        baseDamage = 28f,
        fireRate = 12f,
        projectileSpeed = 1600f,
        spreadAngle = 3.5f,
        heatPerShot = 4.5f,
        projectileCount = 2,
        projectileColor = Color(0xFF00F0FF),
        glowColor = Color(0x880099FF)
    )

    val TWIN_LASER = WeaponSpec(
        id = "twin_laser",
        name = "Chrono Laser Cannons",
        slot = WeaponSlot.PRIMARY,
        description = "Dual concentrated coherent light beams with high accuracy and armor penetration.",
        baseDamage = 45f,
        fireRate = 7.5f,
        projectileSpeed = 2200f,
        spreadAngle = 1f,
        heatPerShot = 6.0f,
        projectileCount = 2,
        isPiercing = true,
        projectileColor = Color(0xFFC084FC),
        glowColor = Color(0x99A855F7)
    )

    val RAILGUN = WeaponSpec(
        id = "railgun",
        name = "Hyper-Velocity Railgun",
        slot = WeaponSlot.PRIMARY,
        description = "Electromagnetic hyper-slug that punches clean through enemy flight columns.",
        baseDamage = 160f,
        fireRate = 2.2f,
        projectileSpeed = 2800f,
        spreadAngle = 0f,
        heatPerShot = 18f,
        projectileCount = 1,
        isPiercing = true,
        projectileColor = Color(0xFFFEF08A),
        glowColor = Color(0xBBF59E0B)
    )

    val HEAVY_FLAK = WeaponSpec(
        id = "heavy_flak",
        name = "Vulcan Flak Scatter",
        slot = WeaponSlot.PRIMARY,
        description = "Heavy spread of explosive proximity flak pellets for close-range aerial clearing.",
        baseDamage = 22f,
        fireRate = 4.5f,
        projectileSpeed = 1400f,
        spreadAngle = 18f,
        heatPerShot = 12f,
        projectileCount = 5,
        projectileColor = Color(0xFFF97316),
        glowColor = Color(0x99DC2626)
    )

    // Secondary
    val SWARM_MISSILES = WeaponSpec(
        id = "swarm_missiles",
        name = "Micro Swarm Missiles",
        slot = WeaponSlot.SECONDARY,
        description = "Fires a salvo of autonomous heat-seeking micro-missiles with devastating turn capability.",
        baseDamage = 75f,
        fireRate = 1.8f,
        projectileSpeed = 950f,
        spreadAngle = 25f,
        heatPerShot = 20f,
        projectileCount = 4,
        isHoming = true,
        projectileColor = Color(0xFFEF4444),
        glowColor = Color(0x99F87171)
    )

    val EMP_TORPEDO = WeaponSpec(
        id = "emp_torpedo",
        name = "EMP Disruptor Torpedo",
        slot = WeaponSlot.SECONDARY,
        description = "Heavy energy payload that paralyzes enemy systems and shreds active shields.",
        baseDamage = 190f,
        fireRate = 1.0f,
        projectileSpeed = 800f,
        spreadAngle = 0f,
        heatPerShot = 30f,
        projectileCount = 1,
        projectileColor = Color(0xFF38BDF8),
        glowColor = Color(0xAA0284C7)
    )

    val CLUSTER_BOMBS = WeaponSpec(
        id = "cluster_bombs",
        name = "Cluster Submunitions",
        slot = WeaponSlot.SECONDARY,
        description = "Splits into secondary explosive bomblets causing extensive area bombardment.",
        baseDamage = 60f,
        fireRate = 1.2f,
        projectileSpeed = 700f,
        spreadAngle = 30f,
        heatPerShot = 25f,
        projectileCount = 6,
        projectileColor = Color(0xFFFB923C),
        glowColor = Color(0x99EA580C)
    )

    val HUNTER_DRONES = WeaponSpec(
        id = "hunter_drones",
        name = "Escort Hunter Drones",
        slot = WeaponSlot.SECONDARY,
        description = "Deploys autonomous wingman drones that orbit the aircraft and intercept incoming targets.",
        baseDamage = 35f,
        fireRate = 3.0f,
        projectileSpeed = 1200f,
        spreadAngle = 10f,
        heatPerShot = 15f,
        projectileCount = 2,
        isHoming = true,
        projectileColor = Color(0xFF34D399),
        glowColor = Color(0x99059669)
    )

    // Special Abilities
    val CHRONO_OVERDRIVE = WeaponSpec(
        id = "chrono_overdrive",
        name = "Chrono Stasis Overdrive",
        slot = WeaponSlot.SPECIAL,
        description = "Slows down world time by 60% while boosting your weapon fire rate and maneuverability by 200%.",
        baseDamage = 0f,
        fireRate = 0.05f, // 20s cooldown
        projectileSpeed = 0f,
        spreadAngle = 0f,
        heatPerShot = 0f,
        projectileCount = 0,
        projectileColor = AeroCyan,
        glowColor = AeroCyanGlow
    )

    val HYPER_SHIELD = WeaponSpec(
        id = "hyper_shield",
        name = "Aegis Kinetic Barrier",
        slot = WeaponSlot.SPECIAL,
        description = "Projects an impenetrable 360-degree energy shield that absorbs all damage and reflects incoming fire.",
        baseDamage = 0f,
        fireRate = 0.06f,
        projectileSpeed = 0f,
        spreadAngle = 0f,
        heatPerShot = 0f,
        projectileCount = 0,
        projectileColor = ShieldBlue,
        glowColor = Color(0x8838BDF8)
    )

    val NOVA_BLAST = WeaponSpec(
        id = "nova_blast",
        name = "Omega Shockwave Cannon",
        slot = WeaponSlot.SPECIAL,
        description = "Discharges a tactical thermonuclear shockwave erasing all enemy projectiles and devastating screen targets.",
        baseDamage = 650f,
        fireRate = 0.04f,
        projectileSpeed = 600f,
        spreadAngle = 360f,
        heatPerShot = 0f,
        projectileCount = 1,
        projectileColor = Color(0xFFFDE047),
        glowColor = Color(0xCCF59E0B)
    )

    val WARP_DASH = WeaponSpec(
        id = "warp_dash",
        name = "Phase Warp Dash",
        slot = WeaponSlot.SPECIAL,
        description = "Performs an instantaneous dimensional phase forward, triggering kinetic shockwaves and total invulnerability.",
        baseDamage = 120f,
        fireRate = 0.15f,
        projectileSpeed = 0f,
        spreadAngle = 0f,
        heatPerShot = 0f,
        projectileCount = 0,
        projectileColor = AeroViolet,
        glowColor = Color(0x88B347FF)
    )

    val ALL_PRIMARY = listOf(PLASMA_GATLING, TWIN_LASER, RAILGUN, HEAVY_FLAK)
    val ALL_SECONDARY = listOf(SWARM_MISSILES, EMP_TORPEDO, CLUSTER_BOMBS, HUNTER_DRONES)
    val ALL_SPECIAL = listOf(CHRONO_OVERDRIVE, HYPER_SHIELD, NOVA_BLAST, WARP_DASH)

    fun getById(id: String): WeaponSpec {
        return (ALL_PRIMARY + ALL_SECONDARY + ALL_SPECIAL).firstOrNull { it.id == id } ?: PLASMA_GATLING
    }
}

// Roguelite Perk Definitions
enum class PerkRarity(val label: String, val color: Color) {
    COMMON("COMMON", Color(0xFF94A3B8)),
    RARE("RARE", Color(0xFF38BDF8)),
    EPIC("EPIC", Color(0xFFA855F7)),
    LEGENDARY("LEGENDARY", Color(0xFFF59E0B))
}

data class RoguelitePerk(
    val id: String,
    val name: String,
    val rarity: PerkRarity,
    val description: String,
    val iconTag: String,
    val maxStacks: Int = 3
)

object PerkCatalog {
    val ALL_PERKS = listOf(
        RoguelitePerk(
            id = "cluster_warheads",
            name = "Cluster Warheads",
            rarity = PerkRarity.EPIC,
            description = "All missiles split into 3 additional mini-explosive submunitions on impact.",
            iconTag = "cluster"
        ),
        RoguelitePerk(
            id = "chain_lightning",
            name = "Chain Lightning Arcs",
            rarity = PerkRarity.RARE,
            description = "Critical hits discharge electrical arcs striking up to 3 nearby enemies for 40% damage.",
            iconTag = "lightning"
        ),
        RoguelitePerk(
            id = "overclocked_heatsinks",
            name = "Overclocked Heatsinks",
            rarity = PerkRarity.COMMON,
            description = "Weapon fire rate increased by 20% and heat dissipation improved by 35%.",
            iconTag = "fire_rate"
        ),
        RoguelitePerk(
            id = "kinetic_piercing",
            name = "Hyperion Penetrator",
            rarity = PerkRarity.RARE,
            description = "All primary projectiles pierce through 1 additional enemy and deal +25% armor damage.",
            iconTag = "pierce"
        ),
        RoguelitePerk(
            id = "emergency_matrix",
            name = "Emergency Shield Matrix",
            rarity = PerkRarity.EPIC,
            description = "When shields deplete, instantly triggers an invulnerability bubble and EMP pulse for 2.5 seconds.",
            iconTag = "shield"
        ),
        RoguelitePerk(
            id = "nanite_vampirism",
            name = "Nanite Siphon",
            rarity = PerkRarity.RARE,
            description = "Destroying elite enemies and bosses restores 5% of maximum hull and full shield.",
            iconTag = "repair"
        ),
        RoguelitePerk(
            id = "drone_wingman",
            name = "Autonomous Wingman",
            rarity = PerkRarity.LEGENDARY,
            description = "Deploys a permanent tactical drone escort that fires micro-lasers at closest hostiles.",
            iconTag = "drone"
        ),
        RoguelitePerk(
            id = "critical_overdrive",
            name = "Critical Overdrive",
            rarity = PerkRarity.COMMON,
            description = "+15% Critical Chance and +50% Critical Strike Damage multiplier.",
            iconTag = "crit"
        ),
        RoguelitePerk(
            id = "tactical_boosters",
            name = "Afterburner Overcharge",
            rarity = PerkRarity.COMMON,
            description = "+30% Boost capacity and +40% Boost recharge rate with shockwave trail.",
            iconTag = "boost"
        ),
        RoguelitePerk(
            id = "magnetic_collector",
            name = "Quantum Magnetism",
            rarity = PerkRarity.COMMON,
            description = "Significantly increases pickup radius for plasma credits, repair nanites, and power-ups.",
            iconTag = "magnet"
        ),
        RoguelitePerk(
            id = "apocalypse_detonation",
            name = "Megaton Warheads",
            rarity = PerkRarity.LEGENDARY,
            description = "Explosion radius increased by 65%. Enemies killed explode and trigger chain detonations.",
            iconTag = "bomb"
        ),
        RoguelitePerk(
            id = "chrono_distortion",
            name = "Temporal Reflexes",
            rarity = PerkRarity.RARE,
            description = "Grazing enemy projectiles or near-misses slows world time briefly and refills 15% boost.",
            iconTag = "chrono"
        )
    )

    fun getRandomChoices(count: Int = 3, existingPerkIds: List<String>): List<RoguelitePerk> {
        val available = ALL_PERKS.shuffled()
        return available.take(count)
    }
}

// Biomes and Missions
data class BiomeSpec(
    val id: String,
    val name: String,
    val description: String,
    val skyColorTop: Color,
    val skyColorBottom: Color,
    val groundColor: Color,
    val weatherType: String,
    val difficultyMultiplier: Float
)

object BiomeCatalog {
    val NEO_TOKYO = BiomeSpec(
        id = "neo_tokyo",
        name = "Neo-Tokyo Megacity",
        description = "Dense vertical cybernetic metropolis illuminated by neon holograms, aerial traffic lanes, and towering megastructures.",
        skyColorTop = Color(0xFF050814),
        skyColorBottom = Color(0xFF0F1A30),
        groundColor = Color(0xFF101928),
        weatherType = "NEON_RAIN",
        difficultyMultiplier = 1.0f
    )

    val CETI_CANYON = BiomeSpec(
        id = "ceti_canyon",
        name = "Ceti Red Canyons",
        description = "Hazardous alien canyon trench with high-speed low-altitude dogfights, geothermal vents, and military radar stations.",
        skyColorTop = Color(0xFF1A0A08),
        skyColorBottom = Color(0xFF3B1510),
        groundColor = Color(0xFF2E110D),
        weatherType = "SAND_STORM",
        difficultyMultiplier = 1.25f
    )

    val ORBITAL_DOCK = BiomeSpec(
        id = "orbital_dock",
        name = "Orbital Defense Station",
        description = "Sub-orbital combat above the planetary curve with asteroid fields, solar arrays, and deep vacuum lighting.",
        skyColorTop = Color(0xFF02040A),
        skyColorBottom = Color(0xFF071226),
        groundColor = Color(0xFF0A1320),
        weatherType = "ION_DUST",
        difficultyMultiplier = 1.5f
    )

    val ALL_BIOMES = listOf(NEO_TOKYO, CETI_CANYON, ORBITAL_DOCK)
}
