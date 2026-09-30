package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Database(
    entities = [
        PlayerProfileEntity::class,
        AircraftSaveEntity::class,
        SettingsEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AeroStrikeDatabase : RoomDatabase() {
    abstract fun dao(): AeroStrikeDao

    companion object {
        @Volatile
        private var INSTANCE: AeroStrikeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AeroStrikeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AeroStrikeDatabase::class.java,
                    "aerostrike_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE aircraft_saves ADD COLUMN overclockLevel INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE game_settings ADD COLUMN touchInputEnabled INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE game_settings ADD COLUMN touchOffsetY REAL NOT NULL DEFAULT 55")
                database.execSQL("ALTER TABLE game_settings ADD COLUMN soundEnabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE player_profile ADD COLUMN hasClaimedPromoBundle INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE player_profile ADD COLUMN claimedPassTiers TEXT NOT NULL DEFAULT '1,2'")
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.dao())
                    }
                }
            }

            suspend fun populateInitialData(dao: AeroStrikeDao) {
                AeroStrikeDatabase.populateInitialData(dao)
            }
        }

        suspend fun populateInitialData(dao: AeroStrikeDao) {
            dao.insertOrUpdateProfile(PlayerProfileEntity())
            dao.insertOrUpdateSettings(SettingsEntity())
            val initialSaves = com.example.game.model.AircraftCatalog.ALL_AIRCRAFT.map { spec ->
                AircraftSaveEntity(
                    aircraftId = spec.id,
                    isUnlocked = spec.unlockCostCredits == 0L,
                    level = 1,
                    paintSchemeId = "stealth_black",
                    exhaustColorId = "cyan_flame",
                    primaryWeaponId = "plasma_gatling",
                    secondaryWeaponId = "swarm_missiles",
                    specialAbilityId = spec.defaultSpecialAbilityId
                )
            }
            dao.insertAllAircraft(initialSaves)
        }
    }
}

class GameRepository(private val dao: AeroStrikeDao) {
    val playerProfile: Flow<PlayerProfileEntity?> = dao.getPlayerProfile()
    val allAircraft: Flow<List<AircraftSaveEntity>> = dao.getAllAircraft()
    val settings: Flow<SettingsEntity?> = dao.getSettings()

    suspend fun ensureInitialized() {
        if (dao.getProfileDirect() == null) {
            AeroStrikeDatabase.populateInitialData(dao)
        } else {
            // Guarantee all 24 aircraft exist in database for existing saves
            val existingSaves = com.example.game.model.AircraftCatalog.ALL_AIRCRAFT.map { spec ->
                AircraftSaveEntity(
                    aircraftId = spec.id,
                    isUnlocked = spec.unlockCostCredits == 0L,
                    level = 1,
                    paintSchemeId = "stealth_black",
                    exhaustColorId = "cyan_flame",
                    primaryWeaponId = "plasma_gatling",
                    secondaryWeaponId = "swarm_missiles",
                    specialAbilityId = spec.defaultSpecialAbilityId
                )
            }
            dao.insertAllAircraft(existingSaves)
        }
        val currentSettings = dao.getSettingsDirect()
        if (currentSettings != null) {
            val optimized = currentSettings.copy(
                graphicsPreset = "ULTRA",
                targetFps = 120,
                controlScheme = if (currentSettings.controlScheme == "JOYSTICK") "TOUCH_FOLLOW" else currentSettings.controlScheme
            )
            if (optimized != currentSettings) {
                dao.insertOrUpdateSettings(optimized)
            }
        }
    }

    suspend fun updateProfile(profile: PlayerProfileEntity) = dao.insertOrUpdateProfile(profile)
    suspend fun updateAircraft(aircraft: AircraftSaveEntity) = dao.insertOrUpdateAircraft(aircraft)
    suspend fun updateSettings(settings: SettingsEntity) = dao.insertOrUpdateSettings(settings)
    suspend fun getAircraftById(id: String) = dao.getAircraftById(id)
}
