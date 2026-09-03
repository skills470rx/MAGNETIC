package com.magneticlab.v1

import android.os.Bundle
import android.provider.Settings
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF050912)
private val Panel = Color(0xFF0B1422)
private val Purple = Color(0xFF9B6CFF)
private val Cyan = Color(0xFF35C8FF)
private val White = Color(0xFFF4F7FB)
private val Muted = Color(0xFF8B9AAF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MagneticApp() }
    }
}

@Composable
private fun MagneticApp() {
    var url by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Ready") }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.35f
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("MAGNETIC", color = Purple, fontSize = 26.sp)
            Text("LOCAL ANDROID CONTROL", color = Muted, fontSize = 10.sp)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Command", color = White, fontSize = 22.sp)
                    Text("Use a configured URL or open an Android system page.", color = Muted, fontSize = 12.sp)

                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("URL") },
                        singleLine = true
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { result = "URL saved locally for this session" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Purple)
                        ) { Text("SAVE") }

                        Button(
                            onClick = { openWifiSettings() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan)
                        ) { Text("WI-FI") }
                    }

                    Text(result, color = Muted, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("APP READY", color = Cyan, fontSize = 11.sp)
        }
    }
}

private fun ComponentActivity.openWifiSettings() {
    runCatching {
        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }
}
