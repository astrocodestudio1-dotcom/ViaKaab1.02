package com.example.viakaab.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessibleForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.R
import com.example.viakaab.data.CarrilloCommerceCatalog
import com.example.viakaab.model.EatsCategory
import com.example.viakaab.model.LocalCommerce
import com.example.viakaab.model.ScheduledRide
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.model.UserRole
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.BrandSkyTint
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate300
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate500
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.ui.theme.Slate950
import com.example.viakaab.ui.theme.UberCard
import com.example.viakaab.ui.theme.UberStroke
import com.example.viakaab.viewmodel.MainAppViewModel

@Composable
fun MainContainerScreen(
    viewModel: MainAppViewModel,
    onOpenPassengerMap: (ServiceMode, String) -> Unit,
    onOpenQuickDestination: (ServiceMode, String) -> Unit = { mode, _ -> onOpenPassengerMap(mode, "") },
    onOpenEatsMarketplace: (String, String) -> Unit,
    onOpenDriverMap: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenPaymentMethods: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenGeminiChat: () -> Unit,
    onOpenDriverProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showScheduleDialog by rememberSaveable { mutableStateOf(false) }

    val session by viewModel.session.collectAsStateWithLifecycle()
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    val scheduledRides by viewModel.scheduledRides.collectAsStateWithLifecycle()
    val inboxMessages by viewModel.inboxMessages.collectAsStateWithLifecycle()
    val passengerRating by viewModel.passengerRating.collectAsStateWithLifecycle()
    val unreadCount = inboxMessages.count { it.isUnread }

    if (showScheduleDialog) {
        ScheduleRideDialog(
            scheduledRides = scheduledRides,
            onSchedule = { dest, date, time, mode ->
                viewModel.scheduleRide(dest, date, time, mode, "Reserva programada")
            },
            onCancelRide = viewModel::cancelScheduledRide,
            onLaunchNow = { mode ->
                showScheduleDialog = false
                onOpenPassengerMap(mode, "")
            },
            onDismiss = { showScheduleDialog = false }
        )
    }

    Scaffold(
        containerColor = Slate950,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                HorizontalDivider(color = Slate800, thickness = 1.dp)
                NavigationBar(
                    containerColor = Slate900,
                    contentColor = PureWhite,
                    windowInsets = WindowInsets.navigationBars
                ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("tab_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Opciones") },
                    label = { Text("Opciones") },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("tab_options")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Actividad") },
                    label = { Text("Actividad") },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("tab_activity")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = BrandEmerald, contentColor = PureWhite) {
                                        Text(unreadCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Cuenta")
                        }
                    },
                    label = { Text("Cuenta") },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("tab_account")
                )
            }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedTab) {
                0 -> HomeTabContent(
                    scheduledCount = scheduledRides.size,
                    onOpenPassengerMap = onOpenPassengerMap,
                    onOpenQuickDestination = onOpenQuickDestination,
                    onOpenEatsMarketplace = onOpenEatsMarketplace,
                    onOpenSchedule = { showScheduleDialog = true }
                )
                1 -> ServicesTabContent(
                    onOpenPassengerMap = onOpenPassengerMap,
                    onOpenEatsMarketplace = onOpenEatsMarketplace,
                    onOpenSchedule = { showScheduleDialog = true }
                )
                2 -> ActivityTabContent(
                    trips = trips,
                    scheduledRides = scheduledRides,
                    onRepeatTrip = { mode -> onOpenPassengerMap(mode, "") },
                    onCancelScheduled = viewModel::cancelScheduledRide,
                    onRequestTrip = { onOpenPassengerMap(ServiceMode.STANDARD, "") }
                )
                3 -> AccountTabContent(
                    session = session,
                    passengerRating = passengerRating,
                    inboxMessages = inboxMessages,
                    onMarkInboxRead = viewModel::markInboxRead,
                    onOpenProfile = onOpenProfile,
                    onOpenSupport = onOpenSupport,
                    onOpenPaymentMethods = onOpenPaymentMethods,
                    onOpenTerms = onOpenTerms,
                    onOpenGeminiChat = onOpenGeminiChat,
                    onOpenDriverProfile = onOpenDriverProfile,
                    onSwitchRole = { newRole: UserRole ->
                        viewModel.switchRole(newRole)
                        if (newRole.isDriverRole) {
                            onOpenDriverMap()
                        }
                    },
                    onLogout = {
                        viewModel.logout()
                        onLogout()
                    }
                )
            }
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = BrandEmerald,
    selectedTextColor = PureWhite,
    unselectedIconColor = Slate400,
    unselectedTextColor = Slate400,
    indicatorColor = Slate800
)

