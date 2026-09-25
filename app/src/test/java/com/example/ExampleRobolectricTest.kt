package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.engine.FlightPhysics
import com.example.game.model.AircraftCatalog
import com.example.game.model.PlayerAircraftState
import com.example.game.model.WeaponCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AeroStrike", appName)
    }

    @Test
    fun testAircraftCatalog() {
        assertEquals(4, AircraftCatalog.ALL_AIRCRAFT.size)
        val falcon = AircraftCatalog.getById("apex_falcon")
        assertNotNull(falcon)
        assertEquals("Apex Falcon", falcon.name)
        assertTrue(falcon.baseHealth > 0f)
    }

    @Test
    fun testWeaponCatalog() {
        val gatling = WeaponCatalog.getById("plasma_gatling")
        assertNotNull(gatling)
        assertTrue(gatling.baseDamage > 0f)
        assertTrue(gatling.fireRate > 0f)
    }

    @Test
    fun testFlightPhysicsBanking() {
        val physics = FlightPhysics()
        val player = PlayerAircraftState(x = 500f, y = 800f)

        // Accelerate to the right
        physics.updateAircraftPhysics(
            player = player,
            inputDirX = 1.0f,
            inputDirY = 0.0f,
            dt = 0.05f,
            screenWidth = 1080f,
            screenHeight = 2160f,
            baseSpeed = 500f,
            handling = 1.0f
        )

        assertTrue("Player should move right", player.vx > 0f)
        assertTrue("Player should bank into the turn", player.bankAngle > 0f)
    }
}
