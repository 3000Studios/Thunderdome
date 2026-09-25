package com.example.game.model

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class PlayerAircraftState(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var bankAngle: Float = 0f, // Degrees (-35 to +35)
    var pitchScale: Float = 1.0f,
    var barrelRollProgress: Float = 0f, // 0..1 when rolling
    var health: Float = 1000f,
    var maxHealth: Float = 1000f,
    var shield: Float = 600f,
    var maxShield: Float = 600f,
    var shieldRechargeTimer: Float = 0f,
    var boost: Float = 100f,
    var maxBoost: Float = 100f,
    var isBoosting: Boolean = false,
    var heat: Float = 0f,
    var isOverheated: Boolean = false,
    var primaryCooldown: Float = 0f,
    var secondaryCooldown: Float = 0f,
    var specialCooldown: Float = 0f,
    var specialDurationLeft: Float = 0f,
    var invulnerableTimer: Float = 0f,
    var recoilY: Float = 0f,
    var perks: MutableMap<String, Int> = mutableMapOf()
)

enum class EnemyType(
    val title: String,
    val maxHp: Float,
    val speed: Float,
    val radius: Float,
    val score: Int
) {
    SCOUT_DRONE("Scout Drone", 45f, 380f, 22f, 150),
    FAST_INTERCEPTOR("Interceptor", 90f, 440f, 28f, 300),
    HEAVY_GUNSHIP("Heavy Gunship", 380f, 210f, 45f, 750),
    STEALTH_RAIDER("Stealth Raider", 160f, 350f, 32f, 500),
    MISSILE_CORVETTE("Missile Corvette", 260f, 240f, 38f, 600)
}

data class EnemyEntity(
    val id: Long,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var health: Float = type.maxHp,
    var maxHealth: Float = type.maxHp,
    var shield: Float = if (type == EnemyType.HEAVY_GUNSHIP) 150f else 0f,
    var fireTimer: Float = 0f,
    var aiStateTimer: Float = 0f,
    var angle: Float = 180f,
    var isCloaked: Boolean = false,
    var cloakAlpha: Float = 1f,
    var hitFlashTimer: Float = 0f
)

data class BossComponent(
    val id: String,
    val name: String,
    var offsetX: Float,
    var offsetY: Float,
    var width: Float,
    var height: Float,
    var health: Float,
    var maxHealth: Float,
    var isDestroyed: Boolean = false,
    var fireTimer: Float = 0f
)

enum class BossPhase {
    PHASE_1_ARMAMENT,
    PHASE_2_SHIELD_CORE,
    PHASE_3_RAGE_OVERDRIVE,
    DYING,
    DESTROYED
}

data class BossEntity(
    val id: String,
    val name: String,
    var x: Float,
    var y: Float,
    var targetX: Float,
    var targetY: Float,
    var width: Float = 360f,
    var height: Float = 280f,
    var health: Float = 6000f,
    var maxHealth: Float = 6000f,
    var shield: Float = 2500f,
    var maxShield: Float = 2500f,
    var phase: BossPhase = BossPhase.PHASE_1_ARMAMENT,
    var components: MutableList<BossComponent> = mutableListOf(),
    var attackPatternTimer: Float = 0f,
    var deathSequenceTimer: Float = 0f,
    var laserBeamAngle: Float = 90f,
    var isFiringLaser: Boolean = false,
    var hitFlashTimer: Float = 0f
)

enum class ProjectileType {
    PLASMA,
    LASER,
    RAILGUN,
    FLAK,
    HOMING_MISSILE,
    CLUSTER_BOMB,
    EMP_TORPEDO,
    DRONE_BOLT,
    ENEMY_PLASMA,
    ENEMY_LASER,
    ENEMY_MISSILE,
    SHOCKWAVE
}

data class TrailPoint(val x: Float, val y: Float, var alpha: Float = 1f)

data class ProjectileEntity(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val damage: Float,
    val isPlayer: Boolean,
    val type: ProjectileType,
    val color: Color,
    val glowColor: Color,
    val size: Float,
    var pierceCount: Int = 1,
    var life: Float = 3.0f,
    var homingTargetId: Long? = null,
    val trail: ArrayDeque<TrailPoint> = ArrayDeque()
)

enum class ParticleType {
    FIREBALL,
    SMOKE,
    SPARK,
    SHOCKWAVE_RING,
    DEBRIS,
    SPEED_STREAK,
    AFTERBURNER
}

data class ParticleEntity(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var alpha: Float = 1.0f,
    var life: Float = 1.0f,
    var maxLife: Float = 1.0f,
    var rotation: Float = 0f,
    var rotSpeed: Float = 0f,
    val type: ParticleType
)

enum class PowerUpType(val title: String, val color: Color) {
    SHIELD_REFILL("Shield Overcharge", Color(0xFF38BDF8)),
    WEAPON_OVERDRIVE("Quad Overdrive", Color(0xFFF59E0B)),
    REPAIR_NANO("Hull Repair", Color(0xFF22C55E)),
    MEGA_BOMB("Nuke Cleanser", Color(0xFFEF4444)),
    BOOST_INFINITY("Infinite Boost", Color(0xFF00F0FF)),
    TECH_CORE("Plasma Tech Core", Color(0xFFA855F7))
}

data class PowerUpEntity(
    val id: Long,
    var x: Float,
    var y: Float,
    var vy: Float = 90f,
    val type: PowerUpType,
    var bobTimer: Float = 0f,
    var lifeTimer: Float = 18f
)

data class ShockwaveImpact(
    val x: Float,
    val y: Float,
    var radius: Float = 10f,
    val maxRadius: Float = 160f,
    var alpha: Float = 1.0f,
    val color: Color = Color(0xFF00F0FF)
)

data class FloatingCombatText(
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var alpha: Float = 1.0f,
    var scale: Float = 1.0f,
    var life: Float = 0.9f
)

data class EnvironmentalStructure(
    val id: Long,
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val type: String, // FUEL_TANK, RADAR, SAM_SITE, GENERATOR
    var health: Float,
    var maxHealth: Float,
    var isDestroyed: Boolean = false,
    var burningTimer: Float = 0f
)
