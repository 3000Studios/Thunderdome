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
    val multiplayerManager = com.example.game.multiplayer.MultiplayerManager()

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

    // Camera Perspective & Display Scaling Modes
    var cameraViewMode: String = "FOLLOW_3RD" // "COCKPIT_1ST", "FOLLOW_3RD", "TOP_DOWN_CHASE"
    var screenSizeScale: String = "MAX_IMMERSIVE" // "COMPACT", "STANDARD", "MAX_IMMERSIVE"

    // Environmental Hazards & Handling Modifiers
    var activeGooSlowTimer: Float = 0f
    var activeWindForceX: Float = 0f
    var hasBossHijackedRadio: Boolean = false

    // Interactive Holo-Comms System (Concept Art)
    // 1: Trigger -> 2: Hologram Appears -> 3: Taunt -> 4: Player Reply -> 5: Counter Response -> 6: Dissolve -> 7: Resume
    var isHoloCommsActive: Boolean = false
    var holoBossProfile: BossProfileSpec? = null
    var holoCurrentTaunt: String = ""
    var holoEnemyResponse: String = ""
    var holoStep: Int = 1 // 1: Taunt display, 2: Player replied, 3: Counter-response
    var holoHologramAlpha: Float = 0f
    private var hasTriggeredEncounterHolo: Boolean = false

    // Touch controls input state
    var inputDirX: Float = 0f
    var inputDirY: Float = 0f
    var isFireHeld: Boolean = false

    var isMultiplayerMatchActive: Boolean = false
    var isDirectTouchActive: Boolean = false
    var fingerOffsetX: Float = 0f
    var fingerOffsetY: Float = 65f
    var lastTouchX: Float = 0f
    var lastTouchY: Float = 0f

    fun onDirectTouchDown(
        touchX: Float,
        touchY: Float,
        screenWidth: Float,
        screenHeight: Float,
        touchOffsetY: Float = 65f,
        touchOffsetX: Float = 0f,
        controlScheme: String = "TOUCH_FOLLOW"
    ) {
        isDirectTouchActive = true
        this.fingerOffsetX = touchOffsetX
        this.fingerOffsetY = touchOffsetY
        lastTouchX = touchX
        lastTouchY = touchY

        val padX = 30f
        val padY = 60f

        if (controlScheme == "TOUCH_FOLLOW") {
            val targetX = (touchX + fingerOffsetX).coerceIn(padX, screenWidth - padX)
            val targetY = (touchY - fingerOffsetY).coerceIn(padY, screenHeight - padY)

            val deltaX = targetX - playerState.x
            val deltaY = targetY - playerState.y

            playerState.vx = (deltaX * 30f).coerceIn(-3000f, 3000f)
            playerState.vy = (deltaY * 30f).coerceIn(-3000f, 3000f)

            playerState.x = targetX
            playerState.y = targetY
        }
    }

    fun onDirectTouchMove(
        touchX: Float,
        touchY: Float,
        screenWidth: Float,
        screenHeight: Float,
        sensitivity: Float = 1.0f,
        controlScheme: String = "TOUCH_FOLLOW"
    ) {
        isDirectTouchActive = true
        val padX = 30f
        val padY = 60f

        val deltaRawX = touchX - lastTouchX
        val deltaRawY = touchY - lastTouchY
        lastTouchX = touchX
        lastTouchY = touchY

        if (controlScheme == "RELATIVE_DRAG") {
            val newX = (playerState.x + deltaRawX * sensitivity).coerceIn(padX, screenWidth - padX)
            val newY = (playerState.y + deltaRawY * sensitivity).coerceIn(padY, screenHeight - padY)
            val deltaX = newX - playerState.x
            val deltaY = newY - playerState.y

            playerState.vx = (deltaX * 60f).coerceIn(-4000f, 4000f)
            playerState.vy = (deltaY * 60f).coerceIn(-4000f, 4000f)
            playerState.x = newX
            playerState.y = newY
        } else {
            val targetX = (touchX + fingerOffsetX).coerceIn(padX, screenWidth - padX)
            val targetY = (touchY - fingerOffsetY).coerceIn(padY, screenHeight - padY)

            val deltaX = targetX - playerState.x
            val deltaY = targetY - playerState.y

            playerState.vx = (deltaX * 60f).coerceIn(-4000f, 4000f)
            playerState.vy = (deltaY * 60f).coerceIn(-4000f, 4000f)

            playerState.x = targetX
            playerState.y = targetY
        }

        // Dynamic visual banking into turns (pilot feel)
        val targetBank = (playerState.vx / 650f).coerceIn(-1f, 1f) * 36f
        playerState.bankAngle += (targetBank - playerState.bankAngle) * 0.5f
    }

    fun onDirectTouchUp() {
        isDirectTouchActive = false
        playerState.vx = 0f
        playerState.vy = 0f
    }

    fun toggleBoost() {
        if (playerState.boost > 5f) {
            playerState.isBoosting = !playerState.isBoosting
            if (playerState.isBoosting) {
                audioHaptics.playSound(com.example.game.audio.AudioHapticSystem.SoundType.BOOST_BURST)
                audioHaptics.triggerBoostHaptic()
                vfx.addText("⚡ AFTERBURNER ENGAGED", playerState.x, playerState.y - 35f, Color(0xFFFF9500))
            }
        } else {
            audioHaptics.playSound(com.example.game.audio.AudioHapticSystem.SoundType.WARNING_BEEP)
            vfx.addText("⚠️ BOOST EMPTY", playerState.x, playerState.y - 35f, Color(0xFFEF4444))
        }
    }

    fun setBoostActive(active: Boolean) {
        if (active && playerState.boost > 5f) {
            if (!playerState.isBoosting) {
                playerState.isBoosting = true
                audioHaptics.playSound(com.example.game.audio.AudioHapticSystem.SoundType.BOOST_BURST)
                audioHaptics.triggerBoostHaptic()
            }
        } else {
            playerState.isBoosting = false
        }
    }

    var graphicsPreset: String = "ULTRA"

    // Stage Progress & Distance Line Tracking (3 min to 8 min stage scaling)
    var stageDistanceCurrent: Float = 0f
    var stageDistanceTotal: Float = 20000f // 20,000m to 45,000m (3 to 8 min)
    var damageTakenThisStage: Float = 0f
    var totalTargetsSpawned: Int = 0
    var totalTargetsDestroyed: Int = 0

    // Tachyon Warp Drive Cinematic Drop-In
    var isWarpIntroActive: Boolean = false
    var warpIntroTimer: Float = 0f
    val warpIntroDuration: Float = 2.4f

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

    // Background Soundtrack & Radio Player
    var musicVolume: Float = 1.0f
    var isSoundMuted: Boolean = false
    private var musicMediaPlayer: android.media.MediaPlayer? = null

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
        isMultiplayerMatchActive = false
        playerState.isBoosting = false
        pendingPerkSelection = null
        isDirectTouchActive = false
        inputDirX = 0f
        inputDirY = 0f

        // Dynamic Stage Distance Scaling: 3 minutes minimum (20,000m) to 8 minutes maximum (45,000m)
        val stageIndex = BiomeCatalog.ALL_BIOMES.indexOfFirst { it.id == biome.id }.let { if (it >= 0) it else 0 }
        stageDistanceTotal = 20000f + (stageIndex * 1086f)
        stageDistanceCurrent = 0f

        // Engage Tachyon Warp Drive Intro
        isWarpIntroActive = true
        warpIntroTimer = warpIntroDuration
        audioHaptics.playSound(AudioHapticSystem.SoundType.BOOST_BURST)
        audioHaptics.triggerBoostHaptic()
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
        enemySystem.activeBiomeId = biome.id
        environment.initWorld(screenWidth, screenHeight, biome)

        // Reset Holo Comms
        isHoloCommsActive = false
        hasTriggeredEncounterHolo = false
        holoStep = 1
        holoBossProfile = BossProfileCatalog.getForBiome(biome.id)
        holoCurrentTaunt = holoBossProfile?.taunts?.trigger1 ?: ""

        // Start stage soundtrack (Stage 03 "void_gate" launches Am I Wrong)
        val stageSongIdx = when (biome.id) {
            "void_gate" -> allSoundtracks.indexOfFirst { it.rawResId == com.example.R.raw.track_stage_03_am_i_wrong }.takeIf { it >= 0 } ?: 0
            else -> abs(biome.id.hashCode()) % allSoundtracks.size
        }
        playRadioTrack(stageSongIdx)
    }

    fun update(dt: Float, screenWidth: Float, screenHeight: Float) {
        if (isPaused || isGameOver || isVictory || pendingPerkSelection != null) return

        val clampedDt = dt.coerceIn(0.001f, 0.05f)

        // 1. Update Camera and Physics with environmental goo & wind effects
        if (activeGooSlowTimer > 0f) {
            activeGooSlowTimer -= clampedDt
            vfx.spawnExplosion(playerState.x, playerState.y, isHeavy = false, colorScheme = Color(0xFF22C55E))
        }
        val handlingMult = if (activeGooSlowTimer > 0f) 0.55f else 1.0f

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
            isDirectTouch = isDirectTouchActive,
            handlingMultiplier = handlingMult,
            windForceX = activeWindForceX
        )

        // Reset Chrono Stasis time dilation factor when special ability expires
        if (playerState.specialDurationLeft <= 0f && weaponSystem.timeDilationFactor != 1.0f) {
            weaponSystem.timeDilationFactor = 1.0f
        }

        // Decay transient wind gusts smoothly
        activeWindForceX *= (1.0f - clampedDt * 3.5f)

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

        // Update 4-Player Co-op Squad Formation AI & Multiplayer Telemetry
        if (isMultiplayerMatchActive) {
            multiplayerManager.updateSquadAi(clampedDt, playerState.x, playerState.y, playerState.isBoosting)
            multiplayerManager.sendLocalState(
                x = playerState.x,
                y = playerState.y,
                vx = playerState.vx,
                vy = playerState.vy,
                bankAngle = playerState.bankAngle,
                health = playerState.health,
                shield = playerState.shield,
                isFiring = isFireHeld,
                isBoosting = playerState.isBoosting,
                score = combatStats.score
            )
        }

        // Dynamic Adaptive Difficulty (Scales challenge to player ability & squad size)
        val healthRatio = (playerState.health / playerState.maxHealth).coerceIn(0f, 1f)
        val skillMult = if (combatStats.comboCount > 15) 1.25f else if (healthRatio < 0.35f) 0.75f else 1.0f

        // 4. Update Multi-layered Environment
        environment.update(clampedDt, screenWidth, screenHeight, playerState.isBoosting)

        // 5. Update Projectiles & Homing Physics
        weaponSystem.updateProjectiles(
            dt = clampedDt * skillMult,
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
                isVictory = true
                stopTunnelMusic()
                stopRadioMusic()
                audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
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

        // Update Warp Intro Timer
        if (isWarpIntroActive) {
            warpIntroTimer -= clampedDt
            if (warpIntroTimer <= 0f) {
                isWarpIntroActive = false
                vfx.addText("⚡ WARP EXIT: ENGAGING SECTOR DEFENSE ⚡", screenWidth * 0.5f, screenHeight * 0.38f, Color(0xFF00F0FF))
            }
        }

        // 12. Stage Distance & Bonus Vortex Black Hole Logic
        if (!isInBonusTunnel) {
            // Advance stage distance (scaled by flight speed)
            val speedFactor = if (playerState.isBoosting) 180f else 110f
            stageDistanceCurrent = (stageDistanceCurrent + clampedDt * speedFactor).coerceAtMost(stageDistanceTotal)
            val progressRatio = stageDistanceCurrent / stageDistanceTotal

            // Trigger Epic Boss Encounter upon reaching 100% stage distance (3-8 min flight completed)
            if (stageDistanceCurrent >= stageDistanceTotal && enemySystem.currentBoss == null && !enemySystem.isBossWave) {
                enemySystem.spawnBoss(screenWidth, screenHeight)
                enemySystem.isBossWave = true
                vfx.addText("⚠️ WARNING: BOSS DREADNOUGHT DETECTED ⚠️", screenWidth * 0.5f, screenHeight * 0.30f, Color(0xFFEF4444))
                audioHaptics.playSound(AudioHapticSystem.SoundType.WARNING_BEEP)
            }

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
                triggerHoloCommsEncounter()

                vfx.addText("⚡ WARP EXIT: BOSS LEVEL ENCOUNTER! +15% BONUS CR (${bonus15Percent} CR) ⚡", screenWidth * 0.5f, screenHeight * 0.35f, Color(0xFFFFD700))
                audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
                audioHaptics.triggerExplosionHaptic(true)
            }
        }

        // Trigger Holo-Comms when normal stage reaches 100% and boss spawns
        if (!hasTriggeredEncounterHolo && enemySystem.currentBoss != null && !isInBonusTunnel) {
            triggerHoloCommsEncounter()
        }

        // 13. Send Real-Time Multiplayer Telemetry (30 Hz sync)
        if (multiplayerManager.status.value == com.example.game.multiplayer.MultiplayerStatus.CONNECTED_DOGFIGHT) {
            multiplayerManager.sendLocalState(
                x = playerState.x,
                y = playerState.y,
                vx = playerState.vx,
                vy = playerState.vy,
                bankAngle = playerState.bankAngle,
                health = playerState.health,
                shield = playerState.shield,
                isFiring = isFireHeld,
                isBoosting = playerState.isBoosting
            )
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
                        val prevRatio = (enemy.health) / enemy.maxHealth
                        if (enemy.shield > 0f) {
                            enemy.shield = max(0f, enemy.shield - p.damage)
                            audioHaptics.playSound(AudioHapticSystem.SoundType.SHIELD_HIT, 0.5f)
                        } else {
                            enemy.health -= p.damage
                        }

                        val healthRatio = enemy.health / enemy.maxHealth
                        if (healthRatio <= 0.25f) {
                            enemy.damageState = DamageState.CRITICAL
                            enemy.isWingDamaged = true
                            enemy.isSmoking = true
                            enemy.isSparking = true
                        } else if (healthRatio <= 0.50f) {
                            enemy.damageState = DamageState.HEAVY
                            enemy.isSmoking = true
                            enemy.isSparking = true
                            if (prevRatio > 0.50f) {
                                audioHaptics.playSound(AudioHapticSystem.SoundType.ENEMY_ARMOR_CRACK, 0.7f)
                            }
                        } else if (healthRatio <= 0.75f) {
                            enemy.damageState = DamageState.LIGHT
                            enemy.isSmoking = true
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

    // Radio Track Switcher & Real Soundtrack Playlist
    data class SoundTrack(val title: String, val rawResId: Int)

    val allSoundtracks = listOf(
        SoundTrack("Electric Skies", com.example.R.raw.track_3000_studios_electric_skies),
        SoundTrack("Creek Road Lantern", com.example.R.raw.track_3000_studios_creek_road_lantern),
        SoundTrack("Die In A Fire Remix", com.example.R.raw.track_3000_studios_lick_my_balls_and_die_in_a_fire_remix),
        SoundTrack("3000 Studios Podcast", com.example.R.raw.track_3000_studios_podcast),
        SoundTrack("Burn Up The Bass", com.example.R.raw.track_burn_up_the_bass),
        SoundTrack("Cruise Voltage", com.example.R.raw.track_cruise_voltage),
        SoundTrack("Code Red", com.example.R.raw.track_code_red),
        SoundTrack("Room Goes Cold", com.example.R.raw.track_room_goes_cold),
        SoundTrack("Subwoofer Pressure", com.example.R.raw.track_subwoofer_pressure),
        SoundTrack("Subwoofer From Hell", com.example.R.raw.track_subwoofer_from_hell),
        SoundTrack("Floor Ya", com.example.R.raw.track_floor_ya),
        SoundTrack("Am I Wrong (Stage 03 - Void Gate)", com.example.R.raw.track_stage_03_am_i_wrong),
        SoundTrack("Always Feel Like", com.example.R.raw.track_always_feel_like),
        SoundTrack("Go The Other Way", com.example.R.raw.track_go_the_other_way_player),
        SoundTrack("Tropical Bass Land", com.example.R.raw.track_tropical_bass_land),
        SoundTrack("Still Learning", com.example.R.raw.track_still_learning),
        SoundTrack("So Fresh Tribute", com.example.R.raw.track_so_fresh_tribute),
        SoundTrack("Not Giving Up Tonight", com.example.R.raw.track_not_giving_up_tonight),
        SoundTrack("Pressure Has A Name", com.example.R.raw.track_pressure_has_a_name),
        SoundTrack("Quarter Goblin", com.example.R.raw.track_quarter_goblin),
        SoundTrack("The Mailman Is A Spy", com.example.R.raw.track_the_mailman_is_a_spy),
        SoundTrack("The Peepers", com.example.R.raw.track_the_peepers),
        SoundTrack("Microwave Cowboy", com.example.R.raw.track_microwave_cowboy),
        SoundTrack("Motel Television", com.example.R.raw.track_motel_television),
        SoundTrack("Crabs N Aidas", com.example.R.raw.track_crabs_n_aidas),
        SoundTrack("Taqueesha", com.example.R.raw.track_taqueesha_cant_never_get_right),
        SoundTrack("Why Do I Not Like My Songs", com.example.R.raw.track_why_do_i_not_like_my_songs),
        SoundTrack("Bonus Stage Trippy", com.example.R.raw.track_bonus_stage_trippy)
    )

    var radioTrackIndex: Int = 0

    fun playRadioTrack(index: Int) {
        radioTrackIndex = ((index % allSoundtracks.size) + allSoundtracks.size) % allSoundtracks.size
        val track = allSoundtracks[radioTrackIndex]
        try {
            stopRadioMusic()
            if (!isSoundMuted && musicVolume > 0.01f) {
                musicMediaPlayer = android.media.MediaPlayer.create(context, track.rawResId)?.apply {
                    val vol = musicVolume.coerceIn(0f, 1f)
                    setVolume(vol, vol)
                    isLooping = true
                    start()
                }
            }
        } catch (_: Exception) {}
        vfx.addText("📻 ${track.title.uppercase()}", playerState.x, playerState.y - 50f, Color(0xFF00F0FF))
    }

    fun nextRadioTrack() {
        playRadioTrack(radioTrackIndex + 1)
        audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
    }

    fun toggleMuteAllSounds() {
        isSoundMuted = !isSoundMuted
        if (isSoundMuted) {
            audioHaptics.sfxVolume = 0f
            musicMediaPlayer?.setVolume(0f, 0f)
            vfx.addText("🔇 ALL SOUND MUTED", playerState.x, playerState.y - 50f, Color(0xFFEF4444))
        } else {
            audioHaptics.sfxVolume = 1.0f
            val vol = musicVolume.coerceIn(0f, 1f)
            musicMediaPlayer?.setVolume(vol, vol)
            if (musicMediaPlayer == null || !musicMediaPlayer!!.isPlaying) {
                playRadioTrack(radioTrackIndex)
            }
            vfx.addText("🔊 SOUND UNMUTED", playerState.x, playerState.y - 50f, Color(0xFF22C55E))
        }
    }

    fun stopRadioMusic() {
        try {
            musicMediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        musicMediaPlayer = null
    }

    fun pauseRadioMusic() {
        try {
            if (musicMediaPlayer?.isPlaying == true) {
                musicMediaPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun resumeRadioMusic() {
        try {
            if (!isSoundMuted && musicVolume > 0.01f) {
                if (musicMediaPlayer != null) {
                    musicMediaPlayer?.start()
                } else {
                    playRadioTrack(radioTrackIndex)
                }
            }
        } catch (_: Exception) {}
    }

    // ── INTERACTIVE HOLO-COMMS ENCOUNTER CONTROLLER ──
    fun triggerHoloCommsEncounter() {
        hasTriggeredEncounterHolo = true
        val profile = BossProfileCatalog.getForBiome(currentBiome.id)
        holoBossProfile = profile
        holoCurrentTaunt = profile.taunts.trigger1
        holoEnemyResponse = ""
        holoStep = 1 // Hologram appears + enemy taunts
        isHoloCommsActive = true
        audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
        vfx.addText("📡 INCOMING TRANSMISSION: ${profile.name}", playerState.x, playerState.y - 60f, profile.primaryColor)

        // Boss radio hijack: Boss takes over the comms and radio frequency
        if (!hasBossHijackedRadio) {
            hasBossHijackedRadio = true
            // Switch radio to boss hijack track
            val hijackTrackIdx = allSoundtracks.indexOfFirst {
                it.rawResId == com.example.R.raw.track_why_do_i_not_like_my_songs ||
                it.rawResId == com.example.R.raw.track_crabs_n_aidas
            }.takeIf { it >= 0 } ?: (allSoundtracks.size - 2)
            playRadioTrack(hijackTrackIdx)
            vfx.addText("⚠️ RADIO SIGNAL HIJACKED BY ${profile.name}! ⚠️", playerState.x, playerState.y - 80f, Color(0xFFEF4444))
        }
    }

    fun onPlayerReplyToHolo(replyText: String) {
        val profile = holoBossProfile ?: return
        holoStep = 2 // Player replied
        val counter = profile.comms.enemyCounterResponses[replyText] ?: profile.comms.defaultCounter
        holoEnemyResponse = counter
        audioHaptics.playSound(AudioHapticSystem.SoundType.BOOST_BURST)
    }

    fun closeHoloComms() {
        isHoloCommsActive = false
        holoStep = 1
        audioHaptics.playSound(AudioHapticSystem.SoundType.PLASMA_SHOT)
    }

    private fun checkPowerUpCollisions() {
        val puIter = weaponSystem.powerUps.iterator()
        while (puIter.hasNext()) {
            val pu = puIter.next()
            val dist = hypot(playerState.x - pu.x, playerState.y - pu.y)
            if (dist < 50f) { // Larger, more satisfying pickup radius
                puIter.remove()
                audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
                audioHaptics.triggerExplosionHaptic(false)

                when (pu.type) {
                    PowerUpType.SHIELD_REFILL -> {
                        playerState.shield = playerState.maxShield
                        combatStats.score += 800L
                        vfx.addText("🛡️ SHIELDS RESTORED! (+800 PTS)", playerState.x, playerState.y - 35f, Color(0xFF38BDF8))
                    }
                    PowerUpType.REPAIR_NANO -> {
                        playerState.health = min(playerState.maxHealth, playerState.health + playerState.maxHealth * 0.40f)
                        combatStats.score += 1000L
                        vfx.addText("💚 NANO REPAIR OVERDRIVE! (+1000 PTS)", playerState.x, playerState.y - 35f, Color(0xFF22C55E))
                    }
                    PowerUpType.WEAPON_OVERDRIVE -> {
                        playerState.heat = 0f
                        playerState.isOverheated = false
                        combatStats.score += 900L
                        vfx.addText("🔥 WEAPON OVERDRIVE! (+900 PTS)", playerState.x, playerState.y - 35f, Color(0xFFF59E0B))
                    }
                    PowerUpType.MEGA_BOMB -> {
                        weaponSystem.projectiles.removeAll { !it.isPlayer }
                        for (e in enemySystem.enemies) {
                            e.health -= 450f
                            vfx.spawnExplosion(e.x, e.y, isHeavy = true, colorScheme = Color(0xFFFF2200))
                        }
                        combatStats.score += 2500L
                        vfx.addText("💣 APOCALYPSE NUKE DETONATED! (+2500 PTS)", playerState.x, playerState.y - 35f, Color(0xFFEF4444))
                        audioHaptics.triggerExplosionHaptic(true)
                    }
                    PowerUpType.BOOST_INFINITY -> {
                        playerState.boost = playerState.maxBoost
                        combatStats.score += 750L
                        vfx.addText("⚡ SPEED BURST REFILLED! (+750 PTS)", playerState.x, playerState.y - 35f, Color(0xFF00F0FF))
                    }
                    PowerUpType.TECH_CORE -> {
                        combatStats.plasmaCoresEarned += 1
                        combatStats.score += 2000L
                        vfx.addText("💎 +1 PLASMA CORE CLAIMED! (+2000 PTS)", playerState.x, playerState.y - 35f, Color(0xFFA855F7))
                    }
                }
            }
        }

        // Speed Gates Collisions (Fly through acceleration gate)
        for (sg in environment.speedGates) {
            if (!sg.isTriggered && playerState.x in (sg.x - sg.width * 0.5f)..(sg.x + sg.width * 0.5f) &&
                playerState.y in (sg.y - 25f)..(sg.y + 25f)
            ) {
                sg.isTriggered = true
                playerState.boost = playerState.maxBoost
                playerState.isBoosting = true
                combatStats.score += 500L
                audioHaptics.playSound(AudioHapticSystem.SoundType.BOOST_BURST)
                vfx.addText("⚡ SPEED GATE BOOST! (+500 PTS)", playerState.x, playerState.y - 40f, Color(0xFF00F0FF))
            }
        }

        // Course Obstacle Collisions (Laser Barricades, Asteroids, Green Goo, Wind Gusts)
        for (co in environment.courseObstacles) {
            if (!co.isDestroyed && playerState.x in (co.x - co.width * 0.45f)..(co.x + co.width * 0.45f) &&
                playerState.y in (co.y - co.height * 0.45f)..(co.y + co.height * 0.45f)
            ) {
                when (co.type) {
                    "GREEN_GOO" -> {
                        // Slow down flight speed and handling, drain small boost
                        activeGooSlowTimer = 2.5f
                        playerState.boost = max(0f, playerState.boost - 15f)
                        vfx.spawnExplosion(playerState.x, playerState.y, isHeavy = false, colorScheme = Color(0xFF22C55E))
                        vfx.addText("⚠️ BIO SLIME! CONTROLS SLOWED (-45%)", playerState.x, playerState.y - 40f, Color(0xFF22C55E))
                    }
                    "WIND_GUST" -> {
                        // Push aircraft laterally with high crosswind
                        activeWindForceX = co.windDirX
                        audioHaptics.playSound(AudioHapticSystem.SoundType.BOOST_BURST)
                        vfx.addText("💨 CROSSWIND TURBULENCE!", playerState.x, playerState.y - 40f, Color(0xFF38BDF8))
                    }
                    else -> {
                        if (playerState.invulnerableTimer <= 0f) {
                            co.isDestroyed = true
                            applyDamageToPlayer(80f)
                            vfx.spawnExplosion(co.x, co.y, isHeavy = true, colorScheme = Color(0xFFFF2A4D))
                            vfx.addText("⚠️ HAZARD COLLISION (-80 HP)!", playerState.x, playerState.y - 40f, Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }

    private fun onEnemyKilled(enemy: EnemyEntity) {
        combatStats.kills++
        combatStats.comboCount++
        combatStats.comboTimer = 3.2f
        val comboMultiplier = 1.0f + (combatStats.comboCount * 0.12f)
        val gainedScore = (enemy.type.score * comboMultiplier).toLong()
        combatStats.score += gainedScore

        combatStats.creditsEarned += (enemy.type.score / 6) + Random.nextInt(20)

        // VFX & Audio
        vfx.spawnExplosion(enemy.x, enemy.y, isHeavy = enemy.type == EnemyType.HEAVY_GUNSHIP, colorScheme = Color(0xFFFF5500))
        audioHaptics.playSound(
            if (enemy.type == EnemyType.HEAVY_GUNSHIP) AudioHapticSystem.SoundType.EXPLOSION_HEAVY else AudioHapticSystem.SoundType.EXPLOSION_LIGHT
        )
        audioHaptics.triggerExplosionHaptic(enemy.type == EnemyType.HEAVY_GUNSHIP)
        physics.addTrauma(if (enemy.type == EnemyType.HEAVY_GUNSHIP) 0.18f else 0.08f)

        // Big-Time Award Bonuses & Combat Announcements
        when (combatStats.comboCount) {
            10 -> {
                combatStats.score += 2500L
                audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
                vfx.addText("⚡ 10x COMBO! AIR ACE! (+2,500) ⚡", playerState.x, playerState.y - 65f, Color(0xFF00F0FF))
            }
            25 -> {
                combatStats.score += 10000L
                combatStats.creditsEarned += 250L
                audioHaptics.playSound(AudioHapticSystem.SoundType.WARP_ENGAGE)
                vfx.addText("🔥 25x RAMPAGE! +10,000 BONUS! 🔥", playerState.x, playerState.y - 65f, Color(0xFFFF9500))
            }
            50 -> {
                combatStats.score += 50000L
                combatStats.plasmaCoresEarned += 2
                audioHaptics.playSound(AudioHapticSystem.SoundType.WARP_ENGAGE)
                vfx.addText("👑 50x GODLIKE STREAK! +50,000 BONUS! 👑", playerState.x, playerState.y - 65f, Color(0xFFFFD700))
            }
        }

        if (enemy.type == EnemyType.HEAVY_GUNSHIP) {
            combatStats.creditsEarned += 100L
            vfx.addText("💎 ELITE ANNIHILATED! +100 CR 💎", enemy.x, enemy.y, Color(0xFF38BDF8))
        }

        // Chance to spawn power-up
        if (Random.nextFloat() < 0.25f || enemy.type == EnemyType.HEAVY_GUNSHIP) {
            weaponSystem.spawnPowerUp(enemy.x, enemy.y)
        }

        // 20-Kill Streak Voice Lines for Secret Planes
        if (combatStats.kills > 0 && combatStats.kills % 20 == 0) {
            if (currentAircraftSpec.id == "aircraft_jerica") {
                audioHaptics.playSound(AudioHapticSystem.SoundType.VOICE_DIE_IN_A_FIRE)
                vfx.addText("🔥 JERICA: \"DIE IN A FIRE!\" 🔥", playerState.x, playerState.y - 80f, Color(0xFFFFD700))
            } else if (currentAircraftSpec.id == "aircraft_jadon") {
                audioHaptics.playSound(AudioHapticSystem.SoundType.VOICE_GOT_EM)
                vfx.addText("⚡ JADON: \"GOT 'EM.\" ⚡", playerState.x, playerState.y - 80f, Color(0xFF00F0FF))
            }
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

            // Trigger Roguelite Perk selection modal on level-up
            val perkChoices = PerkCatalog.getRandomChoices(3, playerState.perks.keys.toList())
            if (perkChoices.isNotEmpty()) {
                pendingPerkSelection = perkChoices
                vfx.addText("⭐ LEVEL UP! SELECT OVERCLOCK PERK ⭐", playerState.x, playerState.y - 60f, Color(0xFFFFD700))
            }
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
            stopRadioMusic()
            vfx.spawnExplosion(playerState.x, playerState.y, isHeavy = true, colorScheme = Color(0xFFEF4444))
            audioHaptics.playSound(AudioHapticSystem.SoundType.EXPLOSION_HEAVY)
            audioHaptics.triggerExplosionHaptic(true)
            physics.addTrauma(0.6f)
        }
    }

    fun revivePlayer() {
        isGameOver = false
        playerState.health = playerState.maxHealth * 0.6f
        playerState.shield = playerState.maxShield
        playerState.invulnerableTimer = 4.0f
        vfx.spawnExplosion(playerState.x, playerState.y, isHeavy = false, colorScheme = Color(0xFF00F0FF))
        audioHaptics.playSound(AudioHapticSystem.SoundType.POWERUP)
        vfx.addText("⚡ SYSTEMS RESTORED // REVIVED!", playerState.x, playerState.y - 50f, Color(0xFF00F0FF))
        resumeRadioMusic()
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
