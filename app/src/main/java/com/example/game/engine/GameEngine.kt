package com.example.game.engine

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.game.audio.AudioHapticSystem
import com.example.game.model.*
import kotlin.math.*
import kotlin.random.Random

data class GameCombatStats(
    var score: Long = 0L,
    var kills: Int = 0,
    var bossKills: Int = 0,
    var comboCount: Int = 0,
    var comboTimer: Float = 0f,
    var creditsEarned: Long = 0L,
    var plasmaCoresEarned: Int = 0,
    var currentLevel: Int = 1,
    var currentXp: Int = 0,
    var xpToNextLevel: Int = 100,
    var overclocksEarned: Int = 0
)

class GameEngine(
    val context: Context,
    val audioHaptics: AudioHapticSystem
) {
    val physics = FlightPhysics()
    val vfx = VFXSystem(maxParticles = 350)
    val environment = EnvironmentSystem()
    val enemySystem = EnemySystem()
    val weaponSystem = WeaponSystem(audioHaptics, vfx)

    val playerState = PlayerAircraftState()
    val combatStats = GameCombatStats()

    var currentAircraftSpec: AircraftSpec = AircraftCatalog.APEX_FALCON
    var currentPrimarySpec: WeaponSpec = WeaponCatalog.PLASMA_GATLING
    var currentSecondarySpec: WeaponSpec = WeaponCatalog.SWARM_MISSILES
    var currentSpecialSpec: WeaponSpec = WeaponCatalog.CHRONO_OVERDRIVE
    var currentBiome: BiomeSpec = BiomeCatalog.NEO_TOKYO
    var currentExhaust: ExhaustFlame = ExhaustCatalog.ALL.first()
    var currentPaint: PaintScheme = PaintCatalog.ALL.first()

    var isPaused: Boolean = false
    var isGameOver: Boolean = false
    var isVictory: Boolean = false
    var pendingPerkSelection: List<RoguelitePerk>? = null

    // Touch controls input state
    var inputDirX: Float = 0f
    var inputDirY: Float = 0f
    var isFireHeld: Boolean = false

    var isDirectTouchActive: Boolean = false
    var fingerOffsetX: Float = 0f
    var fingerOffsetY: Float = 55f

    fun onDirectTouchDown(
        touchX: Float,
        touchY: Float,
        screenWidth: Float,
        screenHeight: Float,
        touchOffsetY: Float = 75f
    ) {
        isDirectTouchActive = true
        // Set standard finger window offset: plane floats gracefully ahead of player thumb so action is 100% visible
        fingerOffsetX = 0f
        fingerOffsetY = touchOffsetY.coerceAtLeast(65f)

        val padX = 35f
        val padY = 80f
        val targetX = touchX.coerceIn(padX, screenWidth - padX)
        val targetY = (touchY - fingerOffsetY).coerceIn(padY, screenHeight - padY)

        playerState.x = targetX
        playerState.y = targetY
        playerState.vx = 0f
        playerState.vy = 0f
    }

    fun onDirectTouchMove(
        touchX: Float,
        touchY: Float,
        screenWidth: Float,
        screenHeight: Float,
        sensitivity: Float = 1.0f
    ) {
        isDirectTouchActive = true
        val padX = 35f
        val padY = 80f

        val targetX = touchX.coerceIn(padX, screenWidth - padX)
        val targetY = (touchY - fingerOffsetY).coerceIn(padY, screenHeight - padY)

        val deltaX = targetX - playerState.x
        val deltaY = targetY - playerState.y

        // Responsive velocity for visual banking and exhaust plume direction
        playerState.vx = (deltaX * 60f).coerceIn(-4000f, 4000f)
        playerState.vy = (deltaY * 60f).coerceIn(-4000f, 4000f)

        playerState.x = targetX
        playerState.y = targetY

        // Dynamic visual banking into turns
        val targetBank = (playerState.vx / 800f).coerceIn(-1f, 1f) * 35f
        playerState.bankAngle += (targetBank - playerState.bankAngle) * 0.45f
    }

    fun onDirectTouchUp() {
        isDirectTouchActive = false
    }

    var graphicsPreset: String = "ULTRA"

    // Stage Progress & Distance Line Tracking
    var stageDistanceCurrent: Float = 0f
    val stageDistanceTotal: Float = 3000f // 3,000 meters stage length
    var damageTakenThisStage: Float = 0f
    var totalTargetsSpawned: Int = 0
    var totalTargetsDestroyed: Int = 0

    // Bonus Vortex & Tunnel State
    var bonusVortexActive: Boolean = false
    var vortexX: Float = 0f
    var vortexY: Float = 0f
    var vortexRadius: Float = 60f
    var vortexRotation: Float = 0f

    var isInBonusTunnel: Boolean = false
    var tunnelTimer: Float = 0f
    val tunnelDuration: Float = 14f // song section duration
    var tunnelCoinsCollected: Int = 0
    var tunnelBonusCredits: Long = 0L
    var tunnelSpeedMultiplier: Float = 1.0f
    private var tunnelMediaPlayer: android.media.MediaPlayer? = null

    fun startMission(
        aircraft: AircraftSpec,
        primary: WeaponSpec,
        secondary: WeaponSpec,
        special: WeaponSpec,
        biome: BiomeSpec,
        paint: PaintScheme,
        exhaust: ExhaustFlame,
        screenWidth: Float,
        screenHeight: Float,
        savedUpgrades: Map<String, Int> = emptyMap()
    ) {
        currentAircraftSpec = aircraft
        currentPrimarySpec = primary
        currentSecondarySpec = secondary
        currentSpecialSpec = special
        currentBiome = biome
        currentPaint = paint
        currentExhaust = exhaust

        isPaused = false
        isGameOver = false
        isVictory = false
        pendingPerkSelection = null
        isDirectTouchActive = false
        inputDirX = 0f
        inputDirY = 0f

        // Reset stage distance & bonus vortex state
        stageDistanceCurrent = 0f
        damageTakenThisStage = 0f
        totalTargetsSpawned = 0
        totalTargetsDestroyed = 0
        bonusVortexActive = false
        isInBonusTunnel = false
        tunnelTimer = 0f
        tunnelCoinsCollected = 0
        tunnelBonusCredits = 0L
        tunnelSpeedMultiplier = 1.0f
        stopTunnelMusic()

        // Apply upgrades to base specs
        val engLvl = savedUpgrades["engine"] ?: 0
        val armLvl = savedUpgrades["armor"] ?: 0
        val shdLvl = savedUpgrades["shield"] ?: 0

        val maxHp = aircraft.baseHealth * (1.0f + armLvl * 0.15f)
        val maxShd = aircraft.baseShield * (1.0f + shdLvl * 0.15f)
        val maxBst = aircraft.baseBoostCapacity * (1.0f + engLvl * 0.12f)

        playerState.x = screenWidth * 0.5f
        playerState.y = screenHeight * 0.78f
        playerState.vx = 0f
        playerState.vy = 0f
        playerState.bankAngle = 0f
        playerState.health = maxHp
        playerState.maxHealth = maxHp
        playerState.shield = maxShd
        playerState.maxShield = maxShd
        playerState.boost = maxBst
        playerState.maxBoost = maxBst
        playerState.heat = 0f
        playerState.isOverheated = false
        playerState.barrelRollProgress = 0f
        playerState.invulnerableTimer = 1.0f // initial spawn protection
        playerState.perks.clear()

        combatStats.score = 0L
        combatStats.kills = 0
        combatStats.bossKills = 0
        combatStats.comboCount = 0
        combatStats.comboTimer = 0f
        combatStats.creditsEarned = 0L
        combatStats.plasmaCoresEarned = 0
        combatStats.currentLevel = 1
        combatStats.currentXp = 0
        combatStats.xpToNextLevel = 120
        combatStats.overclocksEarned = 0

        vfx.clear()
        weaponSystem.clear()
        enemySystem.reset()
        environment.initWorld(screenWidth, screenHeight, biome)
    }

    fun update(dt: Float, screenWidth: Float, screenHeight: Float) {
        if (isPaused || isGameOver || isVictory || pendingPerkSelection != null) return

        val clampedDt = dt.coerceIn(0.001f, 0.05f)

        // 1. Update Camera and Physics
        physics.updateCamera(clampedDt, playerState.vx, playerState.vy)
        physics.updateAircraftPhysics(
            player = playerState,
            inputDirX = inputDirX,
            inputDirY = inputDirY,
            dt = clampedDt,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            baseSpeed = currentAircraftSpec.baseSpeed,
            handling = currentAircraftSpec.baseHandling,
            isDirectTouch = isDirectTouchActive
        )

        // 2. Continuous Weapon Firing
        if (isFireHeld) {
            weaponSystem.firePrimary(
                player = playerState,
                spec = currentPrimarySpec,
                critChance = currentAircraftSpec.baseCritChance
            )
        }

        // 3. Thruster & Boost Particles
        val leftNozzle = playerState.x - 14f
        val rightNozzle = playerState.x + 14f
        val nozzleY = playerState.y + 24f
        vfx.spawnThrusterParticles(leftNozzle, rightNozzle, nozzleY, currentExhaust, playerState.isBoosting)
        if (playerState.isBoosting) {
            vfx.spawnSpeedStreaks(screenWidth, screenHeight)
        }

        // 4. Update Multi-layered Environment
        environment.update(clampedDt, screenWidth, screenHeight, playerState.isBoosting)

        // 5. Update Projectiles & Homing Physics
        weaponSystem.updateProjectiles(
            dt = clampedDt,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            enemies = enemySystem.enemies,
            boss = enemySystem.currentBoss
        )

        // 6. Update Enemy AI & Waves
        enemySystem.update(
            dt = clampedDt,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            player = playerState,
            spawnEnemyProjectile = { px, py, pvx, pvy, type, dmg, col ->
                weaponSystem.projectiles.add(
                    ProjectileEntity(
                        id = System.nanoTime(),
                        x = px,
                        y = py,
                        vx = pvx,
                        vy = pvy,
                        damage = dmg,
                        isPlayer = false,
                        type = type,
                        color = col,
                        glowColor = Color(0x66FF0055),
                        size = 5f
                    )
                )
            },
            onBossDyingExplosion = { bx, by ->
                vfx.spawnExplosion(bx, by, isHeavy = true, colorScheme = Color(0xFFFF5500))
                audioHaptics.playSound(AudioHapticSystem.SoundType.EXPLOSION_HEAVY)
                physics.addTrauma(0.25f)
            },
            onBossDefeated = {
                combatStats.bossKills++
                combatStats.score += 25000L
                combatStats.creditsEarned += 1200L
                combatStats.plasmaCoresEarned += 5
                vfx.spawnExplosion(screenWidth * 0.5f, screenHeight * 0.25f, isHeavy = true, colorScheme = Color(0xFFFDE047))
                audioHaptics.triggerExplosionHaptic(true)
                weaponSystem.spawnPowerUp(screenWidth * 0.5f, screenHeight * 0.25f)
                vfx.addText("BOSS ANNIHILATED! +25000", screenWidth * 0.5f, screenHeight * 0.25f, Color(0xFFFFD700))
            }
        )

        // 7. Check Collisions: Player Projectiles vs Enemies & Boss & Structures
        checkPlayerProjectileCollisions()

        // 8. Check Collisions: Enemy Projectiles vs Player
        checkEnemyProjectileCollisions()

        // 9. Check Collisions: Power-ups vs Player
        checkPowerUpCollisions()

        // 10. Update VFX & Particles
        vfx.update(clampedDt)

        // 11. Update Combos & Stage Distance
        if (combatStats.comboTimer > 0f) {
            combatStats.comboTimer -= clampedDt
            if (combatStats.comboTimer <= 0f) {
                combatStats.comboCount = 0
            }
        }

        // 12. Stage Distance & Bonus Vortex Black Hole Logic
        if (!isInBonusTunnel) {
            // Advance stage distance (scaled by flight speed)
            val speedFactor = if (playerState.isBoosting) 180f else 110f
            stageDistanceCurrent = (stageDistanceCurrent + clampedDt * speedFactor).coerceAtMost(stageDistanceTotal)
            val progressRatio = stageDistanceCurrent / stageDistanceTotal

            // Check for Bonus Vortex Spawn at 90% Stage Progress
            // Requirement: Reached 90%, 0 damage taken on stage, and destroyed targets
            if (progressRatio >= 0.90f && !bonusVortexActive && damageTakenThisStage == 0f && combatStats.kills >= 3) {
                bonusVortexActive = true
                vortexX = screenWidth * 0.5f
                vortexY = screenHeight * 0.32f
                vfx.addText("⭐ PERFECT RUN! BONUS VORTEX DETECTED! ⭐", screenWidth * 0.5f, screenHeight * 0.22f, Color(0xFF00F0FF))
                audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
            }

            // Animate Active Vortex
            if (bonusVortexActive) {
                vortexRotation += clampedDt * 280f
                // Check if player craft enters the vortex event horizon
                val distToVortex = hypot(playerState.x - vortexX, playerState.y - vortexY)
                if (distToVortex < vortexRadius + 35f) {
                    // ENTER WARP TUNNEL!
                    bonusVortexActive = false
                    isInBonusTunnel = true
                    tunnelTimer = 0f
                    tunnelCoinsCollected = 0
                    tunnelBonusCredits = 0L
                    startTunnelMusic()
                    vfx.addText("🌌 HYPER WARP TUNNEL ENGAGED! 🌌", screenWidth * 0.5f, screenHeight * 0.45f, Color(0xFF00F0FF))
                    audioHaptics.triggerExplosionHaptic(true)
                }
            }
        } else {
            // ── INSIDE GLOWING BONUS WARP TUNNEL ──
            tunnelTimer += clampedDt
            val tunnelProgressRatio = (tunnelTimer / tunnelDuration).coerceIn(0f, 1f)

            // Speed up in tunnel then slow down to sound of music
            tunnelSpeedMultiplier = if (tunnelProgressRatio < 0.5f) {
                1.0f + (tunnelProgressRatio / 0.5f) * 2.2f // Accelerate to 3.2x
            } else {
                3.2f - ((tunnelProgressRatio - 0.5f) / 0.5f) * 2.4f // Decelerate down to 0.8x
            }

            // Auto-collect bonus tunnel coins on every frame pulse
            if ((tunnelTimer * 10f).toInt() > tunnelCoinsCollected) {
                tunnelCoinsCollected++
                combatStats.score += 800L
                audioHaptics.playSound(AudioHapticSystem.SoundType.PLASMA_SHOT)
                vfx.spawnExplosion(
                    x = playerState.x + (Random.nextFloat() - 0.5f) * 120f,
                    y = playerState.y - 100f + (Random.nextFloat() - 0.5f) * 80f,
                    isHeavy = false,
                    colorScheme = Color(0xFFFFD700)
                )
            }

            // Tunnel Completion -> Warp Out to Boss Level Stage!
            if (tunnelTimer >= tunnelDuration) {
                isInBonusTunnel = false
                stopTunnelMusic()

                // Calculate +15% extra bonus coins/credits from stage total
                val bonus15Percent = ((combatStats.creditsEarned + 1000L) * 0.15f).toLong().coerceAtLeast(450L)
                combatStats.creditsEarned += bonus15Percent
                tunnelBonusCredits = bonus15Percent

                // Warp directly to 100% stage completion & trigger Boss encounter
                stageDistanceCurrent = stageDistanceTotal
                enemySystem.spawnBoss(screenWidth, screenHeight)
                enemySystem.isBossWave = true

                vfx.addText("⚡ WARP EXIT: BOSS LEVEL ENCOUNTER! +15% BONUS CR (${bonus15Percent} CR) ⚡", screenWidth * 0.5f, screenHeight * 0.35f, Color(0xFFFFD700))
                audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
                audioHaptics.triggerExplosionHaptic(true)
            }
        }
    }

    private fun checkPlayerProjectileCollisions() {
        val pIter = weaponSystem.projectiles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            if (!p.isPlayer) continue

            var hit = false

            // Against Boss Components & Body
            enemySystem.currentBoss?.let { boss ->
                if (boss.phase != BossPhase.DYING && boss.phase != BossPhase.DESTROYED) {
                    for (comp in boss.components) {
                        if (!comp.isDestroyed) {
                            val compLeft = boss.x + comp.offsetX - comp.width * 0.5f
                            val compRight = boss.x + comp.offsetX + comp.width * 0.5f
                            val compTop = boss.y + comp.offsetY - comp.height * 0.5f
                            val compBottom = boss.y + comp.offsetY + comp.height * 0.5f

                            if (p.x in compLeft..compRight && p.y in compTop..compBottom) {
                                comp.health -= p.damage
                                hit = true
                                vfx.spawnExplosion(p.x, p.y, isHeavy = false, colorScheme = p.color)
                                if (comp.health <= 0f) {
                                    comp.isDestroyed = true
                                    vfx.spawnExplosion(boss.x + comp.offsetX, boss.y + comp.offsetY, isHeavy = true, colorScheme = Color(0xFFFF4500))
                                    audioHaptics.playSound(AudioHapticSystem.SoundType.EXPLOSION_HEAVY)
                                    vfx.addText("${comp.name} DESTROYED!", p.x, p.y, Color(0xFFFFD700))
                                }
                                break
                            }
                        }
                    }

                    // Main Boss Core Hitbox
                    val halfW = boss.width * 0.45f
                    val halfH = boss.height * 0.4f
                    if (!hit && p.x in (boss.x - halfW)..(boss.x + halfW) && p.y in (boss.y - halfH)..(boss.y + halfH)) {
                        hit = true
                        boss.hitFlashTimer = 0.08f
                        if (boss.shield > 0f) {
                            boss.shield = max(0f, boss.shield - p.damage)
                            audioHaptics.playSound(AudioHapticSystem.SoundType.SHIELD_HIT, 0.4f)
                        } else {
                            boss.health -= p.damage
                        }
                        vfx.spawnExplosion(p.x, p.y, isHeavy = false, colorScheme = p.color)

                        if (boss.health <= 0f && boss.phase != BossPhase.DYING) {
                            boss.phase = BossPhase.DYING
                            boss.deathSequenceTimer = 0f
                            audioHaptics.playSound(AudioHapticSystem.SoundType.BOSS_ROAR)
                        }
                    }
                }
            }

            // Against Normal Enemies
            if (!hit) {
                for (enemy in enemySystem.enemies) {
                    val dist = hypot(enemy.x - p.x, enemy.y - p.y)
                    if (dist < enemy.type.radius + p.size) {
                        hit = true
                        enemy.hitFlashTimer = 0.08f
                        if (enemy.shield > 0f) {
                            enemy.shield = max(0f, enemy.shield - p.damage)
                            audioHaptics.playSound(AudioHapticSystem.SoundType.SHIELD_HIT, 0.5f)
                        } else {
                            enemy.health -= p.damage
                        }

                        vfx.spawnExplosion(p.x, p.y, isHeavy = false, colorScheme = p.color)

                        if (enemy.health <= 0f) {
                            onEnemyKilled(enemy)
                        }
                        break
                    }
                }
            }

            // Against Environmental Structures
            if (!hit) {
                for (s in environment.structures) {
                    if (!s.isDestroyed && p.x in (s.x - s.width * 0.5f)..(s.x + s.width * 0.5f) &&
                        p.y in (s.y - s.height * 0.5f)..(s.y + s.height * 0.5f)
                    ) {
                        hit = true
                        s.health -= p.damage
                        vfx.spawnExplosion(p.x, p.y, isHeavy = false, colorScheme = p.color)
                        if (s.health <= 0f) {
                            s.isDestroyed = true
                            vfx.spawnExplosion(s.x, s.y, isHeavy = true, colorScheme = Color(0xFFFF5500))
                            audioHaptics.playSound(AudioHapticSystem.SoundType.EXPLOSION_HEAVY)
                            physics.addTrauma(0.2f)
                            combatStats.score += 800L
                            weaponSystem.spawnPowerUp(s.x, s.y)
                            vfx.addText("TARGET DESTROYED! +800", s.x, s.y, Color(0xFFFF9500))
                        }
                        break
                    }
                }
            }

            if (hit) {
                p.pierceCount--
                if (p.pierceCount <= 0) {
                    pIter.remove()
                }
            }
        }
    }

    private fun checkEnemyProjectileCollisions() {
        if (playerState.invulnerableTimer > 0f) return

        val pIter = weaponSystem.projectiles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            if (p.isPlayer) continue

            val dist = hypot(playerState.x - p.x, playerState.y - p.y)
            if (dist < 26f + p.size) {
                pIter.remove()
                applyDamageToPlayer(p.damage)
                vfx.spawnExplosion(p.x, p.y, isHeavy = false, colorScheme = Color(0xFFFF3366))
                break
            }
        }
    }

    private fun checkPowerUpCollisions() {
        val puIter = weaponSystem.powerUps.iterator()
        while (puIter.hasNext()) {
            val pu = puIter.next()
            val dist = hypot(playerState.x - pu.x, playerState.y - pu.y)
            if (dist < 38f) {
                puIter.remove()
                audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
                audioHaptics.triggerExplosionHaptic(false)

                when (pu.type) {
                    PowerUpType.SHIELD_REFILL -> {
                        playerState.shield = playerState.maxShield
                        vfx.addText("SHIELDS RESTORED!", playerState.x, playerState.y - 30f, Color(0xFF38BDF8))
                    }
                    PowerUpType.REPAIR_NANO -> {
                        playerState.health = min(playerState.maxHealth, playerState.health + playerState.maxHealth * 0.35f)
                        vfx.addText("HULL REPAIRED!", playerState.x, playerState.y - 30f, Color(0xFF22C55E))
                    }
                    PowerUpType.WEAPON_OVERDRIVE -> {
                        playerState.heat = 0f
                        playerState.isOverheated = false
                        vfx.addText("HEAT FLUSHED!", playerState.x, playerState.y - 30f, Color(0xFFF59E0B))
                    }
                    PowerUpType.MEGA_BOMB -> {
                        weaponSystem.projectiles.removeAll { !it.isPlayer }
                        for (e in enemySystem.enemies) {
                            e.health -= 350f
                            vfx.spawnExplosion(e.x, e.y, isHeavy = false, colorScheme = Color(0xFFFF2200))
                        }
                        vfx.addText("MEGA NUKE CLEARED!", playerState.x, playerState.y - 30f, Color(0xFFEF4444))
                    }
                    PowerUpType.BOOST_INFINITY -> {
                        playerState.boost = playerState.maxBoost
                        vfx.addText("BOOST RECHARGED!", playerState.x, playerState.y - 30f, Color(0xFF00F0FF))
                    }
                    PowerUpType.TECH_CORE -> {
                        combatStats.plasmaCoresEarned += 1
                        vfx.addText("+1 PLASMA CORE!", playerState.x, playerState.y - 30f, Color(0xFFA855F7))
                    }
                }
            }
        }
    }

    private fun onEnemyKilled(enemy: EnemyEntity) {
        combatStats.kills++
        combatStats.comboCount++
        combatStats.comboTimer = 2.8f
        val comboMultiplier = 1.0f + (combatStats.comboCount * 0.1f)
        val gainedScore = (enemy.type.score * comboMultiplier).toLong()
        combatStats.score += gainedScore

        combatStats.creditsEarned += (enemy.type.score / 8) + Random.nextInt(15)

        // VFX & Audio
        vfx.spawnExplosion(enemy.x, enemy.y, isHeavy = enemy.type == EnemyType.HEAVY_GUNSHIP, colorScheme = Color(0xFFFF5500))
        audioHaptics.playSound(
            if (enemy.type == EnemyType.HEAVY_GUNSHIP) AudioHapticSystem.SoundType.EXPLOSION_HEAVY else AudioHapticSystem.SoundType.EXPLOSION_LIGHT
        )
        audioHaptics.triggerExplosionHaptic(enemy.type == EnemyType.HEAVY_GUNSHIP)
        physics.addTrauma(if (enemy.type == EnemyType.HEAVY_GUNSHIP) 0.18f else 0.08f)

        // Chance to spawn power-up
        if (Random.nextFloat() < 0.22f || enemy.type == EnemyType.HEAVY_GUNSHIP) {
            weaponSystem.spawnPowerUp(enemy.x, enemy.y)
        }

        // Roguelite XP progression
        addCombatXp(enemy.type.score / 2)
    }

    private fun addCombatXp(amount: Int) {
        combatStats.currentXp += amount
        if (combatStats.currentXp >= combatStats.xpToNextLevel) {
            combatStats.currentXp -= combatStats.xpToNextLevel
            combatStats.currentLevel++
            combatStats.xpToNextLevel = (combatStats.xpToNextLevel * 1.35f).toInt()
            combatStats.overclocksEarned++
            audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
        }
    }

    fun selectPerk(perk: RoguelitePerk) {
        val currentStacks = playerState.perks[perk.id] ?: 0
        playerState.perks[perk.id] = currentStacks + 1
        pendingPerkSelection = null

        // Apply instant perk bonuses
        when (perk.id) {
            "emergency_matrix" -> {
                playerState.shield = playerState.maxShield
            }
            "tactical_boosters" -> {
                playerState.maxBoost += 30f
                playerState.boost = playerState.maxBoost
            }
            "nanite_vampirism" -> {
                playerState.health = min(playerState.maxHealth, playerState.health + 200f)
            }
        }
        vfx.addText("${perk.name} EQUIPPED!", playerState.x, playerState.y - 40f, perk.rarity.color)
    }

    fun applyDamageToPlayer(amount: Float) {
        damageTakenThisStage += amount
        playerState.shieldRechargeTimer = 3.5f
        audioHaptics.triggerDamageHaptic()
        physics.addTrauma(0.2f)

        if (playerState.shield > 0f) {
            val remainingShield = playerState.shield - amount
            if (remainingShield < 0f) {
                playerState.shield = 0f
                playerState.health = max(0f, playerState.health + remainingShield)
                audioHaptics.playSound(AudioHapticSystem.SoundType.SHIELD_BREAK)
                vfx.addText("SHIELD DEPLETED!", playerState.x, playerState.y - 30f, Color(0xFFEF4444))
            } else {
                playerState.shield = remainingShield
                audioHaptics.playSound(AudioHapticSystem.SoundType.SHIELD_HIT)
            }
        } else {
            playerState.health = max(0f, playerState.health - amount)
            audioHaptics.playSound(AudioHapticSystem.SoundType.EXPLOSION_LIGHT)
        }

        if (playerState.health <= 0f) {
            isGameOver = true
            stopTunnelMusic()
            vfx.spawnExplosion(playerState.x, playerState.y, isHeavy = true, colorScheme = Color(0xFFEF4444))
            audioHaptics.playSound(AudioHapticSystem.SoundType.EXPLOSION_HEAVY)
            audioHaptics.triggerExplosionHaptic(true)
            physics.addTrauma(0.6f)
        }
    }

    private fun startTunnelMusic() {
        try {
            stopTunnelMusic()
            tunnelMediaPlayer = android.media.MediaPlayer.create(context, com.example.R.raw.bonus_vortex_tunnel)?.apply {
                setVolume(1.0f, 1.0f)
                isLooping = false
                start()
            }
        } catch (_: Exception) {}
    }

    fun stopTunnelMusic() {
        try {
            tunnelMediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        tunnelMediaPlayer = null
    }
}
