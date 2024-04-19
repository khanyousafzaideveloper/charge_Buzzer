package com.example.charge_buzzer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class buzzerViewModel : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    var musicService: MusicService? = null
//    fun startService(){
//        viewModelScope.launch {
//            musicService?.onStartCommand()
//        }
//    }
    fun stopService(){
        viewModelScope.launch {
            musicService?.playMusic()
        }
    }
}