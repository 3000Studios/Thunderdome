package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.model.*
import kotlin.math.*
import kotlin.random.Random

class EnemySystem {
    val enemies = ArrayList<EnemyEntity>(32)
    var currentBoss: BossEntity? = null
    private var enemyIdCounter = 1L

    var waveNumber = 1
    var waveSpawnTimer = 1.5f
    var enemiesKilledInWave = 0
    var isBossWave = false

    fun reset() {
        enemies.clear()
        currentBoss = null
        waveNumber = 1
        waveSpawnTimer = 1.5f
        enemiesKilledInWave = 0
        isBossWave = false
    }

    fun update(
        dt: Float,
        screenWidth: Float,
        screenHeight: Float,
        player: PlayerAircraftState,
        spawnEnemyProjectile: (x: Float, y: Float, vx: Float, vy: Float, type: ProjectileType, damage: Float, color: Color) -> Unit,
        onBossDyingExplosion: (x: Float, y: Float) -> Unit,
        onBossDefeated: () -> Unit
    ) {
        // Spawn waves if no boss
        if (currentBoss == null) {
            waveSpawnTimer -= dt
            if (waveSpawnTimer <= 0f) {
                if (waveNumber % 4 == 0 && !isBossWave) {
                    // Trigger Epic Boss Encounter
                    spawnBoss(screenWidth, screenHeight)
                    isBossWave = true
                } else {
                    spawnWaveFormation(screenWidth)
                    waveSpawnTimer = 3.8f + Random.nextFloat() * 1.5f
                }
            }
        }

        // Update normal enemies
        val eIter = enemies.iterator()
        while (eIter.hasNext()) {
            val e = eIter.next()
            e.aiStateTimer += dt
            e.fireTimer += dt
            if (e.hitFlashTimer > 0f) e.hitFlashTimer -= dt

            // Type-specific behaviors
            when (e.type) {
                EnemyType.SCOUT_DRONE -> {
                    // Erratic zig-zag dive
                    e.vy = e.type.speed
                    e.vx = sin(e.aiStateTimer * 4.5f) * 160f
                    if (e.fireTimer > 1.4f) {
                        e.fireTimer = 0f
                        spawnEnemyProjectile(e.x, e.y + 12f, 0f, 480f, ProjectileType.ENEMY_PLASMA, 18f, Color(0xFFFF3366))
                    }
                }
                EnemyType.FAST_INTERCEPTOR -> {
                    // Dive and track player's X
                    e.vy = e.type.speed
                    val dx = player.x - e.x
                    e.vx += dx * dt * 4f
                    e.vx = e.vx.coerceIn(-240f, 240f)
                    if (e.fireTimer > 1.1f && e.y < player.y - 80f) {
                        e.fireTimer = 0f
                        // Burst of 2 shots
                        spawnEnemyProjectile(e.x - 10f, e.y + 16f, -30f, 550f, ProjectileType.ENEMY_PLASMA, 24f, Color(0xFFFF5500))
                        spawnEnemyProjectile(e.x + 10f, e.y + 16f, 30f, 550f, ProjectileType.ENEMY_PLASMA, 24f, Color(0xFFFF5500))
                    }
                }
                EnemyType.HEAVY_GUNSHIP -> {
                    // Slow downward drift, hover at top third
                    if (e.y < screenHeight * 0.28f) {
                        e.vy = e.type.speed
                    } else {
                        e.vy = sin(e.aiStateTimer * 1.2f) * 35f
                    }
                    e.vx = cos(e.aiStateTimer * 0.9f) * 110f

                    if (e.fireTimer > 1.8f) {
                        e.fireTimer = 0f
                        // 3-way spread attack
                        for (angle in listOf(-25f, 0f, 25f)) {
                            val rad = (90f + angle) * PI.toFloat() / 180f
                            val speed = 420f
                            spawnEnemyProjectile(
                                e.x, e.y + 24f,
                                cos(rad) * speed, sin(rad) * speed,
                                ProjectileType.ENEMY_PLASMA, 32f, Color(0xFFFF2200)
                            )
                        }
                    }
                }
                EnemyType.STEALTH_RAIDER -> {
                    // Cloaking cycle
                    val cycle = (e.aiStateTimer % 4.0f)
                    if (cycle < 2.0f) {
                        e.isCloaked = true
                        e.cloakAlpha = (e.cloakAlpha - dt * 2.5f).coerceIn(0.12f, 1f)
                    } else {
                        e.isCloaked = false
                        e.cloakAlpha = (e.cloakAlpha + dt * 3.5f).coerceIn(0.12f, 1f)
                    }
                    e.vy = e.type.speed * 0.7f
                    e.vx = sin(e.aiStateTimer * 2f) * 140f

                    if (!e.isCloaked && e.fireTimer > 1.5f) {
                        e.fireTimer = 0f
                        spawnEnemyProjectile(e.x, e.y + 18f, 0f, 620f, ProjectileType.ENEMY_LASER, 40f, Color(0xFFC084FC))
                    }
                }
                EnemyType.MISSILE_CORVETTE -> {
                    // Standoff at top, fires guided missiles
                    if (e.y < screenHeight * 0.2f) {
                        e.vy = e.type.speed
                    } else {
                        e.vy = 0f
                    }
                    e.vx = sin(e.aiStateTimer * 0.7f) * 90f

                    if (e.fireTimer > 2.8f) {
                        e.fireTimer = 0f
                        spawnEnemyProjectile(e.x - 18f, e.y + 15f, -60f, 280f, ProjectileType.ENEMY_MISSILE, 55f, Color(0xFFFF2A4D))
                        spawnEnemyProjectile(e.x + 18f, e.y + 15f, 60f, 280f, ProjectileType.ENEMY_MISSILE, 55f, Color(0xFFFF2A4D))
                    }
                }
            }

            e.x += e.vx * dt
            e.y += e.vy * dt

            // Despawn if flown off-screen bottom
            if (e.y > screenHeight + 60f || e.health <= 0f) {
                eIter.remove()
            }
        }

        // Update Boss Entity
        currentBoss?.let { boss ->
            boss.attackPatternTimer += dt
            if (boss.hitFlashTimer > 0f) boss.hitFlashTimer -= dt

            if (boss.phase == BossPhase.DYING) {
                boss.deathSequenceTimer += dt
                if (Random.nextFloat() < 0.35f) {
                    val expX = boss.x + (Random.nextFloat() - 0.5f) * boss.width
                    val expY = boss.y + (Random.nextFloat() - 0.5f) * boss.height
                    onBossDyingExplosion(expX, expY)
                }

                if (boss.deathSequenceTimer >= 3.2f) {
                    boss.phase = BossPhase.DESTROYED
                    onBossDefeated()
                    currentBoss = null
                    isBossWave = false
                    waveNumber++
                }
                return@let
            }

            // Move boss smoothly into combat area
            boss.x += (boss.targetX - boss.x) * dt * 1.5f
            boss.y += (boss.targetY - boss.y) * dt * 1.5f

            // Wander side-to-side
            boss.targetX = (screenWidth * 0.5f) + sin(boss.attackPatternTimer * 0.8f) * (screenWidth * 0.28f)

            // Check component destruction
            val leftTurret = boss.components.find { it.id == "left_turret" }
            val rightTurret = boss.components.find { it.id == "right_missile" }

            // Phase progression based on health
            val healthPercent = boss.health / boss.maxHealth
            if (healthPercent <= 0.6f && boss.phase == BossPhase.PHASE_1_ARMAMENT) {
                boss.phase = BossPhase.PHASE_2_SHIELD_CORE
            } else if (healthPercent <= 0.25f && boss.phase == BossPhase.PHASE_2_SHIELD_CORE) {
                boss.phase = BossPhase.PHASE_3_RAGE_OVERDRIVE
            }

            // Boss Attack Patterns:
            // 1. Left turret plasma barrage
            if (leftTurret != null && !leftTurret.isDestroyed) {
                leftTurret.fireTimer += dt
                if (leftTurret.fireTimer > 1.4f) {
                    leftTurret.fireTimer = 0f
                    val tx = boss.x + leftTurret.offsetX
                    val ty = boss.y + leftTurret.offsetY
                    for (i in -1..1) {
                        spawnEnemyProjectile(tx, ty, i * 80f, 420f, ProjectileType.ENEMY_PLASMA, 35f, Color(0xFFFF5500))
                    }
                }
            }

            // 2. Right turret missile salvo
            if (rightTurret != null && !rightTurret.isDestroyed) {
                rightTurret.fireTimer += dt
                if (rightTurret.fireTimer > 2.6f) {
                    rightTurret.fireTimer = 0f
                    val tx = boss.x + rightTurret.offsetX
                    val ty = boss.y + rightTurret.offsetY
                    spawnEnemyProjectile(tx - 15f, ty, -100f, 250f, ProjectileType.ENEMY_MISSILE, 60f, Color(0xFFFF1744))
                    spawnEnemyProjectile(tx + 15f, ty, 100f, 250f, ProjectileType.ENEMY_MISSILE, 60f, Color(0xFFFF1744))
                }
            }

            // 3. Central Core: Mega Plasma Spiral or Laser Sweep in Rage Phase
            if (boss.phase == BossPhase.PHASE_3_RAGE_OVERDRIVE) {
                // Sweeping apocalyptic laser beam
                boss.isFiringLaser = true
                val cycle = (boss.attackPatternTimer % 3.0f)
                boss.laserBeamAngle = 70f + sin(boss.attackPatternTimer * 2.5f) * 40f
                if (cycle > 1.2f && Random.nextFloat() < 0.25f) {
                    val coreX = boss.x
                    val coreY = boss.y + 40f
                    val rad = boss.laserBeamAngle * PI.toFloat() / 180f
                    spawnEnemyProjectile(
                        coreX, coreY,
                        cos(rad) * 750f, sin(rad) * 750f,
                        ProjectileType.ENEMY_LASER, 45f, Color(0xFFFF0055)
                    )
                }
            } else {
                boss.isFiringLaser = false
                // Spiral plasma hell
                val coreCycle = (boss.attackPatternTimer % 2.0f)
                if (coreCycle < 0.4f && Random.nextFloat() < 0.35f) {
                    val angle = (boss.attackPatternTimer * 360f) % 360f
                    val rad = angle * PI.toFloat() / 180f
                    spawnEnemyProjectile(
                        boss.x, boss.y + 35f,
                        cos(rad) * 380f, sin(rad) * 380f,
                        ProjectileType.ENEMY_PLASMA, 30f, Color(0xFFFF2A4D)
                    )
                }
            }
        }
    }

