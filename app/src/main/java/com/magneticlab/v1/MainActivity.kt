@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.magneticlab.v1

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.StatFs
import android.view.Display
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val Ink = Color(0xFF090B12)
private val Panel = Color(0xFF121722)
private val PanelRaised = Color(0xFF192131)
private val Blue = Color(0xFF5CC8FF)
private val Purple = Color(0xFFA879FF)
private val Green = Color(0xFF63E6A8)
private val Red = Color(0xFFFF6B7A)
private val Amber = Color(0xFFFFC857)

private enum class Screen(val title: String, val subtitle: String) {
    HOME("MAGNETIC LAB", "Offline device experiment lab"),
    MAGNETIC("MAGNETIC FIELD", "Compare raw and processed touch paths"),
    TOUCH("TOUCH RESPONSE", "Observe input and motion behavior"),
    DISPLAY("DISPLAY MOTION", "Explore motion on the device display"),
    DEVICE("DEVICE EXPLORER", "Publicly exposed device capabilities"),
    SYSTEM("SYSTEM STATE", "Meaningful live information only")
}

private enum class MagneticMode(val label: String) { ATTRACTION("Attraction"), REPULSION("Repulsion"), NEUTRAL("Neutral") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MagneticLabApp() }
    }
}

@Composable
private fun MagneticLabApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    Surface(color = Ink) {
        Scaffold(
            containerColor = Ink,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(screen.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                            Text(screen.subtitle, color = Color(0xFF8D9AAF), fontSize = 11.sp)
                        }
                    },
                    navigationIcon = {
                        if (screen != Screen.HOME) IconButton(onClick = { screen = Screen.HOME }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Ink)
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (screen) {
                    Screen.HOME -> HomeScreen { screen = it }
                    Screen.MAGNETIC -> MagneticScreen()
                    Screen.TOUCH -> TouchScreen()
                    Screen.DISPLAY -> DisplayScreen()
                    Screen.DEVICE -> DeviceScreen()
                    Screen.SYSTEM -> SystemScreen()
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(open: (Screen) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Spacer(Modifier.height(8.dp))
        Text("EXPLORE DEVICE RESPONSE", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text("Observe. Compare. Understand.", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("A local-first laboratory for touch, motion, display response, and device capabilities.", color = Color(0xFF9CA9BB), fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        ExperimentCard("Magnetic Field", "Interactive touch attraction experiment", Icons.Default.Tune, Blue, "LIVE EXPERIMENT") { open(Screen.MAGNETIC) }
        ExperimentCard("Touch Response", "Visualize raw, smooth, and experimental prediction trails", Icons.Default.TouchApp, Purple, "OFFLINE") { open(Screen.TOUCH) }
        ExperimentCard("Display Motion", "Observe moving patterns and accessible refresh information", Icons.Default.DisplaySettings, Green, "OBSERVE") { open(Screen.DISPLAY) }
        ExperimentCard("Device Explorer", "Discover supported and unavailable capabilities", Icons.Default.Explore, Amber, "CAPABILITIES") { open(Screen.DEVICE) }
        ExperimentCard("System State", "View live memory, battery, storage, and display data", Icons.Default.Memory, Red, "LIVE DATA") { open(Screen.SYSTEM) }
        Spacer(Modifier.height(8.dp))
        Text("Magnetic Lab runs locally on your device. Touch experiments and device information are not uploaded.", color = Color(0xFF718096), fontSize = 11.sp, modifier = Modifier.padding(bottom = 16.dp))
    }
}

@Composable
private fun ExperimentCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, status: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = .18f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).background(accent.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, title, tint = accent, modifier = Modifier.size(25.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title.uppercase(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(4.dp))
                Text(description, color = Color(0xFF9CA9BB), fontSize = 12.sp)
            }
            Text(status, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MagneticScreen() {
    var strength by remember { mutableFloatStateOf(.28f) }
    var radius by remember { mutableFloatStateOf(150f) }
    var smoothing by remember { mutableFloatStateOf(.15f) }
    var mode by remember { mutableStateOf(MagneticMode.ATTRACTION) }
    var showRings by remember { mutableStateOf(true) }
    var target by remember { mutableStateOf(Offset.Unspecified) }
    var movingTarget by remember { mutableStateOf(false) }
    val raw = remember { mutableStateListOf<Offset>() }
    val processed = remember { mutableStateListOf<Offset>() }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        LabCanvas(Modifier.fillMaxWidth().height(300.dp).background(Color(0xFF0D111A), RoundedCornerShape(18.dp)).pointerInteropFilter { event ->
            val point = Offset(event.x, event.y)
            val actualTarget = if (target != Offset.Unspecified) target else Offset(event.x.coerceAtLeast(130f), event.y.coerceAtLeast(130f)).also { target = it }
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { raw.clear(); processed.clear(); movingTarget = (point - actualTarget).getDistance() < 55f; if (!movingTarget) { raw.add(point); processed.add(magneticPoint(point, actualTarget, radius, strength, smoothing, null, mode)) }; true }
                MotionEvent.ACTION_MOVE -> { if (movingTarget) target = point else { raw.add(point); processed.add(magneticPoint(point, target, radius, strength, smoothing, processed.lastOrNull(), mode)) }; true }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { movingTarget = false; true }
                else -> true
            }
        }) { center ->
            val t = if (target != Offset.Unspecified) target else center
            if (showRings) for (i in 1..4) drawCircle(Blue.copy(alpha = .07f + i * .015f), radius * i / 4f, t, style = Stroke(1.5f))
            drawCircle(Blue.copy(alpha = .09f), radius, t)
            drawSignalPath(raw, Red); drawSignalPath(processed, Green)
            drawCircle(Amber, 16f, t); drawCircle(Color.White, 6f, t)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Legend("RAW INPUT", Red); Legend("PROCESSED", Green); Legend("FIELD", Blue) }
        Text("FIELD MODE", color = Color(0xFF8794A8), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { MagneticMode.values().forEach { m -> FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(m.label, fontSize = 12.sp) }) } }
        SliderControl("Strength", strength, 0.02f..0.75f) { strength = it }
        SliderControl("Field radius", radius, 60f..260f) { radius = it }
        SliderControl("Smoothing", smoothing, 0f..0.8f) { smoothing = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { raw.clear(); processed.clear() }, colors = ButtonDefaults.buttonColors(containerColor = PanelRaised)) { Text("RESET PATH", fontSize = 11.sp) }
            Button(onClick = { target = Offset.Unspecified }, colors = ButtonDefaults.buttonColors(containerColor = PanelRaised)) { Text("RESET TARGET", fontSize = 11.sp) }
            Button(onClick = { raw.clear(); processed.clear(); target = Offset.Unspecified; mode = MagneticMode.ATTRACTION }, colors = ButtonDefaults.buttonColors(containerColor = Blue.copy(alpha = .25f))) { Text("RESET ALL", fontSize = 11.sp) }
        }
        Text("Drag the yellow target to reposition the field. This is a visual experiment; it does not modify Android touch input.", color = Color(0xFF718096), fontSize = 11.sp)
    }
}

private fun magneticPoint(raw: Offset, target: Offset, radius: Float, strength: Float, smoothing: Float, previous: Offset?, mode: MagneticMode): Offset {
    val dx = target.x - raw.x; val dy = target.y - raw.y; val distance = sqrt(dx * dx + dy * dy)
    val result = if (distance > .001f && distance < radius && mode != MagneticMode.NEUTRAL) {
        val influence = (1f - distance / radius).let { it * it * strength } * if (mode == MagneticMode.REPULSION) -1f else 1f
        Offset(raw.x + dx * influence, raw.y + dy * influence)
    } else raw
    return previous?.let { Offset(it.x * smoothing + result.x * (1f - smoothing), it.y * smoothing + result.y * (1f - smoothing)) } ?: result
}

@Composable
private fun TouchScreen() {
    var mode by remember { mutableStateOf("Smooth") }; val raw = remember { mutableStateListOf<Offset>() }; val smooth = remember { mutableStateListOf<Offset>() }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Raw", "Smooth", "Predict").forEach { FilterChip(selected = mode == it, onClick = { mode = it }, label = { Text(it, fontSize = 12.sp) }) } }
        LabCanvas(Modifier.fillMaxWidth().height(390.dp).background(Color(0xFF0D111A), RoundedCornerShape(18.dp)).pointerInteropFilter { e ->
            val p = Offset(e.x, e.y); when (e.actionMasked) { MotionEvent.ACTION_DOWN -> { raw.clear(); smooth.clear(); raw.add(p); smooth.add(p); true }; MotionEvent.ACTION_MOVE -> { raw.add(p); val last = smooth.lastOrNull() ?: p; smooth.add(Offset(last.x * .78f + p.x * .22f, last.y * .78f + p.y * .22f)); true }; else -> true }
        }) { center -> drawSignalPath(raw, Red); if (mode != "Raw") drawSignalPath(smooth, Purple); if (mode == "Predict" && smooth.size > 1) { val a = smooth[smooth.lastIndex] - smooth[smooth.lastIndex - 1]; drawCircle(Amber.copy(alpha = .7f), 11f, smooth.last() + a * 8f) }; drawCircle(Blue, 5f, center) }
        Text("LIVE COORDINATE", color = Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text("Points captured: ${raw.size}    |    Mode: ${mode.uppercase()}", color = Color.White, fontSize = 14.sp)
        if (mode == "Predict") Text("Experimental visualization — prediction is illustrative and does not change system input.", color = Amber, fontSize = 11.sp)
        Button(onClick = { raw.clear(); smooth.clear() }, colors = ButtonDefaults.buttonColors(containerColor = PanelRaised)) { Text("RESET TRAIL") }
    }
}

@Composable
private fun DisplayScreen() {
    var speed by remember { mutableFloatStateOf(.5f) }; var pattern by remember { mutableStateOf("Moving Dot") }; val transition = rememberInfiniteTransition(label = "motion"); val phase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween((2200 - speed * 1600).toInt()), RepeatMode.Restart), label = "phase")
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Moving Dot", "Moving Grid", "Motion Trail", "Pattern Test").forEach { FilterChip(selected = pattern == it, onClick = { pattern = it }, label = { Text(it, fontSize = 12.sp) }) } }
        LabCanvas(Modifier.fillMaxWidth().height(360.dp).background(Color(0xFF0D111A), RoundedCornerShape(18.dp))) {
            val canvasSize = this.size
            if (pattern == "Moving Grid") { for (x in 0..canvasSize.width.toInt() step 28) drawLine(Blue.copy(alpha = .25f), Offset(x.toFloat() - phase * 28f, 0f), Offset(x.toFloat() - phase * 28f, canvasSize.height), 1f); for (y in 0..canvasSize.height.toInt() step 28) drawLine(Purple.copy(alpha = .2f), Offset(0f, y.toFloat()), Offset(canvasSize.width, y.toFloat()), 1f) }
            else if (pattern == "Pattern Test") for (x in 0..canvasSize.width.toInt() step 24) drawLine(Purple.copy(alpha = .55f), Offset(x - phase * 80f, 0f), Offset(x - phase * 80f + 120f, canvasSize.height), 8f)
            val x = phase * canvasSize.width; drawCircle(Green, 14f, Offset(x, canvasSize.height / 2)); if (pattern == "Motion Trail") for (i in 1..8) drawCircle(Blue.copy(alpha = .35f / i), 14f, Offset(x - i * 18f, canvasSize.height / 2))
        }
        SliderControl("Movement speed", speed, 0.05f..1f) { speed = it }
        val refresh = LocalContext.current.display?.refreshRate ?: 0f
        StatusRow("Display refresh", if (refresh > 0f) "${"%.0f".format(refresh)} Hz" else "Not available", if (refresh > 0f) Green else Amber)
        Text("Visual observation only. No FPS or performance boost is claimed.", color = Color(0xFF718096), fontSize = 11.sp)
    }
}

