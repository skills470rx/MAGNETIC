@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class
)

package com.magneticlab.v1

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

private val Bg = Color(0xFF050810)
private val SurfaceDark = Color(0xFF101622)
private val SurfaceSoft = Color(0xFF151E2D)
private val Purple = Color(0xFF8667FF)
private val Cyan = Color(0xFF16B8E6)
private val Green = Color(0xFF61E6A0)
private val Muted = Color(0xFF8995A8)
private val White = Color(0xFFF4F7FB)

private enum class Tab(val label: String) { HOME("Home"), COMMAND("Command"), AUTOMATIONS("Automations"), LAB("Lab") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MagneticWorkspace() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MagneticWorkspace() {
    var tab by remember { mutableStateOf(Tab.HOME) }
    var showResult by remember { mutableStateOf(false) }
    var command by remember { mutableStateOf("") }
    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (showResult) "Command Result" else if (tab == Tab.HOME) "MAGNETIC" else tab.label, color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        if (tab == Tab.HOME && !showResult) Text("Local Android Command & Automation", color = Muted, fontSize = 11.sp)
                    }
                },
                navigationIcon = { if (showResult) IconButton(onClick = { showResult = false }) { Icon(Icons.Default.ArrowBack, "Back", tint = White) } },
                actions = { IconButton(onClick = {}) { Icon(if (tab == Tab.HOME) Icons.Default.Settings else Icons.Default.MoreVert, "Options", tint = White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0B101B)) {
                listOf(Tab.HOME to Icons.Default.Home, Tab.COMMAND to Icons.Default.Workspaces, Tab.AUTOMATIONS to Icons.Default.Sync, Tab.LAB to Icons.Default.Science).forEach { (item, icon) ->
                    NavigationBarItem(selected = tab == item, onClick = { tab = item; showResult = false }, icon = { Icon(icon, item.label) }, label = { Text(item.label, fontSize = 10.sp) })
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (showResult) CommandResult(command) else when (tab) {
                Tab.HOME -> HomeWorkspace { tab = it }
                Tab.COMMAND -> CommandWorkspace(command, { command = it }, { showResult = true })
                Tab.AUTOMATIONS -> AutomationWorkspace()
                Tab.LAB -> MagneticLabScreen()
            }
        }
    }
}

@Composable
private fun HomeWorkspace(open: (Tab) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Spacer(Modifier.height(4.dp))
        Text("LOCAL CONTROL CENTER", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
        Text("Command your device.\nLocally.", color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
        QuickActions()
        SectionHeader("Automation", "See all")
        AutomationRow(Icons.Default.BatteryAlert, "When battery < 20%", "Enable Battery Saver", Green, true)
        AutomationRow(Icons.Default.Bluetooth, "When headphones connected", "Open Spotify", Green, true)
        AutomationRow(Icons.Default.DarkMode, "At 23:00", "Enable Do Not Disturb", Purple, true)
        SectionHeader("Recent Commands", "See all")
        RecentCommand("Turn flashlight on", "Today, 09:30")
        RecentCommand("Hello", "Today, 09:29")
        RecentCommand("Open YouTube", "Today, 09:28")
        Text("Safe by default · Runs locally · No cloud account", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun QuickActions() {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Quick Actions", color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                QuickAction(Icons.Default.Wifi, "Wi-Fi", "On", Cyan); QuickAction(Icons.Default.Bluetooth, "Bluetooth", "On", Cyan); QuickAction(Icons.Default.FlashOn, "Flashlight", "Off", Muted); QuickAction(Icons.Default.BrightnessHigh, "Brightness", "Auto", Purple)
            }
        }
    }
}

@Composable
private fun RowScope.QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, name: String, state: String, tint: Color) {
    Column(Modifier.weight(1f).background(SurfaceSoft, RoundedCornerShape(10.dp)).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, name, tint = tint, modifier = Modifier.size(20.dp)); Spacer(Modifier.height(5.dp)); Text(name, color = White, fontSize = 10.sp); Text(state, color = tint, fontSize = 9.sp) }
}

@Composable
private fun SectionHeader(title: String, action: String) { Row(Modifier.fillMaxWidth().padding(top = 3.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(title, color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold); TextButton(onClick = {}) { Text(action, color = Cyan, fontSize = 11.sp) } } }

@Composable
private fun AutomationRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, action: String, tint: Color, enabled: Boolean) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(11.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(31.dp).background(tint.copy(alpha = .14f), RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) { Icon(icon, title, tint = tint, modifier = Modifier.size(18.dp)) }; Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(title, color = White, fontSize = 12.sp); Text(action, color = Muted, fontSize = 10.sp) }; StatusPill(if (enabled) "ON" else "OFF", if (enabled) Green else Muted) } } }

@Composable
private fun RecentCommand(command: String, time: String) { Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.PlayArrow, "Command", tint = Muted, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(8.dp)); Column(Modifier.weight(1f)) { Text(command, color = White, fontSize = 12.sp); Text(time, color = Muted, fontSize = 10.sp) }; Icon(Icons.Default.Check, "Completed", tint = Green, modifier = Modifier.size(17.dp)) } }

@Composable
private fun StatusPill(text: String, color: Color) { Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(color.copy(alpha = .12f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 5.dp)) }

