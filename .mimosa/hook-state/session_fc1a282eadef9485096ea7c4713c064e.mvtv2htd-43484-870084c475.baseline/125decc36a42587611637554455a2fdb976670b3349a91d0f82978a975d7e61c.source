package com.nexus.app

import android.app.Application
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.nexus.common.Constants
import com.nexus.network.api.AuthTokenProvider
import com.nexus.notifications.NotificationHelper
import com.nexus.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class NexusApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncScheduler: SyncScheduler

    // Created eagerly so the Firebase auth-state listener is registered before
    // the first authenticated request.
    @Inject lateinit var authTokenProvider: AuthTokenProvider
    @Inject lateinit var dataSeeder: com.nexus.app.data.DataSeeder

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.WARN)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build()
            )
            Timber.plant(Timber.DebugTree())
        }
        NotificationHelper.createChannels(this)
        runCatching { syncScheduler.scheduleAll() }
            .onFailure { Timber.w(it, "Unable to schedule background sync") }
        dataSeeder.seedIfEmpty()
    }
}
