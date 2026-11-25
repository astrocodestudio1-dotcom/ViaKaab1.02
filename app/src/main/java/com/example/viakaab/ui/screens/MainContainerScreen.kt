package com.example.viakaab.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.model.ScheduledRide
import com.example.viakaab.model.ServiceMode
import com.example.viakaab.model.UserRole
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate500
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.ui.theme.UberBlack
import com.example.viakaab.ui.theme.UberCard
import com.example.viakaab.ui.theme.UberMuted
import com.example.viakaab.ui.theme.UberStroke
import com.example.viakaab.viewmodel.MainAppViewModel

@Composable
fun MainContainerScreen(
    viewModel: MainAppViewModel,
    onOpenPassengerMap: (ServiceMode, String) -> Unit,
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
        containerColor = if (selectedTab == 0) UberBlack else Slate900,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = Slate800,
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
                                    Badge { Text(unreadCount.toString()) }
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
                    onOpenSchedule = { showScheduleDialog = true }
                )
                1 -> ServicesTabContent(
                    onOpenPassengerMap = onOpenPassengerMap,
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
    selectedIconColor = PureWhite,
    selectedTextColor = PureWhite,
    unselectedIconColor = Slate400,
    unselectedTextColor = Slate400,
    indicatorColor = Slate700
)