@Composable
private fun CommandWorkspace(command: String, onCommandChange: (String) -> Unit, run: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Spacer(Modifier.height(4.dp)); Text("LOCAL COMMAND", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Card(Modifier.fillMaxWidth().height(192.dp), shape = RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F18)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF303A4D))) {
            OutlinedTextField(value = command, onValueChange = onCommandChange, modifier = Modifier.fillMaxSize(), placeholder = { Text(">  Type your command...", color = Muted, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) }, textStyle = androidx.compose.ui.text.TextStyle(color = White, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace), colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent, cursorColor = Cyan))
        }
        Text("Examples", color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Turn flashlight on", "Show battery level", "Vibrate for 2 seconds", "Open Wi-Fi settings").forEach { example -> FilterChip(selected = false, onClick = { onCommandChange(example) }, label = { Text(example, fontSize = 11.sp) }) } }
        Spacer(Modifier.weight(1f))
        Button(onClick = run, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Cyan)) { Icon(Icons.Default.PlayArrow, "Run"); Spacer(Modifier.width(7.dp)); Text("RUN LOCAL", fontWeight = FontWeight.Bold) }
        Text("Commands are simulated locally in this workspace. No system changes are performed without an explicit Android API integration.", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(bottom = 10.dp))
    }
}

@Composable
private fun CommandResult(command: String) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Spacer(Modifier.height(5.dp)); Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) { TerminalLine("> ${if (command.isBlank()) "hello" else command}", White); TerminalLine(if (command.contains("battery", true)) "Battery Level: 87%" else "Hello World!  👋", Green); TerminalLine("Running...", Muted); TerminalLine("✓ Command completed locally", Green); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("All commands executed", color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text("1 / 1", color = Green, fontSize = 12.sp) } } }
        SectionHeader("Command History", "Clear"); RecentCommand(if (command.isBlank()) "hello" else command, "Just now"); RecentCommand("device.getBatteryLevel()", "09:30:14"); RecentCommand("Turn flashlight on", "09:30:13")
    }
}

@Composable private fun TerminalLine(text: String, color: Color) { Text(text, color = color, fontSize = 13.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) }

@Composable
private fun AutomationWorkspace() {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("YOUR AUTOMATIONS", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp); IconButton(onClick = {}) { Icon(Icons.Default.Add, "Add", tint = Cyan) } }
        Text("Create local routines that respond to device state.", color = Muted, fontSize = 13.sp)
        AutomationRow(Icons.Default.BatteryAlert, "Battery Saver (Low Battery)", "Battery level · Less than 20%", Green, true)
        AutomationRow(Icons.Default.Sync, "Night mode", "Time · Every day at 23:00", Purple, true)
        AutomationRow(Icons.Default.Bluetooth, "Headphones routine", "Bluetooth · Device connected", Cyan, false)
        Spacer(Modifier.height(5.dp)); Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("SAFE BY DEFAULT", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp); Text("Automations run only on this device. Permissions are requested only when a selected Android action needs them.", color = Muted, fontSize = 12.sp) } }
    }
}

@Composable
private fun MagneticLabScreen() {
    var strength by remember { mutableFloatStateOf(.28f) }; var radius by remember { mutableFloatStateOf(150f) }; var target by remember { mutableStateOf(Offset.Unspecified) }; val raw = remember { mutableStateListOf<Offset>() }; val processed = remember { mutableStateListOf<Offset>() }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("EXPERIMENTAL SANDBOX", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Text("Magnetic Lab", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text("Try commands and visual experiments in a safe local environment.", color = Muted, fontSize = 12.sp)
        Canvas(Modifier.fillMaxWidth().height(275.dp).background(Color(0xFF080D17), RoundedCornerShape(15.dp)).pointerInteropFilter { event -> val p = Offset(event.x, event.y); val actual = if (target != Offset.Unspecified) target else centerFallback(event.x, event.y).also { target = it }; when (event.actionMasked) { MotionEvent.ACTION_DOWN -> { raw.clear(); processed.clear(); raw.add(p); processed.add(magneticPoint(p, actual, radius, strength, null)); true }; MotionEvent.ACTION_MOVE -> { raw.add(p); processed.add(magneticPoint(p, actual, radius, strength, processed.lastOrNull())); true }; else -> true } }) { val t = if (target != Offset.Unspecified) target else center; for (i in 1..3) drawCircle(Cyan.copy(alpha = .12f), radius * i / 3f, t, style = Stroke(1.4f)); drawPathLine(raw, Color(0xFFFF667A)); drawPathLine(processed, Green); drawCircle(Purple, 15f, t); drawCircle(White, 5f, t) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Legend("RAW INPUT", Color(0xFFFF667A)); Legend("PROCESSED", Green); Legend("FIELD", Cyan) }
        Slider(value = strength, onValueChange = { strength = it }, valueRange = .02f.. .75f); Text("Strength ${"%.2f".format(strength)} · Radius ${radius.toInt()} dp", color = Muted, fontSize = 11.sp)
        Button(onClick = { raw.clear(); processed.clear(); target = Offset.Unspecified }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = SurfaceSoft)) { Text("RESET EXPERIMENT") }
    }
}

private fun centerFallback(x: Float, y: Float) = Offset(x.coerceAtLeast(100f), y.coerceAtLeast(100f))
private fun magneticPoint(raw: Offset, target: Offset, radius: Float, strength: Float, previous: Offset?): Offset { val dx = target.x - raw.x; val dy = target.y - raw.y; val d = sqrt(dx * dx + dy * dy); val f = if (d > .001f && d < radius) (1 - d / radius) * strength else 0f; val next = Offset(raw.x + dx * f, raw.y + dy * f); return previous?.let { Offset(it.x * .15f + next.x * .85f, it.y * .15f + next.y * .85f) } ?: next }
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPathLine(points: List<Offset>, color: Color) { if (points.size < 2) return; val path = Path().apply { moveTo(points.first().x, points.first().y); points.drop(1).forEach { lineTo(it.x, it.y) } }; drawPath(path, color, style = Stroke(4f, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
@Composable private fun Legend(label: String, color: Color) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).background(color, RoundedCornerShape(4.dp))); Spacer(Modifier.width(5.dp)); Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
