package com.example.viakaab.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.viakaab.BuildConfig
import com.example.viakaab.data.CarrilloLandmark
import com.example.viakaab.model.MapVehicleType
import com.example.viakaab.model.OnlineMapUnit
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.PureBlack
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate300
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate500
import com.example.viakaab.ui.theme.Slate600
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.ui.theme.Slate950
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val UBER_DARK_MAP_STYLE_JSON = """
[
  {"elementType":"geometry","stylers":[{"color":"#0f172a"}]},
  {"elementType":"labels.icon","stylers":[{"visibility":"off"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#64748b"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#0f172a"}]},
  {"featureType":"administrative","elementType":"geometry","stylers":[{"color":"#1e293b"}]},
  {"featureType":"poi","elementType":"geometry","stylers":[{"color":"#131c31"}]},
  {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#0d2622"}]},
  {"featureType":"road","elementType":"geometry.fill","stylers":[{"color":"#1e293b"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#0b1120"}]},
  {"featureType":"road.arterial","elementType":"geometry.fill","stylers":[{"color":"#283548"}]},
  {"featureType":"road.highway","elementType":"geometry.fill","stylers":[{"color":"#334155"}]},
  {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#1e293b"}]},
  {"featureType":"road.local","elementType":"labels.text.fill","stylers":[{"color":"#475569"}]},
  {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#1e293b"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#07101f"}]}
]
"""

fun hasConfiguredGoogleMapsKey(): Boolean {
    val key = BuildConfig.MAPS_API_KEY.trim()
    return key.isNotEmpty() &&
        !key.startsWith("YOUR_", ignoreCase = true) &&
        !key.equals("null", ignoreCase = true)
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
    return mapView
}

private data class UnitTelemetryTween(
    val unit: OnlineMapUnit,
    val startLat: Double,
    val startLng: Double,
    val startHeading: Float,
    val targetLat: Double,
    val targetLng: Double,
    val targetHeading: Float,
    val startedAtNanos: Long
) {
    fun sample(nowNanos: Long, durationNanos: Long = 950_000_000L): Pair<LatLng, Float> {
        val t = if (durationNanos <= 0L) 1f else {
            ((nowNanos - startedAtNanos).toFloat() / durationNanos.toFloat()).coerceIn(0f, 1f)
        }
        val eased = easeInOutRoute(t)
        val lat = startLat + (targetLat - startLat) * eased
        val lng = startLng + (targetLng - startLng) * eased
        val heading = lerpBearingDegrees(startHeading, targetHeading, eased)
        return LatLng(lat, lng) to heading
    }
}

private fun interpolateLatLng(from: LatLng, to: LatLng, t: Float): LatLng {
    val clamped = t.coerceIn(0f, 1f).toDouble()
    return LatLng(
        from.latitude + (to.latitude - from.latitude) * clamped,
        from.longitude + (to.longitude - from.longitude) * clamped
    )
}

private fun computeBearingDegrees(from: LatLng, to: LatLng): Float {
    val lat1 = Math.toRadians(from.latitude)
    val lat2 = Math.toRadians(to.latitude)
    val dLng = Math.toRadians(to.longitude - from.longitude)
    val y = sin(dLng) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
    return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
}

private fun lerpBearingDegrees(fromDeg: Float, toDeg: Float, t: Float): Float {
    val delta = ((((toDeg - fromDeg) % 360f) + 540f) % 360f) - 180f
    return ((fromDeg + delta * t.coerceIn(0f, 1f)) + 360f) % 360f
}

private fun quadraticLatLng(p0: LatLng, p1: LatLng, p2: LatLng, t: Float): LatLng {
    val u = 1.0 - t
    val tt = t.toDouble() * t.toDouble()
    val uu = u * u
    val twoUt = 2.0 * u * t
    return LatLng(
        uu * p0.latitude + twoUt * p1.latitude + tt * p2.latitude,
        uu * p0.longitude + twoUt * p1.longitude + tt * p2.longitude
    )
}

private fun quadraticOffset(p0: Offset, p1: Offset, p2: Offset, t: Float): Offset {
    val u = 1f - t
    return Offset(
        x = u * u * p0.x + 2f * u * t * p1.x + t * t * p2.x,
        y = u * u * p0.y + 2f * u * t * p1.y + t * t * p2.y
    )
}

private fun buildCurvedRoutePoints(start: LatLng, end: LatLng, steps: Int = 44): List<LatLng> {
    val dLat = end.latitude - start.latitude
    val dLng = end.longitude - start.longitude
    val chord = hypot(dLat, dLng).coerceAtLeast(1e-6)
    val curveLift = min(chord * 0.16, 0.012)
    val nx = -dLng / chord
    val ny = dLat / chord
    val control = LatLng(
        (start.latitude + end.latitude) * 0.5 + nx * curveLift,
        (start.longitude + end.longitude) * 0.5 + ny * curveLift
    )
    return (0..steps).map { i ->
        quadraticLatLng(start, control, end, i.toFloat() / steps)
    }
}

private fun buildCanvasRouteControl(start: Offset, end: Offset): Offset {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val dist = hypot(dx, dy).coerceAtLeast(1f)
    val lift = (dist * 0.16f).coerceIn(14f, 68f)
    val nx = dy / dist
    val ny = -dx / dist
    return Offset(
        x = (start.x + end.x) * 0.5f + nx * lift,
        y = (start.y + end.y) * 0.5f + ny * lift
    )
}

