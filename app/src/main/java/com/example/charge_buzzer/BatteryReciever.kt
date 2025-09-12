package com.example.charge_buzzer


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

import android.os.Build

class ChargingAutoStartReceiver(private val viewModel: BatteryViewModel) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("battery_alarm_prefs", Context.MODE_PRIVATE)
        val autoStartEnabled = prefs.getBoolean("auto_start", true)
        val targetLevel = prefs.getInt("target_level", 80)

        if (!autoStartEnabled) return

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                // Start monitoring service when charger connected
                val serviceIntent = Intent(context, BatteryMonitoringService::class.java)
                serviceIntent.putExtra("target_level", targetLevel)
                serviceIntent.putExtra("auto_start", true)
                serviceIntent.putExtra("system_start", true)

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    // Handle potential restrictions
                }
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                // Stop monitoring service when charger disconnected
                val serviceIntent = Intent(context, BatteryMonitoringService::class.java)
                context.stopService(serviceIntent)
            }

            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                val batteryPct = if (level != -1 && scale != -1) {
                    (level * 100 / scale)
                } else {
                    0
                }

                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                viewModel.updateBatteryStatus(batteryPct, isCharging)
            }
        }
    }
}
