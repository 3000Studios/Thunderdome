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

    val APEX_FOUNDER_ZERO = AircraftSpec(
        id = "apex_founder_zero",
        name = "Apex Zero [Founder]",
        role = "3000 Studios Founder Prototype",
        description = "Exclusive Founder Edition gold-alloy warbird with dual quantum turbines, titanium armor and infinite kinetic shields.",
        baseHealth = 1800f,
        baseShield = 1400f,
        baseSpeed = 1120f,
        baseHandling = 1.45f,
        baseBoostCapacity = 160f,
        baseBoostRecharge = 50f,
        baseCritChance = 0.35f,
        unlockCostCredits = 0L,
        unlockCostCores = 0,
        primaryColor = Color(0xFFFFD700),
        accentColor = Color(0xFFF59E0B),
        abilityName = "Founder Supernova",
        abilityDescription = "Discharges a golden solar pulse destroying all enemy fire and supercharging fire rate.",
        defaultSpecialAbilityId = "nova_blast"
    )

    // ── SECRET DEVELOPER-MODE AIRCRAFT ──
    val AIRCRAFT_JERICA = AircraftSpec(
        id = "aircraft_jerica",
        name = "Jerica (Queen Bee)",
        role = "Apex Hive Queen",
        description = "Secret prototype inspired by the predatory honey bee. Carbon-fiber black chassis with glowing yellow bio-vector wings.",
        baseHealth = 9999f,
        baseShield = 9999f,
        baseSpeed = 1600f,
        baseHandling = 2.0f,
        baseBoostCapacity = 300f,
        baseBoostRecharge = 100f,
        baseCritChance = 0.90f,
        unlockCostCredits = 0L,
        unlockCostCores = 0,
        primaryColor = Color(0xFFFFD700),
        accentColor = Color(0xFF18181B),
        abilityName = "Stinger Swarm Nova",
        abilityDescription = "Devastating multi-vector bio-plasma stinger burst with instant screen destruction.",
        defaultSpecialAbilityId = "nova_blast"
    )

    val AIRCRAFT_JADON = AircraftSpec(
        id = "aircraft_jadon",
        name = "Jadon (Sovereign)",
        role = "Apex Sovereign",
        description = "Secret hyper-tier hero fighter. Aggressive dual-toned crimson and cobalt chassis with supercharged tachyon thrusters.",
        baseHealth = 9999f,
        baseShield = 9999f,
        baseSpeed = 1600f,
        baseHandling = 2.0f,
        baseBoostCapacity = 300f,
        baseBoostRecharge = 100f,
        baseCritChance = 0.90f,
        unlockCostCredits = 0L,
        unlockCostCores = 0,
        primaryColor = Color(0xFFFF1E56),
        accentColor = Color(0xFF00F0FF),
        abilityName = "Tachyon Sovereign Beam",
        abilityDescription = "Unleashes dual relativistic crimson & cyan particle beams annihilating all hostile columns.",
        defaultSpecialAbilityId = "nova_blast"
    )

    val SECRET_PLANES = listOf(AIRCRAFT_JERICA, AIRCRAFT_JADON)

    val ALL_AIRCRAFT = listOf(APEX_FALCON, VALKYRIE_PHANTOM, TITAN_DREAD, SOLARIS_SPECTER, APEX_FOUNDER_ZERO) + ROSTER_EXPANSION + SECRET_PLANES

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
        PaintScheme("cyber_neon", "Cyberpunk Phantom", Color(0xFF581C87), AeroEmerald, 3500L),
        PaintScheme("military_camo", "Desert Vanguard Camo", Color(0xFF785E3A), Color(0xFF4A381E), 1500L),
        PaintScheme("arctic_camo", "Arctic Ghost Camo", Color(0xFFCBD5E1), Color(0xFF38BDF8), 1800L),
        PaintScheme("lava_inferno", "Volcanic Magma Weave", Color(0xFF450A0A), Color(0xFFFF5500), 2800L),
        PaintScheme("ice_shatter", "Glacial Crystalline", Color(0xFF082F49), Color(0xFF38BDF8), 3000L),
        PaintScheme("alien_xenon", "Xenon Bioluminescent", Color(0xFF052E16), Color(0xFF22C55E), 4000L),
        PaintScheme("executive_carbon", "Carbon Fiber Matte", Color(0xFF09090B), Color(0xFF71717A), 4500L),
        PaintScheme("founder_gold", "3000 Founder Gold", Color(0xFFFFD700), Color(0xFFF59E0B), 0L),
        PaintScheme("jerica_honey_gold", "Queen Bee Hex Amber", Color(0xFFFFD700), Color(0xFF18181B), 0L),
        PaintScheme("jadon_apex_hero", "Apex Sovereign Red/Blue", Color(0xFFFF1E56), Color(0xFF00F0FF), 0L)
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
        ExhaustFlame("emerald_flame", "Tachyon Emerald", Color(0xFFDCFCE7), AeroEmerald, 1500L),
        ExhaustFlame("founder_ion_gold", "Founder Solar Ion", Color(0xFFFFFBEB), Color(0xFFFFD700), 0L),
        ExhaustFlame("honey_plasma", "Honey Bio-Plume", Color(0xFFFEF08A), Color(0xFFFFD700), 0L),
        ExhaustFlame("tachyon_hero", "Sovereign Dual Trail", Color(0xFFFFFFFF), Color(0xFFFF1E56), 0L)
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
    // 24 Tactical Mission Theaters from the 3000 Studios Master Sheet
    val THEATER_01 = BiomeSpec("neon_outpost", "01. Neon Outpost", "City defense, neon corridors & automated anti-air artillery.", Color(0xFF050814), Color(0xFF0F1A30), Color(0xFF101928), "NEON_RAIN", 1.0f)
    val THEATER_02 = BiomeSpec("asteroid_belt", "02. Asteroid Belt", "Drifting dense asteroids, magnetic proximity mines & rock fields.", Color(0xFF100C08), Color(0xFF24160A), Color(0xFF1F140A), "SPACE_DUST", 1.05f)
    val THEATER_03 = BiomeSpec("void_gate", "03. Void Gate", "Dimensional rift gate with portal ambushes and localized gravity shears.", Color(0xFF1A0A2E), Color(0xFF2B094C), Color(0xFF160627), "VOID_WARP", 1.1f)
    val THEATER_04 = BiomeSpec("toxic_sector", "04. Toxic Sector", "Caustic green gas clouds that slowly dissolve kinetic shields.", Color(0xFF061A0C), Color(0xFF0A3016), Color(0xFF082410), "TOXIC_MIST", 1.15f)
    val THEATER_05 = BiomeSpec("ice_fortress", "05. Ice Fortress", "Sub-zero glacial citadels with freezing blizzard crosswinds.", Color(0xFF0A1826), Color(0xFF132F4C), Color(0xFF0E2238), "BLIZZARD", 1.2f)
    val THEATER_06 = BiomeSpec("solar_core", "06. Solar Core", "Intense stellar proximity, blinding solar flares & thermal radiation.", Color(0xFF2E1304), Color(0xFF5C2405), Color(0xFF3D1804), "SOLAR_FLARES", 1.25f)
    val THEATER_07 = BiomeSpec("cyber_city", "07. Cyber City", "Mega-skyscrapers, holographic ads & dense low-altitude trench runs.", Color(0xFF0A0F24), Color(0xFF141F48), Color(0xFF0D1430), "NEON_RAIN", 1.3f)
    val THEATER_08 = BiomeSpec("junkyard", "08. Junkyard", "Kilometers of decaying capital hulls, debris fields & scavenger ambushes.", Color(0xFF1C130B), Color(0xFF332010), Color(0xFF24170C), "RUST_STORM", 1.35f)
    val THEATER_09 = BiomeSpec("black_hole", "09. Black Hole", "Supermassive singularity edge pulling craft inward with extreme gravity.", Color(0xFF170024), Color(0xFF2A0342), Color(0xFF11001A), "GRAVITY_PULL", 1.4f)
    val THEATER_10 = BiomeSpec("lava_planet", "10. Lava Planet", "Geothermal volcanic lakes, bursting magma bombs & heat haze.", Color(0xFF260502), Color(0xFF4C0A04), Color(0xFF330703), "LAVA_RAIN", 1.45f)
    val THEATER_11 = BiomeSpec("orbital_array", "11. Orbital Array", "Planetary defense satellite grid with sweeping automated lasers.", Color(0xFF030A17), Color(0xFF081838), Color(0xFF050F24), "ION_DUST", 1.5f)
    val THEATER_12 = BiomeSpec("sand_wastes", "12. Sand Wastes", "Alien desert dunes swept by blinding electromagnetic sandstorms.", Color(0xFF261D0C), Color(0xFF4A3816), Color(0xFF33260F), "SAND_STORM", 1.55f)
    val THEATER_13 = BiomeSpec("bio_labs", "13. Bio Labs", "Abandoned genetic chambers releasing virulent mutagens and spores.", Color(0xFF051C0A), Color(0xFF0D3D16), Color(0xFF08260E), "BIO_SPORES", 1.6f)
    val THEATER_14 = BiomeSpec("underwater_ruins", "14. Underwater Ruins", "Sub-oceanic abyssal trench lighting, geysers & sonar disruption.", Color(0xFF02131C), Color(0xFF06293D), Color(0xFF031A26), "HYDRO_STREAM", 1.65f)
    val THEATER_15 = BiomeSpec("sky_temple", "15. Sky Temple", "Floating crystalline islands amidst swirling high-altitude cloud towers.", Color(0xFF0D2038), Color(0xFF1A3E6D), Color(0xFF122C4D), "AURA_BREEZE", 1.7f)
    val THEATER_16 = BiomeSpec("machine_world", "16. Machine World", "Planetary automated factory fortress with blast furnace vents.", Color(0xFF1F0C0C), Color(0xFF3D1616), Color(0xFF290E0E), "SMELTER_ASH", 1.75f)
    val THEATER_17 = BiomeSpec("crystal_caverns", "17. Crystal Caverns", "Subterranean cavern system with laser-refracting crystal formations.", Color(0xFF1B072B), Color(0xFF390F5C), Color(0xFF240A3B), "CRYSTAL_DUST", 1.8f)
    val THEATER_18 = BiomeSpec("storm_front", "18. Storm Front", "Continuous atmospheric lightning strikes, EMP discharges & squalls.", Color(0xFF070B1F), Color(0xFF101947), Color(0xFF0B1130), "LIGHTNING", 1.85f)
    val THEATER_19 = BiomeSpec("alien_jungle", "19. Alien Jungle", "Bioluminescent canopy choke points with predatory bio-creatures.", Color(0xFF091C08), Color(0xFF143B11), Color(0xFF0D260B), "NEURO_MIST", 1.9f)
    val THEATER_20 = BiomeSpec("space_graveyard", "20. Space Graveyard", "Wrecked alien battle fleets offering tactical cover and sniper corridors.", Color(0xFF080D14), Color(0xFF121C2B), Color(0xFF0C131F), "DEBRIS_HAZARD", 1.95f)
    val THEATER_21 = BiomeSpec("dimension_rift", "21. Dimension Rift", "Fractured space-time corridor with phase anomalies & reality shifts.", Color(0xFF210538), Color(0xFF450A75), Color(0xFF2D074D), "PHASE_SHIFT", 2.0f)
    val THEATER_22 = BiomeSpec("the_citadel", "22. The Citadel", "Grand fortress outer defense rings, heavy flak walls & command spires.", Color(0xFF240608), Color(0xFF4A0C10), Color(0xFF30080B), "FLAK_BURSTS", 2.1f)
    val THEATER_23 = BiomeSpec("final_approach", "23. Final Approach", "The vanguard battle line — flagship armadas and dreadnought escorts.", Color(0xFF2B0810), Color(0xFF571020), Color(0xFF380A15), "WAR_HAZE", 2.25f)
    val THEATER_24 = BiomeSpec("thunder_dome", "24. Thunder Dome", "The Apex Colosseum: 1v1 Arena against the Nexus Obliterator.", Color(0xFF291B03), Color(0xFF543606), Color(0xFF3B2704), "COLOSSEUM_LIGHTNING", 2.5f)

    // Backward-compatible aliases
    val NEO_TOKYO = THEATER_01
    val CETI_CANYON = THEATER_12
    val ORBITAL_DOCK = THEATER_11

    val ALL_BIOMES = listOf(
        THEATER_01, THEATER_02, THEATER_03, THEATER_04, THEATER_05, THEATER_06,
        THEATER_07, THEATER_08, THEATER_09, THEATER_10, THEATER_11, THEATER_12,
        THEATER_13, THEATER_14, THEATER_15, THEATER_16, THEATER_17, THEATER_18,
        THEATER_19, THEATER_20, THEATER_21, THEATER_22, THEATER_23, THEATER_24
    )

    fun getById(id: String): BiomeSpec = ALL_BIOMES.firstOrNull { it.id == id } ?: THEATER_01
}