@Composable
private fun DeviceScreen() { val context = LocalContext.current; val metrics = context.resources.displayMetrics; val refresh = context.display?.refreshRate ?: 0f; Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { InfoGroup("DISPLAY") { StatusRow("Resolution", "${metrics.widthPixels} × ${metrics.heightPixels}", Green); StatusRow("Density", "${"%.1f".format(metrics.density)} dp scale", Green); StatusRow("Refresh rate", if (refresh > 0) "${"%.0f".format(refresh)} Hz" else "Not available", if (refresh > 0) Green else Amber) }; InfoGroup("DEVICE") { StatusRow("Manufacturer", Build.MANUFACTURER, Green); StatusRow("Model", Build.MODEL, Green); StatusRow("Android", Build.VERSION.RELEASE ?: "Unknown", Green); StatusRow("SDK level", Build.VERSION.SDK_INT.toString(), Green) }; InfoGroup("CAPABILITY STATUS") { StatusRow("High refresh display", if (refresh >= 90) "SUPPORTED" else "NOT DETECTED", if (refresh >= 90) Green else Amber); StatusRow("MEMC", "NOT DETECTED", Amber); StatusRow("Advanced OEM feature", "UNAVAILABLE", Amber) }; Text("Statuses distinguish what Android exposes from what is unavailable. No hidden OEM activities or permissions are used.", color = Color(0xFF718096), fontSize = 11.sp) } }

