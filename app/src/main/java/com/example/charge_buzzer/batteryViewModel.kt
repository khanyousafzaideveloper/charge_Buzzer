package com.example.charge_buzzer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException


data class AlarmType(
    val name: String,
    val value: String
)

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

    var autoStartEnabled by mutableStateOf(true)
        private set

    var chargingStartLevel by mutableStateOf(0)
        private set

    var whatsappNumber by mutableStateOf("")
        private set

    var whatsappEnabled by mutableStateOf(false)
        private set

    var customMessage by mutableStateOf("🔋 Battery charged to [LEVEL]%! Please unplug the charger.")
        private set

    var selectedAlarmType by mutableStateOf("default")
        private set

    var customAlarmUri by mutableStateOf("")
        private set

    val availableAlarmTypes = listOf(
        AlarmType("Default System Alarm", "default"),
        AlarmType("System Notification", "notification"),
        AlarmType("System Ringtone", "ringtone"),
        AlarmType("Custom Sound", "custom")
    )

    private val alarmManager = AlarmManager(context)

    fun setTargetChargeLevel(level: String) {
        val parsed = level.toIntOrNull()
        if (parsed != null && parsed in 1..100) {
            targetChargeLevel = parsed
            saveTargetLevel(parsed)
        }
    }

    fun updateWhatsappNumber(number: String) {
        // Remove non-digits and format
        val cleanNumber = number.filter { it.isDigit() || it == '+' }
        whatsappNumber = cleanNumber
        saveWhatsappSettings()
    }

    fun toggleWhatsapp() {
        whatsappEnabled = !whatsappEnabled
        saveWhatsappSettings()
    }

    fun updateCustomMessage(message: String) {
        customMessage = message
        saveWhatsappSettings()
    }

    fun setAlarmType(type: String) {
        selectedAlarmType = type
        saveAlarmSettings()
    }

    fun setCustomAlarmSound(uri: String) {
        customAlarmUri = uri
        selectedAlarmType = "custom"
        saveAlarmSettings()
    }

    fun toggleAutoStart() {
        autoStartEnabled = !autoStartEnabled
        saveAutoStartSetting(autoStartEnabled)

        if (!autoStartEnabled && isMonitoring) {
            stopMonitoring()
        }
    }

    fun startMonitoring() {
        if (targetChargeLevel in 1..100) {
            isMonitoring = true

            val serviceIntent = Intent(context, BatteryMonitoringService::class.java)
            serviceIntent.putExtra("target_level", targetChargeLevel)
            serviceIntent.putExtra("auto_start", false)
            serviceIntent.putExtra("whatsapp_number", whatsappNumber)
            serviceIntent.putExtra("whatsapp_enabled", whatsappEnabled)
            serviceIntent.putExtra("custom_message", customMessage)
            serviceIntent.putExtra("alarm_type", selectedAlarmType)
            serviceIntent.putExtra("custom_alarm_uri", customAlarmUri)

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

        val serviceIntent = Intent(context, BatteryMonitoringService::class.java)
        context.stopService(serviceIntent)
    }

    fun updateBatteryStatus(level: Int, charging: Boolean) {
        val wasCharging = isCharging
        currentBatteryLevel = level
        isCharging = charging

        if (!wasCharging && charging && autoStartEnabled && !isMonitoring) {
            chargingStartLevel = level
            if (level < targetChargeLevel) {
                startAutoMonitoring()
            }
        }

        if (wasCharging && !charging && isMonitoring) {
            stopMonitoring()
        }

        if (isMonitoring && charging && level >= targetChargeLevel) {
            triggerAlarm()
        }
    }

    private fun startAutoMonitoring() {
        isMonitoring = true

        val serviceIntent = Intent(context, BatteryMonitoringService::class.java)
        serviceIntent.putExtra("target_level", targetChargeLevel)
        serviceIntent.putExtra("auto_start", true)
        serviceIntent.putExtra("start_level", chargingStartLevel)
        serviceIntent.putExtra("whatsapp_number", whatsappNumber)
        serviceIntent.putExtra("whatsapp_enabled", whatsappEnabled)
        serviceIntent.putExtra("custom_message", customMessage)
        serviceIntent.putExtra("alarm_type", selectedAlarmType)
        serviceIntent.putExtra("custom_alarm_uri", customAlarmUri)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun triggerAlarm() {
        showAlarm = true
        alarmManager.startAlarm(selectedAlarmType, customAlarmUri)
        isMonitoring = false

        // Send WhatsApp message if enabled
        if (whatsappEnabled && whatsappNumber.isNotEmpty()) {
            sendWhatsAppMessage(
                token = "EAAaIIkhZBC2EBPRcaWjcwRAz8VS61a1uxjwlA5gBjXKc57m0MCzW31qGfMO1rkkTPGM8lQblpkOp1vWeQQ9ZA2jlG6XUk15ivvfUWVmLQa97MhpVzKr08RszyZAx0IHwjhItMZAf1mWnUGRNjlkZBiB9UOoiUvZBXFg7uHJAHWrxCkFZBc8MRdTkZBJwYqe7aixrufZAy4BNtCRpR5eTGP4GtqZBTuSCF1vlcxEVLZAe3owB0ilHwZDZD",
                phoneNumberId = "813935998465997",
                to =  whatsappNumber,
                currentBatteryLevel = currentBatteryLevel,
                customMessage =  customMessage.replace("[LEVEL]", currentBatteryLevel.toString())
            )
        }
    }

//    fun sendWhatsAppMessage() {
//        try {
//            val message = customMessage.replace("[LEVEL]", currentBatteryLevel.toString())
//            val url =
//                "https://api.whatsapp.com/send?phone=$whatsappNumber&text=${Uri.encode(message)}"
//
//            val intent = Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse(url)
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            }
//
//            // Try to open WhatsApp directly first
//            intent.setPackage("com.whatsapp")
//
//            try {
//                context.startActivity(intent)
//            } catch (e: Exception) {
//                // If WhatsApp not installed, try WhatsApp Business
//                intent.setPackage("com.whatsapp.w4b")
//                try {
//                    context.startActivity(intent)
//                } catch (e: Exception) {
//                    // If neither installed, open in browser
//                    intent.setPackage(null)
//                    context.startActivity(intent)
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
private fun sendWhatsAppMessage(
    token: String,
    phoneNumberId: String,
    to: String,
    customMessage: String,
    currentBatteryLevel: Int
) {
    val client = OkHttpClient()
    val TAG = "WhatsAppAPI"

    val message = customMessage.replace("[LEVEL]", currentBatteryLevel.toString())

    val json = """
        {
          "messaging_product": "whatsapp",
          "to": "$to",
          "type": "text",
          "text": { "body": "$message" }
        }
    """.trimIndent()

    Log.d(TAG, "🚀 Sending WhatsApp message...")
    Log.d(TAG, "➡️ To: $to")
    Log.d(TAG, "➡️ PhoneNumberId: $phoneNumberId")
    Log.d(TAG, "➡️ Message: $message")

    val body = json.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://graph.facebook.com/v19.0/$phoneNumberId/messages")
        .post(body)
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            Log.e(TAG, "❌ Request failed: ${e.localizedMessage}", e)
        }

        override fun onResponse(call: okhttp3.Call, response: Response) {
            response.use {
                val respBody = it.body?.string()
                Log.d(TAG, "✅ Response code: ${it.code}")
                Log.d(TAG, "✅ Response body: $respBody")
            }
        }
    })
}
 //   }

    fun testWhatsApp() {
        val testMessage = "🔋 Test message from Battery Alarm app!"
        val url = "https://api.whatsapp.com/send?phone=$whatsappNumber&text=${Uri.encode(testMessage)}"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        intent.setPackage("com.whatsapp")

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            intent.setPackage("com.whatsapp.w4b")
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                intent.setPackage(null)
                context.startActivity(intent)
            }
        }
    }

    fun stopAlarm() {
        showAlarm = false
        alarmManager.stopAlarm()
    }

    private fun saveTargetLevel(level: Int) {
        val prefs = context.getSharedPreferences("battery_alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("target_level", level).apply()
    }

    private fun saveAutoStartSetting(enabled: Boolean) {
        val prefs = context.getSharedPreferences("battery_alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("auto_start", enabled).apply()
    }

    private fun saveWhatsappSettings() {
        val prefs = context.getSharedPreferences("battery_alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("whatsapp_number", whatsappNumber)
            putBoolean("whatsapp_enabled", whatsappEnabled)
            putString("custom_message", customMessage)
        }.apply()
    }

    private fun saveAlarmSettings() {
        val prefs = context.getSharedPreferences("battery_alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("alarm_type", selectedAlarmType)
            putString("custom_alarm_uri", customAlarmUri)
        }.apply()
    }

    init {
        val prefs = context.getSharedPreferences("battery_alarm_prefs", Context.MODE_PRIVATE)
        targetChargeLevel = prefs.getInt("target_level", 80)
        autoStartEnabled = prefs.getBoolean("auto_start", true)
        whatsappNumber = prefs.getString("whatsapp_number", "") ?: ""
        whatsappEnabled = prefs.getBoolean("whatsapp_enabled", false)
        customMessage = prefs.getString("custom_message", "🔋 Battery charged to [LEVEL]%! Please unplug the charger.") ?: "🔋 Battery charged to [LEVEL]%! Please unplug the charger."
        selectedAlarmType = prefs.getString("alarm_type", "default") ?: "default"
        customAlarmUri = prefs.getString("custom_alarm_uri", "") ?: ""
    }

    fun cleanup() {
        alarmManager.stopAlarm()
    }

    override fun onCleared() {
        super.onCleared()
        alarmManager.stopAlarm()
    }
}