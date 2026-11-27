package com.example.viakaab.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viakaab.data.CarrilloCommerceCatalog
import com.example.viakaab.model.CartItem
import com.example.viakaab.model.EatsCategory
import com.example.viakaab.model.LocalCommerce
import com.example.viakaab.model.StoreProduct
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandRed
import com.example.viakaab.ui.theme.BrandSky
import com.example.viakaab.ui.theme.PureWhite
import com.example.viakaab.ui.theme.Slate300
import com.example.viakaab.ui.theme.Slate400
import com.example.viakaab.ui.theme.Slate500
import com.example.viakaab.ui.theme.Slate700
import com.example.viakaab.ui.theme.Slate800
import com.example.viakaab.ui.theme.Slate900
import com.example.viakaab.ui.theme.Slate950
import com.example.viakaab.viewmodel.MapViewModel

@Composable
fun EatsMarketplaceScreen(
    mapViewModel: MapViewModel,
    initialCategoryKey: String = "ALL",
    initialStoreId: String = "",
    onBack: () -> Unit,
    onOpenCustomDeliveryMap: () -> Unit,
    onOrderConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val passengerState by mapViewModel.passengerState.collectAsStateWithLifecycle()

    var selectedCategory by rememberSaveable {
        mutableStateOf(EatsCategory.fromKey(initialCategoryKey))
    }
    var selectedStoreId by rememberSaveable { mutableStateOf(initialStoreId) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showCheckoutDialog by rememberSaveable { mutableStateOf(false) }

    val cartQuantities = remember { mutableStateMapOf<String, Int>() }
    val selectedStore = remember(selectedStoreId) {
        CarrilloCommerceCatalog.findById(selectedStoreId)
    }

    val activeCartItems: List<CartItem> = remember(selectedStore, cartQuantities.toMap()) {
        if (selectedStore == null) {
            emptyList()
        } else {
            selectedStore.products.mapNotNull { product ->
                val qty = cartQuantities[product.id] ?: 0
                if (qty > 0) CartItem(product, qty) else null
            }
        }
    }

    val totalItemsCount = activeCartItems.sumOf { it.quantity }
    val subtotalMxn = activeCartItems.sumOf { it.lineTotalMxn }

    BackHandler(enabled = selectedStore != null) {
        selectedStoreId = ""
        cartQuantities.clear()
    }

    if (showCheckoutDialog && selectedStore != null) {
        StoreOrderCheckoutDialog(
            commerce = selectedStore,
            cartItems = activeCartItems,
            defaultAddress = passengerState.pickupText.removePrefix("En vivo: ").ifBlank {
                "Centro, Felipe Carrillo Puerto"
            },
            onIncrement = { productId ->
                cartQuantities[productId] = (cartQuantities[productId] ?: 0) + 1
            },
            onDecrement = { productId ->
                val current = cartQuantities[productId] ?: 0
                if (current <= 1) cartQuantities.remove(productId) else cartQuantities[productId] = current - 1
            },
            onDismiss = { showCheckoutDialog = false },
            onConfirmOrder = { address, notes ->
                mapViewModel.publishStoreDeliveryOrder(
                    commerce = selectedStore,
                    cartItems = activeCartItems,
                    deliveryAddress = address,
                    orderNotes = notes
                )
                showCheckoutDialog = false
                cartQuantities.clear()
                Toast.makeText(
                    context,
                    "¡Pedido enviado a ${selectedStore.name}! Repartidor en camino.",
                    Toast.LENGTH_SHORT
                ).show()
                onOrderConfirmed()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Slate950, Slate900, Slate950)))
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (selectedStore != null) {
                            selectedStoreId = ""
                            cartQuantities.clear()
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                        .testTag("eats_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = PureWhite
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedStore?.name ?: "Comercios en Carrillo Puerto",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = selectedStore?.neighborhood ?: "Antojitos, farmacias y súper a domicilio",
                        color = Slate400,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (selectedStore != null && totalItemsCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = BrandEmerald,
                        modifier = Modifier
                            .clickable { showCheckoutDialog = true }
                            .testTag("eats_top_cart_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Carrito",
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$totalItemsCount",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            if (selectedStore == null) {
                StoreDirectoryContent(
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    onCategoryChange = { selectedCategory = it },
                    onSearchChange = { searchQuery = it },
                    onSelectStore = { store ->
                        cartQuantities.clear()
                        selectedStoreId = store.id
                    },
                    onOpenCustomDeliveryMap = onOpenCustomDeliveryMap
                )
            } else {
                StoreCatalogContent(
                    commerce = selectedStore,
                    cartQuantities = cartQuantities,
                    totalItemsCount = totalItemsCount,
                    subtotalMxn = subtotalMxn,
                    onIncrement = { product ->
                        cartQuantities[product.id] = (cartQuantities[product.id] ?: 0) + 1
                    },
                    onDecrement = { product ->
                        val current = cartQuantities[product.id] ?: 0
                        if (current <= 1) cartQuantities.remove(product.id) else cartQuantities[product.id] = current - 1
                    },
                    onOpenCheckout = { showCheckoutDialog = true }
                )
            }
        }
    }
}

@Composable
private fun StoreDirectoryContent(
    selectedCategory: EatsCategory,
    searchQuery: String,
    onCategoryChange: (EatsCategory) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectStore: (LocalCommerce) -> Unit,
    onOpenCustomDeliveryMap: () -> Unit
) {
    val filteredStores = remember(selectedCategory, searchQuery) {
        val base = CarrilloCommerceCatalog.byCategory(selectedCategory)
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            base
        } else {
            base.filter { store ->
                store.name.lowercase().contains(q) ||
                    store.subtitle.lowercase().contains(q) ||
                    store.neighborhood.lowercase().contains(q) ||
                    store.products.any { it.name.lowercase().contains(q) }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Buscar tacos, farmacia, leche, huevo...", color = Slate400, fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = BrandEmerald)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda", tint = Slate400)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedBorderColor = BrandEmerald,
                unfocusedBorderColor = Slate700,
                focusedContainerColor = Slate900,
                unfocusedContainerColor = Slate900
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("eats_search_input")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EatsCategory.entries.forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategoryChange(category) },
                    leadingIcon = {
                        Icon(
                            imageVector = categoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = category.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Slate800,
                        labelColor = Slate300,
                        iconColor = Slate400,
                        selectedContainerColor = BrandEmerald,
                        selectedLabelColor = PureWhite,
                        selectedLeadingIconColor = PureWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Slate700,
                        selectedBorderColor = BrandEmerald
                    ),
                    modifier = Modifier.testTag("eats_chip_${category.key.lowercase()}")
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredStores.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        border = BorderStroke(1.dp, Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sin resultados para \"$searchQuery\"",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Puedes pedir un repartidor personalizado para recoger cualquier producto en Felipe Carrillo Puerto.",
                                color = Slate400,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredStores, key = { it.id }) { store ->
                    CommerceStoreCard(
                        commerce = store,
                        onClick = { onSelectStore(store) }
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clickable(onClick = onOpenCustomDeliveryMap)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandBlue.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¿No aparece tu tienda o mandado?",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Solicita un repartidor en mapa de punto A a punto B",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Slate400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommerceStoreCard(
    commerce: LocalCommerce,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = BorderStroke(1.dp, commerce.accentColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("store_card_${commerce.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(commerce.accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(commerce.category),
                        contentDescription = null,
                        tint = commerce.accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = commerce.accentColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = commerce.category.label.uppercase(),
                                color = commerce.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFACC15),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = commerce.rating,
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = commerce.name,
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = commerce.neighborhood,
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = commerce.subtitle,
                color = Slate300,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${commerce.deliveryTimeLabel} · Envío $${commerce.deliveryFeeMxn} MXN",
                        color = Slate300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandEmerald
                ) {
                    Text(
                        text = "Ver menú",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreCatalogContent(
    commerce: LocalCommerce,
    cartQuantities: Map<String, Int>,
    totalItemsCount: Int,
    subtotalMxn: Int,
    onIncrement: (StoreProduct) -> Unit,
    onDecrement: (StoreProduct) -> Unit,
    onOpenCheckout: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = if (totalItemsCount > 0) 104.dp else 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    border = BorderStroke(1.dp, commerce.accentColor.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(commerce.accentColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = categoryIcon(commerce.category),
                                    contentDescription = null,
                                    tint = commerce.accentColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = commerce.name,
                                    color = PureWhite,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = commerce.neighborhood,
                                    color = Slate300,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = commerce.subtitle, color = Slate400, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = commerce.rating,
                                    color = Color(0xFFFACC15),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Slate300, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = commerce.deliveryTimeLabel,
                                    color = PureWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = BrandEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Envío $${commerce.deliveryFeeMxn} MXN",
                                    color = BrandEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Selecciona tus productos",
                    color = PureWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(commerce.products, key = { it.id }) { product ->
                val qty = cartQuantities[product.id] ?: 0
                ProductMenuCard(
                    product = product,
                    quantity = qty,
                    onIncrement = { onIncrement(product) },
                    onDecrement = { onDecrement(product) }
                )
            }
        }

        if (totalItemsCount > 0) {
            val totalMxn = subtotalMxn + commerce.deliveryFeeMxn
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandEmerald),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable(onClick = onOpenCheckout)
                    .testTag("eats_view_cart_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PureWhite.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = "$totalItemsCount",
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ver carrito y pedir",
                                color = PureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Productos $$subtotalMxn + Envío $${commerce.deliveryFeeMxn}",
                                color = PureWhite.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        text = "$$totalMxn MXN",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductMenuCard(
    product: StoreProduct,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(
            1.dp,
            if (quantity > 0) BrandEmerald.copy(alpha = 0.7f) else Slate800
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (product.badge.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandEmerald.copy(alpha = 0.16f),
                        modifier = Modifier.padding(bottom = 5.dp)
                    ) {
                        Text(
                            text = product.badge,
                            color = BrandEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = product.name,
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    color = Slate400,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${product.priceMxn} MXN",
                    color = BrandEmerald,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (quantity == 0) {
                Button(
                    onClick = onIncrement,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandEmerald,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("add_product_${product.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Quitar uno",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "$quantity",
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = onIncrement, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar uno",
                            tint = BrandEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreOrderCheckoutDialog(
    commerce: LocalCommerce,
    cartItems: List<CartItem>,
    defaultAddress: String,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmOrder: (String, String) -> Unit
) {
    var deliveryAddress by rememberSaveable { mutableStateOf(defaultAddress) }
    var orderNotes by rememberSaveable { mutableStateOf("") }
    val subtotal = cartItems.sumOf { it.lineTotalMxn }
    val total = subtotal + commerce.deliveryFeeMxn
    val quickColonias = listOf("Centro", "Martínez Ross", "Emiliano Zapata", "TecNM", "Hospital")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        titleContentColor = PureWhite,
        textContentColor = Slate300,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = BrandEmerald,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tu pedido en ${commerce.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (cartItems.isEmpty()) {
                    Text("Tu carrito está vacío.", color = BrandRed)
                } else {
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    color = PureWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$${item.lineTotalMxn} MXN",
                                    color = BrandEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onDecrement(item.product.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                }
                                Text("${item.quantity}", color = PureWhite, fontWeight = FontWeight.Bold)
                                IconButton(
                                    onClick = { onIncrement(item.product.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = BrandEmerald, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Slate700)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Productos", color = Slate400, fontSize = 13.sp)
                        Text("$$subtotal MXN", color = PureWhite, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Envío repartidor FCP", color = Slate400, fontSize = 13.sp)
                        Text("$${commerce.deliveryFeeMxn} MXN", color = PureWhite, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total a pagar", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("$$total MXN", color = BrandEmerald, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }

                    HorizontalDivider(color = Slate700)

                    Text(
                        text = "Dirección de entrega en Felipe Carrillo Puerto",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickColonias.forEach { col ->
                            FilterChip(
                                selected = deliveryAddress.contains(col, ignoreCase = true),
                                onClick = { deliveryAddress = "$col, Felipe Carrillo Puerto" },
                                label = { Text(col, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Slate800,
                                    labelColor = Slate300,
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Calle, número y colonia") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandEmerald) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = BrandEmerald,
                            unfocusedBorderColor = Slate700
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("eats_checkout_address_input")
                    )

                    OutlinedTextField(
                        value = orderNotes,
                        onValueChange = { orderNotes = it },
                        label = { Text("Indicaciones (ej. sin cebolla, portón verde)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = BrandSky,
                            unfocusedBorderColor = Slate700
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmOrder(deliveryAddress, orderNotes) },
                enabled = cartItems.isNotEmpty() && deliveryAddress.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald, contentColor = PureWhite),
                modifier = Modifier.testTag("eats_confirm_order_button")
            ) {
                Text("Confirmar Pedido ($$total MXN)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Seguir viendo", color = Slate400)
            }
        }
    )
}

private fun categoryIcon(category: EatsCategory): ImageVector = when (category) {
    EatsCategory.ALL -> Icons.Default.Storefront
    EatsCategory.ANTOJITOS -> Icons.Default.Restaurant
    EatsCategory.FARMACIA -> Icons.Default.LocalPharmacy
    EatsCategory.SUPER -> Icons.Default.ShoppingCart
}
