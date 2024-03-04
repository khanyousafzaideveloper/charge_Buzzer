package com.example.charge_buzzer

import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.os.BatteryManager
import android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun BatterySetter(){
    val context : Context = LocalContext.current
    val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
    val batteryLevel = batteryManager.getIntProperty(BATTERY_PROPERTY_CAPACITY)

    Column {
        val musicService = remember { MusicService() }

        Text(text = batteryLevel.toString())
        Button(onClick = { musicService.playMusic() }) {
            Text(text = "Start Service")
        }
        Button(onClick = { musicService.stopMusic() }) {
            Text(text = "Stop Service")
        }
    }
}