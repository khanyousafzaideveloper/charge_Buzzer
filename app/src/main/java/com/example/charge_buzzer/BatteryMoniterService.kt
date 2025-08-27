package com.example.charge_buzzer

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat


class BatteryMonitoringService : Service() {
    private var targetLevel = 80
    private var isMonitoring = false
    private lateinit var alarmManager: AlarmManager
    private lateinit var batteryReceiver: ServiceBatteryReceiver

    companion object {
        const val CHANNEL_ID = "battery_monitoring_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP_ALARM = "com.example.batteryalarm.STOP_ALARM"
    }

    override fun onCreate() {
        super.onCreate()
        alarmManager = AlarmManager(this)
        batteryReceiver = ServiceBatteryReceiver()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ALARM -> {
                alarmManager.stopAlarm()
                updateNotification("Monitoring stopped", false)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                targetLevel = intent?.getIntExtra("target_level", 80) ?: 80
                startMonitoring()
            }
        }
        return START_STICKY // Restart if killed
    }

    private fun startMonitoring() {
        isMonitoring = true

        // Register battery receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)

        // Start foreground service with notification
        val notification = createNotification(
            "Monitoring for ${targetLevel}% charge level",
            false
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows battery monitoring status"
                setSound(null, null) // Silent for monitoring notification
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String, isAlarm: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Battery Alarm")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_power_off)
            .setContentIntent(pendingIntent)
            .setOngoing(!isAlarm)
            .setPriority(if (isAlarm) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)

        if (isAlarm) {
            // Add stop alarm action
            val stopIntent = Intent(this, BatteryMonitoringService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            val stopPendingIntent = PendingIntent.getService(
                this, 1, stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            builder.addAction(
                android.R.drawable.ic_media_pause,
                "Stop Alarm",
                stopPendingIntent
            )
            builder.setAutoCancel(true)
        }

        return builder.build()
    }

    private fun updateNotification(text: String, isAlarm: Boolean) {
        val notification = createNotification(text, isAlarm)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    inner class ServiceBatteryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isMonitoring) return

            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                    val batteryPct = if (level != -1 && scale != -1) {
                        (level * 100 / scale.toFloat()).toInt()
                    } else 0

                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    // Update notification with current status
                    val statusText = if (isCharging) {
                        "Charging: ${batteryPct}% (Target: ${targetLevel}%)"
                    } else {
                        "Not charging: ${batteryPct}% - Connect charger"
                    }
                    updateNotification(statusText, false)

                    // Check if target reached
                    if (isCharging && batteryPct >= targetLevel) {
                        triggerAlarm(batteryPct)
                    }
                }
            }
        }
    }

    private fun triggerAlarm(currentLevel: Int) {
        isMonitoring = false
        alarmManager.startAlarm()

        updateNotification(
            "🔋 Battery charged to ${currentLevel}%! Tap to stop alarm.",
            true
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
        alarmManager.stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}