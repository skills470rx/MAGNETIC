@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.magneticlab.v1

import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.util.Locale

private val Bg = Color(0xFF050912)
private val Panel = Color(0xFF0B1422)
private val Panel2 = Color(0xFF101C2D)
private val Purple = Color(0xFF9B6CFF)
private val Cyan = Color(0xFF35C8FF)
private val Green = Color(0xFF68E39B)
private val Amber = Color(0xFFFFC766)
private val Red = Color(0xFFFF7087)
private val Muted = Color(0xFF8B9AAF)
private val White = Color(0xFFF4F7FB)

private enum class Room(val title: String) { HOME("Home"), COMMAND("Command"), AUTOMATIONS("Automations"), MONITOR("Monitor"), NETWORK("Network"), BOOST("Game / Boost"), LAB("Signal Lab") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MagneticV21() }
    }
}

@Composable
private fun MagneticV21() {
    var room by remember { mutableStateOf(Room.HOME) }
    val context = LocalContext.current
    var snapshot by remember {
        mutableStateOf(
            DeviceSnapshot(
                battery = 0, ramUsed = 0, ramFreeMb = 0, ramTotalMb = 0,
                storageUsed = 0, storageFreeMb = 0, temperature = "Loading…",
                charging = "Unknown", connection = "Unknown", connected = false,
                wifi = "Loading…", link = "Loading…", bluetooth = "Loading…", cpu = "Loading…"
            )
        )
    }
    LaunchedEffect(Unit) {
        while (true) {
            snapshot = runCatching { readSnapshot(context) }.getOrElse { snapshot }
            kotlinx.coroutines.delay(2000)
        }
    }
    Scaffold(containerColor = Bg, topBar = { Header(room) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            RoomTabs(room) { room = it }
            when (room) {
                Room.HOME -> HomeRoom(snapshot, onOpen = { room = it })
                Room.COMMAND -> CommandRoom()
                Room.AUTOMATIONS -> AutomationsRoom(snapshot)
                Room.MONITOR -> MonitorRoom(snapshot)
                Room.NETWORK -> NetworkRoom(snapshot)
                Room.BOOST -> BoostRoom(snapshot)
                Room.LAB -> SignalLabRoom(snapshot)
            }
        }
    }
}

@Composable private fun Header(room: Room) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text("MAGNETIC", color = Purple, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold); Text("LOCAL ANDROID CONTROL / ${room.title.uppercase(Locale.US)}", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp) }
        Icon(Icons.Default.Settings, "Settings", tint = Muted, modifier = Modifier.size(21.dp))
    }
}

@Composable private fun RoomTabs(selected: Room, onSelect: (Room) -> Unit) {
    ScrollableTabRow(selectedTabIndex = selected.ordinal, containerColor = Bg, contentColor = Purple) {
        Room.values().forEach { room ->
            Tab(selected = selected == room, onClick = { onSelect(room) }, text = { Text(room.title, fontSize = 11.sp, maxLines = 1) })
        }
    }
}

@Composable private fun HomeRoom(s: DeviceSnapshot, onOpen: (Room) -> Unit) {
    ScrollColumn {
        Eyebrow("DEVICE DASHBOARD · LIVE"); Text("See what your device is doing.", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold); Text("Values refresh from Android system services every 2 seconds.", color = Muted, fontSize = 12.sp)
        StatusCard(s); Section("QUICK ACTIONS")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { ActionTile(Icons.Default.Wifi, "Wi-Fi", s.wifi, Cyan) { onOpen(Room.NETWORK) }; ActionTile(Icons.Default.Bluetooth, "Bluetooth", s.bluetooth, Purple) { onOpen(Room.COMMAND) }; ActionTile(Icons.Default.Speed, "Boost", "READY", Green) { onOpen(Room.BOOST) } }
        Section("OPEN A ROOM")
        RoomCard("Monitor", "CPU · RAM · thermal · storage", Icons.Default.GraphicEq, Room.MONITOR, onOpen)
        RoomCard("Network", "Wi-Fi signal · link · latency", Icons.Default.NetworkCheck, Room.NETWORK, onOpen)
        RoomCard("Signal Lab", "Sensors and device signal availability", Icons.Default.Dashboard, Room.LAB, onOpen)
    }
}