private fun easeInOutRoute(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

private fun createUberPickupDescriptor(context: Context): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val sizePx = (48 * density).roundToInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val cx = sizePx / 2f
    val cy = sizePx / 2f

    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = BrandEmerald.copy(alpha = 0.26f).toArgb()
    }
    canvas.drawCircle(cx, cy, sizePx * 0.46f, haloPaint)

    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PureBlack.toArgb()
    }
    canvas.drawCircle(cx, cy, sizePx * 0.28f, outerPaint)

    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PureWhite.toArgb()
    }
    canvas.drawCircle(cx, cy, sizePx * 0.21f, ringPaint)

    val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = BrandEmerald.toArgb()
    }
    canvas.drawCircle(cx, cy, sizePx * 0.11f, corePaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun createUberDestinationDescriptor(context: Context): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val w = (38 * density).roundToInt()
    val h = (48 * density).roundToInt()
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val cx = w / 2f
    val headSize = w * 0.70f
    val left = (w - headSize) / 2f
    val top = 2f * density

    val stemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PureBlack.toArgb()
        strokeWidth = 3.2f * density
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawLine(cx, top + headSize - 2f, cx, h - 4f * density, stemPaint)

    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = BrandRed.toArgb()
    }
    canvas.drawCircle(cx, h - 3.5f * density, 3.5f * density, dotPaint)

    val headPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PureBlack.toArgb()
    }
    canvas.drawRoundRect(
        RectF(left, top, left + headSize, top + headSize),
        6f * density,
        6f * density,
        headPaint
    )

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = BrandSkyTint.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 1.6f * density
    }
    canvas.drawRoundRect(
        RectF(left, top, left + headSize, top + headSize),
        6f * density,
        6f * density,
        borderPaint
    )

    val innerSize = headSize * 0.34f
    val innerLeft = (w - innerSize) / 2f
    val innerTop = top + (headSize - innerSize) / 2f
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PureWhite.toArgb()
    }
    canvas.drawRect(
        RectF(innerLeft, innerTop, innerLeft + innerSize, innerTop + innerSize),
        innerPaint
    )

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun createUberVehicleDescriptor(
    context: Context,
    isPrimary: Boolean = true
): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val w = ((if (isPrimary) 40 else 34) * density).roundToInt()
    val h = ((if (isPrimary) 64 else 54) * density).roundToInt()
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val beamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            w / 2f,
            h * 0.16f,
            w / 2f,
            0f,
            Color(0xFFFEF08A).copy(alpha = if (isPrimary) 0.38f else 0.22f).toArgb(),
            Color.Transparent.toArgb(),
            Shader.TileMode.CLAMP
        )
    }
    val beamPath = AndroidPath().apply {
        moveTo(w * 0.30f, h * 0.16f)
        lineTo(w * 0.12f, 0f)
        lineTo(w * 0.88f, 0f)
        lineTo(w * 0.70f, h * 0.16f)
        close()
    }
    canvas.drawPath(beamPath, beamPaint)

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PureBlack.copy(alpha = 0.45f).toArgb()
    }
    canvas.drawRoundRect(
        RectF(w * 0.15f, h * 0.16f, w * 0.89f, h * 0.96f),
        11f * density,
        11f * density,
        shadowPaint
    )

    val mirrorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isPrimary) PureWhite.toArgb() else Slate300.toArgb()
    }
    canvas.drawRoundRect(
        RectF(w * 0.09f, h * 0.35f, w * 0.23f, h * 0.42f),
        2.5f * density,
        2.5f * density,
        mirrorPaint
    )
    canvas.drawRoundRect(
        RectF(w * 0.77f, h * 0.35f, w * 0.91f, h * 0.42f),
        2.5f * density,
        2.5f * density,
        mirrorPaint
    )

    val bodyPath = AndroidPath().apply {
        moveTo(w * 0.27f, h * 0.15f)
        quadTo(w * 0.50f, h * 0.11f, w * 0.73f, h * 0.15f)
        quadTo(w * 0.82f, h * 0.25f, w * 0.82f, h * 0.52f)
        lineTo(w * 0.80f, h * 0.85f)
        quadTo(w * 0.76f, h * 0.93f, w * 0.50f, h * 0.93f)
        quadTo(w * 0.24f, h * 0.93f, w * 0.20f, h * 0.85f)
        lineTo(w * 0.18f, h * 0.52f)
        quadTo(w * 0.18f, h * 0.25f, w * 0.27f, h * 0.15f)
        close()
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            w * 0.18f,
            h * 0.15f,
            w * 0.82f,
            h * 0.92f,
            if (isPrimary) PureWhite.toArgb() else Color(0xFFF8FAFC).toArgb(),
            Color(0xFFCBD5E1).toArgb(),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawPath(bodyPath, bodyPaint)

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isPrimary) BrandEmerald.toArgb() else Slate700.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = if (isPrimary) 2.2f * density else 1.4f * density
    }
    canvas.drawPath(bodyPath, borderPaint)

    val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xFF0F172A).toArgb()
    }
    canvas.drawRoundRect(
        RectF(w * 0.25f, h * 0.31f, w * 0.75f, h * 0.78f),
        6f * density,
        6f * density,
        glassPaint
    )

    val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PureWhite.copy(alpha = 0.14f).toArgb()
        strokeWidth = 2.5f * density
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawLine(w * 0.30f, h * 0.36f, w * 0.58f, h * 0.42f, sheenPaint)

    val roofPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isPrimary) PureWhite.toArgb() else Color(0xFFF1F5F9).toArgb()
    }
    canvas.drawRoundRect(
        RectF(w * 0.30f, h * 0.45f, w * 0.70f, h * 0.67f),
        4f * density,
        4f * density,
        roofPaint
    )

    if (isPrimary) {
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BrandEmerald.toArgb()
        }
        canvas.drawRoundRect(
            RectF(w * 0.37f, h * 0.52f, w * 0.63f, h * 0.60f),
            2.5f * density,
            2.5f * density,
            badgePaint
        )
    }

    val headlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xFFFEF08A).toArgb()
    }
    canvas.drawRoundRect(
        RectF(w * 0.26f, h * 0.15f, w * 0.38f, h * 0.19f),
        2f * density,
        2f * density,
        headlightPaint
    )
    canvas.drawRoundRect(
        RectF(w * 0.62f, h * 0.15f, w * 0.74f, h * 0.19f),
        2f * density,
        2f * density,
        headlightPaint
    )

    val taillightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = BrandRed.toArgb()
    }
    canvas.drawRoundRect(
        RectF(w * 0.25f, h * 0.87f, w * 0.38f, h * 0.91f),
        1.8f * density,
        1.8f * density,
        taillightPaint
    )
    canvas.drawRoundRect(
        RectF(w * 0.62f, h * 0.87f, w * 0.75f, h * 0.91f),
        1.8f * density,
        1.8f * density,
        taillightPaint
    )

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun createDidiMotoDescriptor(context: Context): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val w = (30 * density).roundToInt()
    val h = (50 * density).roundToInt()
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val beamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            w / 2f,
            h * 0.18f,
            w / 2f,
            0f,
            Color(0xFFBAE6FD).copy(alpha = 0.32f).toArgb(),
            Color.Transparent.toArgb(),
            Shader.TileMode.CLAMP
        )
    }
    val beamPath = AndroidPath().apply {
        moveTo(w * 0.40f, h * 0.18f)
        lineTo(w * 0.18f, 0f)
        lineTo(w * 0.82f, 0f)
        lineTo(w * 0.60f, h * 0.18f)
        close()
    }
    canvas.drawPath(beamPath, beamPaint)

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PureBlack.copy(alpha = 0.42f).toArgb()
    }
    canvas.drawRoundRect(
        RectF(w * 0.24f, h * 0.20f, w * 0.82f, h * 0.92f),
        8f * density,
        8f * density,
        shadowPaint
    )

    val tirePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Slate900.toArgb() }
    canvas.drawRoundRect(RectF(w * 0.42f, h * 0.14f, w * 0.58f, h * 0.30f), 3f * density, 3f * density, tirePaint)
    val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Slate300.toArgb()
        strokeWidth = 2.6f * density
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawLine(w * 0.22f, h * 0.30f, w * 0.78f, h * 0.30f, barPaint)

    val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BrandBlue.toArgb() }
    canvas.drawRoundRect(RectF(w * 0.34f, h * 0.26f, w * 0.66f, h * 0.82f), 5f * density, 5f * density, framePaint)

    val helmetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PureWhite.toArgb() }
    canvas.drawCircle(w * 0.50f, h * 0.44f, w * 0.18f, helmetPaint)
    val visorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Slate900.toArgb() }
    canvas.drawRoundRect(RectF(w * 0.36f, h * 0.30f, w * 0.64f, h * 0.38f), 2f * density, 2f * density, visorPaint)

    val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BrandEmerald.toArgb() }
    canvas.drawRoundRect(RectF(w * 0.27f, h * 0.57f, w * 0.73f, h * 0.86f), 4f * density, 4f * density, boxPaint)
    val boxBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PureWhite.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 1.4f * density
    }
    canvas.drawRoundRect(RectF(w * 0.27f, h * 0.57f, w * 0.73f, h * 0.86f), 4f * density, 4f * density, boxBorder)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@SuppressLint("MissingPermission")
