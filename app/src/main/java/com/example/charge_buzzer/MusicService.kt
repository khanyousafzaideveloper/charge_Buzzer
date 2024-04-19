package com.example.charge_buzzer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Binder
import android.os.IBinder
import androidx.compose.ui.platform.LocalContext

class MusicService: Service(){

    private var mediaPlayer: MediaPlayer? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        playMusic()
        return START_STICKY
    }
    fun playMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.mixkit) // Replace with your music file
            mediaPlayer?.isLooping = true
        }
        mediaPlayer?.start()
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}