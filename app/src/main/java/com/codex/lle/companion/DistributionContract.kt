package com.codex.lle.companion

interface DistributionCallbacks {
    fun showProgress(message: String, percent: Int? = null)
    fun hideProgress()
    fun showMessage(message: String)
    fun refreshInstalledState()
}
