package com.example.game.engine

import com.example.game.model.PlayerAircraftState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class FlightPhysics {
    // Camera shake state
    var cameraShakeX: Float = 0f
    var cameraShakeY: Float = 0f
    private var shakeTrauma: Float = 0f

    // Camera lag
    var cameraLagX: Float = 0f
    var cameraLagY: Float = 0f

    fun addTrauma(amount: Float) {
        shakeTrauma = min(1.0f, shakeTrauma + amount)
    }

    fun updateCamera(dt: Float, playerVx: Float, playerVy: Float) {
        // Shake decay
        if (shakeTrauma > 0f) {
            val shakeStrength = shakeTrauma * shakeTrauma
            val time = System.currentTimeMillis() * 0.05f
            cameraShakeX = (sin(time * 1.3f) * 22f * shakeStrength)
            cameraShakeY = (cos(time * 1.7f) * 22f * shakeStrength)
            shakeTrauma = max(0f, shakeTrauma - dt * 1.8f)
        } else {
            cameraShakeX = 0f
            cameraShakeY = 0f
        }

        // Camera lag responds to player velocity
        val targetLagX = -playerVx * 0.035f
        val targetLagY = -playerVy * 0.025f
        cameraLagX += (targetLagX - cameraLagX) * min(1f, dt * 8f)
        cameraLagY += (targetLagY - cameraLagY) * min(1f, dt * 8f)
    }

    fun updateAircraftPhysics(
        player: PlayerAircraftState,
        inputDirX: Float,
        inputDirY: Float,
        dt: Float,
        screenWidth: Float,
        screenHeight: Float,
        baseSpeed: Float,
        handling: Float
    ) {
        // Boost multiplier
        val speedMultiplier = if (player.isBoosting && player.boost > 0f) 1.65f else 1.0f
        val targetMaxSpeed = baseSpeed * speedMultiplier

        // Acceleration Curves & Drag
        val accelRate = handling * 2600f * (if (player.isBoosting) 1.4f else 1.0f)
        val drag = 6.5f

        val targetVx = inputDirX * targetMaxSpeed
        val targetVy = inputDirY * targetMaxSpeed

        // Smooth physics response
        player.vx += (targetVx - player.vx) * min(1f, dt * (accelRate / targetMaxSpeed))
        player.vy += (targetVy - player.vy) * min(1f, dt * (accelRate / targetMaxSpeed))

        // Natural air resistance when no input
        if (abs(inputDirX) < 0.05f) {
            player.vx -= player.vx * drag * dt
        }
        if (abs(inputDirY) < 0.05f) {
            player.vy -= player.vy * drag * dt
        }

        // Apply recoil
        if (player.recoilY > 0f) {
            player.y += player.recoilY * dt * 60f
            player.recoilY = max(0f, player.recoilY - dt * 25f)
        }

        // Move aircraft
        player.x += player.vx * dt
        player.y += player.vy * dt

        // Clamp inside screen bounds with padding
        val padX = 40f
        val padY = 90f
        if (player.x < padX) {
            player.x = padX
            player.vx = 0f
        } else if (player.x > screenWidth - padX) {
            player.x = screenWidth - padX
            player.vx = 0f
        }

        if (player.y < padY) {
            player.y = padY
            player.vy = 0f
        } else if (player.y > screenHeight - padY) {
            player.y = screenHeight - padY
            player.vy = 0f
        }

        // Visual Banking: Lean aircraft into turns
        val targetBank = (player.vx / targetMaxSpeed) * 35f // -35 to +35 degrees
        player.bankAngle += (targetBank - player.bankAngle) * min(1f, dt * 10f)

        // Pitch scale: compresses slightly when accelerating vertically
        val targetPitch = if (player.vy < -50f) 0.92f else 1.0f
        player.pitchScale += (targetPitch - player.pitchScale) * min(1f, dt * 6f)

        // Barrel Roll Evasive Maneuver Progress
        if (player.barrelRollProgress > 0f) {
            player.barrelRollProgress += dt * 2.8f
            if (player.barrelRollProgress >= 1.0f) {
                player.barrelRollProgress = 0f
            }
        }

        // Boost Energy Consumption & Recharge
        if (player.isBoosting && (abs(inputDirX) > 0.1f || abs(inputDirY) > 0.1f)) {
            player.boost = max(0f, player.boost - dt * 32f)
            if (player.boost <= 0f) {
                player.isBoosting = false
            }
        } else {
            player.boost = min(player.maxBoost, player.boost + dt * 22f)
        }

        // Shield Recharge Logic
        if (player.shieldRechargeTimer > 0f) {
            player.shieldRechargeTimer -= dt
        } else if (player.shield < player.maxShield) {
            player.shield = min(player.maxShield, player.shield + dt * (player.maxShield * 0.18f))
        }

        // Heat Dissipation
        if (player.heat > 0f) {
            val cooldownRate = if (player.isOverheated) 35f else 45f
            player.heat = max(0f, player.heat - dt * cooldownRate)
            if (player.heat <= 15f && player.isOverheated) {
                player.isOverheated = false
            }
        }

        // Cooldowns
        if (player.primaryCooldown > 0f) player.primaryCooldown -= dt
        if (player.secondaryCooldown > 0f) player.secondaryCooldown -= dt
        if (player.specialCooldown > 0f) player.specialCooldown -= dt
        if (player.specialDurationLeft > 0f) player.specialDurationLeft -= dt
        if (player.invulnerableTimer > 0f) player.invulnerableTimer -= dt
    }

    fun triggerBarrelRoll(player: PlayerAircraftState) {
        if (player.barrelRollProgress == 0f && player.boost >= 25f) {
            player.barrelRollProgress = 0.01f
            player.invulnerableTimer = 0.65f
            player.boost -= 20f
            addTrauma(0.2f)
        }
    }
}
