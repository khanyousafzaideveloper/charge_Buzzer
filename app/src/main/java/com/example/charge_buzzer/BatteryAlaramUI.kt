package com.example.charge_buzzer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// UI Components
@Composable
fun BatteryAlarmApp(viewModel: BatteryViewModel) {
    var inputText by remember { mutableStateOf(viewModel.targetChargeLevel.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Battery Charge Alarm",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Current Battery Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.isCharging) {
                    Color.Green.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Battery Level",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${viewModel.currentBatteryLevel}%",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (viewModel.isCharging) "⚡ Charging" else "🔋 Not Charging",
                    fontSize = 14.sp,
                    color = if (viewModel.isCharging) Color.Green else Color.Gray
                )
            }
        }

        // Target Level Input
        OutlinedTextField(
            value = inputText,
            onValueChange = { newValue ->
                // Allow empty string or valid numbers 1-100
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    val parsed = newValue.toIntOrNull()
                    if (newValue.isEmpty() || (parsed != null && parsed in 1..100)) {
                        inputText = newValue
                        if (newValue.isNotEmpty()) {
                            viewModel.setTargetChargeLevel(newValue)
                        }
                    }
                }
            },
            label = { Text("Target Charge Level (1-100)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true,
            placeholder = { Text("Enter target level") }
        )

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.startMonitoring() },
                enabled = !viewModel.isMonitoring &&
                        viewModel.targetChargeLevel in 1..100 &&
                        inputText.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Start monitoring",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Monitoring")
            }

            Button(
                onClick = { viewModel.stopMonitoring() },
                enabled = viewModel.isMonitoring,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Stop monitoring",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop")
            }
        }

        // Status Text
        if (viewModel.isMonitoring) {
            Text(
                text = "🔍 Monitoring for ${viewModel.targetChargeLevel}% charge level...",
                color = Color.Green,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )

            if (viewModel.isCharging) {
                Text(
                    text = "Device is charging - alarm will trigger at target level",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            } else {
                Text(
                    text = "Connect charger to start monitoring",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How to use:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "1. Set your target charge level (1-100%)\n" +
                            "2. Tap 'Start Monitoring'\n" +
                            "3. Connect your charger\n" +
                            "4. Alarm will sound when target is reached",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    // Alarm Dialog
    if (viewModel.showAlarm) {
        AlarmDialog(
            targetLevel = viewModel.targetChargeLevel,
            currentLevel = viewModel.currentBatteryLevel,
            onDismiss = { viewModel.stopAlarm() }
        )
    }
}

@Composable
fun AlarmDialog(
    targetLevel: Int,
    currentLevel: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🔋 Battery Charged!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your battery has reached the target level!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontSize = 16.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Target",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$targetLevel%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Current",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currentLevel%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Green
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Turn Off Alarm")
            }
        }
    )
}