// Master Stage Specifications & Wormhole Architecture
data class RouteEventSpec(
    val percent: Int,
    val event: String
)

data class WormholeUnlockSpec(
    val atPercent: Int = 90,
    val requiresNoDamage: Boolean = true,
    val requiresAllEnemiesKilled: Boolean = true,
    val appearanceSeconds: Float = 4.0f
)

data class StageMasterSpec(
    val stage: Int,
    val name: String,
    val weather: String,
    val boss: String,
    val palette: List<String>,
    val obstacles: List<String>,
    val boostWarSpeed: List<String>,
    val bossAbilities: List<String>,
    val heroPlaneSkin: String,
    val routeEvents: List<RouteEventSpec>,
    val wormholeUnlock: WormholeUnlockSpec = WormholeUnlockSpec(),
    val biome: BiomeSpec
)

object StageMasterCatalog {
    val DEFAULT_ROUTE_EVENTS = listOf(
        RouteEventSpec(3, "SPAWN / LOADOUT LOCK"),
        RouteEventSpec(12, "WAVE A"),
        RouteEventSpec(24, "OBSTACLE GATE"),
        RouteEventSpec(40, "BOOST ZONE"),
        RouteEventSpec(53, "ELITE WAVE"),
        RouteEventSpec(65, "WEATHER ESCALATION"),
        RouteEventSpec(76, "WAR-SPEED SPOT"),
        RouteEventSpec(85, "MINIBOSS / CHECKPOINT"),
        RouteEventSpec(90, "PERFECT-RUN WORMHOLE"),
        RouteEventSpec(100, "BOSS ARENA")
    )

