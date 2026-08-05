package com.codex.lle.companion

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat

data class InstalledLle(
    val version: LleVersion,
    val versionName: String,
    val versionCode: Long
)

object InstalledLleReader {
    fun read(context: Context): InstalledLle? = try {
        val info = context.packageManager.getPackageInfo(UpdateConfig.LLE_PACKAGE, 0)
        val name = info.versionName.orEmpty()
        val parsed = LleVersion.parse(name) ?: return null
        InstalledLle(parsed, name, PackageInfoCompat.getLongVersionCode(info))
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
}
