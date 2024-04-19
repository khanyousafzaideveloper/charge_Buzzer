package com.example.charge_buzzer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.BatteryManager
import android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY
import android.os.IBinder
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt


@SuppressLint("RememberReturnType")
@Preview
@Composable
fun BatterySetter() {
    val chargingReceiver = ChargingReceiver()
    val context: Context = LocalContext.current
    val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
    var sliderPosition by remember { mutableStateOf(0f) }

    val baterryPercentage by remember {
        mutableStateOf(batteryManager.getIntProperty(BATTERY_PROPERTY_CAPACITY))
    }
    val batteryLevel = batteryManager.getIntProperty(BATTERY_PROPERTY_CAPACITY)

    // Initialize musicService as MutableLiveData
    val musicService = remember { mutableStateOf<MusicService?>(null) }
    // Service connection to bind the service
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MusicService.LocalBinder
                musicService.value = binder.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                musicService.value = null
            }
        }
    }

    // Effect to bind and unbind the service when the composable is active
    DisposableEffect(Unit) {
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        onDispose {
            // Unbind the service when the composable is disposed
            context.unbindService(serviceConnection)
        }
    }
    DisposableEffect(Unit) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)

            //addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        if(baterryPercentage>73) {
            context.registerReceiver(chargingReceiver, filter)
        }
        onDispose { 
            context.unregisterReceiver(chargingReceiver)
        }
    }




    Column (modifier = Modifier.padding(8.dp)){
        Text(text = baterryPercentage.toString())


        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            modifier = Modifier.padding(16.dp)
        )
        Text(text = (sliderPosition.toDouble()*100).roundToInt().toString())

        Button(
            onClick = {
                musicService.value?.playMusic()

            }
        ) {
            Text(text = "Start Service")
        }

        Button(
            onClick = {
                musicService.value?.stopMusic()
            }
        ) {
            Text(text = "Stop Service")
        }
    }
}