@Composable
private fun HomeTabContent(
    scheduledCount: Int,
    onOpenPassengerMap: (ServiceMode, String) -> Unit,
    onOpenQuickDestination: (ServiceMode, String) -> Unit,
    onOpenEatsMarketplace: (String, String) -> Unit,
    onOpenSchedule: () -> Unit
) {
    var showEatsTab by rememberSaveable { mutableStateOf(false) }
    val gpsPulseTransition = rememberInfiniteTransition(label = "home_gps_pulse")
    val gpsDotAlpha by gpsPulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "home_gps_dot"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Slate950, Slate900, Slate950)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Slate800.copy(alpha = 0.92f))
                    .border(1.dp, Slate700, RoundedCornerShape(24.dp))
                    .clickable { onOpenPassengerMap(ServiceMode.STANDARD, "") }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(BrandEmerald.copy(alpha = gpsDotAlpha))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = BrandSkyTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Felipe Carrillo Puerto · GPS",
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate800.copy(alpha = 0.92f))
                    .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenSchedule)
                    .testTag("home_schedule_button"),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (scheduledCount > 0) {
                            Badge(containerColor = BrandEmerald, contentColor = PureWhite) {
                                Text(scheduledCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Agenda de viajes",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Segmented Pill Switch: ViaKaab | ViaKaab Eats
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Slate900,
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (!showEatsTab) BrandEmerald else Color.Transparent)
                        .clickable { showEatsTab = false }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = if (!showEatsTab) PureWhite else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ViaKaab",
                            color = if (!showEatsTab) PureWhite else Slate400,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (showEatsTab) BrandBlue else Color.Transparent)
                        .clickable { showEatsTab = true }
                        .padding(vertical = 12.dp)
                        .testTag("home_eats_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = if (showEatsTab) PureWhite else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ViaKaab Eats",
                            color = if (showEatsTab) PureWhite else Slate400,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        AnimatedContent(targetState = showEatsTab, label = "home_mode_switch") { isEats ->
            if (!isEats) {
                Column {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = UberCard),
                        border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { onOpenPassengerMap(ServiceMode.STANDARD, "") }
                            .testTag("home_search_where_to")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(BrandEmerald.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = BrandEmerald,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¿A dónde vas?",
                                    color = PureWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tarifa justa · Desde $25 MXN",
                                    color = BrandEmerald,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenSchedule,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, UberStroke),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("Más tarde", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Centro", "TecNM", "Terminal ADO", "Hospital", "Mercado").forEach { spot ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Slate800,
                                border = BorderStroke(1.dp, Slate700),
                                modifier = Modifier.clickable {
                                    onOpenQuickDestination(ServiceMode.STANDARD, spot)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = BrandEmerald,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = spot,
                                        color = Slate300,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Para ti",
                        color = PureWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 26.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HomeQuickOption(
                            icon = Icons.Default.DirectionsCar,
                            accentColor = BrandEmerald,
                            label = "Viaje",
                            modifier = Modifier.weight(1f),
                            testTag = "home_option_trip",
                            onClick = { onOpenPassengerMap(ServiceMode.STANDARD, "") }
                        )
                        HomeQuickOption(
                            icon = Icons.Default.AccessibleForward,
                            accentColor = BrandSky,
                            label = "Personas",
                            modifier = Modifier.weight(1f),
                            testTag = "home_option_seniors",
                            onClick = {
                                onOpenPassengerMap(
                                    ServiceMode.STANDARD,
                                    "Asistencia especial para adulto mayor"
                                )
                            }
                        )
                        HomeQuickOption(
                            icon = Icons.Default.Inventory2,
                            accentColor = BrandBlue,
                            label = "Envíos",
                            modifier = Modifier.weight(1f),
                            testTag = "home_option_delivery",
                            onClick = { onOpenPassengerMap(ServiceMode.DELIVERY, "") }
                        )
                        HomeQuickOption(
                            icon = Icons.Default.EventAvailable,
                            accentColor = BrandSkyTint,
                            label = "Reserva",
                            modifier = Modifier.weight(1f),
                            testTag = "home_option_reserve",
                            onClick = onOpenSchedule
                        )
                    }

                    Text(
                        text = "Viajes por Quintana Roo",
                        color = PureWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.08f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPassengerMap(ServiceMode.INTERCITY, "") }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(196.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_viakaab_hero_caribe),
                                contentDescription = "Viajes por Quintana Roo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Slate950.copy(alpha = 0.15f),
                                                Slate950.copy(alpha = 0.88f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(20.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BrandEmerald.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "CARIBE EXPRESS · SIN COMISIÓN",
                                        color = BrandEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tu próxima escapada",
                                    color = PureWhite,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Felipe Carrillo Puerto ➔ Tulum, Chetumal o Playa",
                                    color = Slate300,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        border = BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenEatsMarketplace("ALL", "") }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_viakaab_eats_banner),
                                    contentDescription = "ViaKaab Eats entregas locales",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Slate900.copy(alpha = 0.92f))
                                            )
                                        )
                                )
                                Text(
                                    text = "Comercios en Carrillo Puerto",
                                    color = PureWhite,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                )
                            }
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Elige comida, medicamentos o despensa de tiendas locales en Felipe Carrillo Puerto.",
                                    color = Slate300,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { onOpenEatsMarketplace("ALL", "") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandEmerald,
                                        contentColor = PureWhite
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("eats_explore_stores_button")
                                ) {
                                    Text("Ver Comercios y Menús", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EatsCategoryCard(
                            icon = Icons.Default.Restaurant,
                            accent = BrandEmerald,
                            title = "Antojitos",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("eats_cat_antojitos")
                        ) {
                            onOpenEatsMarketplace(EatsCategory.ANTOJITOS.key, "")
                        }
                        EatsCategoryCard(
                            icon = Icons.Default.LocalPharmacy,
                            accent = BrandBlue,
                            title = "Farmacia",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("eats_cat_farmacia")
                        ) {
                            onOpenEatsMarketplace(EatsCategory.FARMACIA.key, "")
                        }
                        EatsCategoryCard(
                            icon = Icons.Default.ShoppingCart,
                            accent = BrandSkyTint,
                            title = "Súper",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("eats_cat_super")
                        ) {
                            onOpenEatsMarketplace(EatsCategory.SUPER.key, "")
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comercios locales cerca de ti",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ver todos",
                            color = BrandEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onOpenEatsMarketplace("ALL", "") }
                        )
                    }

                    CarrilloCommerceCatalog.commerces.take(6).forEach { store ->
                        FeaturedHomeStoreCard(
                            store = store,
                            onClick = { onOpenEatsMarketplace(store.category.key, store.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedHomeStoreCard(
    store: LocalCommerce,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = BorderStroke(1.dp, store.accentColor.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(store.accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (store.category) {
                        EatsCategory.ANTOJITOS -> Icons.Default.Restaurant
                        EatsCategory.FARMACIA -> Icons.Default.LocalPharmacy
                        EatsCategory.SUPER -> Icons.Default.ShoppingCart
                        EatsCategory.ALL -> Icons.Default.Storefront
                    },
                    contentDescription = null,
                    tint = store.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = store.name,
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${store.category.label} · ${store.neighborhood}",
                    color = Slate400,
                    fontSize = 12.sp
                )
                Text(
                    text = "${store.deliveryTimeLabel} · Envío $${store.deliveryFeeMxn} MXN",
                    color = BrandEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = store.rating,
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Abrir menú",
                    tint = Slate300,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EatsCategoryCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
        modifier = modifier
            .height(106.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun HomeQuickOption(
    icon: ImageVector,
    accentColor: Color,
    label: String,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = label,
            color = PureWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ServicesTabContent(
    onOpenPassengerMap: (ServiceMode, String) -> Unit,
    onOpenEatsMarketplace: (String, String) -> Unit,
    onOpenSchedule: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .background(
                Brush.verticalGradient(colors = listOf(Slate950, Slate900))
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Opciones",
            color = PureWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Elige el servicio ideal para ti hoy",
            color = Slate400,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 22.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ServiceOptionCard(
                icon = Icons.Default.DirectionsCar,
                accentColor = BrandEmerald,
                title = "Viaje Local",
                modifier = Modifier.weight(1f),
                testTag = "service_trip_card",
                onClick = { onOpenPassengerMap(ServiceMode.STANDARD, "") }
            )
            ServiceOptionCard(
                icon = Icons.Default.AccessibleForward,
                accentColor = BrandSky,
                title = "Personas Mayores",
                modifier = Modifier.weight(1f),
                testTag = "service_seniors_card",
                onClick = {
                    onOpenPassengerMap(ServiceMode.STANDARD, "Asistencia especial para adulto mayor")
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ServiceOptionCard(
                icon = Icons.Default.Inventory2,
                accentColor = BrandBlue,
                title = "Envíos",
                modifier = Modifier.weight(1f),
                testTag = "service_delivery_card",
                onClick = { onOpenPassengerMap(ServiceMode.DELIVERY, "") }
            )
            ServiceOptionCard(
                icon = Icons.Default.EventAvailable,
                accentColor = BrandSkyTint,
                title = "Reserva",
                modifier = Modifier.weight(1f),
                testTag = "service_reserve_card",
                onClick = onOpenSchedule
            )
            ServiceOptionCard(
                icon = Icons.Default.Restaurant,
                accentColor = BrandEmerald,
                title = "Sal a comer",
                modifier = Modifier.weight(1f),
                testTag = "service_food_card",
                onClick = { onOpenEatsMarketplace(EatsCategory.ANTOJITOS.key, "") }
            )
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenPassengerMap(ServiceMode.INTERCITY, "") }
                .testTag("service_intercity_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BrandBlue.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Viajes Ciudad a Ciudad",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Traslados hacia Tulum, Chetumal, Playa del Carmen o Cancún.",
                        color = Slate400,
                        fontSize = 13.sp
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = BrandSkyTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ServiceOptionCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
        modifier = modifier
            .height(122.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScheduleRideDialog(
    scheduledRides: List<ScheduledRide>,
    onSchedule: (String, String, String, ServiceMode) -> Unit,
    onCancelRide: (String) -> Unit,
    onLaunchNow: (ServiceMode) -> Unit,
    onDismiss: () -> Unit
) {
    var destination by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf("Mañana") }
    var timeText by rememberSaveable { mutableStateOf("08:30 AM") }
    var selectedMode by rememberSaveable { mutableStateOf(ServiceMode.STANDARD) }

    val dialogFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Slate950,
        unfocusedContainerColor = Slate950,
        focusedTextColor = PureWhite,
        unfocusedTextColor = PureWhite,
        focusedBorderColor = BrandEmerald,
        unfocusedBorderColor = Slate700,
        focusedLabelColor = BrandEmerald,
        unfocusedLabelColor = Slate400,
        cursorColor = BrandEmerald
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        titleContentColor = PureWhite,
        textContentColor = Slate300,
        title = { Text("Programar Viaje o Reserva", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destino (ej. Terminal ADO, Tulum)") },
                    singleLine = true,
                    colors = dialogFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Fecha") },
                        singleLine = true,
                        colors = dialogFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Hora") },
                        singleLine = true,
                        colors = dialogFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ServiceMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(mode.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Slate800,
                                labelColor = Slate300,
                                selectedContainerColor = BrandBlue,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
                if (scheduledRides.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Reservas activas:", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    scheduledRides.take(3).forEach { ride ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${ride.destination} (${ride.dateLabel}, ${ride.timeLabel})",
                                color = Slate300,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { onCancelRide(ride.id) }) {
                                Text("Quitar", color = BrandSkyTint, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val dest = destination.trim().ifBlank { "Terminal ADO" }
                    onSchedule(dest, dateText.ifBlank { "Hoy" }, timeText.ifBlank { "Ahora" }, selectedMode)
                    onDismiss()
                }
            ) {
                Text("Guardar Reserva", color = BrandEmerald, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { onLaunchNow(selectedMode) }) {
                Text("Pedir Ahora", color = BrandSkyTint)
            }
        }
    )
}
