package com.example.data

import android.content.Context
import androidx.room.Database
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
    version = 1,
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
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
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
                dao.insertOrUpdateProfile(PlayerProfileEntity())
                dao.insertOrUpdateSettings(SettingsEntity())
                dao.insertAllAircraft(
                    listOf(
                        AircraftSaveEntity(
                            aircraftId = "apex_falcon",
                            isUnlocked = true,
                            level = 1,
                            paintSchemeId = "stealth_black",
                            exhaustColorId = "cyan_flame",
                            primaryWeaponId = "plasma_gatling",
                            secondaryWeaponId = "swarm_missiles",
                            specialAbilityId = "chrono_overdrive"
                        ),
                        AircraftSaveEntity(
                            aircraftId = "valkyrie_phantom",
                            isUnlocked = true,
                            level = 1,
                            paintSchemeId = "cobalt_frost",
                            exhaustColorId = "violet_flame",
                            primaryWeaponId = "twin_laser",
                            secondaryWeaponId = "emp_torpedo",
                            specialAbilityId = "warp_dash"
                        ),
                        AircraftSaveEntity(
                            aircraftId = "titan_dread",
                            isUnlocked = false,
                            level = 1,
                            paintSchemeId = "crimson_war",
                            exhaustColorId = "amber_flame",
                            primaryWeaponId = "heavy_flak",
                            secondaryWeaponId = "cluster_bombs",
                            specialAbilityId = "hyper_shield"
                        ),
                        AircraftSaveEntity(
                            aircraftId = "solaris_specter",
                            isUnlocked = false,
                            level = 1,
                            paintSchemeId = "solar_flare",
                            exhaustColorId = "emerald_flame",
                            primaryWeaponId = "railgun",
                            secondaryWeaponId = "hunter_drones",
                            specialAbilityId = "nova_blast"
                        )
                    )
                )
            }
        }
    }
}

class GameRepository(private val dao: AeroStrikeDao) {
    val playerProfile: Flow<PlayerProfileEntity?> = dao.getPlayerProfile()
    val allAircraft: Flow<List<AircraftSaveEntity>> = dao.getAllAircraft()
    val settings: Flow<SettingsEntity?> = dao.getSettings()

    suspend fun updateProfile(profile: PlayerProfileEntity) = dao.insertOrUpdateProfile(profile)
    suspend fun updateAircraft(aircraft: AircraftSaveEntity) = dao.insertOrUpdateAircraft(aircraft)
    suspend fun updateSettings(settings: SettingsEntity) = dao.insertOrUpdateSettings(settings)
    suspend fun getAircraftById(id: String) = dao.getAircraftById(id)
}
