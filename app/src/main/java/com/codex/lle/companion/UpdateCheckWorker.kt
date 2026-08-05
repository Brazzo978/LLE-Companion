package com.codex.lle.companion

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class UpdateCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val preferences = CompanionPreferences(applicationContext)
        if (!preferences.notificationsEnabled) return Result.success()

        return try {
            val remote = UpdateRepository().fetchLatestVersion()
            preferences.lastCheckedAt = System.currentTimeMillis()
            preferences.lastRemoteVersion = remote.toString()
            val installed = InstalledLleReader.read(applicationContext)
            if (installed != null && remote > installed.version &&
                preferences.lastNotifiedVersion != remote.toString()
            ) {
                NotificationHelper.notifyUpdate(applicationContext, remote)
                preferences.lastNotifiedVersion = remote.toString()
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
