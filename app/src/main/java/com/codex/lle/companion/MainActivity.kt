package com.codex.lle.companion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class MainActivity : AppCompatActivity(), DistributionCallbacks {
    private lateinit var preferences: CompanionPreferences
    private lateinit var distribution: DistributionController
    private lateinit var statusText: TextView
    private lateinit var installedVersionText: TextView
    private lateinit var availableVersionText: TextView
    private lateinit var lastCheckedText: TextView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var progressText: TextView
    private lateinit var actionButton: Button
    private lateinit var checkButton: Button
    private lateinit var notificationSwitch: Switch

    private var installedLle: InstalledLle? = null
    private var remoteVersion: LleVersion? = null
    private var suppressSwitchCallback = false

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            setNotificationsEnabled(true)
        } else {
            setNotificationsEnabled(false)
            showMessage(getString(R.string.notifications_denied))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemBarInsets()

        preferences = CompanionPreferences(this)
        distribution = DistributionController(this, this)
        bindViews()
        setupActions()
        refreshInstalledState()
        checkRemoteVersion()
    }

    override fun onResume() {
        super.onResume()
        if (::distribution.isInitialized) distribution.onResume()
        if (::installedVersionText.isInitialized) refreshInstalledState()
    }

    override fun onDestroy() {
        if (::distribution.isInitialized) distribution.cancel()
        super.onDestroy()
    }

    private fun bindViews() {
        statusText = findViewById(R.id.statusText)
        installedVersionText = findViewById(R.id.installedVersionText)
        availableVersionText = findViewById(R.id.availableVersionText)
        lastCheckedText = findViewById(R.id.lastCheckedText)
        progressIndicator = findViewById(R.id.progressIndicator)
        progressText = findViewById(R.id.progressText)
        actionButton = findViewById(R.id.actionButton)
        checkButton = findViewById(R.id.checkButton)
        notificationSwitch = findViewById(R.id.notificationSwitch)
        suppressSwitchCallback = true
        notificationSwitch.isChecked = preferences.notificationsEnabled
        suppressSwitchCallback = false
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.rootLayout)
        val header = findViewById<View>(R.id.headerLayout)
        val content = findViewById<View>(R.id.contentScroll)
        val headerTopPadding = header.paddingTop
        val contentBottomPadding = content.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            header.updatePadding(top = headerTopPadding + systemBars.top)
            content.updatePadding(bottom = contentBottomPadding + systemBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun setupActions() {
        checkButton.setOnClickListener { checkRemoteVersion() }
        actionButton.setOnClickListener {
            val version = remoteVersion
            if (version == null) {
                checkRemoteVersion()
            } else {
                distribution.performAction(version)
            }
        }
        findViewById<TextView>(R.id.releaseLink).setOnClickListener {
            distribution.showTutorialThenOpen(
                remoteVersion?.let(UpdateConfig::releasePage) ?: UpdateConfig.RELEASES
            )
        }
        findViewById<TextView>(R.id.installTutorialLink).setOnClickListener {
            distribution.showInstallTutorial()
        }
        notificationSwitch.setOnCheckedChangeListener { _, checked ->
            if (suppressSwitchCallback) return@setOnCheckedChangeListener
            if (checked && Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                setNotificationsEnabled(checked)
            }
        }
    }

    private fun setNotificationsEnabled(enabled: Boolean) {
        preferences.notificationsEnabled = enabled
        suppressSwitchCallback = true
        notificationSwitch.isChecked = enabled
        suppressSwitchCallback = false
        if (enabled) UpdateScheduler.enable(this) else UpdateScheduler.disable(this)
    }

    private fun checkRemoteVersion() {
        checkButton.isEnabled = false
        showProgress(getString(R.string.status_checking))
        lifecycleScope.launch {
            try {
                val version = UpdateRepository().fetchLatestVersion()
                remoteVersion = version
                preferences.lastCheckedAt = System.currentTimeMillis()
                preferences.lastRemoteVersion = version.toString()
                availableVersionText.text = version.toString()
                renderState()
            } catch (_: Exception) {
                remoteVersion = null
                availableVersionText.setText(R.string.unknown)
                statusText.setText(R.string.status_error)
                statusText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.lle_red))
                actionButton.visibility = View.GONE
                showMessage(getString(R.string.version_file_error))
            } finally {
                hideProgress()
                checkButton.isEnabled = true
                renderLastChecked()
            }
        }
    }

    override fun refreshInstalledState() {
        installedLle = InstalledLleReader.read(this)
        installedVersionText.text = installedLle?.versionName ?: getString(R.string.not_installed)
        renderState()
    }

    private fun renderState() {
        val remote = remoteVersion ?: return
        val installed = installedLle
        actionButton.visibility = View.VISIBLE
        actionButton.isEnabled = true

        when {
            installed == null -> {
                statusText.setText(R.string.status_not_installed)
                statusText.setTextColor(ContextCompat.getColor(this, R.color.lle_amber))
                actionButton.setText(R.string.install_lle)
            }
            remote > installed.version -> {
                statusText.setText(R.string.status_update_available)
                statusText.setTextColor(ContextCompat.getColor(this, R.color.lle_amber))
                actionButton.setText(R.string.update_lle)
            }
            remote == installed.version -> {
                statusText.setText(R.string.status_up_to_date)
                statusText.setTextColor(ContextCompat.getColor(this, R.color.lle_green))
                actionButton.visibility = View.GONE
            }
            else -> {
                statusText.setText(R.string.status_dev_version)
                statusText.setTextColor(ContextCompat.getColor(this, R.color.lle_cyan))
                actionButton.visibility = View.GONE
            }
        }
        renderLastChecked()
    }

    private fun renderLastChecked() {
        val formatted = if (preferences.lastCheckedAt == 0L) {
            getString(R.string.never)
        } else {
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(preferences.lastCheckedAt))
        }
        lastCheckedText.text = getString(R.string.last_checked_value, formatted)
    }

    override fun showProgress(message: String, percent: Int?) {
        progressIndicator.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        progressText.text = message
        if (percent == null) {
            progressIndicator.isIndeterminate = true
        } else {
            progressIndicator.isIndeterminate = false
            progressIndicator.progress = percent.coerceIn(0, 100)
        }
        actionButton.isEnabled = false
    }

    override fun hideProgress() {
        progressIndicator.visibility = View.GONE
        progressText.visibility = View.GONE
        actionButton.isEnabled = true
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}
