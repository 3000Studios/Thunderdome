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
    version = 7,
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE aircraft_saves ADD COLUMN overclockLevel INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_settings ADD COLUMN touchInputEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE game_settings ADD COLUMN touchOffsetY REAL NOT NULL DEFAULT 55")
                db.execSQL("ALTER TABLE game_settings ADD COLUMN soundEnabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player_profile ADD COLUMN hasClaimedPromoBundle INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN claimedPassTiers TEXT NOT NULL DEFAULT '1,2'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_settings ADD COLUMN cameraViewMode TEXT NOT NULL DEFAULT 'FOLLOW_3RD'")
                db.execSQL("ALTER TABLE game_settings ADD COLUMN screenSizeScale TEXT NOT NULL DEFAULT 'MAX_IMMERSIVE'")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player_profile ADD COLUMN isAdsRemoved INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN hasFounderPack INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN hasStarterPack INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN purchasedProductIds TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN lastSyncTimestamp INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_settings ADD COLUMN isDeveloperMode INTEGER NOT NULL DEFAULT 0")
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
    suspend fun getProfileDirect() = dao.getProfileDirect()
    suspend fun getAllAircraftDirect() = dao.getAllAircraftDirect()

    suspend fun restoreSaveSnapshot(snapshot: CloudSaveSnapshot) {
        dao.insertOrUpdateProfile(snapshot.profile)
        for (a in snapshot.aircraft) {
            dao.insertOrUpdateAircraft(a)
        }
    }
}