@Composable private fun StatusCard(s: DeviceSnapshot) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(15.dp)) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("SYSTEM STATE", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text("LIVE", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold) }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Metric("BATTERY", "${s.battery}%", Green); Metric("RAM", "${s.ramUsed}%", Purple); Metric("STORAGE", "${s.storageUsed}%", Amber); Metric("TEMP", s.temperature, Cyan) } } } }
@Composable private fun Metric(label: String, value: String, tint: Color) { Column { Text(label, color = Muted, fontSize = 9.sp); Text(value, color = tint, fontSize = 16.sp, fontWeight = FontWeight.Bold) } }

@Composable private fun CommandRoom() { val context = LocalContext.current; val scope = rememberCoroutineScope(); var command by remember { mutableStateOf(TextFieldValue()) }; var output by remember { mutableStateOf("Ready. Commands execute through Android intents or documented system APIs.") }; ScrollColumn { Eyebrow("LOCAL EXECUTION"); Text("Command", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text("OPEN → RUN ACTION → VERIFY RESULT", color = Muted, fontSize = 12.sp); OutlinedTextField(value = command, onValueChange = { command = it }, modifier = Modifier.fillMaxWidth().height(145.dp), placeholder = { Text("Type a supported command…", color = Muted) }, leadingIcon = { Text(">", color = Cyan) }); Text("SUPPORTED COMMANDS", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold); val examples = listOf("Open Wi-Fi settings", "Open Bluetooth settings", "Show battery level", "Vibrate for 1 second", "Open display settings"); examples.forEach { example -> Button(onClick = { command = TextFieldValue(example) }, colors = ButtonDefaults.buttonColors(containerColor = Panel2), modifier = Modifier.fillMaxWidth()) { Text(example, color = White, modifier = Modifier.fillMaxWidth()) } }; Button(onClick = { scope.launch { output = executeCommand(context, command.text) } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Icon(Icons.Default.PlayArrow, "Run"); Spacer(Modifier.width(8.dp)); Text("RUN ON DEVICE") }; OutputCard(output) } }
private suspend fun executeCommand(context: Context, command: String): String = withContext(Dispatchers.Main) { val c = command.lowercase(Locale.US); try { when { c.contains("wi-fi") || c.contains("wifi") -> { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)); "✓ Wi-Fi settings opened by Android" }; c.contains("bluetooth") -> { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)); "✓ Bluetooth settings opened by Android" }; c.contains("display") || c.contains("brightness") -> { context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)); "✓ Display settings opened by Android" }; c.contains("battery") -> { "✓ ${readSnapshot(context).battery}% battery reported by BatteryManager" }; c.contains("vibrate") -> { val vibrator = context.getSystemService(Vibrator::class.java); vibrator?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)); "✓ Vibration requested through Vibrator API" }; else -> "Unsupported command. No action was simulated." } } catch (e: SecurityException) { "Permission Required: ${e.message ?: "Android denied this action"}" } catch (e: Exception) { "Action failed: ${e.message ?: "unknown Android error"}" } }
@Composable private fun OutputCard(text: String) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF07101A)), shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(14.dp)) { Text("EXECUTION RESULT", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(7.dp)); Text("> $text", color = if (text.startsWith("✓")) Green else Amber, fontSize = 12.sp) } } }

