package ca.qolt

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ca.qolt.workers.ServiceHealthCheckWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class QoltApplication : Application(), Configuration.Provider {
    companion object {
        const val TAG = "QoltApplication"
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("Qolt Application started")

        scheduleServiceHealthCheck()

        GlobalScope.launch {

        }
    }

    private fun scheduleServiceHealthCheck() {
        val healthCheckRequest = PeriodicWorkRequestBuilder<ServiceHealthCheckWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "app_blocking_health_check",
            ExistingPeriodicWorkPolicy.KEEP,
            healthCheckRequest
        )

        Timber.d("Service health check worker scheduled")
    }
}
