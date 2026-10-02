package com.example.game.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.AircraftSaveEntity
import com.example.data.PlayerProfileEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(
        val uid: String,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
        val isAnonymous: Boolean
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "AuthManager"
        // Default Web Client ID fallback for Google Identity
        var WEB_CLIENT_ID: String = "3000studios-thunderdome-auth.apps.googleusercontent.com"
    }

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _syncStatus = MutableStateFlow<String>("Synced locally")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    init {
        checkCurrentAuthStatus()
    }

    fun checkCurrentAuthStatus() {
        try {
            val user = auth.currentUser
            if (user != null) {
                _authState.value = AuthState.Authenticated(
                    uid = user.uid,
                    displayName = user.displayName ?: if (user.isAnonymous) "Guest Pilot" else "Thunder Pilot",
                    email = user.email,
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = user.isAnonymous
                )
            } else {
                signInAnonymously()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth check caught: ${e.message}")
            _authState.value = AuthState.Idle
        }
    }

    fun signInAnonymously() {
        coroutineScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInAnonymously().await()
                val user = result.user
                if (user != null) {
                    _authState.value = AuthState.Authenticated(
                        uid = user.uid,
                        displayName = "Guest Pilot",
                        email = null,
                        photoUrl = null,
                        isAnonymous = true
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Anonymous sign-in failed: ${e.message}")
                _authState.value = AuthState.Idle
            }
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        coroutineScope.launch {
            try {
                _authState.value = AuthState.Loading

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = withContext(Dispatchers.IO) {
                    credentialManager.getCredential(activity, request)
                }

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                    val currentUser = auth.currentUser
                    val authResult = if (currentUser != null && currentUser.isAnonymous) {
                        // Link guest account to Google Account to preserve all local progress & unlocks!
                        try {
                            currentUser.linkWithCredential(firebaseCredential).await()
                        } catch (linkEx: Exception) {
                            // If linking fails (e.g. account already exists), sign in directly
                            auth.signInWithCredential(firebaseCredential).await()
                        }
                    } else {
                        auth.signInWithCredential(firebaseCredential).await()
                    }

                    val user = authResult.user
                    if (user != null) {
                        _authState.value = AuthState.Authenticated(
                            uid = user.uid,
                            displayName = user.displayName ?: googleIdTokenCredential.displayName ?: "Thunder Pilot",
                            email = user.email ?: googleIdTokenCredential.id,
                            photoUrl = user.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString(),
                            isAnonymous = false
                        )
                        _syncStatus.value = "Connected as ${user.displayName ?: user.email}"
                        withContext(Dispatchers.Main) { onSuccess() }
                    }
                } else {
                    _authState.value = AuthState.Error("Unsupported credential type received.")
                    withContext(Dispatchers.Main) { onError("Unsupported credential type") }
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "Google Sign-In cancelled by user.")
                checkCurrentAuthStatus()
            } catch (e: GetCredentialException) {
                Log.w(TAG, "Credential Manager error: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Authentication error")
                withContext(Dispatchers.Main) { onError(e.message ?: "Google Sign-In failed") }
            } catch (e: Exception) {
                Log.w(TAG, "Sign-in error: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
                withContext(Dispatchers.Main) { onError(e.message ?: "Sign-In error") }
            }
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        coroutineScope.launch {
            try {
                auth.signOut()
                _authState.value = AuthState.Idle
                signInAnonymously()
                withContext(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                Log.w(TAG, "Sign-out error: ${e.message}")
            }
        }
    }

    // ── FIRESTORE CLOUD SAVE & CROSS-DEVICE SYNC ──

    fun syncProfileToFirestore(
        profile: PlayerProfileEntity,
        aircraftList: List<AircraftSaveEntity>,
        onSynced: () -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                _syncStatus.value = "Syncing to cloud..."
                val profileMap = hashMapOf(
                    "callsign" to profile.callsign,
                    "level" to profile.level,
                    "xp" to profile.xp,
                    "credits" to profile.credits,
                    "plasmaCores" to profile.plasmaCores,
                    "selectedAircraftId" to profile.selectedAircraftId,
                    "highScore" to profile.highScore,
                    "totalKills" to profile.totalKills,
                    "bossesDefeated" to profile.bossesDefeated,
                    "missionsCompleted" to profile.missionsCompleted,
                    "vanguardPassTier" to profile.vanguardPassTier,
                    "vanguardPassXp" to profile.vanguardPassXp,
                    "claimedPassTiers" to profile.claimedPassTiers,
                    "isAdsRemoved" to profile.isAdsRemoved,
                    "hasFounderPack" to profile.hasFounderPack,
                    "hasStarterPack" to profile.hasStarterPack,
                    "purchasedProductIds" to profile.purchasedProductIds,
                    "updatedAt" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(uid)
                    .set(profileMap, SetOptions.merge())
                    .await()

                val aircraftMapList = aircraftList.map { a ->
                    hashMapOf(
                        "aircraftId" to a.aircraftId,
                        "isUnlocked" to a.isUnlocked,
                        "level" to a.level,
                        "overclockLevel" to a.overclockLevel,
                        "paintSchemeId" to a.paintSchemeId,
                        "exhaustColorId" to a.exhaustColorId,
                        "primaryWeaponId" to a.primaryWeaponId,
                        "secondaryWeaponId" to a.secondaryWeaponId,
                        "specialAbilityId" to a.specialAbilityId,
                        "engineUpgradeLevel" to a.engineUpgradeLevel,
                        "weaponUpgradeLevel" to a.weaponUpgradeLevel,
                        "armorUpgradeLevel" to a.armorUpgradeLevel,
                        "shieldUpgradeLevel" to a.shieldUpgradeLevel,
                        "avionicsUpgradeLevel" to a.avionicsUpgradeLevel
                    )
                }

                firestore.collection("users")
                    .document(uid)
                    .collection("fleet")
                    .document("hangar")
                    .set(mapOf("aircraft" to aircraftMapList), SetOptions.merge())
                    .await()

                _syncStatus.value = "Cloud Save Active"
                withContext(Dispatchers.Main) { onSynced() }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore sync failed: ${e.message}")
                _syncStatus.value = "Sync pending"
            }
        }
    }

    fun restoreProfileFromFirestore(
        onRestored: (profile: PlayerProfileEntity?, aircraft: List<AircraftSaveEntity>?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val profile = PlayerProfileEntity(
                        id = 1,
                        callsign = doc.getString("callsign") ?: "VANGUARD-01",
                        level = doc.getLong("level")?.toInt() ?: 1,
                        xp = doc.getLong("xp") ?: 0L,
                        credits = doc.getLong("credits") ?: 2500L,
                        plasmaCores = doc.getLong("plasmaCores")?.toInt() ?: 15,
                        selectedAircraftId = doc.getString("selectedAircraftId") ?: "apex_falcon",
                        highScore = doc.getLong("highScore") ?: 0L,
                        totalKills = doc.getLong("totalKills")?.toInt() ?: 0,
                        bossesDefeated = doc.getLong("bossesDefeated")?.toInt() ?: 0,
                        missionsCompleted = doc.getLong("missionsCompleted")?.toInt() ?: 0,
                        vanguardPassTier = doc.getLong("vanguardPassTier")?.toInt() ?: 1,
                        vanguardPassXp = doc.getLong("vanguardPassXp")?.toInt() ?: 0,
                        claimedPassTiers = doc.getString("claimedPassTiers") ?: "1,2",
                        isAdsRemoved = doc.getBoolean("isAdsRemoved") ?: false,
                        hasFounderPack = doc.getBoolean("hasFounderPack") ?: false,
                        hasStarterPack = doc.getBoolean("hasStarterPack") ?: false,
                        purchasedProductIds = doc.getString("purchasedProductIds") ?: "",
                        lastSyncTimestamp = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )

                    val hangarDoc = firestore.collection("users")
                        .document(uid)
                        .collection("fleet")
                        .document("hangar")
                        .get()
                        .await()

                    val aircraftList = mutableListOf<AircraftSaveEntity>()
                    @Suppress("UNCHECKED_CAST")
                    val rawList = hangarDoc.get("aircraft") as? List<Map<String, Any>>
                    if (rawList != null) {
                        for (item in rawList) {
                            aircraftList.add(
                                AircraftSaveEntity(
                                    aircraftId = item["aircraftId"] as? String ?: "apex_falcon",
                                    isUnlocked = item["isUnlocked"] as? Boolean ?: false,
                                    level = (item["level"] as? Number)?.toInt() ?: 1,
                                    overclockLevel = (item["overclockLevel"] as? Number)?.toInt() ?: 0,
                                    paintSchemeId = item["paintSchemeId"] as? String ?: "stealth_black",
                                    exhaustColorId = item["exhaustColorId"] as? String ?: "cyan_flame",
                                    primaryWeaponId = item["primaryWeaponId"] as? String ?: "plasma_gatling",
                                    secondaryWeaponId = item["secondaryWeaponId"] as? String ?: "swarm_missiles",
                                    specialAbilityId = item["specialAbilityId"] as? String ?: "chrono_overdrive",
                                    engineUpgradeLevel = (item["engineUpgradeLevel"] as? Number)?.toInt() ?: 0,
                                    weaponUpgradeLevel = (item["weaponUpgradeLevel"] as? Number)?.toInt() ?: 0,
                                    armorUpgradeLevel = (item["armorUpgradeLevel"] as? Number)?.toInt() ?: 0,
                                    shieldUpgradeLevel = (item["shieldUpgradeLevel"] as? Number)?.toInt() ?: 0,
                                    avionicsUpgradeLevel = (item["avionicsUpgradeLevel"] as? Number)?.toInt() ?: 0
                                )
                            )
                        }
                    }

                    _syncStatus.value = "Cloud save restored"
                    withContext(Dispatchers.Main) {
                        onRestored(profile, if (aircraftList.isNotEmpty()) aircraftList else null)
                    }
                } else {
                    withContext(Dispatchers.Main) { onRestored(null, null) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Restore from Firestore failed: ${e.message}")
                withContext(Dispatchers.Main) { onRestored(null, null) }
            }
        }
    }
}
