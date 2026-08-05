package com.codex.lle.companion

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import kotlin.math.roundToInt

class DistributionController(
    private val activity: AppCompatActivity,
    private val callbacks: DistributionCallbacks
) {
    val noticeTextRes: Int = R.string.play_notice
    private val preferences = CompanionPreferences(activity)
    private var tutorialDialog: Dialog? = null

    fun performAction(version: LleVersion) {
        showTutorialThenOpen(UpdateConfig.releasePage(version))
    }

    fun showTutorialThenOpen(url: String) {
        if (activity.isFinishing || activity.isDestroyed) return

        tutorialDialog?.dismiss()
        val dialog = Dialog(activity)
        tutorialDialog = dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_github_tutorial)
        dialog.setCanceledOnTouchOutside(true)
        dialog.findViewById<LoopingGifView>(R.id.downloadTutorialGif)
            .setGifResource(R.raw.github_download_tutorial)
        dialog.findViewById<AppCompatButton>(R.id.openGithubButton).setOnClickListener {
            dialog.dismiss()
            openUrl(url)
        }
        dialog.findViewById<AppCompatButton>(R.id.cancelTutorialButton).setOnClickListener {
            dialog.dismiss()
        }
        dialog.setOnDismissListener {
            if (tutorialDialog === dialog) tutorialDialog = null
        }
        dialog.show()

        val density = activity.resources.displayMetrics.density
        val horizontalMargins = (32 * density).roundToInt()
        val maxWidth = (520 * density).roundToInt()
        val dialogWidth = (activity.resources.displayMetrics.widthPixels - horizontalMargins)
            .coerceAtMost(maxWidth)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            setDimAmount(0.45f)
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            preferences.waitingForBrowserReturn = true
            activity.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            preferences.waitingForBrowserReturn = false
            callbacks.showMessage(activity.getString(R.string.download_failed, "No browser available"))
        }
    }

    fun onResume() {
        if (!preferences.waitingForBrowserReturn) return
        preferences.waitingForBrowserReturn = false
        showDownloadReturnPrompt()
    }

    private fun showDownloadReturnPrompt() {
        if (activity.isFinishing || activity.isDestroyed) return

        val dialog = createDialog(R.layout.dialog_download_return)
        dialog.findViewById<AppCompatButton>(R.id.showInstallTutorialButton).setOnClickListener {
            dialog.dismiss()
            showInstallTutorial()
        }
        dialog.findViewById<AppCompatButton>(R.id.downloadNotReadyButton).setOnClickListener {
            dialog.dismiss()
        }
        showDialog(dialog)
    }

    fun showInstallTutorial() {
        if (activity.isFinishing || activity.isDestroyed) return
        activity.startActivity(Intent(activity, InstallTutorialActivity::class.java))
    }

    private fun createDialog(layoutRes: Int): Dialog {
        tutorialDialog?.dismiss()
        return Dialog(activity).also { dialog ->
            tutorialDialog = dialog
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(layoutRes)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setOnDismissListener {
                if (tutorialDialog === dialog) tutorialDialog = null
            }
        }
    }

    private fun showDialog(dialog: Dialog) {
        dialog.show()
        val density = activity.resources.displayMetrics.density
        val horizontalMargins = (32 * density).roundToInt()
        val maxWidth = (520 * density).roundToInt()
        val dialogWidth = (activity.resources.displayMetrics.widthPixels - horizontalMargins)
            .coerceAtMost(maxWidth)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            setDimAmount(0.45f)
        }
    }

    fun cancel() {
        tutorialDialog?.dismiss()
        tutorialDialog = null
    }
}
