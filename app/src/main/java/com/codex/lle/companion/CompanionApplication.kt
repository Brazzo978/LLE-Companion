package com.codex.lle.companion

import android.app.Application

class CompanionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        if (CompanionPreferences(this).notificationsEnabled) {
            UpdateScheduler.enable(this)
        }
    }
}
