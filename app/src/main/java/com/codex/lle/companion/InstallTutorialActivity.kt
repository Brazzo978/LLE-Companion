package com.codex.lle.companion

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.Locale

class InstallTutorialActivity : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var controls: View
    private lateinit var playbackButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var currentTimeText: TextView
    private lateinit var durationText: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var playbackPosition = 0
    private var videoPrepared = false
    private var controlsVisible = true
    private var userPaused = false
    private var isScrubbing = false

    private val updateProgress = object : Runnable {
        override fun run() {
            if (!videoPrepared || isScrubbing) return
            val position = videoView.currentPosition.coerceAtLeast(0)
            seekBar.progress = position
            currentTimeText.text = formatTime(position)
            handler.postDelayed(this, PROGRESS_INTERVAL_MS)
        }
    }

    private val hideControls = Runnable {
        if (!videoView.isPlaying || isScrubbing || !controlsVisible) return@Runnable
        controls.animate()
            .alpha(0f)
            .setDuration(CONTROLS_FADE_MS)
            .withEndAction {
                controls.visibility = View.INVISIBLE
                controlsVisible = false
            }
            .start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install_tutorial)
        enterFullscreen()

        videoView = findViewById(R.id.installTutorialVideo)
        controls = findViewById(R.id.playerControls)
        playbackButton = findViewById(R.id.playbackButton)
        seekBar = findViewById(R.id.tutorialSeekBar)
        currentTimeText = findViewById(R.id.currentTimeText)
        durationText = findViewById(R.id.durationText)

        videoView.setVideoURI(
            Uri.parse("android.resource://$packageName/${R.raw.install_tutorial}")
        )
        videoView.setOnPreparedListener { player ->
            videoPrepared = true
            player.isLooping = true
            player.setVolume(0f, 0f)
            seekBar.max = player.duration.coerceAtLeast(0)
            durationText.text = formatTime(player.duration)
            if (playbackPosition > 0) videoView.seekTo(playbackPosition)
            videoView.start()
            updatePlaybackButton()
            startProgressUpdates()
            showControls(autoHide = true)
        }

        videoView.setOnClickListener {
            if (controlsVisible) hideControlsNow() else showControls(autoHide = true)
        }
        findViewById<ImageButton>(R.id.closeTutorialButton).setOnClickListener { finish() }
        playbackButton.setOnClickListener { togglePlayback() }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isScrubbing = true
                handler.removeCallbacks(hideControls)
                showControls(autoHide = false)
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) currentTimeText.text = formatTime(progress)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                videoView.seekTo(seekBar.progress)
                currentTimeText.text = formatTime(seekBar.progress)
                isScrubbing = false
                startProgressUpdates()
                if (videoView.isPlaying) scheduleControlsHide()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        enterFullscreen()
        if (videoPrepared && !userPaused && !videoView.isPlaying) {
            videoView.start()
            updatePlaybackButton()
            startProgressUpdates()
            scheduleControlsHide()
        }
    }

    override fun onPause() {
        if (::videoView.isInitialized && videoPrepared) {
            playbackPosition = videoView.currentPosition
            videoView.pause()
        }
        stopProgressUpdates()
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::videoView.isInitialized) videoView.stopPlayback()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterFullscreen()
    }

    private fun togglePlayback() {
        if (!videoPrepared) return
        if (videoView.isPlaying) {
            videoView.pause()
            userPaused = true
            stopProgressUpdates()
            showControls(autoHide = false)
        } else {
            videoView.start()
            userPaused = false
            startProgressUpdates()
            showControls(autoHide = true)
        }
        updatePlaybackButton()
    }

    private fun updatePlaybackButton() {
        val playing = videoView.isPlaying
        playbackButton.setImageResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play)
        playbackButton.contentDescription = getString(
            if (playing) R.string.pause_tutorial else R.string.play_tutorial
        )
    }

    private fun startProgressUpdates() {
        handler.removeCallbacks(updateProgress)
        handler.post(updateProgress)
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(updateProgress)
        handler.removeCallbacks(hideControls)
    }

    private fun showControls(autoHide: Boolean) {
        handler.removeCallbacks(hideControls)
        controls.animate().cancel()
        controls.visibility = View.VISIBLE
        controls.alpha = 1f
        controlsVisible = true
        if (autoHide && videoView.isPlaying) scheduleControlsHide()
    }

    private fun hideControlsNow() {
        handler.removeCallbacks(hideControls)
        hideControls.run()
    }

    private fun scheduleControlsHide() {
        handler.removeCallbacks(hideControls)
        handler.postDelayed(hideControls, CONTROLS_TIMEOUT_MS)
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds.coerceAtLeast(0) / 1_000
        return String.format(Locale.ROOT, "%d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    private fun enterFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    private companion object {
        const val PROGRESS_INTERVAL_MS = 250L
        const val CONTROLS_TIMEOUT_MS = 3_000L
        const val CONTROLS_FADE_MS = 180L
    }
}
