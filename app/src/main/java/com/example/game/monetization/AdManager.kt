package com.example.game.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdManager(private val context: Context) {

    companion object {
        private const val TAG = "AdManager"

        // Official Google Sample / Test Ad Unit IDs (Play Store separation)
        private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

        // Production Ad Unit ID placeholders (Configured via secrets or remote config)
        var PRODUCTION_REWARDED_AD_UNIT_ID: String? = null
        var PRODUCTION_INTERSTITIAL_AD_UNIT_ID: String? = null
    }

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    private val _isRewardedAdLoaded = MutableStateFlow(false)
    val isRewardedAdLoaded: StateFlow<Boolean> = _isRewardedAdLoaded

    private var sortiesCompletedCount: Int = 0

    init {
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob SDK Initialized: $initializationStatus")
                loadRewardedAd()
                loadInterstitialAd()
            }
        } catch (e: Exception) {
            Log.w(TAG, "AdMob initialization caught exception: ${e.message}")
        }
    }

    private fun getRewardedAdUnitId(): String {
        return PRODUCTION_REWARDED_AD_UNIT_ID?.takeIf { it.isNotBlank() } ?: TEST_REWARDED_AD_UNIT_ID
    }

    private fun getInterstitialAdUnitId(): String {
        return PRODUCTION_INTERSTITIAL_AD_UNIT_ID?.takeIf { it.isNotBlank() } ?: TEST_INTERSTITIAL_AD_UNIT_ID
    }

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            getRewardedAdUnitId(),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _isRewardedAdLoaded.value = true
                    Log.d(TAG, "Rewarded ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            getInterstitialAdUnitId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (rewardAmount: Int, rewardType: String) -> Unit,
        onAdDismissedOrFailed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    loadRewardedAd()
                    onAdDismissedOrFailed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    loadRewardedAd()
                    onAdDismissedOrFailed()
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned(rewardItem.amount, rewardItem.type)
            }
        } else {
            // Fallback for offline testing or when ad is loading: grant reward in sandbox
            loadRewardedAd()
            onRewardEarned(1, "SORTIE_BONUS")
            onAdDismissedOrFailed()
        }
    }

    fun onSortieFinished(activity: Activity, isAdsRemoved: Boolean) {
        if (isAdsRemoved) return
        sortiesCompletedCount++

        // Only show interstitial every 3 sorties, never during combat
        if (sortiesCompletedCount % 3 == 0) {
            val ad = interstitialAd
            if (ad != null) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitialAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        interstitialAd = null
                        loadInterstitialAd()
                    }
                }
                ad.show(activity)
            } else {
                loadInterstitialAd()
            }
        }
    }
}