@Composable private fun AutomationsRoom(s: DeviceSnapshot) { var batteryRoutine by remember { mutableStateOf(false) }; var nightRoutine by remember { mutableStateOf(false) }; var headphoneRoutine by remember { mutableStateOf(false) }; ScrollColumn { Eyebrow("ON-DEVICE ROUTINES"); Text("Automations", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text("Armed state is stored locally. Android restrictions are shown instead of bypassed.", color = Muted, fontSize = 12.sp); Routine("Battery below 20%", "Trigger: battery level · Action: open battery saver settings", batteryRoutine, { batteryRoutine = it }, Icons.Default.BatteryFull); Routine("Night check", "Trigger: time · Action: open display settings", nightRoutine, { nightRoutine = it }, Icons.Default.Settings); Routine("Headphones connected", "Trigger: Bluetooth state · Action: inspect connection", headphoneRoutine, { headphoneRoutine = it }, Icons.Default.Bluetooth); OutputCard(if (batteryRoutine || nightRoutine || headphoneRoutine) "✓ Routine(s) armed locally. Background execution requires Android scheduling permission and is not claimed here." else "No routines armed.") } }
@Composable private fun Routine(title: String, detail: String, enabled: Boolean, onChange: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel)) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, title, tint = Cyan, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(title, color = White, fontSize = 12.sp); Text(detail, color = Muted, fontSize = 10.sp) }; Switch(checked = enabled, onCheckedChange = onChange) } } }
@Composable private fun MonitorRoom(s: DeviceSnapshot) { ScrollColumn { Eyebrow("ANDROID SYSTEM TELEMETRY"); Text("Monitor", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold); Telemetry("CPU", s.cpu, "Process/runtime view; per-core frequency is not exposed to this app", Icons.Default.Memory, Purple); Telemetry("RAM", "${s.ramUsed}% used", "${s.ramFreeMb} MB free of ${s.ramTotalMb} MB", Icons.Default.Memory, Cyan); Telemetry("TEMPERATURE", s.temperature, "Battery temperature from ACTION_BATTERY_CHANGED", Icons.Default.Bolt, Amber); Telemetry("BATTERY", "${s.battery}%", s.charging, Icons.Default.BatteryFull, Green); Telemetry("STORAGE", "${s.storageUsed}% used", "${s.storageFreeMb} MB available", Icons.Default.Storage, Cyan) } }
@Composable private fun Telemetry(label: String, value: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, label, tint = tint, modifier = Modifier.size(25.dp)); Spacer(Modifier.width(12.dp)); Column { Text(label, color = Muted, fontSize = 10.sp); Text(value, color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text(detail, color = Muted, fontSize = 10.sp) } } } }
@Composable private fun NetworkRoom(s: DeviceSnapshot) { val context = LocalContext.current; val scope = rememberCoroutineScope(); var latency by remember { mutableStateOf("Tap test") }; ScrollColumn { Eyebrow("CONNECTIVITY DIAGNOSTICS"); Text("Network", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold); NetworkMetric("CONNECTION", s.connection, Icons.Default.NetworkCheck, if (s.connected) Green else Red); NetworkMetric("WIFI SIGNAL", s.wifi, Icons.Default.SignalWifi4Bar, Cyan); NetworkMetric("LINK", s.link, Icons.Default.Wifi, Purple); NetworkMetric("LATENCY", latency, Icons.Default.Speed, Amber); Button(onClick = { scope.launch { latency = withContext(Dispatchers.IO) { val start = System.currentTimeMillis(); val ok = runCatching { InetAddress.getByName("8.8.8.8").isReachable(1200) }.getOrDefault(false); if (ok) "${System.currentTimeMillis() - start} ms" else "Unreachable" } } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Panel2)) { Icon(Icons.Default.NetworkCheck, "Test"); Spacer(Modifier.width(8.dp)); Text("RUN LATENCY TEST") }; Button(onClick = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Icon(Icons.Default.OpenInNew, "Open"); Spacer(Modifier.width(8.dp)); Text("OPEN WIFI SETTINGS") } } }
@Composable private fun NetworkMetric(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, label, tint = tint, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Column { Text(label, color = Muted, fontSize = 10.sp); Text(value, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold) } } } }
@Composable private fun BoostRoom(s: DeviceSnapshot) { val context = LocalContext.current; ScrollColumn { Eyebrow("GAME-ORIENTED DEVICE VIEW"); Text("Game / Boost", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text("Observe device state and launch only Android-supported controls.", color = Muted, fontSize = 12.sp); StatusCard(s); Telemetry("NETWORK READINESS", if (s.connected) "CONNECTED" else "OFFLINE", "Use Network room for measured diagnostics", Icons.Default.NetworkCheck, Green); Telemetry("THERMAL HEADROOM", s.temperature, "No unsafe thermal override is attempted", Icons.Default.Bolt, Amber); Button(onClick = { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Icon(Icons.Default.OpenInNew, "Settings"); Spacer(Modifier.width(8.dp)); Text("OPEN SYSTEM SETTINGS") }; OutputCard("Unsupported: force GPU mode, RAM clearing, CPU overclocking and thermal bypass are not exposed to ordinary Android apps.") } }
@Composable private fun SignalLabRoom(s: DeviceSnapshot) { val context = LocalContext.current; var refreshed by remember { mutableStateOf(0) }; ScrollColumn { Eyebrow("SIGNAL LAB · REAL DEVICE DATA"); Text("Signal Lab", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text("Inspect signal and sensor availability without synthetic readings.", color = Muted, fontSize = 12.sp); SignalRow("Wi-Fi", s.wifi, Icons.Default.Wifi); SignalRow("Network", s.connection, Icons.Default.NetworkCheck); SignalRow("Bluetooth", s.bluetooth, Icons.Default.Bluetooth); SignalRow("Sensors", sensorSummary(context), Icons.Default.GraphicEq); SignalRow("Battery telemetry", "${s.battery}% · ${s.temperature}", Icons.Default.BatteryFull); Button(onClick = { refreshed++ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Panel2)) { Icon(Icons.Default.Refresh, "Refresh"); Spacer(Modifier.width(8.dp)); Text("REFRESH SIGNALS #$refreshed") }; OutputCard("All values above are read from Android services. Unsupported or permission-gated values are labeled explicitly.") } }
@Composable private fun SignalRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, label, tint = Cyan, modifier = Modifier.size(21.dp)); Spacer(Modifier.width(11.dp)); Text(label, color = White, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(value, color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun RowScope.ActionTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, tint: Color, onClick: () -> Unit) { Card(onClick = onClick, modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Panel2), shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, title, tint = tint, modifier = Modifier.size(22.dp)); Text(title, color = White, fontSize = 10.sp); Text(value, color = tint, fontSize = 9.sp) } } }
@Composable private fun RoomCard(title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, room: Room, onOpen: (Room) -> Unit) { Card(onClick = { onOpen(room) }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel)) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, title, tint = Purple, modifier = Modifier.size(23.dp)); Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(title, color = White, fontSize = 13.sp); Text(detail, color = Muted, fontSize = 10.sp) }; Icon(Icons.Default.OpenInNew, "Open", tint = Muted) } } }
@Composable private fun Eyebrow(text: String) { Text(text, color = Purple, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) }
@Composable private fun Section(text: String) { Text(text, color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp)) }
@Composable private fun ScrollColumn(content: @Composable () -> Unit) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp), content = { content(); Spacer(Modifier.height(12.dp)) }) }
private data class DeviceSnapshot(val battery: Int, val ramUsed: Int, val ramFreeMb: Long, val ramTotalMb: Long, val storageUsed: Int, val storageFreeMb: Long, val temperature: String, val charging: String, val connection: String, val connected: Boolean, val wifi: String, val link: String, val bluetooth: String, val cpu: String)
private fun readSnapshot(context: Context): DeviceSnapshot { val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)); val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1; val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100; val temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE); val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1); val charging = if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) "Charging" else "Not charging"; val am = context.getSystemService(ActivityManager::class.java); val mem = ActivityManager.MemoryInfo(); am.getMemoryInfo(mem); val used = ((mem.totalMem - mem.availMem) * 100 / mem.totalMem).toInt(); val stat = StatFs(android.os.Environment.getDataDirectory().path); val free = stat.availableBytes / 1024 / 1024; val total = stat.totalBytes / 1024 / 1024; val storage = if (total > 0) (((total - free) * 100) / total).toInt() else 0; val cm = context.getSystemService(ConnectivityManager::class.java); val network = cm.activeNetwork; val caps = cm.getNetworkCapabilities(network); val connected = caps != null; val connection = when { caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi connected"; caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile data connected"; connected -> "Connected"; else -> "Offline" }; val wifiText = runCatching { val info = context.getSystemService(WifiManager::class.java).connectionInfo; if (info.rssi == -127) "Permission Required" else "${info.rssi} dBm" }.getOrDefault("Permission Required"); val link = runCatching { "${context.getSystemService(WifiManager::class.java).connectionInfo.linkSpeed} Mbps" }.getOrDefault("Unsupported"); val bt = runCatching { val adapter = context.getSystemService(BluetoothManager::class.java).adapter; if (adapter == null) "Unsupported" else if (adapter.isEnabled) "Enabled" else "Disabled" }.getOrDefault("Permission Required"); return DeviceSnapshot(if (level >= 0) level * 100 / scale else 0, used, mem.availMem / 1024 / 1024, mem.totalMem / 1024 / 1024, storage, free, if (temp == null || temp == Int.MIN_VALUE) "Unsupported" else "${temp / 10.0} °C", charging, connection, connected, wifiText, link, bt, "${Runtime.getRuntime().availableProcessors()} cores visible") }
private fun sensorSummary(context: Context): String { val manager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager; val count = manager.getSensorList(android.hardware.Sensor.TYPE_ALL).size; return if (count > 0) "$count sensors available" else "No sensors reported" }
