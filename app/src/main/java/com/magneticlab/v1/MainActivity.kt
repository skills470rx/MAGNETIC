package com.magneticlab.v1

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MagneticLabApp()
            }
        }
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

    val distance = sqrt(dx * dx + dy * dy)

    var result = raw

    if (distance > 0.001f && distance < radius) {

        val influence = 1f - (distance / radius)

        val force =
            influence * influence * strength

        result = Offset(
            raw.x + dx * force,
            raw.y + dy * force
        )
    }

    return if (previous != null) {

        val keep = smoothing.coerceIn(0f, 0.95f)

        Offset(
            previous.x * keep +
                    result.x * (1f - keep),

            previous.y * keep +
                    result.y * (1f - keep)
        )

    } else {
        result
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
private fun MagneticLabApp() {

    var enabled by remember {
        mutableStateOf(true)
    }

    var strength by remember {
        mutableFloatStateOf(0.28f)
    }

    var radiusDp by remember {
        mutableFloatStateOf(150f)
    }

    var smoothing by remember {
        mutableFloatStateOf(0.15f)
    }

    var target by remember {
        mutableStateOf(Offset.Unspecified)
    }

    var movingTarget by remember {
        mutableStateOf(false)
    }

    val rawPath = remember {
        mutableStateListOf<Offset>()
    }

    val processedPath = remember {
        mutableStateListOf<Offset>()
    }

    val density = LocalDensity.current

    val radiusPx =
        with(density) {
            radiusDp.dp.toPx()
        }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        "Magnetic Lab V1",
                        fontWeight = FontWeight.Bold
                    )
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)

        ) {

            Card(
    modifier = Modifier
        .fillMaxWidth()
        .height(420.dp),
    shape = RoundedCornerShape(18.dp)
) {

                Canvas(

                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF101218))
                        .pointerInteropFilter { event ->

                            val p =
                                Offset(
                                    event.x,
                                    event.y
                                )

                            if (
                                target ==
                                Offset.Unspecified
                            ) {

                                target = Offset(
                                    event.x.coerceAtLeast(120f),
                                    event.y.coerceAtLeast(120f)
                                )
                            }

                            when (
                                event.actionMasked
                            ) {

                                MotionEvent.ACTION_DOWN -> {

                                    rawPath.clear()

                                    processedPath.clear()

                                    val nearTarget =
                                        (p - target)
                                            .getDistance() < 55f

                                    movingTarget =
                                        nearTarget

                                    if (!nearTarget) {

                                        rawPath.add(p)

                                        val processed =
                                            if (enabled) {

                                                magneticPoint(
                                                    raw = p,
                                                    target = target,
                                                    radius = radiusPx,
                                                    strength = strength,
                                                    smoothing = smoothing,
                                                    previous = null
                                                )

                                            } else {
                                                p
                                            }

                                        processedPath.add(
                                            processed
                                        )
                                    }

                                    true
                                }

                                MotionEvent.ACTION_MOVE -> {

                                    if (movingTarget) {

                                        target = p

                                    } else {

                                        rawPath.add(p)

                                        val previous =
                                            processedPath.lastOrNull()

                                        val processed =
                                            if (enabled) {

                                                magneticPoint(
                                                    raw = p,
                                                    target = target,
                                                    radius = radiusPx,
                                                    strength = strength,
                                                    smoothing = smoothing,
                                                    previous = previous
                                                )

                                            } else {
                                                p
                                            }

                                        processedPath.add(
                                            processed
                                        )
                                    }

                                    true
                                }

                                MotionEvent.ACTION_UP,
                                MotionEvent.ACTION_CANCEL -> {

                                    movingTarget = false

                                    true
                                }

                                else -> true
                            }
                        }

                ) {

                    val drawTarget =
                        if (
                            target ==
                            Offset.Unspecified
                        ) {
                            center
                        } else {
                            target
                        }

                    // Magnetic field

                    drawCircle(
                        color =
                            Color(0x332196F3),

                        radius =
                            radiusPx,

                        center =
                            drawTarget
                    )

                    drawCircle(
                        color =
                            Color(0xFF64B5F6),

                        radius =
                            radiusPx,

                        center =
                            drawTarget,

                        style =
                            Stroke(
                                width = 2f
                            )
                    )

                    // Raw path

                    if (rawPath.size > 1) {

                        val path =
                            Path().apply {

                                moveTo(
                                    rawPath[0].x,
                                    rawPath[0].y
                                )

                                for (
                                    i in 1 until rawPath.size
                                ) {

                                    lineTo(
                                        rawPath[i].x,
                                        rawPath[i].y
                                    )
                                }
                            }

                        drawPath(
                            path = path,
                            color =
                                Color(0xFFFF6B6B),

                            style =
                                Stroke(
                                    width = 5f
                                )
                        )
                    }

                    // Processed path

                    if (processedPath.size > 1) {

                        val path =
                            Path().apply {

                                moveTo(
                                    processedPath[0].x,
                                    processedPath[0].y
                                )

                                for (
                                    i in 1 until processedPath.size
                                ) {

                                    lineTo(
                                        processedPath[i].x,
                                        processedPath[i].y
                                    )
                                }
                            }

                        drawPath(
                            path = path,
                            color =
                                Color(0xFF69F0AE),

                            style =
                                Stroke(
                                    width = 5f
                                )
                        )
                    }

                    // Target marker

                    drawCircle(
                        color =
                            Color(0xFFFFD54F),

                        radius = 18f,

                        center =
                            drawTarget
                    )

                    drawCircle(
                        color =
                            Color.White,

                        radius = 7f,

                        center =
                            drawTarget
                    )
                }
            }

            Row(

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text(
                    "Red: Raw",
                    color =
                        Color(0xFFD32F2F),
                    fontSize = 12.sp
                )

                Text(
                    "Green: Magnetic",
                    color =
                        Color(0xFF2E7D32),
                    fontSize = 12.sp
                )

                Text(
                    "Drag target",
                    fontSize = 12.sp
                )
            }

            ControlRow(
                label = "Magnetic"
            ) {

                Switch(

                    checked = enabled,

                    onCheckedChange = {
                        enabled = it
                    }
                )
            }

            SliderRow(

                label = "Strength",

                value = strength,

                range = 0.02f..0.75f,

                suffix = ""

            ) { newValue ->

                strength = newValue
            }

            SliderRow(

                label = "Radius",

                value = radiusDp,

                range = 60f..320f,

                suffix = " dp"

            ) { newValue ->

                radiusDp = newValue
            }

            SliderRow(

                label = "Smoothing",

                value = smoothing,

                range = 0f..0.90f,

                suffix = ""

            ) { newValue ->

                smoothing = newValue
            }

            Button(

                onClick = {

                    rawPath.clear()

                    processedPath.clear()
                },

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text("Reset Paths")
            }

            Text(

                text =
                    "Offline experiment • No Internet • No root • No input injection",

                fontSize = 11.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,

                modifier =
                    Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ControlRow(

    label: String,

    content: @Composable () -> Unit

) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween

    ) {

        Text(
            label,
            fontWeight =
                FontWeight.SemiBold
        )

        content()
    }
}

@Composable
private fun SliderRow(

    label: String,

    value: Float,

    range: ClosedFloatingPointRange<Float>,

    suffix: String = "",

    onChange: (Float) -> Unit

) {

    Column {

        Text(

            text =
                "$label: ${
                    String.format(
                        "%.2f",
                        value
                    )
                }$suffix",

            fontWeight =
                FontWeight.SemiBold
        )

        Slider(

            value = value,

            onValueChange = onChange,

            valueRange = range
        )
    }
}
