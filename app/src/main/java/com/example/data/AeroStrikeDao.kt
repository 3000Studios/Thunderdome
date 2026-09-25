package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AeroStrikeDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM aircraft_saves")
    fun getAllAircraft(): Flow<List<AircraftSaveEntity>>

    @Query("SELECT * FROM aircraft_saves WHERE aircraftId = :id")
    suspend fun getAircraftById(id: String): AircraftSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAircraft(aircraft: AircraftSaveEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAircraft(aircraftList: List<AircraftSaveEntity>)

    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SettingsEntity)
}
