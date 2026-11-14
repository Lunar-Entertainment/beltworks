package com.microfactory.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

/**
 * Loads and shows the single AdMob Help ad unit.
 * Automatically resolves whether the unit is configured in AdMob as Rewarded,
 * Rewarded Interstitial, or Interstitial, and gracefully falls back if offline/no-fill.
 */
class HelpAdManager(context: Context) {
    companion object {
        const val HELP_AD_UNIT_ID = "ca-app-pub-9323259012432810/7895437231"
    }

    private val appContext = context.applicationContext
    private var initialized = false
    private var isLoading = false

    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var interstitialAd: InterstitialAd? = null

    private enum class AdFormat { REWARDED, REWARDED_INTERSTITIAL, INTERSTITIAL }
    private var preferredFormat: AdFormat? = null

    fun initialize() {
        if (initialized) return
        initialized = true
        runCatching {
            MobileAds.initialize(appContext) {
                preload()
            }
        }
    }

    fun preload() {
        initialize()
        if (hasLoadedAd() || isLoading) return
        loadWithFallback(
            startFormat = preferredFormat ?: AdFormat.REWARDED,
            onReady = {},
            onAllFailed = {}
        )
    }

    private fun hasLoadedAd(): Boolean =
        rewardedAd != null || rewardedInterstitialAd != null || interstitialAd != null

    fun showHelpAd(
        activity: Activity,
        onAdStarted: () -> Unit,
        onAdEnded: () -> Unit,
        onHelpUnlocked: () -> Unit,
        onSkippedBeforeReward: () -> Unit,
        onFallbackUnlock: (String) -> Unit
    ) {
        initialize()

        if (hasLoadedAd()) {
            displayLoadedAd(
                activity = activity,
                onAdStarted = onAdStarted,
                onAdEnded = onAdEnded,
                onHelpUnlocked = onHelpUnlocked,
                onSkippedBeforeReward = onSkippedBeforeReward
            )
            return
        }

        loadWithFallback(
            startFormat = preferredFormat ?: AdFormat.REWARDED,
            onReady = {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    displayLoadedAd(
                        activity = activity,
                        onAdStarted = onAdStarted,
                        onAdEnded = onAdEnded,
                        onHelpUnlocked = onHelpUnlocked,
                        onSkippedBeforeReward = onSkippedBeforeReward
                    )
                }
            },
            onAllFailed = {
                onFallbackUnlock("Ad unavailable right now — Help unlocked.")
            }
        )
    }

    private fun displayLoadedAd(
        activity: Activity,
        onAdStarted: () -> Unit,
        onAdEnded: () -> Unit,
        onHelpUnlocked: () -> Unit,
        onSkippedBeforeReward: () -> Unit
    ) {
        val rAd = rewardedAd
        if (rAd != null) {
            rewardedAd = null
            var earned = false
            rAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    onAdStarted()
                }

                override fun onAdDismissedFullScreenContent() {
                    onAdEnded()
                    preload()
                    if (earned) onHelpUnlocked() else onSkippedBeforeReward()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    onAdEnded()
                    preload()
                    onHelpUnlocked()
                }
            }
            rAd.show(activity) {
                earned = true
            }
            return
        }

        val riAd = rewardedInterstitialAd
        if (riAd != null) {
            rewardedInterstitialAd = null
            var earned = false
            riAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    onAdStarted()
                }

                override fun onAdDismissedFullScreenContent() {
                    onAdEnded()
                    preload()
                    if (earned) onHelpUnlocked() else onSkippedBeforeReward()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    onAdEnded()
                    preload()
                    onHelpUnlocked()
                }
            }
            riAd.show(activity) {
                earned = true
            }
            return
        }

        val iAd = interstitialAd
        if (iAd != null) {
            interstitialAd = null
            iAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    onAdStarted()
                }

                override fun onAdDismissedFullScreenContent() {
                    onAdEnded()
                    preload()
                    onHelpUnlocked()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    onAdEnded()
                    preload()
                    onHelpUnlocked()
                }
            }
            iAd.show(activity)
        }
    }

    private fun loadWithFallback(
        startFormat: AdFormat,
        onReady: () -> Unit,
        onAllFailed: () -> Unit
    ) {
        isLoading = true
        val request = AdRequest.Builder().build()

        when (startFormat) {
            AdFormat.REWARDED -> {
                RewardedAd.load(
                    appContext,
                    HELP_AD_UNIT_ID,
                    request,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            isLoading = false
                            preferredFormat = AdFormat.REWARDED
                            rewardedAd = ad
                            onReady()
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            if (preferredFormat == AdFormat.REWARDED) {
                                isLoading = false
                                onAllFailed()
                            } else {
                                loadWithFallback(AdFormat.REWARDED_INTERSTITIAL, onReady, onAllFailed)
                            }
                        }
                    }
                )
            }

            AdFormat.REWARDED_INTERSTITIAL -> {
                RewardedInterstitialAd.load(
                    appContext,
                    HELP_AD_UNIT_ID,
                    request,
                    object : RewardedInterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedInterstitialAd) {
                            isLoading = false
                            preferredFormat = AdFormat.REWARDED_INTERSTITIAL
                            rewardedInterstitialAd = ad
                            onReady()
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            if (preferredFormat == AdFormat.REWARDED_INTERSTITIAL) {
                                isLoading = false
                                onAllFailed()
                            } else {
                                loadWithFallback(AdFormat.INTERSTITIAL, onReady, onAllFailed)
                            }
                        }
                    }
                )
            }

            AdFormat.INTERSTITIAL -> {
                InterstitialAd.load(
                    appContext,
                    HELP_AD_UNIT_ID,
                    request,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            isLoading = false
                            preferredFormat = AdFormat.INTERSTITIAL
                            interstitialAd = ad
                            onReady()
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isLoading = false
                            onAllFailed()
                        }
                    }
                )
            }
        }
    }
}
