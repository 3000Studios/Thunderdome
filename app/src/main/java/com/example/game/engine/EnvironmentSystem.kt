package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.model.BiomeSpec
import com.example.game.model.EnvironmentalStructure
import kotlin.math.sin
import kotlin.random.Random

data class CloudPuff(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var speed: Float,
    var alpha: Float
)

data class CityBuilding(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var windowGlowColor: Color,
    var hasHoloAd: Boolean = false,
    var holoColor: Color = Color(0xFF00F0FF)
)

data class WeatherDrop(
    var x: Float,
    var y: Float,
    var length: Float,
    var speed: Float,
    var alpha: Float
)

class EnvironmentSystem {
    var scrollOffset: Float = 0f
    val clouds = ArrayList<CloudPuff>(16)
    val buildings = ArrayList<CityBuilding>(24)
    val structures = ArrayList<EnvironmentalStructure>(8)
    val weatherDrops = ArrayList<WeatherDrop>(60)

    private var structureIdCounter = 1000L

    fun initWorld(screenWidth: Float, screenHeight: Float, biome: BiomeSpec) {
        clouds.clear()
        buildings.clear()
        structures.clear()
        weatherDrops.clear()

        // Initialize clouds
        for (i in 0 until 10) {
            clouds.add(
                CloudPuff(
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * screenHeight,
                    width = 160f + Random.nextFloat() * 180f,
                    height = 90f + Random.nextFloat() * 80f,
                    speed = 70f + Random.nextFloat() * 60f,
                    alpha = 0.18f + Random.nextFloat() * 0.15f
                )
            )
        }

        // Initialize ground buildings
        var curY = 0f
        while (curY < screenHeight * 1.5f) {
            val bWidth = 80f + Random.nextFloat() * 120f
            val bHeight = 120f + Random.nextFloat() * 160f
            val bX = Random.nextFloat() * (screenWidth - bWidth)
            buildings.add(
                CityBuilding(
                    x = bX,
                    y = curY,
                    width = bWidth,
                    height = bHeight,
                    windowGlowColor = if (Random.nextBoolean()) Color(0xFF00E5FF) else Color(0xFFFF9500),
                    hasHoloAd = Random.nextFloat() < 0.35f,
                    holoColor = if (Random.nextBoolean()) Color(0xFFB347FF) else Color(0xFF00F0FF)
                )
            )
            curY += bHeight * 0.7f
        }

        // Initialize weather
        for (i in 0 until 50) {
            weatherDrops.add(
                WeatherDrop(
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * screenHeight,
                    length = 18f + Random.nextFloat() * 22f,
                    speed = 850f + Random.nextFloat() * 450f,
                    alpha = 0.25f + Random.nextFloat() * 0.35f
                )
            )
        }

        // Spawn first environmental destructible structures
        spawnStructure(screenWidth * 0.25f, -120f, "FUEL_DEPOT")
        spawnStructure(screenWidth * 0.75f, -480f, "RADAR_STATION")
    }

    fun spawnStructure(x: Float, y: Float, type: String) {
        val (w, h, hp) = when (type) {
            "FUEL_DEPOT" -> Triple(80f, 65f, 200f)
            "RADAR_STATION" -> Triple(70f, 70f, 280f)
            else -> Triple(60f, 60f, 150f)
        }
        structures.add(
            EnvironmentalStructure(
                id = structureIdCounter++,
                x = x,
                y = y,
                width = w,
                height = h,
                type = type,
                health = hp,
                maxHealth = hp
            )
        )
    }

    fun update(dt: Float, screenWidth: Float, screenHeight: Float, isBoosting: Boolean) {
        val baseSpeed = if (isBoosting) 480f else 280f
        scrollOffset += dt * baseSpeed

        // Update clouds (Parallax layer 2)
        for (c in clouds) {
            c.y += dt * (c.speed + if (isBoosting) 180f else 0f)
            if (c.y > screenHeight + 100f) {
                c.y = -180f
                c.x = Random.nextFloat() * screenWidth
            }
        }

        // Update ground buildings (Parallax layer 3)
        for (b in buildings) {
            b.y += dt * baseSpeed
            if (b.y > screenHeight + 200f) {
                b.y = -220f
                b.x = Random.nextFloat() * (screenWidth - b.width)
            }
        }

        // Update structures (Parallax layer 4)
        val sIter = structures.iterator()
        while (sIter.hasNext()) {
            val s = sIter.next()
            s.y += dt * baseSpeed
            if (s.isDestroyed) {
                s.burningTimer += dt
            }
            if (s.y > screenHeight + 200f) {
                sIter.remove()
            }
        }

        // Chance to spawn new ground structures
        if (structures.size < 4 && Random.nextFloat() < dt * 0.25f) {
            val type = if (Random.nextBoolean()) "FUEL_DEPOT" else "RADAR_STATION"
            spawnStructure(
                x = 60f + Random.nextFloat() * (screenWidth - 140f),
                y = -150f,
                type = type
            )
        }

        // Update weather streaks (Parallax layer 5)
        for (w in weatherDrops) {
            w.y += dt * (w.speed + if (isBoosting) 400f else 0f)
            if (w.y > screenHeight + 50f) {
                w.y = -40f
                w.x = Random.nextFloat() * screenWidth
            }
        }
    }
}