    private fun spawnWaveFormation(screenWidth: Float) {
        val count = 3 + (waveNumber / 2).coerceAtMost(5)
        val formationType = Random.nextInt(3)

        when (formationType) {
            0 -> {
                // V-Formation Interceptors
                val leaderX = screenWidth * 0.5f
                for (i in 0 until count) {
                    val xOffset = (i - count / 2) * 55f
                    val yOffset = -80f - abs(i - count / 2) * 45f
                    enemies.add(
                        EnemyEntity(
                            id = enemyIdCounter++,
                            type = EnemyType.FAST_INTERCEPTOR,
                            x = leaderX + xOffset,
                            y = yOffset
                        )
                    )
                }
            }
            1 -> {
                // Heavy Gunship with Drone Escorts
                val gunshipX = 100f + Random.nextFloat() * (screenWidth - 200f)
                enemies.add(
                    EnemyEntity(
                        id = enemyIdCounter++,
                        type = EnemyType.HEAVY_GUNSHIP,
                        x = gunshipX,
                        y = -100f
                    )
                )
                enemies.add(
                    EnemyEntity(
                        id = enemyIdCounter++,
                        type = EnemyType.SCOUT_DRONE,
                        x = gunshipX - 70f,
                        y = -70f
                    )
                )
                enemies.add(
                    EnemyEntity(
                        id = enemyIdCounter++,
                        type = EnemyType.SCOUT_DRONE,
                        x = gunshipX + 70f,
                        y = -70f
                    )
                )
            }
            else -> {
                // Stealth Raider and Missile Corvette combo
                enemies.add(
                    EnemyEntity(
                        id = enemyIdCounter++,
                        type = EnemyType.STEALTH_RAIDER,
                        x = screenWidth * 0.3f,
                        y = -90f
                    )
                )
                enemies.add(
                    EnemyEntity(
                        id = enemyIdCounter++,
                        type = EnemyType.MISSILE_CORVETTE,
                        x = screenWidth * 0.7f,
                        y = -110f
                    )
                )
            }
        }
    }

    fun spawnBoss(screenWidth: Float, screenHeight: Float) {
        val boss = BossEntity(
            id = "goliath_dreadnought",
            name = "EX-99 GOLIATH DREADNOUGHT",
            x = screenWidth * 0.5f,
            y = -220f,
            targetX = screenWidth * 0.5f,
            targetY = screenHeight * 0.22f,
            width = 340f,
            height = 240f,
            health = 5500f + (waveNumber * 1000f),
            maxHealth = 5500f + (waveNumber * 1000f),
            shield = 2000f,
            maxShield = 2000f
        )

        boss.components.add(
            BossComponent(
                id = "left_turret",
                name = "Port Heavy Flak",
                offsetX = -120f,
                offsetY = 10f,
                width = 50f,
                height = 50f,
                health = 1200f,
                maxHealth = 1200f
            )
        )

        boss.components.add(
            BossComponent(
                id = "right_missile",
                name = "Starboard Missile Rack",
                offsetX = 120f,
                offsetY = 10f,
                width = 50f,
                height = 50f,
                health = 1200f,
                maxHealth = 1200f
            )
        )

        currentBoss = boss
    }
}
