package com.example.charge_buzzer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

class ChargingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
        val serviceIntent = Intent(context, MusicService::class.java)
        if (action == Intent.ACTION_POWER_CONNECTED) {
            context.startService(serviceIntent)

        } else if (action == Intent.ACTION_POWER_DISCONNECTED) {
            context.stopService(serviceIntent)
        }
    }
}
