package com.codex.lle.companion

import android.content.Context

class CompanionPreferences(context: Context) {
    private val values = context.getSharedPreferences("companion", Context.MODE_PRIVATE)

    var notificationsEnabled: Boolean
        get() = values.getBoolean("notifications_enabled", false)
        set(value) = values.edit().putBoolean("notifications_enabled", value).apply()

    var lastCheckedAt: Long
        get() = values.getLong("last_checked_at", 0L)
        set(value) = values.edit().putLong("last_checked_at", value).apply()

    var lastRemoteVersion: String?
        get() = values.getString("last_remote_version", null)
        set(value) = values.edit().putString("last_remote_version", value).apply()

    var lastNotifiedVersion: String?
        get() = values.getString("last_notified_version", null)
        set(value) = values.edit().putString("last_notified_version", value).apply()

    var waitingForBrowserReturn: Boolean
        get() = values.getBoolean("waiting_for_browser_return", false)
        set(value) = values.edit().putBoolean("waiting_for_browser_return", value).apply()
}
