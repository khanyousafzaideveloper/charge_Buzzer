package com.example.charge_buzzer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
    private var startLevel = 0
    private var isAutoStart = false
    private var isMonitoring = false
    private lateinit var alarmManager: AlarmManager
    private lateinit var batteryReceiver: ServiceBatteryReceiver

    companion object {
        const val CHANNEL_ID = "battery_monitoring_channel"
        const val ALARM_CHANNEL_ID = "battery_alarm_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP_ALARM = "com.example.batteryalarm.STOP_ALARM"
        const val ACTION_CLOSE_ALARM = "com.example.batteryalarm.CLOSE_ALARM"
    }

    override fun onCreate() {
        super.onCreate()
        alarmManager = AlarmManager(this)
        batteryReceiver = ServiceBatteryReceiver()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ALARM, ACTION_CLOSE_ALARM -> {
                alarmManager.stopAlarm()
                if (intent.action == ACTION_CLOSE_ALARM) {
                    // Close alarm from UI, stop service completely
                    stopSelf()
                } else {
                    // Just stop alarm, continue monitoring if auto-start
                    if (isAutoStart) {
                        updateNotification("Alarm stopped - Still monitoring", false)
                    } else {
                        stopSelf()
                    }
                }
                return START_NOT_STICKY
            }
            else -> {
                targetLevel = intent?.getIntExtra("target_level", 80) ?: 80
                isAutoStart = intent?.getBooleanExtra("auto_start", false) ?: false
                startLevel = intent?.getIntExtra("start_level", 0) ?: 0
                startMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        isMonitoring = true

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)

        val notificationText = if (isAutoStart) {
            "Auto-monitoring: ${startLevel}% → ${targetLevel}%"
        } else {
            "Monitoring for ${targetLevel}% charge level"
        }

        val notification = createNotification(notificationText, false)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Monitoring channel (low priority)
            val monitoringChannel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows battery monitoring status"
                setSound(null, null)
            }

            // Alarm channel (high priority)
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Battery Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Battery charge alarm notifications"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(monitoringChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    private fun createNotification(text: String, isAlarm: Boolean): Notification {
        val channelId = if (isAlarm) ALARM_CHANNEL_ID else CHANNEL_ID

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(if (isAlarm) "🔋 Battery Charged!" else "Battery Monitor")
            .setContentText(text)
            .setSmallIcon(if (isAlarm) android.R.drawable.ic_lock_power_off else android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(!isAlarm)
            .setPriority(if (isAlarm) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)

        if (isAlarm) {
            // Add both stop and close actions for alarm
            val stopIntent = Intent(this, BatteryMonitoringService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            val stopPendingIntent = PendingIntent.getService(
                this, 1, stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val closeIntent = Intent(this, BatteryMonitoringService::class.java).apply {
                action = ACTION_CLOSE_ALARM
            }
            val closePendingIntent = PendingIntent.getService(
                this, 2, closeIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            builder.addAction(android.R.drawable.ic_media_pause, "Stop Alarm", stopPendingIntent)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", closePendingIntent)
            builder.setAutoCancel(true)
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
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

                    val statusText = if (isCharging) {
                        if (isAutoStart) {
                            "Auto: ${batteryPct}% (Target: ${targetLevel}%)"
                        } else {
                            "Charging: ${batteryPct}% → ${targetLevel}%"
                        }
                    } else {
                        "Not charging: ${batteryPct}% - Will stop monitoring"
                    }
                    updateNotification(statusText, false)

                    if (isCharging && batteryPct >= targetLevel) {
                        triggerAlarm(batteryPct)
                    } else if (!isCharging && isAutoStart) {
                        // Auto-stop when unplugged
                        stopSelf()
                    }
                }

                Intent.ACTION_POWER_DISCONNECTED -> {
                    if (isAutoStart) {
                        stopSelf()
                    }
                }
            }
        }
    }

    private fun triggerAlarm(currentLevel: Int) {
        isMonitoring = false
        alarmManager.startAlarm()

        val alarmText = "Target ${targetLevel}% reached! Current: ${currentLevel}%"
        updateNotification(alarmText, true)
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