package com.example.viakaab.ui.components

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate300
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate600
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.ui.theme.Slate950
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

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
    bottomOverlayReserveDp: Dp = 260.dp,
    badgeTopPaddingDp: Dp = 18.dp,
    onMapTap: ((LatLng) -> Unit)? = null,
    onGoogleMapReady: ((GoogleMap) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val useGoogleMaps = remember { hasConfiguredGoogleMapsKey() }

    if (useGoogleMaps) {
        val mapView = rememberMapViewWithLifecycle()
        var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }

        LaunchedEffect(
            googleMapRef,
            pickupLatLng,
            destinationLatLng,
            pickupTitle,
            destinationTitle,
            hasLocationPermission
        ) {
            val gMap = googleMapRef ?: return@LaunchedEffect
            gMap.clear()
            if (hasLocationPermission) {
                runCatching { gMap.isMyLocationEnabled = true }
            }
            gMap.addMarker(
                MarkerOptions()
                    .position(pickupLatLng)
                    .title(pickupTitle)
                    .snippet(pickupSnippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            if (destinationLatLng != null) {
                gMap.addMarker(
                    MarkerOptions()
                        .position(destinationLatLng)
                        .title(destinationTitle)
                        .snippet(destinationSnippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                gMap.addPolyline(
                    PolylineOptions()
                        .add(pickupLatLng, destinationLatLng)
                        .color(routeColor.toArgb())
                        .width(9f)
                )
                runCatching {
                    val bounds = LatLngBounds.Builder()
                        .include(pickupLatLng)
                        .include(destinationLatLng)
                        .build()
                    gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 140))
                }
            } else {
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 15.5f))
            }
        }

        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { gMap ->
                        googleMapRef = gMap
                        onGoogleMapReady?.invoke(gMap)
                        gMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(LatLng(19.5775, -88.0453), 15f)
                        )
                        if (onMapTap != null) {
                            gMap.setOnMapClickListener { latLng ->
                                onMapTap(latLng)
                            }
                        }
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        )
    } else {
        InteractiveCarrilloMapCanvas(
            pickupLatLng = pickupLatLng,
            pickupTitle = pickupTitle,
            destinationLatLng = destinationLatLng,
            destinationTitle = destinationTitle,
            routeColor = routeColor,
            landmarks = landmarks,
            bottomOverlayReserveDp = bottomOverlayReserveDp,
            badgeTopPaddingDp = badgeTopPaddingDp,
            onMapTap = onMapTap,
            modifier = modifier
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
    bottomOverlayReserveDp: Dp,
    badgeTopPaddingDp: Dp,
    onMapTap: ((LatLng) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Default urban bounding box around Felipe Carrillo Puerto
    val townMinLat = 19.566
    val townMaxLat = 19.591
    val townMinLng = -88.055
    val townMaxLng = -88.033

    val isIntercity = destinationLatLng != null && (
        abs(destinationLatLng.latitude - pickupLatLng.latitude) > 0.15 ||
            abs(destinationLatLng.longitude - pickupLatLng.longitude) > 0.15
        )

    val rawMinLat = if (isIntercity && destinationLatLng != null) {
        min(pickupLatLng.latitude, destinationLatLng.latitude)
    } else {
        min(townMinLat, min(pickupLatLng.latitude, destinationLatLng?.latitude ?: pickupLatLng.latitude))
    }
    val rawMaxLat = if (isIntercity && destinationLatLng != null) {
        max(pickupLatLng.latitude, destinationLatLng.latitude)
    } else {
        max(townMaxLat, max(pickupLatLng.latitude, destinationLatLng?.latitude ?: pickupLatLng.latitude))
    }
    val rawMinLng = if (isIntercity && destinationLatLng != null) {
        min(pickupLatLng.longitude, destinationLatLng.longitude)
    } else {
        min(townMinLng, min(pickupLatLng.longitude, destinationLatLng?.longitude ?: pickupLatLng.longitude))
    }
    val rawMaxLng = if (isIntercity && destinationLatLng != null) {
        max(pickupLatLng.longitude, destinationLatLng.longitude)
    } else {
        max(townMaxLng, max(pickupLatLng.longitude, destinationLatLng?.longitude ?: pickupLatLng.longitude))
    }

    val latPadding = max((rawMaxLat - rawMinLat) * 0.20, 0.004)
    val lngPadding = max((rawMaxLng - rawMinLng) * 0.20, 0.004)
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
                    val tappedInView = landmarks.firstOrNull { landmark ->
                        val landmarkOffset = toOffset(landmark.latLng)
                        (landmarkOffset - tapOffset).getDistance() <= 46f
                    }
                    onMapTap(tappedInView?.latLng ?: toLatLng(tapOffset))
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
            // Subtle city grid
            val gridCols = 9
            val gridRows = 12
            val colStep = size.width / gridCols
            val rowStep = size.height / gridRows

            for (i in 1 until gridCols) {
                val x = i * colStep
                drawLine(
                    color = Slate800.copy(alpha = 0.7f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = if (i % 3 == 0) 3.5f else 1.2f
                )
            }
            for (j in 1 until gridRows) {
                val y = j * rowStep
                drawLine(
                    color = Slate800.copy(alpha = 0.7f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (j % 3 == 0) 3.5f else 1.2f
                )
            }

            // Main Carretera 307 diagonal artery (Chetumal <-> Carrillo Puerto <-> Tulum)
            val caribeStart = Offset(size.width * 0.12f, size.height * 0.75f)
            val caribeEnd = Offset(size.width * 0.88f, size.height * 0.14f)
            drawLine(
                color = Slate700,
                start = caribeStart,
                end = caribeEnd,
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Slate600.copy(alpha = 0.5f),
                start = Offset(0f, size.height * 0.44f),
                end = Offset(size.width, size.height * 0.48f),
                strokeWidth = 7f,
                cap = StrokeCap.Round
            )

            // Route line between origin & destination
            val originPos = toOffset(pickupLatLng)
            if (destinationLatLng != null) {
                val destPos = toOffset(destinationLatLng)
                drawLine(
                    color = routeColor.copy(alpha = 0.35f),
                    start = originPos,
                    end = destPos,
                    strokeWidth = 14f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = routeColor,
                    start = originPos,
                    end = destPos,
                    strokeWidth = 6f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(22f, 12f), 0f)
                )

                // Destination glow & pin
                drawCircle(
                    color = BrandRed.copy(alpha = 0.25f),
                    radius = 28f,
                    center = destPos
                )
                drawCircle(
                    color = BrandRed,
                    radius = 12f,
                    center = destPos
                )
                drawCircle(
                    color = PureWhite,
                    radius = 4.5f,
                    center = destPos
                )
            }

            // Origin glow & pin
            drawCircle(
                color = BrandEmerald.copy(alpha = 0.25f),
                radius = 30f,
                center = originPos
            )
            drawCircle(
                color = BrandEmerald,
                radius = 13f,
                center = originPos
            )
            drawCircle(
                color = PureWhite,
                radius = 5f,
                center = originPos
            )
        }

        // Landmark tags within visible bounds
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
                        .background(Slate800.copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
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

        // Origin & Destination Floating Labels
        val originPos = toOffset(pickupLatLng)
        Column(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (originPos.x - 42f).roundToInt().coerceAtLeast(12),
                        y = (originPos.y + 18f).roundToInt()
                    )
                }
                .clip(RoundedCornerShape(8.dp))
                .background(Slate900.copy(alpha = 0.9f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = pickupTitle,
                color = BrandEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (destinationLatLng != null) {
            val destPos = toOffset(destinationLatLng)
            Row(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (destPos.x - 54f).roundToInt().coerceAtLeast(12),
                            y = (destPos.y - 54f).roundToInt().coerceAtLeast(12)
                        )
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate900.copy(alpha = 0.92f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = BrandRed,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = destinationTitle.removePrefix("Destino: ").take(24),
                    color = BrandSkyTint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top-end Local Map Mode Status Badge
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
                text = if (onMapTap != null) "Toca el mapa · Carrillo Puerto" else "Vista de ruta local",
                color = Slate400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