@Composable
private fun SystemScreen() { val context = LocalContext.current; val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager; val memory = android.app.ActivityManager.MemoryInfo().also { manager.getMemoryInfo(it) }; val battery = ContextCompat.registerReceiver(context, null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED); val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1; val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100; val percent = if (level >= 0) level * 100 / scale else -1; val stat = StatFs(context.filesDir.absolutePath); val freeGb = stat.availableBytes / 1_000_000_000.0; Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { InfoGroup("LIVE TELEMETRY") { StatusRow("Memory available", "${memory.availMem / 1_000_000} MB", Blue); StatusRow("Memory state", if (memory.lowMemory) "LIMITED" else "AVAILABLE", if (memory.lowMemory) Amber else Green); StatusRow("Battery", if (percent >= 0) "$percent%" else "Not available", if (percent > 20) Green else Amber); StatusRow("Storage", "${"%.1f".format(freeGb)} GB available", Green); StatusRow("Display", context.display?.let { "${"%.0f".format(it.refreshRate)} Hz" } ?: "Not available", Blue) }; Text("Information is read from public Android APIs and remains on this device. CPU frequency and thermal values are not fabricated when unavailable.", color = Color(0xFF718096), fontSize = 11.sp) } }

@Composable private fun InfoGroup(title: String, content: @Composable () -> Unit) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Panel)) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(title, color = Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp); content() } } }
@Composable private fun StatusRow(label: String, value: String, color: Color) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Color(0xFFB8C2D1), fontSize = 13.sp); Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) } }
@Composable private fun Legend(label: String, color: Color) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).background(color, CircleShape)); Spacer(Modifier.width(5.dp)); Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun SliderControl(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) { Column { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Color(0xFFB8C2D1), fontSize = 12.sp); Text("${"%.2f".format(value)}", color = Blue, fontSize = 12.sp) }; Slider(value, onChange, valueRange = range) } }
@Composable private fun LabCanvas(modifier: Modifier, content: androidx.compose.ui.graphics.drawscope.DrawScope.(Offset) -> Unit) { Canvas(modifier) { content(center) } }
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSignalPath(points: List<Offset>, color: Color) { if (points.size < 2) return; val path = Path().apply { moveTo(points[0].x, points[0].y); for (i in 1 until points.size) lineTo(points[i].x, points[i].y) }; drawPath(path, color, style = Stroke(4f, cap = StrokeCap.Round)) }
