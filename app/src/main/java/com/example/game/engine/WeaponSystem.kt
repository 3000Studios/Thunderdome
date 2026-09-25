package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.audio.AudioHapticSystem
import com.example.game.model.*
import kotlin.math.*
import kotlin.random.Random

class WeaponSystem(
    private val audioHaptics: AudioHapticSystem,
    private val vfx: VFXSystem
) {
    val projectiles = ArrayList<ProjectileEntity>(120)
    val powerUps = ArrayList<PowerUpEntity>(20)
    private var projectileIdCounter = 1L
    private var powerUpIdCounter = 1L

    var timeDilationFactor: Float = 1.0f

    fun clear() {
        projectiles.clear()
        powerUps.clear()
        timeDilationFactor = 1.0f
    }

    // Fire Player Primary Weapon
    fun firePrimary(
        player: PlayerAircraftState,
        spec: WeaponSpec,
        critChance: Float
    ) {
        if (player.primaryCooldown > 0f || player.isOverheated) return

        val fireInterval = 1.0f / spec.fireRate
        player.primaryCooldown = fireInterval
        player.heat = min(100f, player.heat + spec.heatPerShot)
        if (player.heat >= 100f) {
            player.isOverheated = true
            audioHaptics.playSound(AudioHapticSystem.SoundType.WARNING_BEEP)
        }

        // Apply weapon recoil
        player.recoilY = min(15f, player.recoilY + 4f)
        audioHaptics.triggerFireHaptic()

        // Sound trigger
        when (spec.id) {
            "plasma_gatling" -> audioHaptics.playSound(AudioHapticSystem.SoundType.GATLING_SHOT, 0.7f)
            "twin_laser" -> audioHaptics.playSound(AudioHapticSystem.SoundType.PLASMA_SHOT, 0.8f)
            "railgun" -> audioHaptics.playSound(AudioHapticSystem.SoundType.RAILGUN_SHOT, 1.0f)
            else -> audioHaptics.playSound(AudioHapticSystem.SoundType.EXPLOSION_LIGHT, 0.6f)
        }

        val isCrit = Random.nextFloat() < critChance
        val damage = if (isCrit) spec.baseDamage * 2.0f else spec.baseDamage

        val nozzleOffsets = when (spec.projectileCount) {
            1 -> listOf(0f)
            2 -> listOf(-16f, 16f)
            3 -> listOf(-20f, 0f, 20f)
            5 -> listOf(-28f, -14f, 0f, 14f, 28f)
            else -> listOf(-12f, 12f)
        }

        for (offset in nozzleOffsets) {
            val angleSpread = (Random.nextFloat() - 0.5f) * spec.spreadAngle
            val rad = (-90f + angleSpread) * PI.toFloat() / 180f
            val vx = cos(rad) * spec.projectileSpeed
            val vy = sin(rad) * spec.projectileSpeed

            projectiles.add(
                ProjectileEntity(
                    id = projectileIdCounter++,
                    x = player.x + offset,
                    y = player.y - 28f,
                    vx = vx,
                    vy = vy,
                    damage = damage,
                    isPlayer = true,
                    type = when (spec.id) {
                        "twin_laser" -> ProjectileType.LASER
                        "railgun" -> ProjectileType.RAILGUN
                        "heavy_flak" -> ProjectileType.FLAK
                        else -> ProjectileType.PLASMA
                    },
                    color = if (isCrit) Color(0xFFFFD700) else spec.projectileColor,
                    glowColor = spec.glowColor,
                    size = if (spec.id == "railgun") 7f else 5f,
                    pierceCount = if (spec.isPiercing) 3 else 1
                )
            )
        }
    }

    // Fire Player Secondary Weapon
    fun fireSecondary(
        player: PlayerAircraftState,
        spec: WeaponSpec,
        enemies: List<EnemyEntity>,
        boss: BossEntity?
    ) {
        if (player.secondaryCooldown > 0f) return

        player.secondaryCooldown = 1.0f / spec.fireRate
        audioHaptics.triggerExplosionHaptic(false)
        audioHaptics.playSound(AudioHapticSystem.SoundType.MISSILE_LAUNCH)

        when (spec.id) {
            "swarm_missiles" -> {
                // Fire salvo of 4 missiles with homing locks
                val targets = enemies.filter { it.y > -50f }.shuffled()
                for (i in 0 until 4) {
                    val angleOffset = if (i % 2 == 0) -25f - i * 8f else 25f + i * 8f
                    val rad = (-90f + angleOffset) * PI.toFloat() / 180f
                    val targetId = targets.getOrNull(i % max(1, targets.size))?.id

                    projectiles.add(
                        ProjectileEntity(
                            id = projectileIdCounter++,
                            x = player.x + (if (i % 2 == 0) -22f else 22f),
                            y = player.y - 10f,
                            vx = cos(rad) * spec.projectileSpeed,
                            vy = sin(rad) * spec.projectileSpeed,
                            damage = spec.baseDamage,
                            isPlayer = true,
                            type = ProjectileType.HOMING_MISSILE,
                            color = spec.projectileColor,
                            glowColor = spec.glowColor,
                            size = 6f,
                            homingTargetId = targetId,
                            life = 3.5f
                        )
                    )
                }
            }
            "emp_torpedo" -> {
                projectiles.add(
                    ProjectileEntity(
                        id = projectileIdCounter++,
                        x = player.x,
                        y = player.y - 30f,
                        vx = 0f,
                        vy = -spec.projectileSpeed,
                        damage = spec.baseDamage,
                        isPlayer = true,
                        type = ProjectileType.EMP_TORPEDO,
                        color = spec.projectileColor,
                        glowColor = spec.glowColor,
                        size = 11f,
                        life = 4.0f
                    )
                )
            }
            "cluster_bombs" -> {
                for (i in -2..2) {
                    val angle = -90f + i * 14f
                    val rad = angle * PI.toFloat() / 180f
                    projectiles.add(
                        ProjectileEntity(
                            id = projectileIdCounter++,
                            x = player.x,
                            y = player.y - 20f,
                            vx = cos(rad) * spec.projectileSpeed,
                            vy = sin(rad) * spec.projectileSpeed,
                            damage = spec.baseDamage,
                            isPlayer = true,
                            type = ProjectileType.CLUSTER_BOMB,
                            color = spec.projectileColor,
                            glowColor = spec.glowColor,
                            size = 8f,
                            life = 1.2f
                        )
                    )
                }
            }
            else -> {
                // Drone bolts
                projectiles.add(
                    ProjectileEntity(
                        id = projectileIdCounter++,
                        x = player.x - 30f,
                        y = player.y,
                        vx = 0f,
                        vy = -spec.projectileSpeed,
                        damage = spec.baseDamage,
                        isPlayer = true,
                        type = ProjectileType.DRONE_BOLT,
                        color = spec.projectileColor,
                        glowColor = spec.glowColor,
                        size = 5f
                    )
                )
                projectiles.add(
                    ProjectileEntity(
                        id = projectileIdCounter++,
                        x = player.x + 30f,
                        y = player.y,
                        vx = 0f,
                        vy = -spec.projectileSpeed,
                        damage = spec.baseDamage,
                        isPlayer = true,
                        type = ProjectileType.DRONE_BOLT,
                        color = spec.projectileColor,
                        glowColor = spec.glowColor,
                        size = 5f
                    )
                )
            }
        }
    }

    // Activate Special Ability
    fun activateSpecial(player: PlayerAircraftState, spec: WeaponSpec) {
        if (player.specialCooldown > 0f) return

        player.specialCooldown = 18.0f // 18s cooldown
        player.specialDurationLeft = 5.0f // 5s active duration

        audioHaptics.triggerExplosionHaptic(true)

        when (spec.id) {
            "chrono_overdrive" -> {
                timeDilationFactor = 0.35f
                audioHaptics.playSound(AudioHapticSystem.SoundType.BOOST_BURST)
                vfx.addText("CHRONO STASIS!", player.x, player.y - 40f, Color(0xFF00F0FF))
            }
            "hyper_shield" -> {
                player.invulnerableTimer = 5.0f
                player.shield = player.maxShield
                audioHaptics.playSound(AudioHapticSystem.SoundType.SHIELD_BREAK)
                vfx.addText("AEGIS BARRIER!", player.x, player.y - 40f, Color(0xFF38BDF8))
            }
            "nova_blast" -> {
                // Clear all enemy projectiles and deal massive AOE damage
                projectiles.removeAll { !it.isPlayer }
                vfx.spawnExplosion(player.x, player.y, isHeavy = true, colorScheme = Color(0xFFFDE047))
                audioHaptics.playSound(AudioHapticSystem.SoundType.EXPLOSION_HEAVY)
                vfx.addText("OMEGA NOVA!", player.x, player.y - 40f, Color(0xFFF59E0B))
            }
            "warp_dash" -> {
                player.invulnerableTimer = 2.5f
                player.y -= 180f
                vfx.spawnExplosion(player.x, player.y, isHeavy = false, colorScheme = Color(0xFFB347FF))
                audioHaptics.playSound(AudioHapticSystem.SoundType.BOOST_BURST)
                vfx.addText("PHASE WARP!", player.x, player.y - 40f, Color(0xFFA855F7))
            }
        }
    }

    fun updateProjectiles(
        dt: Float,
        screenWidth: Float,
        screenHeight: Float,
        enemies: List<EnemyEntity>,
        boss: BossEntity?
    ) {
        val effectiveDt = dt * timeDilationFactor

        val iter = projectiles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            val stepDt = if (p.isPlayer) dt else effectiveDt

            // Homing missile logic
            if (p.type == ProjectileType.HOMING_MISSILE) {
                val target = enemies.find { it.id == p.homingTargetId } ?: boss?.let {
                    EnemyEntity(0L, EnemyType.HEAVY_GUNSHIP, it.x, it.y)
                } ?: enemies.minByOrNull { hypot(it.x - p.x, it.y - p.y) }

                if (target != null) {
                    val angleToTarget = atan2(target.y - p.y, target.x - p.x)
                    val currentAngle = atan2(p.vy, p.vx)
                    var diff = angleToTarget - currentAngle
                    while (diff < -PI) diff += (2 * PI).toFloat()
                    while (diff > PI) diff -= (2 * PI).toFloat()
                    val turnRate = 5.5f * stepDt
                    val newAngle = currentAngle + diff.coerceIn(-turnRate, turnRate)
                    val speed = hypot(p.vx, p.vy)
                    p.vx = cos(newAngle) * speed
                    p.vy = sin(newAngle) * speed
                }
                vfx.spawnMissileTrail(p.x, p.y)
            }

            p.x += p.vx * stepDt
            p.y += p.vy * stepDt
            p.life -= stepDt

            // Append trail point
            p.trail.addFirst(TrailPoint(p.x, p.y, 1f))
            if (p.trail.size > 8) p.trail.removeLast()
            for (t in p.trail) {
                t.alpha = max(0f, t.alpha - stepDt * 4.5f)
            }

            // Cluster bomb splitting
            if (p.type == ProjectileType.CLUSTER_BOMB && p.life <= 0f) {
                vfx.spawnExplosion(p.x, p.y, isHeavy = false, colorScheme = Color(0xFFF97316))
                for (b in 0 until 4) {
                    val rad = (Random.nextFloat() * 2f * PI).toFloat()
                    projectiles.add(
                        ProjectileEntity(
                            id = projectileIdCounter++,
                            x = p.x,
                            y = p.y,
                            vx = cos(rad) * 450f,
                            vy = sin(rad) * 450f,
                            damage = p.damage * 0.6f,
                            isPlayer = true,
                            type = ProjectileType.PLASMA,
                            color = Color(0xFFFB923C),
                            glowColor = Color(0x99EA580C),
                            size = 4f,
                            life = 0.6f
                        )
                    )
                }
            }

            // Out of bounds check
            if (p.x < -60f || p.x > screenWidth + 60f || p.y < -80f || p.y > screenHeight + 80f || p.life <= 0f) {
                iter.remove()
            }
        }

        // Update Power-ups
        val pIter = powerUps.iterator()
        while (pIter.hasNext()) {
            val pu = pIter.next()
            pu.bobTimer += dt
            pu.y += pu.vy * dt
            pu.lifeTimer -= dt
            if (pu.y > screenHeight + 60f || pu.lifeTimer <= 0f) {
                pIter.remove()
            }
        }
    }

    fun spawnPowerUp(x: Float, y: Float) {
        val types = PowerUpType.values()
        val picked = types[Random.nextInt(types.size)]
        powerUps.add(
            PowerUpEntity(
                id = powerUpIdCounter++,
                x = x,
                y = y,
                type = picked
            )
        )
    }
}
