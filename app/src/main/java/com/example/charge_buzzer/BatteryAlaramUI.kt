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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun BatteryAlarmApp(
    viewModel: BatteryViewModel,
    onPickAlarmSound: () -> Unit
) {
    var inputText by remember { mutableStateOf(viewModel.targetChargeLevel.toString()) }
    var whatsappText by remember { mutableStateOf(viewModel.whatsappNumber) }
    var customMessageText by remember { mutableStateOf(viewModel.customMessage) }
    var showWhatsappSettings by remember { mutableStateOf(false) }
    var showAlarmSettings by remember { mutableStateOf(false) }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Title
        Text(
            text = "Smart Battery Alarm",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Settings Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // WhatsApp Settings Button
            Button(
                onClick = { showWhatsappSettings = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.whatsappEnabled) Color(0xFF25D366) else MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = "WhatsApp Settings",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("WhatsApp", fontSize = 12.sp)
            }

            // Alarm Settings Button
            Button(
                onClick = { onPickAlarmSound() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Alarm Settings",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Alarm", fontSize = 12.sp)
            }
        }

        // Auto-Start Toggle Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.autoStartEnabled)
                    Color.Blue.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔌 Auto-Start Monitoring",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (viewModel.autoStartEnabled)
                            "Starts when charging begins"
                        else
                            "Manual start only",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = viewModel.autoStartEnabled,
                    onCheckedChange = { viewModel.toggleAutoStart() }
                )
            }
        }

        // Current Battery Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    viewModel.showAlarm -> Color.Red.copy(alpha = 0.2f)
                    viewModel.isCharging -> Color.Green.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
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
                    color = when {
                        viewModel.showAlarm -> Color.Red
                        viewModel.isCharging -> Color.Green
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (viewModel.isCharging) "⚡ Charging" else "🔋 Not Charging",
                        fontSize = 14.sp,
                        color = if (viewModel.isCharging) Color.Green else Color.Gray
                    )
                    if (viewModel.isMonitoring) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• Monitoring",
                            fontSize = 12.sp,
                            color = Color.Blue
                        )
                    }
                }
            }
        }

        // Target Level Input
        OutlinedTextField(
            value = inputText,
            onValueChange = { newValue ->
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
            placeholder = { Text("Enter target level") },
            trailingIcon = {
                Text(
                    text = "%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                    Icons.Default.PlayArrow,
                    contentDescription = "Start monitoring",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Manual")
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
                    Icons.Default.Close,
                    contentDescription = "Stop monitoring",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop")
            }
        }

        // Status Messages
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isMonitoring) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Green.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔍 Monitoring for ${viewModel.targetChargeLevel}% charge level...",
                        color = Color.Green,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (viewModel.whatsappEnabled && viewModel.whatsappNumber.isNotEmpty()) {
                        Text(
                            text = "📱 WhatsApp message will be sent to ${viewModel.whatsappNumber}",
                            color = Color(0xFF25D366),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                }
            }
        } else if (viewModel.autoStartEnabled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Blue.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "⚡ Auto-start enabled - Will monitor when charging begins",
                    color = Color.Blue,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        }
    }

    // WhatsApp Settings Dialog
    if (showWhatsappSettings) {
        AlertDialog(
            onDismissRequest = {
                showWhatsappSettings = false
                whatsappText = viewModel.whatsappNumber
                customMessageText = viewModel.customMessage
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MailOutline,
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp Settings")
                }
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable WhatsApp Message")
                        Switch(
                            checked = viewModel.whatsappEnabled,
                            onCheckedChange = { viewModel.toggleWhatsapp() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = whatsappText,
                        onValueChange = { whatsappText = it },
                        label = { Text("WhatsApp Number") },
                        placeholder = { Text("+1234567890") },
                        enabled = viewModel.whatsappEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = customMessageText,
                        onValueChange = { customMessageText = it },
                        label = { Text("Custom Message") },
                        placeholder = { Text("Use [LEVEL] for battery percentage") },
                        enabled = viewModel.whatsappEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    if (viewModel.whatsappEnabled && viewModel.whatsappNumber.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.testWhatsApp() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366)
                            )
                        ) {
                            Text("Test WhatsApp Message")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateWhatsappNumber(whatsappText)
                        viewModel.updateCustomMessage(customMessageText)
                        showWhatsappSettings = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showWhatsappSettings = false
                        whatsappText = viewModel.whatsappNumber
                        customMessageText = viewModel.customMessage
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}