    val STAGES = listOf(
        StageMasterSpec(
            stage = 1,
            name = "NEON OUTPOST",
            weather = "NEON RAIN",
            boss = "NEON OVERLORD",
            palette = listOf("#08121E", "#00D9FF", "#FF2C9C", "#2478FF", "#7B2CFF"),
            obstacles = listOf("AA turret nests", "holo-billboard canyon", "laser-road gates", "wet rooftop pylons", "drone traffic"),
            boostWarSpeed = listOf("40% cyan booster lane", "76% rail-sling war-speed strip"),
            bossAbilities = listOf("crossfire laser lattice", "hologram decoy wings", "radial micro-missile burst"),
            heroPlaneSkin = "Black chrome + cyan edge light + magenta circuit filigree; reflective wet-look clearcoat.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_01
        ),
        StageMasterSpec(
            stage = 2,
            name = "ASTEROID BELT",
            weather = "SPACE DUST",
            boss = "GOLIATH DREADNOUGHT",
            palette = listOf("#120B08", "#D65A0A", "#FF7A00", "#6E2AD8", "#2A221F"),
            obstacles = listOf("rotating asteroid clusters", "magnetic purple mines", "cratered rock arches", "debris fields", "tumbling boulders"),
            boostWarSpeed = listOf("38% debris slingshot", "72% twin-asteroid gravity boost"),
            bossAbilities = listOf("broadside cannon walls", "gravity tractor cone", "armor-plate break phases"),
            heroPlaneSkin = "Gunmetal hull + amber hazard stripes + violet anti-grav cores; chipped rock-scar decals.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_02
        ),
        StageMasterSpec(
            stage = 3,
            name = "VOID GATE",
            weather = "VOID WARP",
            boss = "VOID STALKER",
            palette = listOf("#09020F", "#8A2BE2", "#FF21D6", "#5D35B8", "#050505"),
            obstacles = listOf("gravity shear rings", "void spike corridors", "warp-orb mines", "fractured obsidian slabs", "portal turbulence"),
            boostWarSpeed = listOf("34% portal sling", "74% vortex acceleration tunnel"),
            bossAbilities = listOf("teleport ambush", "void clone split", "screen-edge gravity scythe"),
            heroPlaneSkin = "Obsidian ceramic + violet plasma veins + magenta portal glyphs; starfield panel texture.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_03
        ),
        StageMasterSpec(
            stage = 4,
            name = "TOXIC SECTOR",
            weather = "TOXIC MIST",
            boss = "TOXIN HAZE",
            palette = listOf("#061006", "#42FF30", "#92D811", "#D8EA23", "#121212"),
            obstacles = listOf("caustic gas clouds", "acid puddle vents", "corroded pipe towers", "sludge channels", "biohazard fans"),
            boostWarSpeed = listOf("42% pressure-vent thrust lane", "78% reactor exhaust warp strip"),
            bossAbilities = listOf("poison cloud bloom", "corrosion beam", "toxic clone spores"),
            heroPlaneSkin = "Matte black + luminous toxic green vents + yellow warning chevrons; biohazard stencil skin.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_04
        ),
        StageMasterSpec(
            stage = 5,
            name = "ICE FORTRESS",
            weather = "BLIZZARD",
            boss = "FROST NOVA",
            palette = listOf("#061420", "#56C8FF", "#F2FAFF", "#5B8FCC", "#1F5A99"),
            obstacles = listOf("ice spike walls", "frost turrets", "glacier crevasses", "wind shear zones", "frozen bridge arches"),
            boostWarSpeed = listOf("36% ice-canyon slipstream", "71% frozen launch rail"),
            bossAbilities = listOf("freeze pulse", "ice shard fan", "crystal armor rebuild"),
            heroPlaneSkin = "Brushed steel + ice-blue emissive ribs + white frost fade; faceted crystal wing tips.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_05
        ),
        StageMasterSpec(
            stage = 6,
            name = "SOLAR CORE",
            weather = "SOLAR FLARES",
            boss = "SOLAR FLARE",
            palette = listOf("#1A0900", "#FFB000", "#FF6A00", "#FF2A1A", "#4A1200"),
            obstacles = listOf("solar flare curtains", "plasma jets", "heat-plate rings", "burning trails", "corona shockwaves"),
            boostWarSpeed = listOf("41% plasma draft", "77% corona slingshot"),
            bossAbilities = listOf("piercing solar beam", "burning trail cage", "corona overload pulse"),
            heroPlaneSkin = "Mirror black + molten gold trim + orange heat vents; sunburst wing graphics with ember clearcoat.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_06
        ),
        StageMasterSpec(
            stage = 7,
            name = "CYBER CITY",
            weather = "NEON RAIN",
            boss = "VOID REAPER",
            palette = listOf("#070916", "#FF22CB", "#00CFFF", "#3B45FF", "#7E2FB3"),
            obstacles = listOf("skyscraper canyon", "traffic drones", "holo-ad minefields", "service bridges", "electric rooftop fences"),
            boostWarSpeed = listOf("39% maglev corridor", "73% neon transit warp lane"),
            bossAbilities = listOf("black-hole pull", "void orb barrage", "shadow dash ram"),
            heroPlaneSkin = "Carbon fiber + cyan/magenta racing graphics + animated equalizer strips along the fuselage.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_07
        ),
        StageMasterSpec(
            stage = 8,
            name = "JUNKYARD",
            weather = "RUST STORM",
            boss = "BLADE STORM",
            palette = listOf("#130D08", "#F36B17", "#8C3D1F", "#64412B", "#181818"),
            obstacles = listOf("sawblade debris", "capital hull wrecks", "scrap ambushes", "explosive piles", "crane arms"),
            boostWarSpeed = listOf("35% salvage-catapult lane", "70% turbine-corridor boost"),
            bossAbilities = listOf("spinning blade halo", "ricochet shot", "scrap cyclone"),
            heroPlaneSkin = "Weathered titanium + orange weld seams + stenciled serial numbers; patchwork armored panels.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_08
        ),
        StageMasterSpec(
            stage = 9,
            name = "BLACK HOLE",
            weather = "GRAVITY PULL",
            boss = "NEON PHANTOM",
            palette = listOf("#05030A", "#6E35E7", "#A647FF", "#341D78", "#000000"),
            obstacles = listOf("singularity pull zones", "lensing rings", "distorted asteroids", "event-horizon lanes", "tidal debris"),
            boostWarSpeed = listOf("43% gravity-assist arc", "79% horizon-surf warp burst"),
            bossAbilities = listOf("phase dash", "neon laser sweep", "afterimage swarm"),
            heroPlaneSkin = "Ultra-black hull + purple lensing rings + violet star specks; curved gravitational distortion graphics.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_09
        ),
        StageMasterSpec(
            stage = 10,
            name = "LAVA PLANET",
            weather = "LAVA RAIN",
            boss = "OMEGA DRONE",
            palette = listOf("#130500", "#FF5218", "#E62717", "#3A3635", "#050505"),
            obstacles = listOf("magma bombs", "lava eruptions", "basalt spires", "factory platforms", "heat distortion pockets"),
            boostWarSpeed = listOf("37% magma updraft lane", "74% furnace-jet warp strip"),
            bossAbilities = listOf("summon attack drones", "laser grid", "molten missile spread"),
            heroPlaneSkin = "Charcoal armor + red-hot cracks + orange underside glow; volcanic fracture graphic across wings.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_10
        ),
        StageMasterSpec(
            stage = 11,
            name = "ORBITAL ARRAY",
            weather = "ION DUST",
            boss = "LIQUID METAL",
            palette = listOf("#05111B", "#21A7F3", "#9FD9FF", "#1B64D4", "#DCE8F1"),
            obstacles = listOf("laser sweep rings", "ion pulse nodes", "station modules", "satellite spokes", "antenna fields"),
            boostWarSpeed = listOf("40% ion conduit", "75% ring-orbit catapult"),
            bossAbilities = listOf("shape shift", "reflective skin", "metal-wave projectile"),
            heroPlaneSkin = "Polished silver + electric-blue circuitry + white ion streaks; satellite-ring insignia.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_11
        ),
        StageMasterSpec(
            stage = 12,
            name = "SAND WASTES",
            weather = "SAND STORM",
            boss = "SAND VIPER",
            palette = listOf("#1A120B", "#D58A2B", "#A25D27", "#6D4527", "#E9B64A"),
            obstacles = listOf("sand tornadoes", "EM dust bursts", "canyon spires", "buried ruins", "rock arches"),
            boostWarSpeed = listOf("33% dune crest tailwind", "69% canyon vent war-speed"),
            bossAbilities = listOf("sand tornado pull", "razor-dart spread", "burrow strike"),
            heroPlaneSkin = "Desert tan + black belly + gold edge guards; viper-scale wing graphics and dust-worn nose.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_12
        ),
        StageMasterSpec(
            stage = 13,
            name = "BIO LABS",
            weather = "BIO SPORES",
            boss = "BIOSYNTH HYDRA",
            palette = listOf("#071008", "#6FE51C", "#A8E72B", "#1F6D38", "#D8E74A"),
            obstacles = listOf("mutagen domes", "glass tube towers", "spore clouds", "bio-weapon pods", "slime channels"),
            boostWarSpeed = listOf("41% nutrient-flow booster", "77% gene-tube acceleration rail"),
            bossAbilities = listOf("multi-head plasma spit", "regeneration phase", "bio missile homing swarm"),
            heroPlaneSkin = "Gloss black + luminous green vein lattice + translucent bio-cells; gene-helix wing markings.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_13
        ),
        StageMasterSpec(
            stage = 14,
            name = "UNDERWATER RUINS",
            weather = "HYDRO STREAM",
            boss = "AQUA STRIKE",
            palette = listOf("#051620", "#0DAED0", "#53E7FF", "#1B627C", "#87F0ED"),
            obstacles = listOf("sunken towers", "hydro current lanes", "bubble mines", "caustic pillars", "collapsed arches"),
            boostWarSpeed = listOf("36% current jet", "72% hydro-tunnel slingshot"),
            bossAbilities = listOf("water cannon knockback", "bubble shield", "torpedo spiral"),
            heroPlaneSkin = "Deep navy + cyan caustic shimmer + pearl-white trim; scale-like hydrodynamic pattern.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_14
        ),
        StageMasterSpec(
            stage = 15,
            name = "SKY TEMPLE",
            weather = "AURA BREEZE",
            boss = "CELESTIAL GUARD",
            palette = listOf("#10233A", "#8CCAF0", "#F7FBFF", "#7EA6C9", "#D2EEF8"),
            obstacles = listOf("floating island gaps", "crystal shards", "wind columns", "temple gates", "god-ray blind zones"),
            boostWarSpeed = listOf("39% jetstream lane", "75% celestial launch beam"),
            bossAbilities = listOf("divine shield", "holy pulse", "wing-lance rain"),
            heroPlaneSkin = "Pearl white + sky-blue inlays + gold micro-trim; feathered geometric graphics on wings.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_15
        ),
        StageMasterSpec(
            stage = 16,
            name = "MACHINE WORLD",
            weather = "SMELTER ASH",
            boss = "MAGMA BRUTE",
            palette = listOf("#120A07", "#C94B18", "#FF7A22", "#633020", "#181818"),
            obstacles = listOf("crusher presses", "moving belts", "gear walls", "molten drains", "robotic foundry arms"),
            boostWarSpeed = listOf("42% conveyor overdrive", "78% smelter exhaust warp"),
            bossAbilities = listOf("magma ball barrage", "lava trail ram", "hydraulic shockwave"),
            heroPlaneSkin = "Blackened steel + copper welds + hot orange mechanical glyphs; gear-tooth wing striping.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_16
        ),
        StageMasterSpec(
            stage = 17,
            name = "CRYSTAL CAVERNS",
            weather = "CRYSTAL DUST",
            boss = "CYBER HAWK",
            palette = listOf("#0B0715", "#7A2AE8", "#B44DFF", "#34C9FF", "#E0EAFF"),
            obstacles = listOf("mirror crystal fields", "laser reflections", "shard avalanches", "prism gates", "fracture pits"),
            boostWarSpeed = listOf("38% prism-refraction boost", "73% crystal resonance warp"),
            bossAbilities = listOf("target lock pursuit", "missile swarm", "reflective feather shield"),
            heroPlaneSkin = "Dark violet + iridescent crystal facets + cyan laser lines; holographic prismatic wing skin.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_17
        ),
        StageMasterSpec(
            stage = 18,
            name = "STORM FRONT",
            weather = "LIGHTNING",
            boss = "QUANTUM SHIFT",
            palette = listOf("#090C1A", "#6947D9", "#A157FF", "#48B7FF", "#D6E7FF"),
            obstacles = listOf("lightning curtains", "EMP arcs", "storm vortices", "charged cloud walls", "temporal turbulence"),
            boostWarSpeed = listOf("40% thunderhead updraft", "76% lightning-rail war-speed"),
            bossAbilities = listOf("time warp slow field", "quantum teleport", "temporal blade strike"),
            heroPlaneSkin = "Midnight blue + white lightning forks + violet quantum rings; animated pulse texture on tail.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_18
        ),
        StageMasterSpec(
            stage = 19,
            name = "ALIEN JUNGLE",
            weather = "NEURO MIST",
            boss = "STORM LORD",
            palette = listOf("#06100C", "#57D71D", "#B8EA24", "#1B6D54", "#6639A5"),
            obstacles = listOf("predatory vines", "bioluminescent canopy", "spore pods", "living root gates", "acid flower turrets"),
            boostWarSpeed = listOf("37% canopy wind tunnel", "72% bio-electric surge lane"),
            bossAbilities = listOf("random thunder strike", "persistent storm field", "charged wing dive"),
            heroPlaneSkin = "Forest-black + acid-green edge veins + purple bio-lights; alien leaf/fractal graphics.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_19
        ),
        StageMasterSpec(
            stage = 20,
            name = "SPACE GRAVEYARD",
            weather = "DEBRIS HAZARD",
            boss = "GRAVITY TITAN",
            palette = listOf("#050912", "#2860D9", "#443CB4", "#7569E7", "#292151"),
            obstacles = listOf("wrecked battleship hulls", "debris collisions", "sniper corridors", "engine carcasses", "floating armor plates"),
            boostWarSpeed = listOf("34% reactor-remnant boost", "69% wreck-corridor gravity sling"),
            bossAbilities = listOf("gravity field", "meteor drop", "hull-fragment shield"),
            heroPlaneSkin = "Cold gunmetal + spectral blue-violet exhaust + ghosted fleet emblems; battle-scar skin.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_20
        ),
        StageMasterSpec(
            stage = 21,
            name = "DIMENSION RIFT",
            weather = "PHASE SHIFT",
            boss = "CRYSTAL REVENANT",
            palette = listOf("#0B0414", "#7720E8", "#A42DDC", "#E1297A", "#B81731"),
            obstacles = listOf("reality seams", "phase walls", "fractured chunks", "glitch corridors", "lava-wake scars"),
            boostWarSpeed = listOf("41% phase skip", "77% multiverse tear warp"),
            bossAbilities = listOf("multi-direction crystal spikes", "reflective armor", "phase inversion"),
            heroPlaneSkin = "Black-violet base + magenta/red glitch slices + fractured mirror panels; chromatic split graphics.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_21
        ),
        StageMasterSpec(
            stage = 22,
            name = "THE CITADEL",
            weather = "FLAK BURSTS",
            boss = "CITADEL COMMANDER",
            palette = listOf("#110708", "#982123", "#D73722", "#6F202A", "#262626"),
            obstacles = listOf("heavy flak walls", "command spires", "defense rings", "missile towers", "armored blast doors"),
            boostWarSpeed = listOf("39% launch-bay catapult", "74% reactor trench overdrive"),
            bossAbilities = listOf("flak grid command", "shielded turret ring", "command missile swarm"),
            heroPlaneSkin = "Dark armor + crimson command stripes + metallic silver insignia; angular military geometry.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_22
        ),
        StageMasterSpec(
            stage = 23,
            name = "FINAL APPROACH",
            weather = "WAR HAZE",
            boss = "INFERNO RIDER",
            palette = listOf("#140707", "#7E151B", "#D01B12", "#F04A18", "#5A2B1D"),
            obstacles = listOf("flagship armada lanes", "missile walls", "fighter swarms", "capital cannon beams", "burning wreck trails"),
            boostWarSpeed = listOf("42% carrier launch wake", "80% final assault war-speed corridor"),
            bossAbilities = listOf("fire dash", "inferno wave", "flame-lance pursuit"),
            heroPlaneSkin = "Satin black + deep red spear graphics + orange afterburner blades; campaign kill-mark decals.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_23
        ),
        StageMasterSpec(
            stage = 24,
            name = "THUNDER DOME",
            weather = "COLOSSEUM LIGHTNING",
            boss = "NEXUS OBLITERATOR",
            palette = listOf("#110D05", "#FFC42D", "#E9A400", "#7B5100", "#1B1B1B"),
            obstacles = listOf("rotating arena rings", "lightning spokes", "energy walls", "moving pylons", "collapse zones"),
            boostWarSpeed = listOf("35% outer-ring accelerator", "70% inner-ring war-speed launch"),
            bossAbilities = listOf("universe collapse screen pulse", "multi-phase form change", "nexus laser crown", "arena-ring shockwave"),
            heroPlaneSkin = "Mirror black + championship gold + electric-blue core lines; 3000 Studios thunder crest across wings.",
            routeEvents = DEFAULT_ROUTE_EVENTS,
            biome = BiomeCatalog.THEATER_24
        )
    )

    fun getForStage(stageNum: Int): StageMasterSpec {
        val idx = (stageNum - 1).coerceIn(0, STAGES.size - 1)
        return STAGES[idx]
    }

    fun getForBiome(biomeId: String): StageMasterSpec {
        return STAGES.firstOrNull { it.biome.id == biomeId } ?: STAGES[0]
    }
}
