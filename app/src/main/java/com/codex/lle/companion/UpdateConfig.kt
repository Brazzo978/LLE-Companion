package com.codex.lle.companion

object UpdateConfig {
    const val LLE_PACKAGE = "com.codex.lle64"
    const val REPOSITORY = "https://github.com/Brazzo978/L.L.E-Legacy-Lockscreen-Effects"
    const val RELEASES = "$REPOSITORY/releases"
    const val USER_AGENT = "LLE-Companion/1.0"

    fun releasePage(version: LleVersion): String = "$RELEASES/tag/v$version"
}