@Composable
fun ViaKaabInteractiveMap(
    pickupLatLng: LatLng,
    pickupTitle: String,
    pickupSnippet: String,
    destinationLatLng: LatLng?,
    destinationTitle: String,
    destinationSnippet: String,
    routeColor: Color,
    hasLocationPermission: Boolean,
    landmarks: List<CarrilloLandmark>,
    onlineUnits: List<OnlineMapUnit> = emptyList(),
    driverLatLng: LatLng? = null,
    driverLabel: String = "Unidad ViaKaab",
    isSearchingDrivers: Boolean = false,
    etaMinutes: Int? = null,
    bottomOverlayReserveDp: Dp = 260.dp,
    badgeTopPaddingDp: Dp = 18.dp,
    onMapTap: ((LatLng) -> Unit)? = null,
    onGoogleMapReady: ((GoogleMap) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val useGoogleMaps = remember { hasConfiguredGoogleMapsKey() }

    // Combine real online units + assigned driver (only if genuinely active; zero fake cars)
    val effectiveUnits = remember(onlineUnits, driverLatLng, driverLabel) {
        if (driverLatLng != null && onlineUnits.none { it.isPrimary }) {
            onlineUnits + OnlineMapUnit(
                id = "assigned_driver_unit",
                label = driverLabel,
                vehicleType = MapVehicleType.CAR,
                role = com.example.viakaab.model.UserRole.DRIVER,
                lat = driverLatLng.latitude,
                lng = driverLatLng.longitude,
                headingDegrees = 0f,
                isPrimary = true
            )
        } else {
            onlineUnits
        }
    }

    // Smooth Uber/DiDi telemetry tween per real unit ID
    val unitTweens = remember { mutableStateMapOf<String, UnitTelemetryTween>() }
    var lastFrameNanos by remember { mutableStateOf(0L) }
    LaunchedEffect(effectiveUnits) {
        val now = System.nanoTime()
        val activeIds = effectiveUnits.map { it.id }.toSet()
        unitTweens.keys.filterNot { it in activeIds }.forEach { unitTweens.remove(it) }
        effectiveUnits.forEach { unit ->
            val existing = unitTweens[unit.id]
            if (existing == null) {
                unitTweens[unit.id] = UnitTelemetryTween(
                    unit = unit,
                    startLat = unit.lat,
                    startLng = unit.lng,
                    startHeading = unit.headingDegrees,
                    targetLat = unit.lat,
                    targetLng = unit.lng,
                    targetHeading = unit.headingDegrees,
                    startedAtNanos = now
                )
            } else if (
                existing.targetLat != unit.lat ||
                existing.targetLng != unit.lng ||
                existing.targetHeading != unit.headingDegrees
            ) {
                val (curPos, curHeading) = existing.sample(lastFrameNanos.takeIf { it > 0L } ?: now)
                unitTweens[unit.id] = UnitTelemetryTween(
                    unit = unit,
                    startLat = curPos.latitude,
                    startLng = curPos.longitude,
                    startHeading = curHeading,
                    targetLat = unit.lat,
                    targetLng = unit.lng,
                    targetHeading = unit.headingDegrees,
                    startedAtNanos = now
                )
            }
        }
    }

    if (useGoogleMaps) {
        val mapView = rememberMapViewWithLifecycle()
        var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }
        val unitMarkers = remember { mutableStateMapOf<String, Marker>() }
        var shimmerPolyline by remember { mutableStateOf<Polyline?>(null) }
        var sonarCircleOuter by remember { mutableStateOf<Circle?>(null) }
        var sonarCircleInner by remember { mutableStateOf<Circle?>(null) }
        var activeCurvedRoute by remember { mutableStateOf<List<LatLng>>(emptyList()) }

        LaunchedEffect(
            googleMapRef,
            pickupLatLng,
            destinationLatLng,
            effectiveUnits.map { "${it.id}:${it.vehicleType}:${it.isPrimary}:${it.label}" },
            isSearchingDrivers,
            pickupTitle,
            destinationTitle,
            hasLocationPermission
        ) {
            val gMap = googleMapRef ?: return@LaunchedEffect
            gMap.clear()
            unitMarkers.clear()
            shimmerPolyline = null
            sonarCircleOuter = null
            sonarCircleInner = null

            if (hasLocationPermission) {
                runCatching { gMap.isMyLocationEnabled = true }
            }

            // Dual Uber/DiDi radar sonar circles around pickup point
            sonarCircleOuter = gMap.addCircle(
                CircleOptions()
                    .center(pickupLatLng)
                    .radius(90.0)
                    .strokeWidth(3f)
                    .strokeColor(BrandEmerald.copy(alpha = 0.55f).toArgb())
                    .fillColor(BrandEmerald.copy(alpha = 0.14f).toArgb())
            )
            sonarCircleInner = gMap.addCircle(
                CircleOptions()
                    .center(pickupLatLng)
                    .radius(45.0)
                    .strokeWidth(2.5f)
                    .strokeColor(BrandSkyTint.copy(alpha = 0.45f).toArgb())
                    .fillColor(BrandBlue.copy(alpha = 0.10f).toArgb())
            )

            gMap.addMarker(
                MarkerOptions()
                    .position(pickupLatLng)
                    .title(pickupTitle)
                    .snippet(pickupSnippet)
                    .anchor(0.5f, 0.5f)
                    .zIndex(5f)
                    .icon(createUberPickupDescriptor(context))
            )

            // Only render REAL active online drivers/delivery riders
            effectiveUnits.forEach { unit ->
                val descriptor = when (unit.vehicleType) {
                    MapVehicleType.MOTO -> createDidiMotoDescriptor(context)
                    MapVehicleType.CAR -> createUberVehicleDescriptor(context, isPrimary = unit.isPrimary)
                }
                val snippetText = if (unit.vehicleType == MapVehicleType.MOTO) {
                    "Repartidor en línea · ${unit.role.displayName}"
                } else {
                    "Conductor en línea · ${unit.role.displayName}"
                }
                gMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(unit.lat, unit.lng))
                        .title(unit.label)
                        .snippet(snippetText)
                        .anchor(0.5f, 0.5f)
                        .flat(true)
                        .zIndex(if (unit.isPrimary) 6f else 4f)
                        .rotation(unit.headingDegrees)
                        .icon(descriptor)
                )?.let { marker ->
                    unitMarkers[unit.id] = marker
                }
            }

            if (destinationLatLng != null) {
                val curvedPoints = buildCurvedRoutePoints(pickupLatLng, destinationLatLng, steps = 44)
                activeCurvedRoute = curvedPoints

                gMap.addMarker(
                    MarkerOptions()
                        .position(destinationLatLng)
                        .title(destinationTitle)
                        .snippet(destinationSnippet)
                        .anchor(0.5f, 0.92f)
                        .zIndex(5f)
                        .icon(createUberDestinationDescriptor(context))
                )

                gMap.addPolyline(
                    PolylineOptions()
                        .addAll(curvedPoints)
                        .color(routeColor.copy(alpha = 0.28f).toArgb())
                        .width(22f)
                        .jointType(JointType.ROUND)
                        .startCap(RoundCap())
                        .endCap(RoundCap())
                        .zIndex(1f)
                )
                gMap.addPolyline(
                    PolylineOptions()
                        .addAll(curvedPoints)
                        .color(routeColor.toArgb())
                        .width(10f)
                        .jointType(JointType.ROUND)
                        .startCap(RoundCap())
                        .endCap(RoundCap())
                        .zIndex(2f)
                )
                shimmerPolyline = gMap.addPolyline(
                    PolylineOptions()
                        .addAll(curvedPoints.take(8))
                        .color(BrandSkyTint.copy(alpha = 0.92f).toArgb())
                        .width(12f)
                        .jointType(JointType.ROUND)
                        .startCap(RoundCap())
                        .endCap(RoundCap())
                        .zIndex(3f)
                )

                runCatching {
                    val builder = LatLngBounds.Builder()
                        .include(pickupLatLng)
                        .include(destinationLatLng)
                    curvedPoints.forEach { builder.include(it) }
                    effectiveUnits.forEach { builder.include(LatLng(it.lat, it.lng)) }
                    gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 145))
                }
            } else {
                activeCurvedRoute = emptyList()
                val camTarget = effectiveUnits.firstOrNull { it.isPrimary }?.let {
                    LatLng(it.lat, it.lng)
                } ?: pickupLatLng
                val camPos = CameraPosition.Builder()
                    .target(camTarget)
                    .zoom(15.6f)
                    .tilt(38f)
                    .build()
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))
            }
        }

        // Frame-synced smooth Uber/DiDi telemetry tweening, radar sonar & route shimmer (~30fps JNI updates)
        LaunchedEffect(googleMapRef, pickupLatLng, destinationLatLng, isSearchingDrivers) {
            if (googleMapRef == null) return@LaunchedEffect
            var startNanos = 0L
            var lastUpdateNanos = 0L
            while (true) {
                withFrameNanos { frameNanos ->
                    lastFrameNanos = frameNanos
                    if (startNanos == 0L) startNanos = frameNanos
                    if (frameNanos - lastUpdateNanos >= 33_000_000L) {
                        lastUpdateNanos = frameNanos
                        val elapsedSec = (frameNanos - startNanos) / 1_000_000_000f
                        val nowWallNanos = System.nanoTime()

                        // 1. Glide real online unit markers smoothly to latest GPS fix & bearing
                        unitTweens.forEach { (id, tween) ->
                            val marker = unitMarkers[id] ?: return@forEach
                            val (pos, bearing) = tween.sample(nowWallNanos)
                            marker.position = pos
                            marker.rotation = bearing
                        }

                        // 2. Pulse radar sonar waves around pickup / active search
                        val waveSpeed = if (isSearchingDrivers) 1.55f else 2.4f
                        val outerT = (elapsedSec / waveSpeed) % 1f
                        val innerT = ((elapsedSec / waveSpeed) + 0.5f) % 1f
                        val maxRadiusMeters = if (isSearchingDrivers) 290.0 else 160.0
                        sonarCircleOuter?.apply {
                            radius = 25.0 + outerT * maxRadiusMeters
                            strokeColor = BrandEmerald.copy(alpha = (1f - outerT) * 0.75f).toArgb()
                            fillColor = BrandEmerald.copy(alpha = (1f - outerT) * 0.18f).toArgb()
                        }
                        sonarCircleInner?.apply {
                            radius = 25.0 + innerT * maxRadiusMeters
                            strokeColor = BrandSkyTint.copy(alpha = (1f - innerT) * 0.65f).toArgb()
                            fillColor = BrandBlue.copy(alpha = (1f - innerT) * 0.14f).toArgb()
                        }

                        // 3. Animate shimmering light pulse along active curved route
                        val routePts = activeCurvedRoute
                        val shimmer = shimmerPolyline
                        if (shimmer != null && routePts.size > 6) {
                            val shimmerHead = ((elapsedSec / 2.2f) % 1.2f)
                            val startIdx = (((shimmerHead - 0.22f).coerceIn(0f, 1f)) * (routePts.size - 1)).toInt()
                            val endIdx = ((shimmerHead.coerceIn(0f, 1f)) * (routePts.size - 1)).toInt()
                            if (endIdx > startIdx) {
                                shimmer.points = routePts.subList(startIdx, endIdx + 1)
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            AndroidView(
                factory = {
                    mapView.apply {
                        getMapAsync { gMap ->
                            runCatching {
                                gMap.setMapStyle(MapStyleOptions(UBER_DARK_MAP_STYLE_JSON))
                            }
                            gMap.isBuildingsEnabled = true
                            gMap.uiSettings.isMapToolbarEnabled = false
                            gMap.uiSettings.isCompassEnabled = false
                            googleMapRef = gMap
                            onGoogleMapReady?.invoke(gMap)
                            gMap.moveCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(pickupLatLng)
                                        .zoom(15.6f)
                                        .tilt(38f)
                                        .build()
                                )
                            )
                            if (onMapTap != null) {
                                gMap.setOnMapClickListener { latLng ->
                                    onMapTap(latLng)
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            UberRouteStatusPill(
                onlineCount = effectiveUnits.size,
                isSearchingDrivers = isSearchingDrivers,
                hasAssignedDriver = effectiveUnits.any { it.isPrimary } && destinationLatLng != null && etaMinutes != null,
                etaMinutes = etaMinutes,
                driverLabel = driverLabel,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(top = badgeTopPaddingDp, start = 16.dp)
            )
        }
    } else {
        InteractiveCarrilloMapCanvas(
            pickupLatLng = pickupLatLng,
            pickupTitle = pickupTitle,
            destinationLatLng = destinationLatLng,
            destinationTitle = destinationTitle,
            routeColor = routeColor,
            landmarks = landmarks,
            effectiveUnits = effectiveUnits,
            unitTweens = unitTweens,
            driverLabel = driverLabel,
            isSearchingDrivers = isSearchingDrivers,
            etaMinutes = etaMinutes,
            bottomOverlayReserveDp = bottomOverlayReserveDp,
            badgeTopPaddingDp = badgeTopPaddingDp,
            onMapTap = onMapTap,
            modifier = modifier
        )
    }
}

@Composable
private fun UberRouteStatusPill(
    onlineCount: Int,
    isSearchingDrivers: Boolean,
    hasAssignedDriver: Boolean,
    etaMinutes: Int?,
    driverLabel: String,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "status_pill_pulse")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_dot_alpha"
    )

    val dotColor = when {
        isSearchingDrivers || onlineCount > 0 -> BrandEmerald
        else -> Slate400
    }
    val text = when {
        isSearchingDrivers -> "Buscando unidades en línea..."
        hasAssignedDriver -> "$driverLabel · ~${etaMinutes ?: 3} min"
        onlineCount == 1 -> "1 unidad en línea"
        onlineCount > 1 -> "$onlineCount unidades en línea"
        else -> "0 unidades en línea"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PureBlack.copy(alpha = 0.88f))
            .border(
                width = 1.dp,
                color = if (isSearchingDrivers || onlineCount > 0) {
                    BrandEmerald.copy(alpha = 0.65f)
                } else {
                    Slate700
                },
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = if (isSearchingDrivers || onlineCount > 0) dotAlpha else 0.85f))
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text,
            color = if (onlineCount > 0 || isSearchingDrivers || hasAssignedDriver) PureWhite else Slate300,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InteractiveCarrilloMapCanvas(
    pickupLatLng: LatLng,
    pickupTitle: String,
    destinationLatLng: LatLng?,
    destinationTitle: String,
    routeColor: Color,
    landmarks: List<CarrilloLandmark>,
    effectiveUnits: List<OnlineMapUnit>,
    unitTweens: Map<String, UnitTelemetryTween>,
    driverLabel: String,
    isSearchingDrivers: Boolean,
    etaMinutes: Int?,
    bottomOverlayReserveDp: Dp,
    badgeTopPaddingDp: Dp,
    onMapTap: ((LatLng) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    var nowWallNanos by remember { mutableStateOf(System.nanoTime()) }
    LaunchedEffect(Unit) {
        var startNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (startNanos == 0L) startNanos = frameNanos
                elapsedSeconds = (frameNanos - startNanos) / 1_000_000_000f
                nowWallNanos = System.nanoTime()
            }
        }
    }

    val sonarWave1 = (elapsedSeconds / if (isSearchingDrivers) 1.55f else 2.2f) % 1f
    val sonarWave2 = ((elapsedSeconds / if (isSearchingDrivers) 1.55f else 2.2f) + 0.5f) % 1f
    val routeShimmerT = (elapsedSeconds / 2.2f) % 1.25f

    val localLatSpan = 0.011
    val localLngSpan = 0.011
    var rawMinLat = min(pickupLatLng.latitude - localLatSpan, destinationLatLng?.latitude ?: pickupLatLng.latitude)
    var rawMaxLat = max(pickupLatLng.latitude + localLatSpan, destinationLatLng?.latitude ?: pickupLatLng.latitude)
    var rawMinLng = min(pickupLatLng.longitude - localLngSpan, destinationLatLng?.longitude ?: pickupLatLng.longitude)
    var rawMaxLng = max(pickupLatLng.longitude + localLngSpan, destinationLatLng?.longitude ?: pickupLatLng.longitude)

    effectiveUnits.forEach { unit ->
        rawMinLat = min(rawMinLat, unit.lat)
        rawMaxLat = max(rawMaxLat, unit.lat)
        rawMinLng = min(rawMinLng, unit.lng)
        rawMaxLng = max(rawMaxLng, unit.lng)
    }

    val latPadding = max((rawMaxLat - rawMinLat) * 0.18, 0.0035)
    val lngPadding = max((rawMaxLng - rawMinLng) * 0.18, 0.0035)
    val minLat = rawMinLat - latPadding
    val maxLat = rawMaxLat + latPadding
    val minLng = rawMinLng - lngPadding
    val maxLng = rawMaxLng + lngPadding

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        val totalWidthPx = with(density) { maxWidth.toPx() }
        val totalHeightPx = with(density) { maxHeight.toPx() }
        val topReservePx = with(density) { 86.dp.toPx() }
        val bottomReservePx = with(density) { bottomOverlayReserveDp.toPx() }
        val mapAreaHeightPx = (totalHeightPx - topReservePx - bottomReservePx).coerceAtLeast(200f)

        fun toOffset(latLng: LatLng): Offset {
            val nx = ((latLng.longitude - minLng) / (maxLng - minLng)).toFloat().coerceIn(0.05f, 0.95f)
            val ny = ((maxLat - latLng.latitude) / (maxLat - minLat)).toFloat().coerceIn(0.05f, 0.95f)
            return Offset(
                x = nx * totalWidthPx,
                y = topReservePx + ny * mapAreaHeightPx
            )
        }

        fun toLatLng(offset: Offset): LatLng {
            val nx = (offset.x / totalWidthPx).coerceIn(0.04f, 0.96f)
            val ny = ((offset.y - topReservePx) / mapAreaHeightPx).coerceIn(0.04f, 0.96f)
            val lng = minLng + nx * (maxLng - minLng)
            val lat = maxLat - ny * (maxLat - minLat)
            return LatLng(lat, lng)
        }

        val tapModifier = if (onMapTap != null) {
            Modifier.pointerInput(minLat, maxLat, minLng, maxLng, totalWidthPx, mapAreaHeightPx) {
                detectTapGestures { tapOffset ->
                    val tappedLandmark = landmarks.firstOrNull { landmark ->
                        val landmarkOffset = toOffset(landmark.latLng)
                        (landmarkOffset - tapOffset).getDistance() <= 46f
                    }
                    onMapTap(tappedLandmark?.latLng ?: toLatLng(tapOffset))
                }
            }
        } else {
            Modifier
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(tapModifier)
        ) {
            val gridCols = 8
            val gridRows = 12
            val colStep = size.width / gridCols
            val rowStep = size.height / gridRows

            for (col in 0 until gridCols) {
                for (row in 0 until gridRows) {
                    val isPark = (col == 3 && row == 4) || (col == 5 && row == 6)
                    drawRoundRect(
                        color = if (isPark) {
                            Color(0xFF0D2824).copy(alpha = 0.72f)
                        } else if ((col + row) % 2 == 0) {
                            Slate900.copy(alpha = 0.62f)
                        } else {
                            Slate900.copy(alpha = 0.34f)
                        },
                        topLeft = Offset(col * colStep + 5f, row * rowStep + 5f),
                        size = Size(colStep - 10f, rowStep - 10f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                }
            }

            for (i in 1 until gridCols) {
                val x = i * colStep
                drawLine(
                    color = if (i % 3 == 0) Slate700.copy(alpha = 0.9f) else Slate800.copy(alpha = 0.85f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = if (i % 3 == 0) 6.5f else 2.2f
                )
            }
            for (j in 1 until gridRows) {
                val y = j * rowStep
                drawLine(
                    color = if (j % 3 == 0) Slate700.copy(alpha = 0.9f) else Slate800.copy(alpha = 0.85f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (j % 3 == 0) 6.5f else 2.2f
                )
            }

            drawLine(
                color = Slate700,
                start = Offset(size.width * 0.08f, size.height * 0.80f),
                end = Offset(size.width * 0.92f, size.height * 0.12f),
                strokeWidth = 14f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Slate600.copy(alpha = 0.55f),
                start = Offset(0f, size.height * 0.45f),
                end = Offset(size.width, size.height * 0.49f),
                strokeWidth = 9f,
                cap = StrokeCap.Round
            )

            val originPos = toOffset(pickupLatLng)

            // Curved Uber/DiDi route with outer neon glow + sweeping animated light pulse
            if (destinationLatLng != null) {
                val destPos = toOffset(destinationLatLng)
                val controlPos = buildCanvasRouteControl(originPos, destPos)

                val routePath = Path().apply {
                    moveTo(originPos.x, originPos.y)
                    quadraticTo(controlPos.x, controlPos.y, destPos.x, destPos.y)
                }
                drawPath(
                    path = routePath,
                    color = routeColor.copy(alpha = 0.25f),
                    style = Stroke(width = 20f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = routePath,
                    color = routeColor,
                    style = Stroke(width = 7.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                val pulseStart = (routeShimmerT - 0.22f).coerceIn(0f, 1f)
                val pulseEnd = routeShimmerT.coerceIn(0f, 1f)
                if (pulseEnd > pulseStart + 0.01f) {
                    val shimmerPath = Path()
                    val steps = 16
                    for (s in 0..steps) {
                        val t = pulseStart + (pulseEnd - pulseStart) * (s / steps.toFloat())
                        val pt = quadraticOffset(originPos, controlPos, destPos, t)
                        if (s == 0) shimmerPath.moveTo(pt.x, pt.y) else shimmerPath.lineTo(pt.x, pt.y)
                    }
                    drawPath(
                        path = shimmerPath,
                        color = BrandSkyTint.copy(alpha = 0.92f),
                        style = Stroke(width = 8.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // Destination Square Pin
                drawCircle(
                    color = BrandRed.copy(alpha = 0.28f),
                    radius = 22f,
                    center = destPos
                )
                drawLine(
                    color = PureBlack,
                    start = destPos,
                    end = Offset(destPos.x, destPos.y - 34f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
                drawRoundRect(
                    color = PureBlack,
                    topLeft = Offset(destPos.x - 19f, destPos.y - 68f),
                    size = Size(38f, 38f),
                    cornerRadius = CornerRadius(9f, 9f)
                )
                drawRoundRect(
                    color = BrandSkyTint,
                    topLeft = Offset(destPos.x - 19f, destPos.y - 68f),
                    size = Size(38f, 38f),
                    cornerRadius = CornerRadius(9f, 9f),
                    style = Stroke(width = 2.5f)
                )
                drawRect(
                    color = PureWhite,
                    topLeft = Offset(destPos.x - 7f, destPos.y - 56f),
                    size = Size(14f, 14f)
                )
            }

            // Dual Uber/DiDi Radar Sonar Waves around Pickup Puck
            val maxSonarPx = if (isSearchingDrivers) 135f else 78f
            drawCircle(
                color = BrandEmerald.copy(alpha = (1f - sonarWave1) * 0.22f),
                radius = 20f + sonarWave1 * maxSonarPx,
                center = originPos
            )
            drawCircle(
                color = BrandEmerald.copy(alpha = (1f - sonarWave1) * 0.55f),
                radius = 20f + sonarWave1 * maxSonarPx,
                center = originPos,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = BrandSkyTint.copy(alpha = (1f - sonarWave2) * 0.42f),
                radius = 20f + sonarWave2 * maxSonarPx,
                center = originPos,
                style = Stroke(width = 2f)
            )

            // Live GPS Pickup Puck
            drawCircle(color = PureBlack, radius = 19f, center = originPos)
            drawCircle(color = PureWhite, radius = 15f, center = originPos)
            drawCircle(color = BrandEmerald, radius = 8.5f, center = originPos)

            // Render ONLY REAL online drivers / delivery riders with smooth telemetry interpolation
            effectiveUnits.forEach { unit ->
                val tween = unitTweens[unit.id]
                val (posLatLng, bearing) = tween?.sample(nowWallNanos)
                    ?: (LatLng(unit.lat, unit.lng) to unit.headingDegrees)
                val unitPos = toOffset(posLatLng)

                if (unit.isPrimary) {
                    drawCircle(
                        color = BrandEmerald.copy(alpha = 0.24f + 0.12f * (1f - sonarWave1)),
                        radius = 36f + 8f * sonarWave1,
                        center = unitPos
                    )
                }
                if (unit.vehicleType == MapVehicleType.MOTO) {
                    drawDidiDeliveryMoto(
                        center = unitPos,
                        bearingDegrees = bearing
                    )
                } else {
                    drawUberTopDownCar(
                        center = unitPos,
                        bearingDegrees = bearing,
                        isPrimary = unit.isPrimary
                    )
                }
            }
        }

        // Clickable Landmark chips
        landmarks.forEach { landmark ->
            val inBounds = landmark.latLng.latitude in minLat..maxLat &&
                landmark.latLng.longitude in minLng..maxLng
            if (inBounds) {
                val pos = toOffset(landmark.latLng)
                Row(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (pos.x - 28f).roundToInt(),
                                y = (pos.y - 16f).roundToInt()
                            )
                        }
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate800.copy(alpha = 0.88f))
                        .then(
                            if (onMapTap != null) {
                                Modifier.clickable { onMapTap(landmark.latLng) }
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BrandBlue)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = landmark.label,
                        color = Slate300,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Pickup Label
        val originPos = toOffset(pickupLatLng)
        Row(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (originPos.x - 52f).roundToInt().coerceAtLeast(12),
                        y = (originPos.y + 22f).roundToInt()
                    )
                }
                .clip(RoundedCornerShape(10.dp))
                .background(PureBlack.copy(alpha = 0.90f))
                .border(1.dp, BrandEmerald.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(BrandEmerald)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = pickupTitle,
                color = PureWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Destination Label
        if (destinationLatLng != null) {
            val destPos = toOffset(destinationLatLng)
            Row(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (destPos.x - 58f).roundToInt().coerceAtLeast(12),
                            y = (destPos.y - 98f).roundToInt().coerceAtLeast(12)
                        )
                    }
                    .clip(RoundedCornerShape(10.dp))
                    .background(PureBlack.copy(alpha = 0.92f))
                    .border(1.dp, BrandSkyTint.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = destinationTitle.removePrefix("Destino: ").take(24),
                    color = BrandSkyTint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Floating labels ONLY for real online drivers / delivery riders
        effectiveUnits.forEach { unit ->
            val tween = unitTweens[unit.id]
            val (posLatLng, _) = tween?.sample(nowWallNanos)
                ?: (LatLng(unit.lat, unit.lng) to unit.headingDegrees)
            val unitPos = toOffset(posLatLng)
            Row(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (unitPos.x - 40f).roundToInt().coerceAtLeast(12),
                            y = (unitPos.y - 54f).roundToInt().coerceAtLeast(12)
                        )
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .background(PureBlack.copy(alpha = 0.92f))
                    .border(1.5.dp, BrandEmerald, RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (unit.vehicleType == MapVehicleType.MOTO) {
                        Icons.Default.TwoWheeler
                    } else {
                        Icons.Default.DirectionsCar
                    },
                    contentDescription = unit.label,
                    tint = BrandEmerald,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (unit.isPrimary && etaMinutes != null && etaMinutes > 0 && destinationLatLng != null) {
                        "${unit.label.take(14)} · ${etaMinutes}m"
                    } else {
                        unit.label.take(18)
                    },
                    color = PureWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        UberRouteStatusPill(
            onlineCount = effectiveUnits.size,
            isSearchingDrivers = isSearchingDrivers,
            hasAssignedDriver = effectiveUnits.any { it.isPrimary } && destinationLatLng != null && etaMinutes != null,
            etaMinutes = etaMinutes,
            driverLabel = driverLabel,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = badgeTopPaddingDp, start = 16.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = badgeTopPaddingDp, end = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Slate900.copy(alpha = 0.88f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (onMapTap != null) "Carrillo Puerto · En vivo" else "Ruta en tiempo real",
                color = Slate400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun DrawScope.drawUberTopDownCar(
    center: Offset,
    bearingDegrees: Float,
    isPrimary: Boolean
) {
    val carW = if (isPrimary) 25f else 20f
    val carH = if (isPrimary) 46f else 37f
    val halfW = carW / 2f
    val halfH = carH / 2f

    drawRoundRect(
        color = PureBlack.copy(alpha = 0.48f),
        topLeft = Offset(center.x - halfW * 1.06f + 2.5f, center.y - halfH * 0.92f + 3.5f),
        size = Size(carW * 1.06f, carH * 0.98f),
        cornerRadius = CornerRadius(halfW * 0.8f, halfW * 0.8f)
    )

    rotate(degrees = bearingDegrees, pivot = center) {
        val beamPath = Path().apply {
            moveTo(center.x - halfW * 0.56f, center.y - halfH * 0.84f)
            lineTo(center.x - halfW * 1.55f, center.y - halfH * 1.95f)
            lineTo(center.x + halfW * 1.55f, center.y - halfH * 1.95f)
            lineTo(center.x + halfW * 0.56f, center.y - halfH * 0.84f)
            close()
        }
        drawPath(
            path = beamPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFFEF08A).copy(alpha = if (isPrimary) 0.22f else 0.13f)
                ),
                startY = center.y - halfH * 1.95f,
                endY = center.y - halfH * 0.84f
            )
        )

        val mirrorColor = if (isPrimary) PureWhite else Slate300
        drawRoundRect(
            color = mirrorColor,
            topLeft = Offset(center.x - halfW * 1.26f, center.y - halfH * 0.34f),
            size = Size(halfW * 0.34f, carH * 0.10f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = mirrorColor,
            topLeft = Offset(center.x + halfW * 0.92f, center.y - halfH * 0.34f),
            size = Size(halfW * 0.34f, carH * 0.10f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        val bodyPath = Path().apply {
            moveTo(center.x - halfW * 0.72f, center.y - halfH * 0.96f)
            quadraticTo(center.x, center.y - halfH * 1.07f, center.x + halfW * 0.72f, center.y - halfH * 0.96f)
            quadraticTo(center.x + halfW, center.y - halfH * 0.65f, center.x + halfW, center.y)
            lineTo(center.x + halfW * 0.96f, center.y + halfH * 0.74f)
            quadraticTo(center.x + halfW * 0.88f, center.y + halfH, center.x, center.y + halfH)
            quadraticTo(center.x - halfW * 0.88f, center.y + halfH, center.x - halfW * 0.96f, center.y + halfH * 0.74f)
            lineTo(center.x - halfW, center.y)
            quadraticTo(center.x - halfW, center.y - halfH * 0.65f, center.x - halfW * 0.72f, center.y - halfH * 0.96f)
            close()
        }
        drawPath(
            path = bodyPath,
            brush = Brush.linearGradient(
                colors = if (isPrimary) {
                    listOf(PureWhite, Color(0xFFCBD5E1))
                } else {
                    listOf(Color(0xFFF8FAFC), Color(0xFFCBD5E1))
                },
                start = Offset(center.x - halfW, center.y - halfH),
                end = Offset(center.x + halfW, center.y + halfH)
            )
        )
        drawPath(
            path = bodyPath,
            color = if (isPrimary) BrandEmerald else Slate600,
            style = Stroke(width = if (isPrimary) 2.2f else 1.4f)
        )

        drawRoundRect(
            color = Slate900,
            topLeft = Offset(center.x - halfW * 0.74f, center.y - halfH * 0.44f),
            size = Size(carW * 0.74f, carH * 0.56f),
            cornerRadius = CornerRadius(5.5f, 5.5f)
        )

        drawLine(
            color = PureWhite.copy(alpha = 0.18f),
            start = Offset(center.x - halfW * 0.52f, center.y - halfH * 0.34f),
            end = Offset(center.x + halfW * 0.18f, center.y - halfH * 0.24f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )

        drawRoundRect(
            color = if (isPrimary) PureWhite else Color(0xFFF1F5F9),
            topLeft = Offset(center.x - halfW * 0.56f, center.y - halfH * 0.14f),
            size = Size(carW * 0.56f, carH * 0.28f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        drawLine(
            color = Slate500.copy(alpha = 0.28f),
            start = Offset(center.x - halfW * 0.42f, center.y - halfH * 0.82f),
            end = Offset(center.x - halfW * 0.54f, center.y - halfH * 0.48f),
            strokeWidth = 1.2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Slate500.copy(alpha = 0.28f),
            start = Offset(center.x + halfW * 0.42f, center.y - halfH * 0.82f),
            end = Offset(center.x + halfW * 0.54f, center.y - halfH * 0.48f),
            strokeWidth = 1.2f,
            cap = StrokeCap.Round
        )

        if (isPrimary) {
            drawRoundRect(
                color = BrandEmerald,
                topLeft = Offset(center.x - halfW * 0.34f, center.y - halfH * 0.05f),
                size = Size(carW * 0.34f, carH * 0.11f),
                cornerRadius = CornerRadius(2.5f, 2.5f)
            )
        }

        drawRoundRect(
            color = Color(0xFFFEF08A),
            topLeft = Offset(center.x - halfW * 0.72f, center.y - halfH * 0.94f),
            size = Size(carW * 0.22f, carH * 0.06f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = Color(0xFFFEF08A),
            topLeft = Offset(center.x + halfW * 0.28f, center.y - halfH * 0.94f),
            size = Size(carW * 0.22f, carH * 0.06f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        drawRoundRect(
            color = BrandRed,
            topLeft = Offset(center.x - halfW * 0.70f, center.y + halfH * 0.86f),
            size = Size(carW * 0.24f, carH * 0.055f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = BrandRed,
            topLeft = Offset(center.x + halfW * 0.22f, center.y + halfH * 0.86f),
            size = Size(carW * 0.24f, carH * 0.055f),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}

private fun DrawScope.drawDidiDeliveryMoto(
    center: Offset,
    bearingDegrees: Float
) {
    val motoW = 14f
    val motoH = 30f
    val halfW = motoW / 2f
    val halfH = motoH / 2f

    drawRoundRect(
        color = PureBlack.copy(alpha = 0.42f),
        topLeft = Offset(center.x - halfW + 1.5f, center.y - halfH + 2.5f),
        size = Size(motoW, motoH),
        cornerRadius = CornerRadius(6f, 6f)
    )

    rotate(degrees = bearingDegrees, pivot = center) {
        val beamPath = Path().apply {
            moveTo(center.x - halfW * 0.4f, center.y - halfH * 0.85f)
            lineTo(center.x - halfW * 1.4f, center.y - halfH * 1.75f)
            lineTo(center.x + halfW * 1.4f, center.y - halfH * 1.75f)
            lineTo(center.x + halfW * 0.4f, center.y - halfH * 0.85f)
            close()
        }
        drawPath(
            path = beamPath,
            color = BrandSkyTint.copy(alpha = 0.16f)
        )

        drawRoundRect(
            color = Slate900,
            topLeft = Offset(center.x - 2.5f, center.y - halfH),
            size = Size(5f, 8f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawLine(
            color = Slate300,
            start = Offset(center.x - halfW * 1.15f, center.y - halfH * 0.55f),
            end = Offset(center.x + halfW * 1.15f, center.y - halfH * 0.55f),
            strokeWidth = 2.6f,
            cap = StrokeCap.Round
        )

        drawRoundRect(
            color = BrandBlue,
            topLeft = Offset(center.x - halfW * 0.65f, center.y - halfH * 0.60f),
            size = Size(motoW * 0.65f, motoH * 0.72f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        drawCircle(
            color = PureWhite,
            radius = 4.6f,
            center = Offset(center.x, center.y - halfH * 0.16f)
        )

        drawRoundRect(
            color = BrandEmerald,
            topLeft = Offset(center.x - halfW * 0.88f, center.y + halfH * 0.14f),
            size = Size(motoW * 0.88f, motoH * 0.38f),
            cornerRadius = CornerRadius(3.5f, 3.5f)
        )
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(center.x - halfW * 0.88f, center.y + halfH * 0.14f),
            size = Size(motoW * 0.88f, motoH * 0.38f),
            cornerRadius = CornerRadius(3.5f, 3.5f),
            style = Stroke(width = 1.2f)
        )
    }
}
