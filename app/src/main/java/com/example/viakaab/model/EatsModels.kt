package com.example.viakaab.model

import androidx.compose.ui.graphics.Color

enum class EatsCategory(val key: String, val label: String) {
    ALL("ALL", "Todos"),
    ANTOJITOS("ANTOJITOS", "Antojitos"),
    FARMACIA("FARMACIA", "Farmacia"),
    SUPER("SUPER", "Súper");

    companion object {
        fun fromKey(key: String?): EatsCategory =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: ALL
    }
}

data class StoreProduct(
    val id: String,
    val name: String,
    val description: String,
    val priceMxn: Int,
    val badge: String = ""
)

data class LocalCommerce(
    val id: String,
    val name: String,
    val category: EatsCategory,
    val neighborhood: String,
    val deliveryTimeLabel: String,
    val deliveryFeeMxn: Int,
    val rating: String,
    val lat: Double,
    val lng: Double,
    val accentColor: Color,
    val subtitle: String,
    val products: List<StoreProduct>
)

data class CartItem(
    val product: StoreProduct,
    val quantity: Int
) {
    val lineTotalMxn: Int
        get() = product.priceMxn * quantity
}
