package com.project.locarm.common.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.project.locarm.BuildConfig

class InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null

    fun load(context: Context) {

        val request = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            BuildConfig.ADS_INTERSTITIAL_ID,
            request,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun show(
        activity: Activity,
        onDismiss: () -> Unit
    ) {
        val ad = interstitialAd

        if (ad == null) {
            onDismiss()
            return
        }

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onDismiss()
                    load(activity)
                }

                override fun onAdFailedToShowFullScreenContent(
                    adError: AdError
                ) {
                    interstitialAd = null
                    onDismiss()
                    load(activity)
                }
            }

        ad.show(activity)
    }
}