@Composable
private fun HomeTabContent(
    scheduledCount: Int,
    onOpenPassengerMap: (ServiceMode, String) -> Unit,
    onOpenSchedule: () -> Unit
) {
    var showEatsTab by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .background(UberBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PureWhite.copy(alpha = 0.14f))
                    .clickable { onOpenPassengerMap(ServiceMode.STANDARD, "") }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(BrandEmerald)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Felipe Carrillo Puerto · GPS",
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PureWhite.copy(alpha = 0.14f))
                    .clickable(onClick = onOpenSchedule)
                    .testTag("home_schedule_button"),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (scheduledCount > 0) {
                            Badge { Text(scheduledCount.toString()) }
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "ViaKaab",
                color = if (!showEatsTab) PureWhite else Slate500,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { showEatsTab = false }
                    .padding(vertical = 4.dp)
            )
            Text(
                text = "ViaKaab Eats",
                color = if (showEatsTab) PureWhite else Slate500,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { showEatsTab = true }
                    .padding(vertical = 4.dp)
                    .testTag("home_eats_tab")
            )
        }

        AnimatedContent(targetState = showEatsTab, label = "home_mode_switch") { isEats ->
            if (!isEats) {
                Column {
                    Card(
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = UberCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 28.dp)
                            .clickable { onOpenPassengerMap(ServiceMode.STANDARD, "") }
                            .testTag("home_search_where_to")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = PureWhite,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "¿A dónde vas?",
                                color = PureWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = onOpenSchedule,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, UberStroke),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("📅 Más tarde", color = PureWhite, fontSize = 13.sp)
                            }
                        }
                    }

                    Text(
                        text = "Para ti",
                        color = PureWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HomeQuickOption(
                            emoji = "🚗",
                            label = "Viaje",
                            modifier = Modifier.weight(1f),
                            testTag = "home_option_trip",
                            onClick = { onOpenPassengerMap(ServiceMode.STANDARD, "") }
                        )
                        HomeQuickOption(
                            emoji = "👵",
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
                            emoji = "📦",
                            label = "Envíos",
                            modifier = Modifier.weight(1f),
                            testTag = "home_option_delivery",
                            onClick = { onOpenPassengerMap(ServiceMode.DELIVERY, "") }
                        )
                        HomeQuickOption(
                            emoji = "📅",
                            label = "Reserva",
                            modifier = Modifier.weight(1f),
                            testTag = "home_option_reserve",
                            onClick = onOpenSchedule
                        )
                    }

                    Text(
                        text = "Viajes por Quintana Roo",
                        color = PureWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = UberCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPassengerMap(ServiceMode.INTERCITY, "") }
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Tu próxima escapada",
                                color = PureWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = "Viaja de Felipe Carrillo Puerto a Tulum, Chetumal o Playa negociando tu tarifa",
                                color = UberMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = UberCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOpenPassengerMap(ServiceMode.DELIVERY, "Pedido ViaKaab Eats")
                            }
                    ) {
                        Column(modifier = Modifier.padding(22.dp)) {
                            Text(
                                text = "🍽️ Entregas de Comida y Mandados",
                                color = PureWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Solicita un repartidor para recoger tu pedido en restaurantes, taquerías, farmacias o súper en Felipe Carrillo Puerto.",
                                color = UberMuted,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    onOpenPassengerMap(ServiceMode.DELIVERY, "Pedido de comida local")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandEmerald,
                                    contentColor = PureWhite
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Pedir Repartidor Ahora", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EatsCategoryCard("🌮", "Antojitos", Modifier.weight(1f)) {
                            onOpenPassengerMap(ServiceMode.DELIVERY, "Recoger antojitos locales")
                        }
                        EatsCategoryCard("💊", "Farmacia", Modifier.weight(1f)) {
                            onOpenPassengerMap(ServiceMode.DELIVERY, "Compra / envío de farmacia")
                        }
                        EatsCategoryCard("🛒", "Súper", Modifier.weight(1f)) {
                            onOpenPassengerMap(ServiceMode.DELIVERY, "Mandado de despensa")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EatsCategoryCard(
    emoji: String,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = UberCard),
        modifier = modifier
            .height(104.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun HomeQuickOption(
    emoji: String,
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
            colors = CardDefaults.cardColors(containerColor = UberCard),
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 28.sp)
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
    onOpenSchedule: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .background(Slate900)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Opciones",
            color = PureWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Obtén de todo, vayas a donde vayas",
            color = Slate400,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ServiceOptionCard(
                emoji = "🚗",
                title = "Viaje Local",
                modifier = Modifier.weight(1f),
                testTag = "service_trip_card",
                onClick = { onOpenPassengerMap(ServiceMode.STANDARD, "") }
            )
            ServiceOptionCard(
                emoji = "👵",
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
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServiceOptionCard(
                emoji = "📦",
                title = "Envíos",
                modifier = Modifier.weight(1f),
                testTag = "service_delivery_card",
                onClick = { onOpenPassengerMap(ServiceMode.DELIVERY, "") }
            )
            ServiceOptionCard(
                emoji = "📅",
                title = "Reserva",
                modifier = Modifier.weight(1f),
                testTag = "service_reserve_card",
                onClick = onOpenSchedule
            )
            ServiceOptionCard(
                emoji = "🍽️",
                title = "Sal a comer",
                modifier = Modifier.weight(1f),
                testTag = "service_food_card",
                onClick = { onOpenPassengerMap(ServiceMode.DELIVERY, "Entrega ViaKaab Eats") }
            )
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenPassengerMap(ServiceMode.INTERCITY, "") }
                .testTag("service_intercity_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🚕 Viajes Ciudad a Ciudad",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Traslados desde Felipe Carrillo Puerto hacia Tulum, Chetumal, Playa del Carmen o Cancún.",
                    color = Slate400,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ServiceOptionCard(
    emoji: String,
    title: String,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        modifier = modifier
            .height(120.dp)
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
            Text(text = emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(6.dp))
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Viaje o Reserva") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destino (ej. Terminal ADO, Tulum)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Fecha") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Hora") },
                        singleLine = true,
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
                                selectedContainerColor = BrandBlue,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
                if (scheduledRides.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Reservas activas:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    scheduledRides.take(3).forEach { ride ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${ride.destination} (${ride.dateLabel}, ${ride.timeLabel})",
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { onCancelRide(ride.id) }) {
                                Text("Quitar", color = BrandSky, fontSize = 12.sp)
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
                Text("Guardar Reserva")
            }
        },
        dismissButton = {
            TextButton(onClick = { onLaunchNow(selectedMode) }) {
                Text("Pedir Ahora")
            }
        }
    )
}
