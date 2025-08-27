package com.example.charge_buzzer

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class BatteryViewModel(private val context: Context) : ViewModel() {
    var targetChargeLevel by mutableStateOf(80)
        private set

    var currentBatteryLevel by mutableStateOf(0)
        private set

    var isCharging by mutableStateOf(false)
        private set

    var isMonitoring by mutableStateOf(false)
        private set

    var showAlarm by mutableStateOf(false)
        private set

    private val alarmManager = AlarmManager(context)

    fun setTargetChargeLevel(level: String) {
        val parsed = level.toIntOrNull()
        if (parsed != null && parsed in 1..100) {
            targetChargeLevel = parsed
        }
    }



    fun updateBatteryStatus(level: Int, charging: Boolean) {
        currentBatteryLevel = level
        isCharging = charging

        if (isMonitoring && charging && level >= targetChargeLevel) {
            triggerAlarm()
        }
    }

    private fun triggerAlarm() {
        showAlarm = true
        alarmManager.startAlarm()
        isMonitoring = false // Stop monitoring once target is reached
    }

    fun stopAlarm() {
        showAlarm = false
        alarmManager.stopAlarm()
    }

    fun startMonitoring() {
        if (targetChargeLevel in 1..100) {
            isMonitoring = true

            // Start background service
            val serviceIntent = Intent(context, BatteryMonitoringService::class.java)
            serviceIntent.putExtra("target_level", targetChargeLevel)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
        stopAlarm()

        // Stop background service
        val serviceIntent = Intent(context, BatteryMonitoringService::class.java)
        context.stopService(serviceIntent)
    }

}