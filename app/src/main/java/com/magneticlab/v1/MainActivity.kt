package com.magneticlab.v1

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { MagneticLabApp() } }
    }
}

private fun magneticPoint(
    raw: Offset,
    target: Offset,
    radius: Float,
    strength: Float,
    smoothing: Float,
    previous: Offset?
): Offset {
    val dx = target.x - raw.x
    val dy = target.y - raw.y
    val distance = hypot(dx, dy)

    var result = raw
    if (distance > 0.001f && distance < radius) {
        // Smooth quadratic falloff: 0 at edge, strongest near center.
        val influence = 1f - (distance / radius)
        val force = influence * influence * strength
        result = Offset(raw.x + dx * force, raw.y + dy * force)
    }

    // Optional low-pass smoothing of processed output.
    return if (previous != null) {
        val keep = smoothing.coerceIn(0f, 0.95f)
        Offset(
            previous.x * keep + result.x * (1f - keep),
            previous.y * keep + result.y * (1f - keep)
        )
    } else result
}

@Composable
private fun MagneticLabApp() {
    var enabled by remember { mutableStateOf(true) }
    var strength by remember { mutableFloatStateOf(0.28f) }
    var radiusDp by remember { mutableFloatStateOf(150f) }
    var smoothing by remember { mutableFloatStateOf(0.15f) }
    var target by remember { mutableStateOf(Offset.Unspecified) }
    val rawPath = remember { mutableStateListOf<Offset>() }
    val processedPath = remember { mutableStateListOf<Offset>() }
    var movingTarget by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val radiusPx = with(density) { radiusDp.dp.toPx() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧲 Magnetic Lab V1", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF101218))
                        .pointerInteropFilter { event ->
                            val p = Offset(event.x, event.y)
                            if (target == Offset.Unspecified) {
                                target = Offset(event.x.coerceAtLeast(120f), event.y.coerceAtLeast(120f))
                            }

                            when (event.actionMasked) {
                                MotionEvent.ACTION_DOWN -> {
                                    rawPath.clear()
                                    processedPath.clear()
                                    val nearTarget = (p - target).getDistance() < 55f
                                    movingTarget = nearTarget
                                    if (!nearTarget) {
                                        rawPath.add(p)
                                        processedPath.add(if (enabled)
                                            magneticPoint(p, target, radiusPx, strength, smoothing, null)
                                        else p)
                                    }
                                    true
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    if (movingTarget) {
                                        target = p
                                    } else {
                                        rawPath.add(p)
                                        val prev = processedPath.lastOrNull()
                                        processedPath.add(if (enabled)
                                            magneticPoint(p, target, radiusPx, strength, smoothing, prev)
                                        else p)
                                    }
                                    true
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    movingTarget = false
                                    true
                                }
                                else -> true
                            }
                        }
                ) {
                    if (target == Offset.Unspecified) {
                        target = center
                    }

                    // Field
                    drawCircle(
                        color = Color(0x332196F3),
                        radius = radiusPx,
                        center = target
                    )
                    drawCircle(
                        color = Color(0xFF64B5F6),
                        radius = radiusPx,
                        center = target,
                        style = Stroke(width = 2f)
                    )

                    // Paths
                    if (rawPath.size > 1) {
                        val path = Path().apply {
                            moveTo(rawPath[0].x, rawPath[0].y)
                            rawPath.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, Color(0xFFFF6B6B), style = Stroke(width = 5f))
                    }
                    if (processedPath.size > 1) {
                        val path = Path().apply {
                            moveTo(processedPath[0].x, processedPath[0].y)
                            processedPath.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, Color(0xFF69F0AE), style = Stroke(width = 5f))
                    }

                    // Target
                    drawCircle(Color(0xFFFFD54F), radius = 18f, center = target)
                    drawCircle(Color.White, radius = 7f, center = target)
                }
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Red: Raw input", color = Color(0xFFD32F2F), fontSize = 12.sp)
                Text("Green: Processed", color = Color(0xFF2E7D32), fontSize = 12.sp)
                Text("Drag target to move", fontSize = 12.sp)
            }

            ControlRow("Magnetic", enabled.toString()) {
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            SliderRow("Strength", strength, 0.02f..0.75f) { strength = it }
            SliderRow("Radius", radiusDp, 60f..320f, suffix = " dp") { radiusDp = it }
            SliderRow("Smoothing", smoothing, 0f..0.90f) { smoothing = it }

            Button(
                onClick = {
                    rawPath.clear()
                    processedPath.clear()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset Paths") }

            Text(
                "Offline experiment • No Internet • No root • No input injection",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

package com.magneticlab.v1

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { MagneticLabApp() } }
    }
}

private fun magneticPoint(
    raw: Offset,
    target: Offset,
    radius: Float,
    strength: Float,
    smoothing: Float,
    previous: Offset?
): Offset {
    val dx = target.x - raw.x
    val dy = target.y - raw.y
    val distance = hypot(dx, dy)

    var result = raw
    if (distance > 0.001f && distance < radius) {
        // Smooth quadratic falloff: 0 at edge, strongest near center.
        val influence = 1f - (distance / radius)
        val force = influence * influence * strength
        result = Offset(raw.x + dx * force, raw.y + dy * force)
    }

    // Optional low-pass smoothing of processed output.
    return if (previous != null) {
        val keep = smoothing.coerceIn(0f, 0.95f)
        Offset(
            previous.x * keep + result.x * (1f - keep),
            previous.y * keep + result.y * (1f - keep)
        )
    } else result
}

@Composable
private fun MagneticLabApp() {
    var enabled by remember { mutableStateOf(true) }
    var strength by remember { mutableFloatStateOf(0.28f) }
    var radiusDp by remember { mutableFloatStateOf(150f) }
    var smoothing by remember { mutableFloatStateOf(0.15f) }
    var target by remember { mutableStateOf(Offset.Unspecified) }
    val rawPath = remember { mutableStateListOf<Offset>() }
    val processedPath = remember { mutableStateListOf<Offset>() }
    var movingTarget by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val radiusPx = with(density) { radiusDp.dp.toPx() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧲 Magnetic Lab V1", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF101218))
                        .pointerInteropFilter { event ->
                            val p = Offset(event.x, event.y)
                            if (target == Offset.Unspecified) {
                                target = Offset(event.x.coerceAtLeast(120f), event.y.coerceAtLeast(120f))
                            }

                            when (event.actionMasked) {
                                MotionEvent.ACTION_DOWN -> {
                                    rawPath.clear()
                                    processedPath.clear()
                                    val nearTarget = (p - target).getDistance() < 55f
                                    movingTarget = nearTarget
                                    if (!nearTarget) {
                                        rawPath.add(p)
                                        processedPath.add(if (enabled)
                                            magneticPoint(p, target, radiusPx, strength, smoothing, null)
                                        else p)
                                    }
                                    true
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    if (movingTarget) {
                                        target = p
                                    } else {
                                        rawPath.add(p)
                                        val prev = processedPath.lastOrNull()
                                        processedPath.add(if (enabled)
                                            magneticPoint(p, target, radiusPx, strength, smoothing, prev)
                                        else p)
                                    }
                                    true
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    movingTarget = false
                                    true
                                }
                                else -> true
                            }
                        }
                ) {
                    if (target == Offset.Unspecified) {
                        target = center
                    }

                    // Field
                    drawCircle(
                        color = Color(0x332196F3),
                        radius = radiusPx,
                        center = target
                    )
                    drawCircle(
                        color = Color(0xFF64B5F6),
                        radius = radiusPx,
                        center = target,
                        style = Stroke(width = 2f)
                    )

                    // Paths
                    if (rawPath.size > 1) {
                        val path = Path().apply {
                            moveTo(rawPath[0].x, rawPath[0].y)
                            rawPath.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, Color(0xFFFF6B6B), style = Stroke(width = 5f))
                    }
                    if (processedPath.size > 1) {
                        val path = Path().apply {
                            moveTo(processedPath[0].x, processedPath[0].y)
                            processedPath.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, Color(0xFF69F0AE), style = Stroke(width = 5f))
                    }

                    // Target
                    drawCircle(Color(0xFFFFD54F), radius = 18f, center = target)
                    drawCircle(Color.White, radius = 7f, center = target)
                }
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Red: Raw input", color = Color(0xFFD32F2F), fontSize = 12.sp)
                Text("Green: Processed", color = Color(0xFF2E7D32), fontSize = 12.sp)
                Text("Drag target to move", fontSize = 12.sp)
            }

            ControlRow("Magnetic", enabled.toString()) {
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            SliderRow("Strength", strength, 0.02f..0.75f) { strength = it }
            SliderRow("Radius", radiusDp, 60f..320f, suffix = " dp") { radiusDp = it }
            SliderRow("Smoothing", smoothing, 0f..0.90f) { smoothing = it }

            Button(
                onClick = {
                    rawPath.clear()
                    processedPath.clear()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset Paths") }

            Text(
                "Offline experiment • No Internet • No root • No input injection",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ControlRow(label: String, value: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun SliderRow(
    label = "Strength",
    range = 0f..100f,
    suffix = "",
    onChange = { value ->
        strength = value
    }
) {
    Column {
        Text("$label: ${"%.2f".format(value)}$suffix", fontWeight = FontWeight.SemiBold)
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

